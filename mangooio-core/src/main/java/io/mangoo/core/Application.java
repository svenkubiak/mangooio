package io.mangoo.core;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.google.inject.*;
import com.google.inject.Module;
import com.mongodb.MongoCommandException;
import com.mongodb.client.model.Collation;
import com.mongodb.client.model.CollationStrength;
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
import io.mangoo.constants.Required;
import io.mangoo.crypto.Vault;
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
import io.mangoo.utils.CommonUtils;
import io.mangoo.utils.PersistenceUtils;
import io.mangoo.utils.internal.MangooUtils;
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
import org.bson.conversions.Bson;

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
    private static String httpsHost;
    private static Undertow undertow;
    private static Mode mode;
    private static Injector injector;
    private static PathHandler pathHandler;
    private static boolean started;
    private static int httpPort;
    private static int httpsPort;

    private Application() {
    }

    public static void main(String... args) {
        start(Mode.PROD);
    }

    @SuppressWarnings({"StatementWithEmptyBody", "LoopConditionNotUpdatedInsideLoop"})
    public static void start(Mode mode) {
        Objects.requireNonNull(mode, Required.MODE);

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
                } catch (Exception e) {
                    LOG.error("Failure in classpath scanning", e);
                    failsafe();
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
                var fieldInfoList = classInfo.getFieldInfo();
                fieldInfoList.stream()
                        .filter(info -> info.getAnnotationInfo().size() == 1)
                        .filter(info -> StringUtils.isNotBlank(info.getName()))
                        .forEach(info -> {
                            List<AnnotationParameterValue> annotationParams = info.getAnnotationInfo().getFirst().getParameterValues();

                            var unique = false;
                            var caseSensitive = false;
                            var sort = "";

                            for (AnnotationParameterValue annotationParam : annotationParams) {
                                String name = annotationParam.getName();
                                if ("unique".equals(name)) {
                                    unique = (boolean) annotationParam.getValue();
                                } else if ("caseSensitive".equals(name)) {
                                    caseSensitive = (boolean) annotationParam.getValue();
                                } else if (("sort").equals(name)) {
                                    sort = annotationParam.getValue().toString();
                                }
                            }

                            Collation collation = Collation.builder()
                                    .locale("en")
                                    .collationStrength(CollationStrength.SECONDARY)
                                    .build();

                            var indexOptions = new IndexOptions().unique(unique);
                            if (!caseSensitive && unique) {
                                indexOptions.collation(collation);
                            }

                            Bson indexType = Sort.ASCENDING.value().equals(sort)
                                    ? Indexes.ascending(info.getName())
                                    : Indexes.descending(info.getName());

                            try {
                                datastore.addIndex(classInfo.loadClass(), indexType, indexOptions);
                            } catch (MongoCommandException e) {
                                if (e.getErrorCode() == 86) {
                                    datastore.query(classInfo.loadClass()).dropIndex("uid_1");
                                    datastore.addIndex(classInfo.loadClass(), indexType, indexOptions);
                                } else {
                                    throw e;
                                }
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
                LOG.info("Registered subscriber '{}'", classInfo.loadClass());
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
        Objects.requireNonNull(clazz, Required.CLASS);

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

        config.validate();
        if (!config.isValid()) {
            LOG.error("Application configuration is invalid");
            failsafe();
        }

        int bitLength = getBitLength(config.getApplicationSecret());
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Application requires a 512 bit application secret. The current property for application.secret has currently only {} bits.", bitLength);
            failsafe();
        }

        bitLength = getBitLength(new String(config.getAuthenticationCookieSecret(), StandardCharsets.UTF_8));
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Authentication cookie requires a 512 bit encryption secret. The current property for authentication.cookie.secret has only {} bits.", bitLength);
            failsafe();
        }
        bitLength = getBitLength(new String(config.getSessionCookieSecret(), StandardCharsets.UTF_8));
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Session cookie secret a 512 bit encryption secret. The current property for session.cookie.secret has only {} bits.", bitLength);
            failsafe();
        }

        bitLength = getBitLength(new String(config.getFlashCookieSecret(), StandardCharsets.UTF_8));
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Flash cookie requires a 512 bit encryption secret. The current property for flash.cookie.secret has only {} bits.", bitLength);
            failsafe();
        }

        bitLength = getBitLength(new String(config.getFlashCookieKey(), StandardCharsets.UTF_8));
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Flash cookie requires a 512 bit signing key. The current property for flash.cookie.key has only {} bits.", bitLength);
            failsafe();
        }

        bitLength = getBitLength(new String(config.getAuthenticationCookieKey(), StandardCharsets.UTF_8));
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Authentication cookie requires a 512 bit signing key. The current property for authentication.cookie.key has only {} bits.", bitLength);
            failsafe();
        }

        bitLength = getBitLength(new String(config.getSessionCookieKey(), StandardCharsets.UTF_8));
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Session cookie requires a 512 bit signing key. The current property for session.cookie.key has only {} bits.", bitLength);
            failsafe();
        }

        if (StringUtils.isNotBlank(config.getString(Key.APPLICATION_API_KEY))) {
            bitLength = getBitLength(config.getString(Key.APPLICATION_API_KEY));
            if (bitLength < KEY_MIN_BIT_LENGTH) {
                LOG.error("API key requires a 512 bit key length. The current property for api.length has only {} bits.", bitLength);
                failsafe();
            }
        }

        if (config.getAllConfigurations().containsKey(Key.APPLICATION_ALLOWED_ORIGINS) && StringUtils.isBlank(config.getString(Key.APPLICATION_ALLOWED_ORIGINS))) {
            LOG.error("application.allowedOrigins is present in config.yaml, but has now value.");
            failsafe();
        }

        if (!("Strict").equals(config.getSessionCookieSameSiteMode()) && !("Lax").equals(config.getSessionCookieSameSiteMode())) {
            LOG.error("Only 'Strict' or 'Lax' is allowed in session.cookie.samesitemode is allowed");
            failsafe();
        }

        if (!("Strict").equals(config.getAuthenticationCookieSameSiteMode()) && !("Lax").equals(config.getAuthenticationCookieSameSiteMode())) {
            LOG.error("Only 'Strict' or 'Lax' is allowed in authentication.cookie.samesitemode is allowed");
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

        if (Default.FLASH_COOKIE_NAME.equals(config.getFlashCookieName())) {
            var warning = "Flash cookie name has default value. Consider changing flash.cookie.name to an application specific value.";
            warnings.add(warning);
            LOG.warn(warning);
        }

        if (Arrays.equals(config.getAuthenticationCookieSecret(), config.getApplicationSecret().getBytes(StandardCharsets.UTF_8))) {
            var warning = "Authentication cookie secret is using application secret. It is highly recommended to set a dedicated value to authentication.cookie.secret.";
            warnings.add(warning);
            LOG.warn(warning);
        }

        if (Arrays.equals(config.getSessionCookieSecret(), config.getApplicationSecret().getBytes(StandardCharsets.UTF_8))) {
            var warning = "Session cookie secret is using application secret. It is highly recommended to set a dedicated value to session.cookie.secret.";
            warnings.add(warning);
            LOG.warn(warning);
        }

        if (Arrays.equals(config.getFlashCookieSecret(), config.getApplicationSecret().getBytes(StandardCharsets.UTF_8))) {
            var warning = "Flash cookie secret is using application secret. It is highly recommended to set a dedicated value to flash.cookie.secret.";
            warnings.add(warning);
            LOG.warn(warning);
        }

        if (Arrays.equals(config.getAuthenticationCookieKey(), config.getApplicationSecret().getBytes(StandardCharsets.UTF_8))) {
            var warning = "Authentication cookie key is using application secret. It is highly recommended to set a dedicated value to authentication.cookie.key.";
            warnings.add(warning);
            LOG.warn(warning);
        }

        if (Arrays.equals(config.getSessionCookieKey(), config.getApplicationSecret().getBytes(StandardCharsets.UTF_8))) {
            var warning = "Session cookie key is using application secret. It is highly recommended to set a dedicated value to session.cookie.key.";
            warnings.add(warning);
            LOG.warn(warning);
        }

        if (Arrays.equals(config.getFlashCookieKey(), config.getApplicationSecret().getBytes(StandardCharsets.UTF_8))) {
            var warning = "Flash cookie key is using application secret. It is highly recommended to set a dedicated value to flash.cookie.key.";
            warnings.add(warning);
            LOG.warn(warning);
        }

        getInstance(CacheProvider.class)
                .getCache(CacheName.APPLICATION)
                .put(Key.MANGOOIO_WARNINGS, warnings);
    }

    /**
     * Validate if the routes that are defined in the router are valid
     */
    private static void prepareRoutes() {
        getInstance(MangooBootstrap.class).initializeRoutes();

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
        Objects.requireNonNull(controllerMethod, Required.CONTROLLER_METHOD);
        Objects.requireNonNull(controllerClass, Required.CONTROLLER_CLASS);

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
                            On.get().to("/@admin/security").respondeWith("security"),
                            On.get().to("/@admin/logout").respondeWith("logout"),
                            On.post().to("/@admin/authenticate").respondeWith("authenticate"),
                            On.post().to("/@admin/verify").respondeWith("verify")
                    );
        }

        Router.getRequestRoutes().forEach((RequestRoute requestRoute) -> {
            var dispatcherHandler = Application.getInstance(DispatcherHandler.class)
                    .dispatch(requestRoute.getControllerClass(), requestRoute.getControllerMethod())
                    .isBlocking(requestRoute.isBlocking())
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
        var vault = getInstance(Vault.class);

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
        httpsHost = config.getConnectorHttpsHost();
        httpsPort = config.getConnectorHttpsPort();

        var hasConnector = false;
        if (httpPort > 0 && StringUtils.isNotBlank(httpHost)) {
            builder.addHttpListener(httpPort, httpHost);
            hasConnector = true;
        }

        if (httpsPort > 0 && StringUtils.isNotBlank(httpsHost)) {
            builder.addHttpsListener(httpsPort, httpsHost, vault.getSSLContext(config.getConnectorHttpsCertificateAlias()));
            hasConnector = true;
        }

        if (hasConnector) {
            undertow = builder.build();
            undertow.start();
        } else {
            LOG.error("No connector found! Please configure a HTTP and/or an HTTPS connector in your config.yaml file");
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

        if (httpsPort > 0 && StringUtils.isNotBlank(httpsHost)) {
            LOG.info("HTTPS connector listening @{}:{}", httpsHost, httpsPort);
        }

        String startup = "mangoo I/O application started in " + ChronoUnit.MILLIS.between(START, LocalDateTime.now()) + " ms in " + mode + " mode. Enjoy.";
        LOG.info(startup);
    }

    private static int getBitLength(String secret) {
        Objects.requireNonNull(secret, Required.SECRET);

        return CommonUtils.bitLength(RegExUtils.replaceAll(secret, "[^\\x00-\\x7F]", Strings.EMPTY));
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
                .removeTemporaryFilesAfterScan()
                .scan();
    }

    public static void stopEmbeddedMongoDB() {
        module.stopEmbeddedMongoDB();
    }
}