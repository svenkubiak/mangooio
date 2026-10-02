package io.mangoo.core;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import com.google.common.reflect.TypeToken;
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
import io.mangoo.crypto.PasswordHasher;
import io.mangoo.crypto.Vault;
import io.mangoo.enums.Mode;
import io.mangoo.enums.Sort;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.persistence.interfaces.Datastore;
import io.mangoo.routing.Bind;
import io.mangoo.routing.On;
import io.mangoo.routing.Router;
import io.mangoo.routing.handlers.*;
import io.mangoo.routing.routes.*;
import io.mangoo.scheduler.CronTask;
import io.mangoo.scheduler.FixedDelayTask;
import io.mangoo.scheduler.Schedule;
import io.mangoo.scheduler.Scheduler;
import io.mangoo.utils.CommonUtils;
import io.mangoo.utils.PersistenceUtils;
import io.mangoo.utils.internal.Log4jListener;
import io.mangoo.utils.internal.MangooUtils;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.server.handlers.sse.ServerSentEventConnectionCallback;
import io.undertow.util.Methods;
import io.undertow.websockets.WebSocketConnectionCallback;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;
import org.bson.conversions.Bson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public final class Application {
    private static final Log4jListener LOG4J_LISTENER = new Log4jListener();
    static {
        StatusLogger.getLogger().registerListener(LOG4J_LISTENER);
    }
    private static final Logger LOG = LogManager.getLogger(Application.class);
    private static final long START = System.currentTimeMillis();
    private static final int SECRET_BIT_LENGTH = 512;
    private static final String COLLECTION = "io.mangoo.annotations.Collection";
    private static final String INDEXED = "io.mangoo.annotations.Indexed";
    private static final String SCHEDULER = "io.mangoo.annotations.Run";
    private static final String MODULE_CLASS = "app.Module";
    private static final String ALL_PACKAGES = "*";
    // Only unambiguous third-party prefixes, as application classes must still be found in any other package
    static final String[] SCAN_REJECTED_PACKAGES = {
            "com.bastiaanjansen", "com.cronutils", "com.fasterxml", "com.github.benmanes", "com.google", "com.icegreen",
            "com.launchdarkly", "com.mongodb", "com.nimbusds", "com.sun", "de.flapdoodle",
            "de.svenkubiak.embeddedmongodb", "edu.umd", "freemarker", "io.github.classgraph", "io.micrometer",
            "io.netty", "io.opentelemetry", "io.smallrye", "io.undertow", "jakarta", "javassist", "javax",
            "jersey.repackaged", "kotlin", "net.bytebuddy", "net.glxn", "net.jawr", "net.jcip",
            "nonapi.io.github.classgraph", "okhttp3", "okio", "org.aopalliance", "org.apache", "org.apiguardian",
            "org.awaitility", "org.bouncycastle", "org.bson", "org.cactoos", "org.codehaus", "org.commonmark",
            "org.eclipse", "org.exparity", "org.glassfish", "org.hamcrest", "org.hibernate", "org.jboss", "org.jgrapht",
            "org.jheaps", "org.jspecify", "org.junit", "org.jvnet", "org.llorllale", "org.mockito", "org.objenesis",
            "org.ocpsoft", "org.opentest4j", "org.reactivestreams", "org.reflections", "org.slf4j", "org.wildfly",
            "org.xnio", "org.yaml"
    };
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

    public static void main() {
        start(Mode.PROD);
    }

    public static void start(Mode mode) {
        Objects.requireNonNull(mode, Required.MODE);

        if (!started) {
            logCheck();
            prepareMode(mode);
            userCheck();
            prepareInjector();
            applicationInitialized();
            prepareConfig();
            preparePasswordHasher();
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
            awaitScan(scan);
            prepareUndertow();
            prepareShutdown();
            sanityChecks();
            checkDatastore();
            applicationStarted();
            showTimezone();
            showLogo();
            started = true;
        }
    }

    // Instantiated eagerly so the effective Argon2 concurrency is logged at startup, not on the first hash.
    private static void preparePasswordHasher() {
        getInstance(PasswordHasher.class);
    }

    private static void logCheck() {
        LOG4J_LISTENER.validateAndCollect();
        if (!LOG4J_LISTENER.getEvents().isEmpty()) {
            System.out.println("Log4j configuration failed with errors:");
            System.out.println("\t" + LOG4J_LISTENER.getEvents().toString());
            failsafe();
        }
    }

    // Collections, indexes, jobs and subscribers must be registered before the server accepts the first request
    private static void awaitScan(Thread scan) {
        try {
            scan.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Interrupted while waiting for the classpath scan", e);
            failsafe();
        }
    }

    private static void checkDatastore() {
        if (!getInstance(Datastore.class).isHealthy()) {
            LOG.error("MongoDB did not startup successfully");
            failsafe();
        }
    }

    private static void validateUrls() {
        if (!Router.validUrls()) {
            failsafe();
        }
    }

    private static void prepareScheduler(ScanResult scanResult) {
        var config = getInstance(Config.class);

        if (config.isSchedulerEnabled()) {
            scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            executorService = Executors.newThreadPerTaskExecutor(Thread.ofPlatform().factory());

            // Only @Run is evaluated, other methods and annotations of a job class are irrelevant
            scanResult.getClassesWithMethodAnnotation(SCHEDULER).forEach(classInfo ->
                classInfo.getMethodInfo().stream()
                    .filter(methodInfo -> methodInfo.hasAnnotation(SCHEDULER))
                    .forEach(methodInfo -> {
                        String at = ((String) methodInfo.getAnnotationInfo(SCHEDULER)
                                .getParameterValues(true).getValue("at"))
                                .toLowerCase(Locale.ENGLISH)
                                .trim();

                        if (at.contains("every")) {
                            at = at.replace("every", Strings.EMPTY).trim();
                            var timespan = at.substring(0, at.length() - 1);
                            var duration = at.substring(at.length() - 1);
                            schedule(classInfo, methodInfo, false, getSeconds(timespan, duration), at);
                        } else if (StringUtils.isNotBlank(at)) {
                            schedule(classInfo, methodInfo, true, 0, at);
                        }
                    })
            );
        }
    }

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

        if (!isSchedulable(classInfo.loadClass(), methodInfo.getName())) {
            LOG.error("@Run method '{}' in class '{}' must be public and without parameters", methodInfo.getName(), classInfo.getName());
            failsafe();
        }

        if (isCron) {
            try {
                var parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX));
                var quartzCron = parser.parse(at);
                quartzCron.validate();

                var cronTask = new CronTask(classInfo.loadClass(), methodInfo.getName(), at);
                ScheduledFuture<?> scheduledFuture = cronTask.schedule();
                if (scheduledFuture == null) {
                    throw new IllegalArgumentException("Cron '" + at + "' has no further execution");
                }
                getInstance(Scheduler.class).addSchedule(Schedule.of(classInfo.loadClass().toString(), methodInfo.getName(), at, cronTask::getScheduledFuture, true));

                LOG.info("Successfully scheduled cron task from class '{}' with method '{}' and cron '{}'", classInfo.getName(), methodInfo.getName(), at);
            } catch (IllegalArgumentException e) {
                LOG.error("Scheduled cron task found, but the unix cron is invalid", e);
                failsafe();
            }
        } else {
            if (time > 0) {
                var fixedDelayTask = new FixedDelayTask(classInfo.loadClass(), methodInfo.getName(), time);
                fixedDelayTask.schedule();
                getInstance(Scheduler.class).addSchedule(Schedule.of(classInfo.loadClass().toString(), methodInfo.getName(), "every " + at, fixedDelayTask::getScheduledFuture, false));

                LOG.info("Successfully scheduled task from class '{}' with method '{}' at rate 'Every {}'", classInfo.getName(), methodInfo.getName(), at);
            } else {
                LOG.error("Scheduled task found, but unable to schedule it. Check class '{}' with method '{}' at rate 'Every {}'", classInfo.getName(), methodInfo.getName(), at);
                failsafe();
            }
        }
    }

    record IndexDefinition(String field, Bson keys, IndexOptions options) {
    }

    // One index per @Indexed field; other annotations on the field and their order are irrelevant.
    static List<IndexDefinition> getIndexDefinitions(Class<?> clazz) {
        Objects.requireNonNull(clazz, Required.CLASS);

        List<IndexDefinition> indexes = new ArrayList<>();
        for (var field : clazz.getDeclaredFields()) {
            var indexed = field.getAnnotation(io.mangoo.annotations.Indexed.class);
            if (indexed == null) {
                continue;
            }

            var options = new IndexOptions().unique(indexed.unique());
            if (!indexed.caseSensitive()) {
                options.collation(Collation.builder()
                        .locale("en")
                        .collationStrength(CollationStrength.SECONDARY)
                        .build());
            }

            Bson keys = indexed.sort() == Sort.ASCENDING
                    ? Indexes.ascending(field.getName())
                    : Indexes.descending(field.getName());

            indexes.add(new IndexDefinition(field.getName(), keys, options));
        }

        return indexes;
    }

    private static void prepareDatastore(ScanResult scanResult) {
        var config = getInstance(Config.class);
        if (config.isPersistenceEnabled()) {
            scanResult.getClassesWithAnnotation(COLLECTION).forEach(classInfo -> {
                var collection = classInfo.loadClass().getAnnotation(io.mangoo.annotations.Collection.class);
                PersistenceUtils.addCollection(classInfo.getName(), collection.name());
            });

            Datastore datastore = getInstance(Datastore.class);
            scanResult.getClassesWithFieldAnnotation(INDEXED).forEach(classInfo -> {
                var clazz = classInfo.loadClass();
                for (IndexDefinition index : getIndexDefinitions(clazz)) {
                    try {
                        datastore.addIndex(clazz, index.keys(), index.options());
                    } catch (MongoCommandException e) {
                        LOG.error("Failed to add mongodb index for class {} and index name {}", clazz, index.field(), e);
                        throw e;
                    }
                }
            });

        }
    }

    private static void prepareSubscriber(ScanResult scanResult) {
        scanResult.getClassesImplementing(Subscriber.class).forEach(classInfo -> {
            if (classInfo.isAbstract() || classInfo.isInterface()) {
                return;
            }

            var subscriberClass = classInfo.loadClass();
            Class<?> eventType = TypeToken.of(subscriberClass)
                    .resolveType(Subscriber.class.getTypeParameters()[0])
                    .getRawType();

            if (eventType == Object.class) {
                LOG.warn("Could not determine the event type of subscriber '{}', declare it as Subscriber<EventType>", subscriberClass);
            } else {
                getInstance(EventBus.class).register(eventType.getName(), subscriberClass);
                LOG.info("Registered subscriber '{}' for '{}'", subscriberClass, eventType.getName());
            }
        });
    }

    private static void userCheck() {
        String osName = System.getProperty("os.name");
        if (StringUtils.isNotBlank(osName) && !osName.startsWith("Windows")) {
            String [] command = {"id", "-u"};

            try {
                Process exec = Runtime.getRuntime().exec(command); //NOSONAR
                var input = new BufferedReader(new InputStreamReader(exec.getInputStream(), StandardCharsets.UTF_8));
                String output = input.lines().collect(Collectors.joining(System.lineSeparator()));

                input.close();

                if (isRootForbidden(output, mode)) {
                    LOG.error("Can not run application as root in PROD mode, run it as an unprivileged user (e.g. USER in the Docker image)");
                    failsafe();
                }
            } catch (IOException e) {
                LOG.error("Failed to check if application is started as root", e);
            }
        }
    }

    public static boolean inDevMode() {
        return Mode.DEV == mode;
    }

    // Task invokes the method via getMethod, which only finds public methods without parameters
    static boolean isSchedulable(Class<?> clazz, String methodName) {
        try {
            clazz.getMethod(methodName);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    static boolean isRootForbidden(String uid, Mode mode) {
        return "0".equals(StringUtils.trim(uid)) && mode == Mode.PROD;
    }

    public static boolean inProdMode() {
        return Mode.PROD == mode;
    }

    public static boolean inTestMode() {
        return Mode.TEST == mode;
    }

    public static Mode getMode() {
        return mode;
    }

    public static ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    public static ExecutorService getExecutorService() {
        return executorService;
    }

    public static Injector getInjector() {
        return injector;
    }

    public static boolean isStarted() {
        return started;
    }

    public static LocalDateTime getStart() {
        var config = getInstance(Config.class);

        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(START),
                config.getApplicationTimeZone()
        );
    }

    public static Duration getUptime() {
        return Duration.between(getStart(), LocalDateTime.now());
    }

    public static <T> T getInstance(Class<T> clazz) {
        Objects.requireNonNull(clazz, Required.CLASS);

        return injector.getInstance(clazz);
    }

    public static void stopUndertow() {
        undertow.stop();
    }

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

    private static void prepareInjector() {
        injector = Guice.createInjector(Stage.PRODUCTION, getModules());
    }

    private static void applicationInitialized() {
        getInstance(MangooBootstrap.class).applicationInitialized();
    }

    private static void prepareConfig() {
        var config = getInstance(Config.class);

        config.validate();
        if (!config.isValid()) {
            LOG.error("Application configuration is invalid");
            failsafe();
        }

        checkSecret(Key.APPLICATION_SECRET, config.getApplicationSecret().getBytes(StandardCharsets.UTF_8));
        checkSecret(Key.AUTHENTICATION_COOKIE_SECRET, config.getAuthenticationCookieSecret());
        checkSecret(Key.SESSION_COOKIE_SECRET, config.getSessionCookieSecret());
        checkSecret(Key.FLASH_COOKIE_SECRET, config.getFlashCookieSecret());
        checkKey(Key.AUTHENTICATION_COOKIE_KEY, config.getAuthenticationCookieKey());
        checkKey(Key.SESSION_COOKIE_KEY, config.getSessionCookieKey());
        checkKey(Key.FLASH_COOKIE_KEY, config.getFlashCookieKey());

        if (StringUtils.isNotBlank(config.getString(Key.APPLICATION_API_KEY))) {
            checkKey(Key.APPLICATION_API_KEY, config.getString(Key.APPLICATION_API_KEY).getBytes(StandardCharsets.UTF_8));
        }

        if (config.getAllConfigurations().containsKey(Key.APPLICATION_ALLOWED_ORIGINS) && StringUtils.isBlank(config.getString(Key.APPLICATION_ALLOWED_ORIGINS))) {
            LOG.error("application.allowedOrigins is present in config.yaml, but has no value.");
            failsafe();
        }

        if (config.getAllConfigurations().containsKey(Key.APPLICATION_ADMIN_SECRET) && StringUtils.isBlank(config.getString(Key.APPLICATION_ADMIN_SECRET))) {
            LOG.error("application.admin.secret is present in config.yaml, but has no value.");
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

    private static void prepareRoutes() {
        getInstance(MangooBootstrap.class).initializeRoutes();

        Router.getRequestRoutes().forEach((RequestRoute requestRoute) -> {
            if (!methodExists(requestRoute.getControllerMethod(), requestRoute.getControllerClass())) {
                LOG.error("Could not find controller method '{}' in controller class '{}'", requestRoute.getControllerMethod(), requestRoute.getControllerClass());
                failsafe();
            }
        });
    }

    private static boolean methodExists(String controllerMethod, Class<?> controllerClass) {
        Objects.requireNonNull(controllerMethod, Required.CONTROLLER_METHOD);
        Objects.requireNonNull(controllerClass, Required.CONTROLLER_CLASS);

        return Arrays.stream(controllerClass.getMethods()).anyMatch(method -> method.getName().equals(controllerMethod));
    }

    private static void createRoutes() {
        pathHandler = new PathHandler(getRoutingHandler());

        Router.getServerSentEventRoutes().forEach((ServerSentEventRoute serverSentEventRoute) -> {
                    Class<? extends ServerSentEventConnectionCallback> clazz = serverSentEventRoute.getHandler();
                    ServerSentEventConnectionCallback callback = clazz == null
                            ? getInstance(ServerSentEventHandler.class)
                            : getInstance(clazz);

                    pathHandler.addExactPath(serverSentEventRoute.getUrl(), ServerSentEventHandler.wrap(callback));
                }
        );

        Router.getWebSocketRoutes().forEach((WebSocketRoute webSocketRoute) -> {
                    Class<? extends WebSocketConnectionCallback> clazz = webSocketRoute.getHandler();
                    pathHandler.addExactPath(webSocketRoute.getUrl(), Handlers.websocket(getInstance(clazz)));
                }
        );

        Router.getPathRoutes().forEach((PathRoute pathRoute) ->
                pathHandler.addPrefixPath(pathRoute.getUrl(),
                        new ResourceHandler(new ClassPathResourceManager(Thread.currentThread().getContextClassLoader(), Default.FILES_FOLDER + pathRoute.getUrl())))
        );

        pathHandler.addPrefixPath("/@admin/assets/",
                new ResourceHandler(new ClassPathResourceManager(Thread.currentThread().getContextClassLoader(), "templates/@admin/assets/")));
    }

    private static RoutingHandler getRoutingHandler() {
        // Do not rewrite route template values into query parameters, otherwise a client query parameter could override a route parameter.
        var routingHandler = Handlers.routing(false);
        routingHandler.setFallbackHandler(getInstance(FallbackHandler.class));

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
            var dispatcherHandler = new DispatcherHandler(
                    requestRoute.getControllerClass(),
                    requestRoute.getControllerMethod(),
                    requestRoute.isBlocking(),
                    requestRoute.hasAuthentication()
            );

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
            httpHandler = MetricsHandler.HANDLER_WRAPPER.wrap(ExceptionHandler.wrap(pathHandler));
        } else {
            httpHandler = ExceptionHandler.wrap(pathHandler);
        }

        var builder = Undertow.builder()
                .setServerOption(UndertowOptions.MAX_ENTITY_SIZE, config.getUndertowMaxEntitySize())
                .setServerOption(UndertowOptions.MAX_PARAMETERS, Default.UNDERTOW_MAX_PARAMETERS)
                .setServerOption(UndertowOptions.MAX_HEADER_SIZE, Default.UNDERTOW_MAX_HEADER_SIZE)
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

    private static void showTimezone() {
        LOG.info("Using timezone: {}", TimeZone.getDefault().getID());
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

        LOG.info("mangoo I/O application started in {} ms in {} mode. Enjoy.", System.currentTimeMillis() - START, mode);
    }

    // Secrets are used as dir/A256CBC_HS512 key, which requires exactly 512 bit; any other length fails at runtime.
    private static void checkSecret(String property, byte[] secret) {
        if (!isValidSecret(secret)) {
            LOG.error("{} must be exactly 512 bit (64 bytes) as it is used as AES-256/HMAC-512 encryption key, but has {} bits.", property, CommonUtils.bitLength(secret));
            failsafe();
        }
    }

    private static void checkKey(String property, byte[] key) {
        if (!isValidKey(key)) {
            LOG.error("{} must be at least 512 bit (64 bytes) as it is used as HMAC-512 signing key, but has only {} bits.", property, CommonUtils.bitLength(key));
            failsafe();
        }
    }

    static boolean isValidSecret(byte[] secret) {
        return secret != null && CommonUtils.bitLength(secret) == SECRET_BIT_LENGTH;
    }

    static boolean isValidKey(byte[] key) {
        return key != null && CommonUtils.bitLength(key) >= SECRET_BIT_LENGTH;
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

    private static void failsafe() {
        System.out.print("Failed to start mangoo I/O application"); //NOSONAR Intentionally as we want to exit the application at this point
        System.exit(1); //NOSONAR Intentionally as we want to exit the application at this point
    }

    private static void prepareShutdown() {
        Runtime
                .getRuntime()
                .addShutdownHook(getInstance(Shutdown.class));
    }

    static ScanResult scanClasspath() {
        long start = System.currentTimeMillis();
        var scanResult = new ClassGraph()
                .enableAllInfo()
                .acceptPackages(ALL_PACKAGES)
                .rejectPackages(SCAN_REJECTED_PACKAGES)
                .removeTemporaryFilesAfterScan()
                .scan();

        LOG.info("Scanned {} classes in {} ms, known library packages are excluded", scanResult.getAllClasses().size(), System.currentTimeMillis() - start);
        LOG.debug("Excluded packages from classpath scan: {}", String.join(", ", SCAN_REJECTED_PACKAGES));

        return scanResult;
    }

    public static void stopEmbeddedMongoDB() {
        module.stopEmbeddedMongoDB();
    }
}