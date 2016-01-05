package io.mangoo.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.reflections.Reflections;
import org.yaml.snakeyaml.Yaml;

import com.github.lalyos.jfiglet.FigletFont;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

import io.mangoo.admin.MangooAdminController;
import io.mangoo.annotations.Schedule;
import io.mangoo.configuration.Config;
import io.mangoo.enums.AdminRoute;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.enums.Mode;
import io.mangoo.enums.RouteType;
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
import io.undertow.Handlers;
import io.undertow.Undertow;
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
 *
 */
public class Bootstrap {
    private static volatile Logger LOG; //NOSONAR
    private static final int INITIAL_SIZE = 255;
    private final LocalDateTime start;
    private PathHandler pathHandler;
    private ResourceHandler resourceHandler;
    private Config config;
    private String host;
    private Mode mode;
    private Injector injector;
    private boolean error;
    private int port;

    public Bootstrap() {
        this.start = LocalDateTime.now();
    }

    public Mode prepareMode() {
        final String applicationMode = System.getProperty(Key.APPLICATION_MODE.toString());
        if (StringUtils.isNotBlank(applicationMode)) {
            switch (applicationMode.toLowerCase(Locale.ENGLISH)) {
                case "dev"  : this.mode = Mode.DEV;
                break;
                case "test" : this.mode = Mode.TEST;
                break;
                default     : this.mode = Mode.PROD;
                break;
            }
        } else {
            this.mode = Mode.PROD;
        }

        return this.mode;
    }

    @SuppressWarnings("all")
    public void prepareLogger() {
        final String configurationFile = "log4j2." + this.mode.toString() + ".xml";
        if (Thread.currentThread().getContextClassLoader().getResource(configurationFile) == null) {
            LOG = LogManager.getLogger(Bootstrap.class); //NOSONAR
        } else {
            try {
                final URL resource = Thread.currentThread().getContextClassLoader().getResource(configurationFile);
                final LoggerContext context = (LoggerContext) LogManager.getContext(false);
                context.setConfigLocation(resource.toURI());
            } catch (final URISyntaxException e) {
                e.printStackTrace(); //NOSONAR
                this.error = true;
            }

            if (!hasError()) {
                LOG = LogManager.getLogger(Bootstrap.class); //NOSONAR
                LOG.info("Found environment specific Log4j2 configuration. Using configuration file: " + configurationFile);                
            }
        }
    }

    public Injector prepareInjector() {
        this.injector = Guice.createInjector(Stage.PRODUCTION, getModules());
        return this.injector;
    }

    public void applicationInitialized() {
        this.injector.getInstance(MangooLifecycle.class).applicationInitialized();
    }

    public void prepareConfig() {
        this.config = this.injector.getInstance(Config.class);
        if (!this.config.hasValidSecret()) {
            LOG.error("Please make sure that your application.yaml has an application.secret property which has at least 16 characters");
            this.error = true;
        }
    }

    @SuppressWarnings("all")
    public void prepareRoutes() {
        if (!hasError()) {
            try (InputStream inputStream = Resources.getResource(Default.ROUTES_FILE.toString()).openStream()) {
                final List<Map<String, String>> routes = (List<Map<String, String>>) new Yaml().load(inputStream);

                routes.forEach(routing -> {
                    routing.entrySet().forEach(entry -> {
                        final String method = entry.getKey().trim();
                        final String mapping = entry.getValue();
                        final String [] mappings = mapping
                                .replace(Default.AUTHENTICATION.toString(), "")
                                .replace(Default.BLOCKING.toString(), "")
                                .split("->");
                        
                        if (mappings != null && mappings.length > 0) {
                            final String url = mappings[0].trim();
                            final Route route = new Route(BootstrapUtils.getRouteType(method));
                            route.toUrl(url);
                            route.withRequest(HttpString.tryFromString(method));
                            route.withAuthentication(BootstrapUtils.hasAuthentication(mapping));
                            route.allowBlocking(BootstrapUtils.hasBlocking(mapping));

                            try {
                                if (mappings.length == 2) {
                                    final String [] classMethod = mappings[1].split("\\.");
                                    if (classMethod != null && classMethod.length > 0) {
                                        route.withClass(Class.forName(this.config.getControllerPackage() + classMethod[0].trim()));
                                        if (classMethod.length == 2) {
                                            route.withMethod(classMethod[1].trim());
                                        }                                    
                                    }
                                }

                                Router.addRoute(route);
                            } catch (final Exception e) {
                                LOG.error("Failed to parse routing: " + routing);
                                LOG.error("Please check, that your routes.yaml syntax is correct", e);
                                this.error = true;
                            } 
                        }
                    });
                });
            } catch (final Exception e) {
                LOG.error("Failed to load routes.yaml Please check, that routes.yaml exists in your application resource folder", e);
                this.error = true;
            }

            if (!hasError()) {
                checkRoutes();
                initPathHandler();
            }
        }
    }

    private void checkRoutes() {
        Router.getRoutes().forEach(route -> {
            if (RouteType.REQUEST.equals(route.getRouteType())) {
                checkRoute(route, route.getControllerClass());
            }
        });        
    }

    private void checkRoute(Route route, Class<?> controllerClass) {
        if (!hasError()) {
            boolean found = false;
            for (final Method method : controllerClass.getMethods()) {
                if (method.getName().equals(route.getControllerMethod())) {
                    found = true;
                }
            }

            if (!found) {
                LOG.error("Could not find controller method '" + route.getControllerMethod() + "' in controller class '" + controllerClass.getSimpleName() + "'");
                this.error = true;
            }
        }
    }

    private void initPathHandler() {
        this.pathHandler = new PathHandler(initRoutingHandler());
        Router.getRoutes().forEach(route -> {
            if (RouteType.WEBSOCKET.equals(route.getRouteType())) {
                this.pathHandler.addExactPath(route.getUrl(), Handlers.websocket(new WebSocketHandler(route.getControllerClass(), route.isAuthenticationRequired())));
            } else if (RouteType.SERVER_SENT_EVENT.equals(route.getRouteType())) {
                this.pathHandler.addExactPath(route.getUrl(), Handlers.serverSentEvents(new ServerSentEventHandler(route.isAuthenticationRequired())));
            } else if (RouteType.RESOURCE_PATH.equals(route.getRouteType())) {
                this.pathHandler.addPrefixPath(route.getUrl(), getResourceHandler(route.getUrl()));
            }
        });
    }

    private RoutingHandler initRoutingHandler() {
        final RoutingHandler routingHandler = Handlers.routing();
        routingHandler.setFallbackHandler(new FallbackHandler());

        Router.addRoute(new Route(RouteType.REQUEST).toUrl(AdminRoute.ROUTES.toString()).withRequest(Methods.GET).withClass(MangooAdminController.class).withMethod("routes"));
        Router.addRoute(new Route(RouteType.REQUEST).toUrl(AdminRoute.CONFIG.toString()).withRequest(Methods.GET).withClass(MangooAdminController.class).withMethod("config"));
        Router.addRoute(new Route(RouteType.REQUEST).toUrl(AdminRoute.HEALTH.toString()).withRequest(Methods.GET).withClass(MangooAdminController.class).withMethod("health"));
        Router.addRoute(new Route(RouteType.REQUEST).toUrl(AdminRoute.CACHE.toString()).withRequest(Methods.GET).withClass(MangooAdminController.class).withMethod("cache"));
        Router.addRoute(new Route(RouteType.REQUEST).toUrl(AdminRoute.METRICS.toString()).withRequest(Methods.GET).withClass(MangooAdminController.class).withMethod("metrics"));
        Router.addRoute(new Route(RouteType.REQUEST).toUrl(AdminRoute.SCHEDULER.toString()).withRequest(Methods.GET).withClass(MangooAdminController.class).withMethod("scheduler"));
        Router.addRoute(new Route(RouteType.REQUEST).toUrl(AdminRoute.SYSTEM.toString()).withRequest(Methods.GET).withClass(MangooAdminController.class).withMethod("system"));
        Router.addRoute(new Route(RouteType.REQUEST).toUrl(AdminRoute.MEMORY.toString()).withRequest(Methods.GET).withClass(MangooAdminController.class).withMethod("memory"));

        Router.getRoutes().forEach(route -> {
            if (RouteType.REQUEST.equals(route.getRouteType())) {
                routingHandler.add(route.getRequestMethod(), route.getUrl(), new DispatcherHandler(route.getControllerClass(), route.getControllerMethod(), route.isBlockingAllowed()));
            } else if (RouteType.RESOURCE_FILE.equals(route.getRouteType())) {
                routingHandler.add(Methods.GET, route.getUrl(), getResourceHandler(null));
            }
        });

        return routingHandler;
    }

    private ResourceHandler getResourceHandler(String postfix) {
        if (StringUtils.isBlank(postfix)) {
            if (this.resourceHandler == null) {
                this.resourceHandler = new ResourceHandler(new ClassPathResourceManager(Thread.currentThread().getContextClassLoader(), Default.FILES_FOLDER.toString() + "/"));
            }

            return this.resourceHandler;
        }

        return new ResourceHandler(new ClassPathResourceManager(Thread.currentThread().getContextClassLoader(), Default.FILES_FOLDER.toString() + postfix));
    }

    public void startUndertow() {
        if (!hasError()) {
            this.host = this.config.getString(Key.APPLICATION_HOST, Default.APPLICATION_HOST.toString());
            this.port = this.config.getInt(Key.APPLICATION_PORT, Default.APPLICATION_PORT.toInt());

            final Undertow server = Undertow.builder()
                    .addHttpListener(this.port, this.host)
                    .setHandler(Handlers.exceptionHandler(this.pathHandler).addExceptionHandler(Throwable.class, new ExceptionHandler()))
                    .build();

            server.start();
        }
    }

    private List<Module> getModules() {
        final List<Module> modules = new ArrayList<>();
        if (!hasError()) {
            try {
                final Class<?> module = Class.forName(Default.MODULE_CLASS.toString());
                AbstractModule abstractModule;
                abstractModule = (AbstractModule) module.getConstructor().newInstance();
                modules.add(abstractModule);
                modules.add(new Modules());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
                LOG.error("Failed to load modules. Check that conf/Module.java exists in your application", e);
                this.error = true;
            }
        }

        return modules;
    }

    public void showLogo() {
        if (!hasError()) {
            final StringBuilder logo = new StringBuilder(INITIAL_SIZE);
            try {
                logo.append('\n')
                     .append(FigletFont.convertOneLine("mangoo I/O"))
                     .append("\n\nhttps://mangoo.io | @mangoo_io | ")
                     .append(BootstrapUtils.getVersion())
                     .append('\n');
            } catch (final IOException e) {//NOSONAR
                //intentionally left blank
            }

            LOG.info(logo.toString());
            LOG.info("mangoo I/O application started @{}:{} in {} ms in {} mode. Enjoy.", this.host, this.port, ChronoUnit.MILLIS.between(this.start, LocalDateTime.now()), this.mode.toString());
        }
    }

    public void applicationStarted() {
        this.injector.getInstance(MangooLifecycle.class).applicationStarted();
    }

    public void startQuartzScheduler() {
        if (!hasError()) {
            final Set<Class<?>> jobs = new Reflections(this.config.getSchedulerPackage()).getTypesAnnotatedWith(Schedule.class);
            if (jobs != null && !jobs.isEmpty() && this.config.isSchedulerAutostart()) {
                final Scheduler mangooScheduler = this.injector.getInstance(Scheduler.class);
                jobs.forEach(clazz -> {
                    final Schedule schedule = clazz.getDeclaredAnnotation(Schedule.class);
                    if (CronExpression.isValidExpression(schedule.cron())) {
                        final JobDetail jobDetail = mangooScheduler.createJobDetail(clazz.getName(), Default.SCHEDULER_JOB_GROUP.toString(), clazz.asSubclass(Job.class));
                        final Trigger trigger = mangooScheduler.createTrigger(clazz.getName() + "-trigger", Default.SCHEDULER_TRIGGER_GROUP.toString(), schedule.description(), schedule.cron());
                        mangooScheduler.schedule(jobDetail, trigger);
                        LOG.info("Successfully scheduled job " + clazz.getName() + " with cron " + schedule.cron());
                    } else {
                        LOG.error("Invalid or missing cron expression for job: " + clazz.getName());
                        this.error = true;
                    }
                });

                if (!hasError()) {
                    mangooScheduler.start();
                }
            }
        }
    }

    public boolean isBootstrapSuccessful() {
        return !this.error;
    }
    
    private boolean hasError() {
        return this.error;
    }
}