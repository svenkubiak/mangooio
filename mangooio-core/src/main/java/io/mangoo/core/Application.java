package io.mangoo.core;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.google.inject.Module;
import com.google.inject.*;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.github.classgraph.*;
import io.mangoo.admin.AdminController;
import io.mangoo.async.EventBus;
import io.mangoo.async.Subscriber;
import io.mangoo.cache.CacheProvider;
import io.mangoo.constants.CacheName;
import io.mangoo.constants.Default;
import io.mangoo.constants.Key;
import io.mangoo.constants.NotNull;
import io.mangoo.enums.Mode;
import io.mangoo.enums.Sort;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.persistence.interfaces.Datastore;
import io.mangoo.routing.Bind;
import io.mangoo.routing.On;
import io.mangoo.routing.Router;
import io.mangoo.routing.handlers.*;
import io.mangoo.routing.routes.FileRoute;
import io.mangoo.routing.routes.PathRoute;
import io.mangoo.routing.routes.RequestRoute;
import io.mangoo.routing.routes.ServerSentEventRoute;
import io.mangoo.scheduler.CronTask;
import io.mangoo.scheduler.Schedule;
import io.mangoo.scheduler.Scheduler;
import io.mangoo.scheduler.Task;
import io.mangoo.utils.ByteUtils;
import io.mangoo.utils.MangooUtils;
import io.mangoo.utils.PersistenceUtils;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.Methods;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public final class Application {
    private static final Logger LOG = LogManager.getLogger(Application.class);
    private static final LocalDateTime START = LocalDateTime.now();
    private static final int KEY_MIN_BIT_LENGTH = 512;
    private static final String COLLECTION = "io.mangoo.annotations.Collection";
    private static final String INDEXED = "io.mangoo.annotations.Indexed";
    private static final String SCHEDULER = "io.mangoo.annotations.Run";
    private static final String MODULE_CLASS = "app.Module";
    private static final String ALL_PACKAGES = "*";
    private static final String LOGO = """
                                                        ___     __  ___ \s
         _ __ ___    __ _  _ __    __ _   ___    ___   |_ _|   / / / _ \\\s
        | '_ ` _ \\  / _` || '_ \\  / _` | / _ \\  / _ \\   | |   / / | | | |
        | | | | | || (_| || | | || (_| || (_) || (_) |  | |  / /  | |_| |
        |_| |_| |_| \\__,_||_| |_| \\__, | \\___/  \\___/  |___|/_/    \\___/\s
                                  |___/                                 \s""";

    private static io.mangoo.core.Module module;
    private static ScheduledExecutorService scheduledExecutorService;
    private static ExecutorService executorService;
    private static String httpHost;
    private static String ajpHost;
    private static Undertow undertow;
    private static Mode mode;
    private static Injector injector;
    private static PathHandler pathHandler;
    private static boolean started;
    private static int httpPort;
    private static int ajpPort;

    private Application() {
    }

    public static void main(String... args) {
        start(Mode.PROD);
    }

    @SuppressWarnings({"StatementWithEmptyBody", "LoopConditionNotUpdatedInsideLoop"})
    public static void start(Mode mode) {
        Objects.requireNonNull(mode, NotNull.MODE);

        if (!started) {
            userCheck();
            prepareMode(mode);
            prepareInjector();
            applicationInitialized();
            prepareConfig();
            Thread scan = Thread.ofVirtual().start(() -> {
                try (var scanResult = scanClasspath()) {
                    prepareScheduler(scanResult);
                    prepareDatastore(scanResult);
                    prepareSubscriber(scanResult);
                }
            });
            prepareRoutes();
            createRoutes();
            validateUrls();
            prepareUndertow();
            prepareShutdown();
            sanityChecks();
            do {} while (scan.isAlive()); //NOSONAR
            applicationStarted();
            showLogo();
            started = true;
        }
    }

    private static void validateUrls() {
        if (!Router.validUrls()) {
            failsafe();
        }
    }

    /**
     * Schedules all tasks annotated with @Run
     */
    private static void prepareScheduler(ScanResult scanResult) {
        var config = getInstance(Config.class);

        if (config.isSchedulerEnabled()) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            executorService = Executors.newThreadPerTaskExecutor(Thread.ofPlatform().factory());

            scanResult.getClassesWithMethodAnnotation(SCHEDULER).forEach(classInfo ->
                classInfo.getMethodInfo().forEach(methodInfo -> {
                    if (!methodInfo.getAnnotationInfo().isEmpty()) {
                        var isCron = false;
                        long seconds = 0;
                        String at = null;

                        for (var i = 0; i < methodInfo.getAnnotationInfo().size(); i++) {
                            var annotationInfo = methodInfo.getAnnotationInfo().get(i);
                            at = ((String) annotationInfo
                                    .getParameterValues(true).get("at").getValue())
                                    .toLowerCase(Locale.ENGLISH)
                                    .trim();

                            if (at.contains("every")) {
                                at = at.replace("every", Strings.EMPTY).trim();
                                var timespan = at.substring(0, at.length() - 1);
                                var duration = at.substring(at.length() - 1);
                                seconds = getSeconds(timespan, duration);
                            } else {
                                isCron = true;
                            }
                        }

                        if (StringUtils.isNotBlank(at)) {
                            schedule(classInfo, methodInfo, isCron, seconds, at);
                        }
                    }
                })
            );
        }
    }

    /**
     * Parses a given time span and duration and returns the number of
     * matching seconds to schedule a task
     *
     * @param timespan The timespan to use
     * @param duration The duration to use for calculation
     *
     * @return The duration in seconds
     */
    private static long getSeconds(String timespan, String duration) {
        Objects.requireNonNull(timespan, "timespan can not be null");
        Objects.requireNonNull(duration, "duration can not be null");

        var time = Long.parseLong(timespan);
        return switch(duration) {
            case "m" -> time * 60;
            case "h" -> time * 3600;
            case "d" -> time * 86400;
            default  -> time;
        };
    }

    /**
     * Schedules a task within the scheduler
     *
     * @param classInfo The classInfo containing the class which holds the method to execute
     * @param methodInfo The methodInfo containing the method to execute
     * @param isCron True if Task is a cron or false if it has a fixed rate
     * @param time The fixed rate for the scheduled task to be executed
     * @param at The cron expression to be used when scheduling a cron
     */
    private static void schedule(ClassInfo classInfo, MethodInfo methodInfo, boolean isCron, long time, String at) {
        Objects.requireNonNull(classInfo, "classInfo can not be null");
        Objects.requireNonNull(methodInfo, "methodInfo can not be null");
        Objects.requireNonNull(at, "at can not be null");

        try {
            getInstance(classInfo.loadClass());
        } catch (Exception e) {
            LOG.error("Failed to scheduled a task as class creation ran into an error. Check class '{}' with method '{}'", classInfo.getName(), methodInfo.getName(), e);
            failsafe();
        }

        if (isCron) {
            try {
                var parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
                var quartzCron = parser.parse(at);
                quartzCron.validate();

                var cronTask = new CronTask(classInfo.loadClass(), methodInfo.getName(), at);
                ScheduledFuture<?> scheduledFuture = scheduledExecutorService.schedule(() -> executorService.submit(cronTask), 0, TimeUnit.SECONDS);
                getInstance(Scheduler.class).addSchedule(Schedule.of(classInfo.loadClass().toString(), methodInfo.getName(), at, scheduledFuture, true));

                LOG.info("Successfully scheduled cron task from class '{}' with method '{}' and cron '{}'", classInfo.getName(), methodInfo.getName(), at);
            } catch (IllegalArgumentException e) {
                LOG.error("Scheduled cron task found, but the unix cron is invalid", e);
                failsafe();
            }
        } else {
            if (time > 0) {
                var task = new Task(classInfo.loadClass(), methodInfo.getName());
                ScheduledFuture<?> scheduledFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> executorService.submit(task), time, time, TimeUnit.SECONDS);
                getInstance(Scheduler.class).addSchedule(Schedule.of(classInfo.loadClass().toString(), methodInfo.getName(), "every " + at, scheduledFuture, false));

                LOG.info("Successfully scheduled task from class '{}' with method '{}' at rate 'Every {}'", classInfo.getName(), methodInfo.getName(), at);
            } else {
                LOG.error("Scheduled task found, but unable to schedule it. Check class '{}' with method '{}' at rate 'Every {}'", classInfo.getName(), methodInfo.getName(), at);
                failsafe();
            }
        }
    }

    /**
     * Configures persistence
     */
    private static void prepareDatastore(ScanResult scanResult) {
        var config = getInstance(Config.class);
        if (config.isPersistenceEnabled()) {
            scanResult.getClassesWithAnnotation(COLLECTION).forEach(classInfo -> {
                String key = classInfo.getName();

                AnnotationInfoList annotationInfo = classInfo.getAnnotationInfo();
                String value = (String) annotationInfo.getFirst().getParameterValues().getFirst().getValue();

                PersistenceUtils.addCollection(key, value);
            });

            Datastore datastore = Application.getInstance(Datastore.class);
            scanResult.getClassesWithFieldAnnotation(INDEXED).forEach(classInfo -> {
                FieldInfoList fieldInfoList = classInfo.getFieldInfo();
                fieldInfoList.stream()
                        .filter(info -> info.getAnnotationInfo().size() == 1)
                        .filter(info -> StringUtils.isNotBlank(info.getName()))
                        .forEach(info -> {
                            List<AnnotationParameterValue> annotationParams = info.getAnnotationInfo().getFirst().getParameterValues();
                            boolean unique = (annotationParams.size() > 1) && (boolean) annotationParams.get(1).getValue();
                            String sortOrder = annotationParams.get(0).getValue().toString();

                            if (Sort.ASCENDING.value().equals(sortOrder)) {
                                datastore.addIndex(classInfo.loadClass(), Indexes.ascending(info.getName()), new IndexOptions().unique(unique));
                            } else if (Sort.DESCENDING.value().equals(sortOrder)) {
                                datastore.addIndex(classInfo.loadClass(), Indexes.descending(info.getName()), new IndexOptions().unique(unique));
                            }
                        });
            });

        }
    }

    @SuppressWarnings("unchecked")
    private static void prepareSubscriber(ScanResult scanResult) {
        scanResult.getClassesImplementing(Subscriber.class).forEach(classInfo -> {
            var methodInfo = classInfo.getMethodInfo().getFirst();
            if (("receive").equals(methodInfo.getName())) {
                var methodParameterInfo = Arrays.asList(methodInfo.getParameterInfo()).getFirst();
                var descriptor = methodParameterInfo.getTypeDescriptor().toString();
                Application.getInstance(EventBus.class).register(descriptor, classInfo.loadClass());
            }
        });
    }

    /**
     * Checks if application is run as root
     * <p>
     * (Hint: There is no need to run as root)
     */
    private static void userCheck() {
        String osName = System.getProperty("os.name");
        if (StringUtils.isNotBlank(osName) && !osName.startsWith("Windows")) {
            String [] command = {"id", "-u"};

            try {
                Process exec = Runtime.getRuntime().exec(command);
                var input = new BufferedReader(new InputStreamReader(exec.getInputStream(), StandardCharsets.UTF_8));
                String output = input.lines().collect(Collectors.joining(System.lineSeparator()));

                input.close();

                if (("0").equals(output) && inProdMode()) {
                    LOG.error("Can not run application as root");
                    failsafe();
                }
            } catch (IOException e) {
                LOG.error("Failed to check if application is started as root", e);
            }
        }
    }

    /**
     * Checks if the application is running in dev mode
     *
     * @return True if the application is running in dev mode, false otherwise
     */
    public static boolean inDevMode() {
        return Mode.DEV == mode;
    }

    /**
     * Checks if the application is running in prod mode
     *
     * @return True if the application is running in prod mode, false otherwise
     */
    public static boolean inProdMode() {
        return Mode.PROD == mode;
    }

    /**
     * Checks if the application is running in test mode
     *
     * @return True if the application is running in test mode, false otherwise
     */
    public static boolean inTestMode() {
        return Mode.TEST == mode;
    }

    /**
     * Returns the current mode the application is running in
     *
     * @return Enum Mode
     */
    public static Mode getMode() {
        return mode;
    }

    /**
     * Returns the ScheduledExecutorService where all tasks are scheduled
     *
     * @return ScheduledExecutorService
     */
    public static ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    /**
     * Returns the ExecutorService the execution of the scheduled tasks
     * are performed
     *
     * @return ExecutorService
     */
    public static ExecutorService getExecutorService() {
        return executorService;
    }

    /**
     * Returns the Google Guice Injector
     *
     * @return Google Guice injector instance
     */
    public static Injector getInjector() {
        return injector;
    }

    /**
     * @return True if the application started successfully, false otherwise
     */
    public static boolean isStarted() {
        return started;
    }

    /**
     * @return The LocalDateTime of the application start
     */
    public static LocalDateTime getStart() {
        return START;
    }

    /**
     * @return The duration of the application uptime
     */
    public static Duration getUptime() {
        return Duration.between(START, LocalDateTime.now());
    }

    /**
     * Short form for getting a Google Guice injected class by
     * calling getInstance(...)
     *
     * @param clazz The class to retrieve from the injector
     * @param <T> JavaDoc requires this (just ignore it)
     *
     * @return An instance of the requested class
     */
    public static <T> T getInstance(Class<T> clazz) {
        Objects.requireNonNull(clazz, NotNull.CLASS);

        return injector.getInstance(clazz);
    }

    /**
     * Stops the underlying undertow server
     */
    public static void stopUndertow() {
        undertow.stop();
    }

    /**
     * Sets the mode the application is running in
     *
     * @param providedMode A given mode or null
     */
    private static void prepareMode(Mode providedMode) {
        final String applicationMode = System.getProperty(Key.APPLICATION_MODE);
        if (StringUtils.isNotBlank(applicationMode)) {
            mode = switch (applicationMode.toLowerCase(Locale.ENGLISH)) {
                case "dev"  -> Mode.DEV;
                case "test" -> Mode.TEST;
                default     -> Mode.PROD;
            };
        } else {
            mode = providedMode;
        }
    }

    /**
     * Sets the injector wrapped through guice modules
     */
    private static void prepareInjector() {
        injector = Guice.createInjector(Stage.PRODUCTION, getModules());
    }

    /**
     * Callback to MangooLifecycle applicationInitialized
     */
    private static void applicationInitialized() {
        getInstance(MangooBootstrap.class).applicationInitialized();
    }

    /**
     * Checks for config failures that prevent the application from starting
     */
    private static void prepareConfig() {
        var config = getInstance(Config.class);

        int bitLength = getBitLength(config.getApplicationSecret());
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Application requires a 512 bit application secret. The current property for application.secret has currently only {} bit.", bitLength);
            failsafe();
        }

        bitLength = getBitLength(config.getAuthenticationCookieSecret());
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Authentication cookie requires a 512 bit encryption key. The current property for authentication.cookie.secret has only {} bit.", bitLength);
            failsafe();
        }

        bitLength = getBitLength(config.getAuthenticationCookieSecret());
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Authentication cookie requires a 512 bit sign key. The current property for authentication.cookie.signkey has only {} bit.", bitLength);
            failsafe();
        }

        bitLength = getBitLength(config.getSessionCookieSecret());
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Session cookie secret a 512 bit encryption key. The current property for session.cookie.secret has only {} bit.", bitLength);
            failsafe();
        }

        bitLength = getBitLength(config.getSessionCookieSecret());
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Session cookie requires a 512 bit sign key. The current property for session.cookie.signkey has only {} bit.", bitLength);
            failsafe();
        }

        bitLength = getBitLength(config.getFlashCookieSecret());
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Flash cookie requires a 512 bit sign key. The current property for flash.cookie.signkey has only {} bit.", bitLength);
            failsafe();
        }

        bitLength = getBitLength(config.getFlashCookieSecret());
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Flash cookie requires a 512 bit encryption key. The current property for flash.cookie.secret has only {} bit.", bitLength);
            failsafe();
        }

        if (!config.isDecrypted()) {
            LOG.error("Found encrypted config values in config.props but decryption was not successful!");
            failsafe();
        }
    }

    /**
     * Do sanity check on the configuration and warn about it in the log
     */
    private static void sanityChecks() {
        var config = getInstance(Config.class);
        List<String> warnings = new ArrayList<>();

        if (!config.isAuthenticationCookieSecure()) {
            var warning = "Authentication cookie has secure flag set to 'false'. It is highly recommended to set authentication.cookie.secure to 'true' in an production environment.";
            warnings.add(warning);
            LOG.warn(warning);
        }

        if (Default.AUTHENTICATION_COOKIE_NAME.equals(config.getAuthenticationCookieName())) {
            var warning = "Authentication cookie name has default value. Consider changing authentication.cookie.name to an application specific value.";
            warnings.add(warning);
            LOG.warn(warning);
        }

        if (config.getAuthenticationCookieSecret().equals(config.getApplicationSecret())) {
            var warning = "Authentication cookie secret is using application secret. It is highly recommended to set a dedicated value to authentication.cookie.secret.";
            warnings.add(warning);
            LOG.warn(warning);
        }

        if (!config.isSessionCookieSecure()) {
            var warning = "Session cookie has secure flag set to 'false'. It is highly recommended to set session.cookie.secure to 'true' in an production environment.";
            warnings.add(warning);
            LOG.warn(warning);
        }

        if (Default.SESSION_COOKIE_NAME.equals(config.getSessionCookieName())) {
            var warning = "Session cookie name has default value. Consider changing session.cookie.name to an application specific value.";
            warnings.add(warning);
            LOG.warn(warning);
        }

        if (config.getSessionCookieSecret().equals(config.getApplicationSecret())) {
            var warning = "Session cookie secret is using application secret. It is highly recommended to set a dedicated value to session.cookie.secret.";
            warnings.add(warning);
            LOG.warn(warning);
        }

        if (Default.FLASH_COOKIE_NAME.equals(config.getFlashCookieName())) {
            var warning = "Flash cookie name has default value. Consider changing flash.cookie.name to an application specific value.";
            warnings.add(warning);
            LOG.warn(warning);
        }

        if (config.getFlashCookieSecret().equals(config.getApplicationSecret())) {
            var warning = "Flash cookie secret is using application secret. It is highly recommended to set a dedicated value to flash.cookie.secret.";
            warnings.add(warning);
            LOG.warn(warning);
        }

        getInstance(CacheProvider.class).getCache(CacheName.APPLICATION).put(Key.MANGOOIO_WARNINGS, warnings);
    }

    /**
     * Validate if the routes that are defined in the router are valid
     */
    private static void prepareRoutes() {
        injector.getInstance(MangooBootstrap.class).initializeRoutes();

        Router.getRequestRoutes().forEach((RequestRoute requestRoute) -> {
            if (!methodExists(requestRoute.getControllerMethod(), requestRoute.getControllerClass())) {
                LOG.error("Could not find controller method '{}' in controller class '{}'", requestRoute.getControllerMethod(), requestRoute.getControllerClass());
                failsafe();
            }
        });
    }

    /**
     * Checks if a given method exists in a given class
     * @param controllerMethod The method to check
     * @param controllerClass The class to check 
     *
     * @return True if the method exists, false otherwise
     */
    private static boolean methodExists(String controllerMethod, Class<?> controllerClass) {
        Objects.requireNonNull(controllerMethod, NotNull.CONTROLLER_METHOD);
        Objects.requireNonNull(controllerClass, NotNull.CONTROLLER_CLASS);

        return Arrays.stream(controllerClass.getMethods()).anyMatch(method -> method.getName().equals(controllerMethod));
    }

    /**
     * Create routes for WebSockets ServerSentEvent and Resource files
     */
    private static void createRoutes() {
        pathHandler = new PathHandler(getRoutingHandler());

        Router.getServerSentEventRoutes().forEach((ServerSentEventRoute serverSentEventRoute) ->
                pathHandler.addExactPath(serverSentEventRoute.getUrl(),
                        Handlers.serverSentEvents(getInstance(ServerSentEventHandler.class)
                                .withAuthentication(serverSentEventRoute.hasAuthentication())))
        );

        Router.getPathRoutes().forEach((PathRoute pathRoute) ->
                pathHandler.addPrefixPath(pathRoute.getUrl(),
                        new ResourceHandler(new ClassPathResourceManager(Thread.currentThread().getContextClassLoader(), Default.FILES_FOLDER + pathRoute.getUrl())))
        );

        pathHandler.addPrefixPath("/@admin/assets/",
                new ResourceHandler(new ClassPathResourceManager(Thread.currentThread().getContextClassLoader(), "templates/@admin/assets/")));
    }

    private static RoutingHandler getRoutingHandler() {
        var routingHandler = Handlers.routing();
        routingHandler.setFallbackHandler(Application.getInstance(FallbackHandler.class));

        var config = getInstance(Config.class);
        if (config.isApplicationAdminEnable()) {
            Bind.controller(AdminController.class)
                    .withRoutes(
                            On.get().to("/@admin").respondeWith("index"),
                            On.get().to("/@admin/cache").respondeWith("cache"),
                            On.get().to("/@admin/login").respondeWith("login"),
                            On.get().to("/@admin/twofactor").respondeWith("twofactor"),
                            On.get().to("/@admin/scheduler").respondeWith("scheduler"),
                            On.get().to("/@admin/tools").respondeWith("tools"),
                            On.get().to("/@admin/logout").respondeWith("logout"),
                            On.post().to("/@admin/authenticate").respondeWith("authenticate"),
                            On.post().to("/@admin/verify").respondeWith("verify"),
                            On.post().to("/@admin/tools").respondeWith("toolsRx")
                    );
        }

        Router.getRequestRoutes().forEach((RequestRoute requestRoute) -> {
            var dispatcherHandler = Application.getInstance(DispatcherHandler.class)
                    .dispatch(requestRoute.getControllerClass(), requestRoute.getControllerMethod())
                    .isBlocking(requestRoute.isBlocking())
                    .withBasicAuthentication(requestRoute.getUsername(), requestRoute.getPassword())
                    .withAuthentication(requestRoute.hasAuthentication());

            routingHandler.add(requestRoute.getMethod().toString(), requestRoute.getUrl(), dispatcherHandler);
        });

        var resourceHandler = Handlers.resource(new ClassPathResourceManager(
                Thread.currentThread().getContextClassLoader(),
                Default.FILES_FOLDER + '/'));

        Router.getFileRoutes().forEach((FileRoute fileRoute) -> routingHandler.add(Methods.GET, fileRoute.getUrl(), resourceHandler));

        return routingHandler;
    }

    private static void prepareUndertow() {
        var config = getInstance(Config.class);

        HttpHandler httpHandler;
        if (config.isMetricsEnable()) {
            httpHandler = MetricsHandler.HANDLER_WRAPPER.wrap(Handlers.exceptionHandler(pathHandler)
                    .addExceptionHandler(Throwable.class, Application.getInstance(ExceptionHandler.class)));
        } else {
            httpHandler = Handlers.exceptionHandler(pathHandler)
                    .addExceptionHandler(Throwable.class, Application.getInstance(ExceptionHandler.class));
        }

        var builder = Undertow.builder()
                .setServerOption(UndertowOptions.MAX_ENTITY_SIZE, config.getUndertowMaxEntitySize())
                .setHandler(httpHandler);

        httpHost = config.getConnectorHttpHost();
        httpPort = config.getConnectorHttpPort();
        ajpHost = config.getConnectorAjpHost();
        ajpPort = config.getConnectorAjpPort();

        var hasConnector = false;
        if (httpPort > 0 && StringUtils.isNotBlank(httpHost)) {
            builder.addHttpListener(httpPort, httpHost);
            hasConnector = true;
        }

        if (ajpPort > 0 && StringUtils.isNotBlank(ajpHost)) {
            builder.addAjpListener(ajpPort, ajpHost);
            hasConnector = true;
        }

        if (hasConnector) {
            undertow = builder.build();
            undertow.start();
        } else {
            LOG.error("No connector found! Please configure a HTTP and/or an AJP connector in your config.props file");
            failsafe();
        }
    }

    @SuppressFBWarnings(justification = "Buffer only used locally, without user input", value = "CRLF_INJECTION_LOGS")
    private static void showLogo() {
        var logo = '\n' +
                LOGO +
                "\n\nhttps://github.com/svenkubiak/mangooio | " +
                MangooUtils.getVersion() +
                '\n';

        LOG.info(logo);

        if (httpPort > 0 && StringUtils.isNotBlank(httpHost)) {
            LOG.info("HTTP connector listening @{}:{}", httpHost, httpPort);
        }

        if (ajpPort > 0 && StringUtils.isNotBlank(ajpHost)) {
            LOG.info("AJP connector listening @{}:{}", ajpHost, ajpPort);
        }

        String startup = "mangoo I/O application started in " + ChronoUnit.MILLIS.between(START, LocalDateTime.now()) + " ms in " + mode + " mode. Enjoy.";
        LOG.info(startup);
    }

    private static int getBitLength(String secret) {
        Objects.requireNonNull(secret, NotNull.SECRET);

        return ByteUtils.bitLength(RegExUtils.replaceAll(secret, "[^\\x00-\\x7F]", Strings.EMPTY));
    }

    private static List<Module> getModules() {
        final List<Module> modules = new ArrayList<>();
        try {
            module = new io.mangoo.core.Module();
            modules.add(module);
            modules.add((AbstractModule) Class.forName(MODULE_CLASS).getConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                 | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
            LOG.error("Failed to load modules. Check that app/Module.java exists in your application", e);
            failsafe();
        }

        return modules;
    }

    private static void applicationStarted() {
        getInstance(MangooBootstrap.class).applicationStarted();
    }

    /**
     * Failsafe exit of application startup
     */
    private static void failsafe() {
        System.out.print("Failed to start mangoo I/O application"); //NOSONAR Intentionally as we want to exit the application at this point
        System.exit(1); //NOSONAR Intentionally as we want to exit the application at this point
    }

    private static void prepareShutdown() {
        Runtime
                .getRuntime()
                .addShutdownHook(getInstance(Shutdown.class));
    }

    private static ScanResult scanClasspath() {
        return new ClassGraph()
                .enableAllInfo()
                .acceptPackages(ALL_PACKAGES)
                .scan();
    }

    public static void stopEmbeddedMongoDB() {
        module.stopEmbeddedMongoDB();
    }
}