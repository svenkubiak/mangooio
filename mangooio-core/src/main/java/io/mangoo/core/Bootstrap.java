package io.mangoo.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Trigger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.netflix.governator.guice.LifecycleInjector;
import com.netflix.governator.lifecycle.LifecycleManager;

import io.github.lukehutch.fastclasspathscanner.FastClasspathScanner;
import io.mangoo.admin.AdminController;
import io.mangoo.annotations.Schedule;
import io.mangoo.configuration.Config;
import io.mangoo.core.yaml.YamlRoute;
import io.mangoo.core.yaml.YamlRouter;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.enums.Mode;
import io.mangoo.enums.RouteType;
import io.mangoo.exceptions.MangooSchedulerException;
import io.mangoo.interfaces.MangooLifecycle;
import io.mangoo.routing.Route;
import io.mangoo.routing.Router;
import io.mangoo.routing.handlers.DispatcherHandler;
import io.mangoo.routing.handlers.ExceptionHandler;
import io.mangoo.routing.handlers.FallbackHandler;
import io.mangoo.routing.handlers.ServerSentEventHandler;
import io.mangoo.routing.handlers.WebSocketHandler;
import io.mangoo.scheduler.Scheduler;
import io.mangoo.utils.BootstrapUtils;
import io.mangoo.utils.SchedulerUtils;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.HttpString;
import io.undertow.util.Methods;

/**
 * Convenient methods for everything to start up a mangoo I/O application
 *
 * @author svenkubiak
 * @author William Dunne
 *
 */
public class Bootstrap {
    private static final Logger LOG = LogManager.getLogger(Bootstrap.class);
    private static final int INITIAL_SIZE = 255;
    private final LocalDateTime start = LocalDateTime.now();
    private final ResourceHandler resourceHandler;
    private Undertow undertow;
    private PathHandler pathHandler;
    private Config config;
    private String httpHost;
    private String ajpHost;
    private Mode mode;
    private Injector injector;
    private boolean error;
    private int httpPort;
    private int ajpPort;
    
    public Bootstrap() {
        this.resourceHandler = Handlers.resource(new ClassPathResourceManager(Thread.currentThread().getContextClassLoader(), Default.FILES_FOLDER.toString() + '/'));
    }

    public Mode prepareMode() {
        this.mode = BootstrapUtils.getMode();
        return this.mode;
    }

    public void prepareLogger() {
        if (StringUtils.isNotBlank(BootstrapUtils.loggerConfig)) {
            LOG.info(BootstrapUtils.loggerConfig);
        }
    }
    
    public Injector prepareInjector() {
        this.injector = LifecycleInjector.builder()
                .withModules(getModules())
                .usingBasePackages(".")
                .build()
                .createInjector();
        
        try {
            this.injector.getInstance(LifecycleManager.class).start();
        } catch (Exception e) {
            LOG.error("Failed to start Governator LifecycleManager", e);
            this.error = true;
        }
        
        return this.injector;
    }

    public void applicationInitialized() {
        this.injector.getInstance(MangooLifecycle.class).applicationInitialized();
    }

    public void prepareConfig() {
        this.config = this.injector.getInstance(Config.class);
        if (!this.config.hasValidSecret()) {
            LOG.error("Please make sure that your application.yaml has an application.secret property which has at least 32 characters");
            this.error = true;
        }
        
        if (!this.config.isDecrypted()) {
            LOG.error("Found encrypted config values in application.yaml but decryption was not successful!");
            this.error = true;
        }
    }
    
    public void sanityChecks() {
        if (mode.equals(Mode.PROD)) {
            if (!config.isAuthenticationCookieSecure()) {
                LOG.warn("Authentication cookie has secure flag set to false. It is highlgy recommended to set cookie.secure to true.");
            }
            
            if (config.getAuthenticationCookieName().equals(Default.AUTHENTICATION_COOKIE_NAME.toString())) {
                LOG.warn("Authentication cookie name has still default value. Consider changeing auth.cookie.name to an application specific value.");
            }
            
            if (!config.isSessionCookieSecure()) {
                LOG.warn("Session cookie has secure flag set to false. It is highlgy recommended to set auth.cookie.secure to true.");
            }
            
            if (config.getSessionCookieName().equals(Default.SESSION_COOKIE_NAME.toString())) {
                LOG.warn("Session cookie name has still default value. Consider changeing cookie.name to an application specific value.");
            }
        }
    }

    @SuppressWarnings("all")
    public void parseRoutes() {
        if (!error()) {
            ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
            YamlRouter yamlRouter = null;
            try {
                yamlRouter = objectMapper.readValue(Resources.getResource(Default.ROUTES_FILE.toString()).openStream(), YamlRouter.class);
            } catch (IOException e) {
                LOG.error("Failed to load routes.yaml Please make sure that your routes.yaml exists in your application src/main/resources folder", e);
                this.error = true;
            }
            
            if (!error() && yamlRouter != null) {
                for (final YamlRoute yamlRoute : yamlRouter.getRoutes()) {
                    RouteType routeType = BootstrapUtils.getRouteType(yamlRoute.getMethod());
                    final Route route = new Route(routeType)
                            .toUrl(yamlRoute.getUrl().trim())
                            .withRequest(HttpString.tryFromString(yamlRoute.getMethod()))
                            .withUsername(yamlRoute.getUsername())
                            .withPassword(yamlRoute.getPassword())
                            .withAuthentication(yamlRoute.isAuthentication())
                            .withTimer(yamlRoute.isTimer())
                            .withLimit(yamlRoute.getLimit())
                            .allowBlocking(yamlRoute.isBlocking());
                    
                    try {
                        String mapping = yamlRoute.getMapping();   
                        if (StringUtils.isNotBlank(mapping)) {
                            if (routeType == RouteType.REQUEST) {
                                int lastIndexOf = mapping.trim().lastIndexOf('.');
                                String controllerClass = BootstrapUtils.getPackageName(this.config.getControllerPackage()) + mapping.substring(0, lastIndexOf);
                                route.withClass(Class.forName(controllerClass));

                                String methodName = mapping.substring(lastIndexOf + 1);
                                if (methodExists(methodName, route.getControllerClass())) {
                                    route.withMethod(methodName);
                                }
                            } else {
                                route.withClass(Class.forName(BootstrapUtils.getPackageName(this.config.getControllerPackage()) + mapping));
                            }
                        }
                       Router.addRoute(route);
                    } catch (final Exception e) {
                        LOG.error("Failed to create routes from routes.yaml");
                        LOG.error("Please verify that your routes.yaml mapping is correct", e);
                        this.error = true;
                    }
                }
            }
            
            if (!error()) {
                createRoutes();
            }
        }
    }

    private boolean methodExists(String controllerMethod, Class<?> controllerClass) {
        boolean exists = false;
        for (final Method method : controllerClass.getMethods()) {
            if (method.getName().equals(controllerMethod)) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            LOG.error("Could not find controller method '" + controllerMethod + "' in controller class '" + controllerClass.getSimpleName() + "'");
            this.error = true;
        }

        return exists;
    }

    private void createRoutes() {
        this.pathHandler = new PathHandler(getRoutingHandler());
        for (final Route route : Router.getRoutes()) {
            if (RouteType.WEBSOCKET == route.getRouteType()) {
                this.pathHandler.addExactPath(route.getUrl(), Handlers.websocket(this.injector.getInstance(WebSocketHandler.class).withControllerClass(route.getControllerClass()).withAuthentication(route.isAuthenticationRequired())));
            } else if (RouteType.SERVER_SENT_EVENT == route.getRouteType()) {
                this.pathHandler.addExactPath(route.getUrl(), Handlers.serverSentEvents(this.injector.getInstance(ServerSentEventHandler.class).withAuthentication(route.isAuthenticationRequired())));
            } else if (RouteType.RESOURCE_PATH == route.getRouteType()) {
                this.pathHandler.addPrefixPath(route.getUrl(), new ResourceHandler(new ClassPathResourceManager(Thread.currentThread().getContextClassLoader(), Default.FILES_FOLDER.toString() + route.getUrl())));
            }
        }
    }

    private RoutingHandler getRoutingHandler() {
        final RoutingHandler routingHandler = Handlers.routing();
        routingHandler.setFallbackHandler(Application.getInstance(FallbackHandler.class));
        
        if (this.config.isAdminEnabled()) {
            Router.addRoute(new Route(RouteType.REQUEST).toUrl("/@admin").withRequest(Methods.GET).withClass(AdminController.class).withMethod("index").useInternalTemplateEngine());
            Router.addRoute(new Route(RouteType.REQUEST).toUrl("/@admin/scheduler").withRequest(Methods.GET).withClass(AdminController.class).withMethod("scheduler").useInternalTemplateEngine());
            Router.addRoute(new Route(RouteType.REQUEST).toUrl("/@admin/logger").withRequest(Methods.GET).withClass(AdminController.class).withMethod("logger").useInternalTemplateEngine());
            Router.addRoute(new Route(RouteType.REQUEST).toUrl("/@admin/logger/ajax").withRequest(Methods.POST).withClass(AdminController.class).withMethod("loggerajax").useInternalTemplateEngine());
            Router.addRoute(new Route(RouteType.REQUEST).toUrl("/@admin/routes").withRequest(Methods.GET).withClass(AdminController.class).withMethod("routes").useInternalTemplateEngine());
            Router.addRoute(new Route(RouteType.REQUEST).toUrl("/@admin/metrics").withRequest(Methods.GET).withClass(AdminController.class).withMethod("metrics").useInternalTemplateEngine());
            Router.addRoute(new Route(RouteType.REQUEST).toUrl("/@admin/tools").withRequest(Methods.GET).withClass(AdminController.class).withMethod("tools").useInternalTemplateEngine());
            Router.addRoute(new Route(RouteType.REQUEST).toUrl("/@admin/tools/ajax").withRequest(Methods.POST).withClass(AdminController.class).withMethod("toolsajax").useInternalTemplateEngine());
            Router.addRoute(new Route(RouteType.REQUEST).toUrl("/@admin/scheduler/execute/{name}").withRequest(Methods.GET).withClass(AdminController.class).withMethod("execute").useInternalTemplateEngine());
            Router.addRoute(new Route(RouteType.REQUEST).toUrl("/@admin/scheduler/state/{name}").withRequest(Methods.GET).withClass(AdminController.class).withMethod("state").useInternalTemplateEngine());
        }

        Router.getRoutes().parallelStream().forEach((Route route) -> {
            if (RouteType.REQUEST == route.getRouteType()) {
                DispatcherHandler dispatcherHandler = Application.getInstance(DispatcherHandler.class).dispatch(route.getControllerClass(), route.getControllerMethod())
                        .isBlocking(route.isBlockingAllowed())
                        .withTimer(route.isTimerEnabled())
                        .withUsername(route.getUsername())
                        .withPassword(route.getPassword())
                        .withLimit(route.getLimit());
                
                routingHandler.add(route.getRequestMethod(),route.getUrl(), dispatcherHandler);
            } else if (RouteType.RESOURCE_FILE == route.getRouteType()) {
                routingHandler.add(Methods.GET, route.getUrl(), this.resourceHandler);
            }
        });

        return routingHandler;
    }

    public void startUndertow() {
        if (!error()) {
            Builder builder = Undertow.builder()
                    .setServerOption(UndertowOptions.MAX_ENTITY_SIZE, this.config.getLong(Key.UNDERTOW_MAX_ENTITY_SIZE, Default.UNDERTOW_MAX_ENTITY_SIZE.toLong()))
                    .setHandler(Handlers.exceptionHandler(this.pathHandler).addExceptionHandler(Throwable.class, Application.getInstance(ExceptionHandler.class)));

            boolean hasConnector = false;
            this.httpHost = this.config.getConnectorHttpHost();
            this.httpPort = this.config.getConnectorHttpPort();
            this.ajpHost = this.config.getConnectorAjpHost();
            this.ajpPort = this.config.getConnectorAjpPort();
            
            if (this.httpPort > 0 && StringUtils.isNotBlank(this.httpHost)) {
                builder.addHttpListener(this.httpPort, this.httpHost);
                hasConnector = true;
            }
            
            if (this.ajpPort > 0 && StringUtils.isNotBlank(this.ajpHost)) {
                builder.addAjpListener(this.ajpPort, this.ajpHost);
                hasConnector = true;
            }
                    
            if (hasConnector) {
                this.undertow = builder.build();
                this.undertow.start();
            } else {
                this.error = true;
                LOG.error("No connector found! Please configure either a HTTP or an AJP connector in your application.yaml");
            }
        }
    }

    private List<Module> getModules() {
        final List<Module> modules = new ArrayList<>();
        if (!error()) {
            try {
                final Class<?> applicationModule = Class.forName(Default.MODULE_CLASS.toString());
                modules.add(new io.mangoo.core.Module());
                modules.add((AbstractModule) applicationModule.getConstructor().newInstance());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
                LOG.error("Failed to load modules. Check that conf/Module.java exists in your application", e);
                this.error = true;
            }
        }
        
        return modules;
    }

    public void showLogo() {
        if (!error()) {
            final StringBuilder buffer = new StringBuilder(INITIAL_SIZE);
            buffer.append('\n')
                .append(BootstrapUtils.getLogo())
                .append("\n\nhttps://mangoo.io | @mangoo_io | ")
                .append(BootstrapUtils.getVersion())
                .append('\n');

            LOG.info(buffer.toString()); //NOSONAR
            
            if (this.httpPort > 0 && StringUtils.isNotBlank(this.httpHost)) {
                LOG.info("HTTP connector listening @{}:{}", this.httpHost, this.httpPort);
            }
            
            if (this.ajpPort > 0 && StringUtils.isNotBlank(this.ajpHost)) {
                LOG.info("AJP connector listening @{}:{}", this.ajpHost, this.ajpPort);
            }
            
            LOG.info("mangoo I/O application started in {} ms in {} mode. Enjoy.", ChronoUnit.MILLIS.between(this.start, LocalDateTime.now()), this.mode.toString());
        }
    }

    public void applicationStarted() {
        this.injector.getInstance(MangooLifecycle.class).applicationStarted();
    }

    public void startQuartzScheduler() {
        if (!error()) {
            List<Class<?>> jobs = new ArrayList<>();
            new FastClasspathScanner(this.config.getSchedulerPackage())
                .matchClassesWithAnnotation(Schedule.class, jobs::add)
                .scan();

            if (!jobs.isEmpty() && this.config.isSchedulerAutostart()) {
                final Scheduler mangooScheduler = this.injector.getInstance(Scheduler.class);
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
                        LOG.info("Successfully scheduled job " + clazz.getName() + " with cron " + schedule.cron());
                    } else {
                        LOG.error("Invalid or missing cron expression for job: " + clazz.getName());
                        this.error = true;
                    }
                }

                if (!error()) {
                    try {
                        mangooScheduler.start();
                    } catch (MangooSchedulerException e) {
                        LOG.error("Failed to start the scheduler", e);
                    }
                }
            }
        }
    }

    public boolean success() {
        return !this.error;
    }

    private boolean error() {
        return this.error;
    }

    public LocalDateTime getStart() {
        return this.start;
    }
    
    public Undertow getUndertow() {
        return this.undertow;
    }
}