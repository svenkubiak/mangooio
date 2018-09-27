package io.mangoo.core;

import java.lang.reflect.InvocationTargetException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Trigger;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import io.mangoo.admin.AdminController;
import io.mangoo.annotations.Schedule;
import io.mangoo.configuration.Config;
import io.mangoo.email.MailEventListener;
import io.mangoo.enums.CacheName;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.enums.Mode;
import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooSchedulerException;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.providers.CacheProvider;
import io.mangoo.routing.Bind;
import io.mangoo.routing.On;
import io.mangoo.routing.Router;
import io.mangoo.routing.handlers.DispatcherHandler;
import io.mangoo.routing.handlers.ExceptionHandler;
import io.mangoo.routing.handlers.FallbackHandler;
import io.mangoo.routing.handlers.MetricsHandler;
import io.mangoo.routing.handlers.ServerSentEventHandler;
import io.mangoo.routing.handlers.WebSocketHandler;
import io.mangoo.routing.routes.FileRoute;
import io.mangoo.routing.routes.PathRoute;
import io.mangoo.routing.routes.RequestRoute;
import io.mangoo.routing.routes.ServerSentEventRoute;
import io.mangoo.routing.routes.WebSocketRoute;
import io.mangoo.scheduler.Scheduler;
import io.mangoo.services.EventBusService;
import io.mangoo.utils.BootstrapUtils;
import io.mangoo.utils.ByteUtils;
import io.mangoo.utils.SchedulerUtils;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.Methods;

/**
 * Main class that starts all components of a mangoo I/O application
 *
 * @author svenkubiak
 *
 */
public final class Application {
    private static Logger LOG = LogManager.getLogger(Application.class);
    private static final int KEY_MIN_BIT_LENGTH = 512;
    private static final int BUFFERSIZE = 255;
    private static volatile String httpHost;
    private static volatile String ajpHost;
    private static volatile int httpPort;
    private static volatile int ajpPort;
    private static volatile Undertow undertow;
    private static volatile Mode mode;
    private static volatile Injector injector;
    private static volatile LocalDateTime start = LocalDateTime.now();
    private static volatile PathHandler pathHandler;
    private static volatile boolean started;
    private static volatile ResourceHandler resourceHandler = Handlers.resource(new ClassPathResourceManager(
            Thread.currentThread().getContextClassLoader(),
            Default.FILES_FOLDER.toString() + '/'));
    
    private Application() {
    }

    public static void main(String... args) {
        start(Mode.PROD);
    }

    public static void start(Mode mode) {
        Objects.requireNonNull(mode, Required.MODE.toString());
        
        if (!started) {
            prepareMode(mode);
            prepareInjector();
            applicationInitialized();
            prepareConfig();
            prepareRoutes();
            createRoutes();
            prepareEventBus();
            prepareScheduler();
            prepareUndertow();
            sanityChecks();
            showLogo();
            applicationStarted();
            
            Runtime.getRuntime().addShutdownHook(getInstance(Shutdown.class));
            started = true;
        }
    }

    /**
     * Registers Listeners at the event bus
     */
    private static void prepareEventBus() {
        getInstance(EventBusService.class).register(getInstance(MailEventListener.class));
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
        return start;
    }

    /**
     * @return The duration of the application uptime
     */
    public static Duration getUptime() {
        Objects.requireNonNull(start, Required.START.toString());

        return Duration.between(start, LocalDateTime.now());
    }

    /**
     * Short form for getting an Goolge Guice injected class by
     * calling getInstance(...)
     *
     * @param clazz The class to retrieve from the injector
     * @param <T> JavaDoc requires this (just ignore it)
     *
     * @return An instance of the requested class
     */
    public static <T> T getInstance(Class<T> clazz) {
        Objects.requireNonNull(clazz, Required.CLASS.toString());

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
        final String applicationMode = System.getProperty(Key.APPLICATION_MODE.toString());
        if (StringUtils.isNotBlank(applicationMode)) {
            switch (applicationMode.toLowerCase(Locale.ENGLISH)) {
                case "dev"  : mode = Mode.DEV;
                break;
                case "test" : mode = Mode.TEST;
                break;
                default     : mode = Mode.PROD;
                break;
            }
        } else {
            mode = providedMode;
        }
    }
    
    /**
     * Sets the injector wrapped through netflix Governator
     */
    private static void prepareInjector() {
        injector = LifecycleInjector.builder()
                .withModules(getModules())
                .usingBasePackages(".")
                .build()
                .createInjector();
        
        try {
            getInstance(LifecycleManager.class).start();
        } catch (Exception e) {
            LOG.error("Failed to start Governator LifecycleManager", e);
            failsafe();
        } 
    }

    /**
     * Callback to MangooLifecycle applicationInitialized
     */
    private static void applicationInitialized() {
        getInstance(MangooBootstrap.class).applicationInitialized();            
    }

    /**
     * Checks for config failures that pervent the application from starting
     */
    private static void prepareConfig() {
        Config config = getInstance(Config.class);
        
        int bitLength = getBitLength(config.getApplicationSecret());
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Application requires a 512 bit application secret. The current property for application.secret has currently only {} bit.", bitLength);
            failsafe();
        }
        
        bitLength = getBitLength(config.getAuthenticationCookieEncryptionKey());
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Authentication cookie requires a 512 bit encryption key. The current property for authentication.cookie.encryptionkey has only {} bit.", bitLength);
            failsafe();
        }
        
        bitLength = getBitLength(config.getAuthenticationCookieSignKey());
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Authentication cookie requires a 512 bit sign key. The current property for authentication.cookie.signkey has only {} bit.", bitLength);
            failsafe();
        }
        
        bitLength = getBitLength(config.getSessionCookieEncryptionKey());
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Session cookie requires a 512 bit encryption key. The current property for session.cookie.encryptionkey has only {} bit.", bitLength);
            failsafe();
        }
        
        bitLength = getBitLength(config.getSessionCookieSignKey());
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Session cookie requires a 512 bit sign key. The current property for session.cookie.signkey has only {} bit.", bitLength);
            failsafe();
        }

        bitLength = getBitLength(config.getFlashCookieSignKey());
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Flash cookie requires a 512 bit sign key. The current property for flash.cookie.signkey has only {} bit.", bitLength);
            failsafe();
        }
        
        bitLength = getBitLength(config.getFlashCookieEncryptionKey());
        if (bitLength < KEY_MIN_BIT_LENGTH) {
            LOG.error("Flash cookie requires a 512 bit encryption key. The current property for flash.cookie.encryptionkey has only {} bit.", bitLength);
            failsafe();
        }
        
        if (!config.isDecrypted()) {
            LOG.error("Found encrypted config values in config.props but decryption was not successful!");
            failsafe();
        }
    }
    
    /**
     * Do sanity checks on the configuration an warn about it in the log
     */
    private static void sanityChecks() {
        Config config = getInstance(Config.class);
        List<String> warnings = new ArrayList<>();
        
        if (!config.isAuthenticationCookieSecure()) {
            String warning = "Authentication cookie has secure flag set to false. It is highly recommended to set authentication.cookie.secure to true in an production environment.";
            warnings.add(warning);
            LOG.warn(warning);
        }
        
        if (config.getAuthenticationCookieName().equals(Default.AUTHENTICATION_COOKIE_NAME.toString())) {
            String warning = "Authentication cookie name has default value. Consider changing authentication.cookie.name to an application specific value.";
            warnings.add(warning);
            LOG.warn(warning);
        }
        
        if (config.getAuthenticationCookieSignKey().equals(config.getApplicationSecret())) {
            String warning = "Authentication cookie sign key is using application secret. It is highly recommended to set a dedicated value to authentication.cookie.signkey.";
            warnings.add(warning);
            LOG.warn(warning);
        }
        
        if (config.getAuthenticationCookieEncryptionKey().equals(config.getApplicationSecret())) {
            String warning = "Authentication cookie encryption is using application secret. It is highly recommended to set a dedicated value to authentication.cookie.encryptionkey.";
            warnings.add(warning);
            LOG.warn(warning);
        }
        
        if (!config.isSessionCookieSecure()) {
            String warning = "Session cookie has secure flag set to false. It is highly recommended to set session.cookie.secure to true in an production environment.";
            warnings.add(warning);
            LOG.warn(warning);
        }
        
        if (config.getSessionCookieName().equals(Default.SESSION_COOKIE_NAME.toString())) {
            String warning = "Session cookie name has default value. Consider changing session.cookie.name to an application specific value.";
            warnings.add(warning);
            LOG.warn(warning);
        }
        
        if (config.getSessionCookieSignKey().equals(config.getApplicationSecret())) {
            String warning = "Session cookie sign key is using application secret. It is highly recommended to set a dedicated value to session.cookie.signkey.";
            warnings.add(warning);
            LOG.warn(warning);
        }
        
        if (config.getSessionCookieEncryptionKey().equals(config.getApplicationSecret())) {
            String warning = "Session cookie encryption is using application secret. It is highly recommended to set a dedicated value to session.cookie.encryptionkey.";
            warnings.add(warning);
            LOG.warn(warning);
        }
        
        if (config.getFlashCookieName().equals(Default.FLASH_COOKIE_NAME.toString())) {
            String warning = "Flash cookie name has default value. Consider changing flash.cookie.name to an application specific value.";
            warnings.add(warning);
            LOG.warn(warning);
        }
        
        if (config.getFlashCookieSignKey().equals(config.getApplicationSecret())) {
            String warning = "Flash cookie sign key is using application secret. It is highly recommended to set a dedicated value to flash.cookie.signkey.";
            warnings.add(warning);
            LOG.warn(warning);
        }
        
        if (config.getFlashCookieEncryptionKey().equals(config.getApplicationSecret())) {
            String warning = "Flash cookie encryption key is using application secret. It is highly recommended to set a dedicated value to flash.cookie.encryptionkey.";
            warnings.add(warning);
            LOG.warn(warning);
        }
        
        getInstance(CacheProvider.class).getCache(CacheName.APPLICATION).put(Key.MANGOOIO_WARNINGS.toString(), warnings);
    }

    /**
     * Validate if the routes that are defined in the router are valid
     */
    private static void prepareRoutes() {
        injector.getInstance(MangooBootstrap.class).initializeRoutes();
        
        Router.getRoutes().forEach((mangooRoute) -> {
            if (mangooRoute instanceof RequestRoute) {
                RequestRoute requestRoute = (RequestRoute) mangooRoute;

                if (!BootstrapUtils.methodExists(requestRoute.getControllerMethod(), requestRoute.getControllerClass())) {
                    LOG.error("Method '{}' does not exists in controller class '{}'", requestRoute.getControllerMethod(), requestRoute.getControllerClass());
                    failsafe();
                }
            }
        });
    }

    /**
     * Create routes for WebSockets ServerSentEvent and Resource files
     */
    private static void createRoutes() {
        pathHandler = new PathHandler(getRoutingHandler());
        Router.getRoutes().forEach((mangooRoute) -> {
            if (mangooRoute instanceof WebSocketRoute) {
                WebSocketRoute webSocketRoute = (WebSocketRoute) mangooRoute;
                
                pathHandler.addExactPath(webSocketRoute.getUrl(),
                        Handlers.websocket(getInstance(WebSocketHandler.class)
                                .withControllerClass(webSocketRoute.getControllerClass())
                                .withAuthentication(webSocketRoute.hasAuthentication())));

            } else if (mangooRoute instanceof ServerSentEventRoute) {
                ServerSentEventRoute serverSentEventRoute = (ServerSentEventRoute) mangooRoute;
                
                pathHandler.addExactPath(serverSentEventRoute.getUrl(),
                        Handlers.serverSentEvents(getInstance(ServerSentEventHandler.class)
                                .withAuthentication(serverSentEventRoute.hasAuthentication())));
            } else if (mangooRoute instanceof PathRoute) {
                PathRoute pathRoute = (PathRoute) mangooRoute;
                
                pathHandler.addPrefixPath(pathRoute.getUrl(),
                        new ResourceHandler(new ClassPathResourceManager(Thread.currentThread().getContextClassLoader(), Default.FILES_FOLDER.toString() + pathRoute.getUrl())));
            } else {
                // Ignoring anything else except WebSocket ServerSentEvent or Resource Path for PathHandler
            }
        });
    }

    private static RoutingHandler getRoutingHandler() {
        final RoutingHandler routingHandler = Handlers.routing();
        routingHandler.setFallbackHandler(Application.getInstance(FallbackHandler.class));
        
        Config config = getInstance(Config.class);
        if (config.isApplicationAdminEnable()) {
            Bind.controller(AdminController.class).withBasicAuthentication(config.getApplicationAdminUsername(), config.getApplicationAdminPassword())
            .withRoutes(
                    On.get().to("/@admin").respondeWith("index"),
                    On.get().to("/@admin/health").respondeWith("health"),
                    On.get().to("/@admin/scheduler").respondeWith("scheduler"),
                    On.get().to("/@admin/logger").respondeWith("logger"),
                    On.post().to("/@admin/logger/ajax").respondeWith("loggerajax"),
                    On.get().to("/@admin/routes").respondeWith("routes"),
                    On.get().to("/@admin/metrics").respondeWith("metrics"),
                    On.get().to("/@admin/metrics/reset").respondeWith("resetMetrics"),
                    On.get().to("/@admin/metrics/reset").respondeWith("resetMetrics"),
                    On.get().to("/@admin/tools").respondeWith("tools"),
                    On.post().to("/@admin/tools/ajax").respondeWith("toolsajax"),
                    On.get().to("/@admin/scheduler/execute/{name}").respondeWith("execute"),
                    On.get().to("/@admin/scheduler/state/{name}").respondeWith("state")
             );
        }

        Router.getRoutes().forEach((mangooRoute) -> {
            if (mangooRoute instanceof RequestRoute) {
                RequestRoute requestRoute = (RequestRoute) mangooRoute;
                DispatcherHandler dispatcherHandler = Application.getInstance(DispatcherHandler.class)
                        .dispatch(requestRoute.getControllerClass(), requestRoute.getControllerMethod())
                        .isBlocking(requestRoute.isBlocking())
                        .withBasicAuthentication(requestRoute.getUsername(), requestRoute.getPassword())
                        .withAuthentication(requestRoute.hasAuthentication())
                        .withAuthorization(requestRoute.hasAuthorization())
                        .withLimit(requestRoute.getLimit());
                
                routingHandler.add(requestRoute.getMethod().toString(), requestRoute.getUrl(), dispatcherHandler);
            } else if (mangooRoute instanceof FileRoute) {
                FileRoute fileRoute = (FileRoute) mangooRoute;
                routingHandler.add(Methods.GET, fileRoute.getUrl(), resourceHandler);
            } else {
                // Ignoring anything else except Request and RequestFile for DispatcherHandler
            }
        });

        return routingHandler;
    }

    private static void prepareUndertow() {
        Config config = getInstance(Config.class);
        
        HttpHandler httpHandler;
        if (config.isMetricsEnable()) {
            httpHandler = MetricsHandler.HANDLER_WRAPPER.wrap(Handlers.exceptionHandler(pathHandler)
                    .addExceptionHandler(Throwable.class, Application.getInstance(ExceptionHandler.class)));
        } else {
            httpHandler = Handlers.exceptionHandler(pathHandler)
                    .addExceptionHandler(Throwable.class, Application.getInstance(ExceptionHandler.class));
        }
        
        Builder builder = Undertow.builder()
                .setServerOption(UndertowOptions.MAX_ENTITY_SIZE, config.getUndertowMaxEntitySize())
                .setHandler(httpHandler);

        httpHost = config.getConnectorHttpHost();
        httpPort = config.getConnectorHttpPort();
        ajpHost = config.getConnectorAjpHost();
        ajpPort = config.getConnectorAjpPort();

        boolean hasConnector = false;
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
            LOG.error("No connector found! Please configure a HTTP and/or AJP connector in your config.props");
            failsafe();
        }
    }

    private static void showLogo() {
        final StringBuilder buffer = new StringBuilder(BUFFERSIZE);
        buffer.append('\n')
            .append(BootstrapUtils.getLogo())
            .append("\n\nhttps://github.com/svenkubiak/mangooio | @mangoo_io | ")
            .append(BootstrapUtils.getVersion())
            .append('\n');

        LOG.info(buffer.toString()); //NOSONAR
        
        if (httpPort > 0 && StringUtils.isNotBlank(httpHost)) {
            LOG.info("HTTP connector listening @{}:{}", httpHost, httpPort);
        }
        
        if (ajpPort > 0 && StringUtils.isNotBlank(ajpHost)) {
            LOG.info("AJP connector listening @{}:{}", ajpHost, ajpPort);
        }
        
        LOG.info("mangoo I/O application started in {} ms in {} mode. Enjoy.", ChronoUnit.MILLIS.between(start, LocalDateTime.now()), mode.toString());
    }

    private static int getBitLength(String secret) {
        secret = RegExUtils.replaceAll(secret, "[^\\x00-\\x7F]", "");
        return ByteUtils.bitLength(secret);
    }

    private static List<Module> getModules() {
        final List<Module> modules = new ArrayList<>();
        try {
            modules.add(new io.mangoo.core.Module());
            modules.add((AbstractModule) Class.forName(Default.MODULE_CLASS.toString()).getConstructor().newInstance());
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

    private static void prepareScheduler() {
        Config config = getInstance(Config.class);
        
        List<Class<?>> jobs = new ArrayList<>();
        try (ScanResult scanResult =
                new ClassGraph()
                    .enableAnnotationInfo()
                    .enableClassInfo()
                    .whitelistPackages(config.getSchedulerPackage())
                    .scan()) {
            scanResult.getClassesWithAnnotation(Default.SCHEDULER_ANNOTATION.toString()).forEach(c -> jobs.add(c.loadClass()));
        }
        
        if (!jobs.isEmpty() && config.isSchedulerAutostart()) {
            final Scheduler mangooScheduler = getInstance(Scheduler.class);
            mangooScheduler.initialize();
            
            for (Class<?> clazz : jobs) {
                final Schedule schedule = clazz.getDeclaredAnnotation(Schedule.class);
                if (CronExpression.isValidExpression(schedule.cron())) {
                    final JobDetail jobDetail = SchedulerUtils.createJobDetail(clazz.getName(), Default.SCHEDULER_JOB_GROUP.toString(), clazz.asSubclass(Job.class));
                    final Trigger trigger = SchedulerUtils.createTrigger(clazz.getName() + "-trigger", Default.SCHEDULER_TRIGGER_GROUP.toString(), schedule.description(), schedule.cron());
                    try {
                        mangooScheduler.schedule(jobDetail, trigger);
                    } catch (MangooSchedulerException e) {
                        LOG.error("Failed to add a job to the scheduler", e);
                    }
                    LOG.info("Successfully scheduled job {} with cron {} ", clazz.getName(), schedule.cron());
                } else {
                    LOG.error("Invalid or missing cron expression for job: {}", clazz.getName());
                    failsafe();
                }
            }

            try {
                mangooScheduler.start();
            } catch (MangooSchedulerException e) {
                LOG.error("Failed to start the scheduler", e);
                failsafe();
            }
        }
    }
    
    /**
     * Failsafe exit of application startup
     */
    private static void failsafe() {
        System.out.print("Failed to start mangoo I/O application"); //NOSONAR
        System.exit(1); //NOSONAR
    }
}