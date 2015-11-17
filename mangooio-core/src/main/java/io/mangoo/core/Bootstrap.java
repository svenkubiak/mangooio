package io.mangoo.core;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import io.mangoo.utils.ConfigUtils;
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
    private static final int INITIAL_SIZE = 255;
    private static volatile Logger LOG; //NOSONAR
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
        String applicationMode = System.getProperty(Key.APPLICATION_MODE.toString());
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

    public void prepareLogger() {
        String configurationFile = "log4j2." + this.mode.toString() + ".xml";
        if (Thread.currentThread().getContextClassLoader().getResource(configurationFile) != null) {
            System.setProperty("log4j.configurationFile", configurationFile);
        }

        LOG = LogManager.getLogger(Bootstrap.class); //NOSONAR
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
        if (!this.error) {
            try {
                Yaml yaml = new Yaml();
                List<Map<String, String>> routes = (List<Map<String, String>>) yaml.load(Resources.getResource(Default.ROUTES_FILE.toString()).openStream());
            
                routes.forEach(routing -> {
                    routing.entrySet().forEach(entry -> {
                        String method = entry.getKey().trim();
                        String mapping = entry.getValue();

                        boolean authentication = false;
                        boolean blocking = false;
                        if (mapping.contains("@authentication")) {
                            mapping = mapping.replace("@authentication", "");
                            authentication = true;
                        } else if (mapping.contains("@blocking")) {
                            mapping = mapping.replace("@blocking", "");
                            blocking = true;
                        }

                        String [] split = mapping.split("->");
                        Route route = new Route(getRouteType(method));
                        route.toUrl(split[0].trim());
                        route.withRequest(HttpString.tryFromString(method));
                        route.withAuthentication(authentication);
                        route.allowBlocking(blocking);
                        
                        try {
                            if (split.length == 2) {
                                String [] classMethod = split[1].split("\\.");
                                route.withClass(Class.forName(ConfigUtils.getControllerPackage() + classMethod[0].trim()));
                                if (classMethod.length == 2) {
                                    route.withMethod(classMethod[1].trim());
                                }
                            }
                            
                            Router.addRoute(route);
                        } catch (ClassNotFoundException e) {
                            LOG.error("Failed to parse routing: " + routing);
                            LOG.error("Please check, that your routes.yaml syntax is correct", e);
                            this.error = true;
                        } 
                    });
                });
            } catch (IOException e) {
                LOG.error("Failed to load routes.yaml Please check, that routes.yaml exists in your application resource folder", e);
                this.error = true;
            }

            Router.getRoutes().forEach(route -> {
                if (RouteType.REQUEST.equals(route.getRouteType())) {
                    Class<?> controllerClass = route.getControllerClass();
                    checkRoute(route, controllerClass);
                } 
            });

            if (!this.error) {
                initPathHandler();
            }
        }
    }

    private RouteType getRouteType(String method) {
        switch (method) {
        case "GET":
        case "POST":
        case "PUT":
        case "DELETE":
        case "HEAD":
            return RouteType.REQUEST;
        case "WSS":
            return RouteType.WEBSOCKET;
        case "SSE":
            return RouteType.SERVER_SENT_EVENT;
        case "FILE":
            return RouteType.RESOURCE_FILE;
        case "PATH":
            return RouteType.RESOURCE_PATH;
        default:
            return null;
        }
    }

    private void checkRoute(Route route, Class<?> controllerClass) {
        boolean found = false;
        for (Method method : controllerClass.getMethods()) {
            if (method.getName().equals(route.getControllerMethod())) {
                found = true;
            }
        }

        if (!found) {
            LOG.error("Could not find controller method '" + route.getControllerMethod() + "' in controller class '" + controllerClass.getSimpleName() + "'");
            this.error = true;
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
        RoutingHandler routingHandler = Handlers.routing();
        routingHandler.setFallbackHandler(new FallbackHandler());
        
        Router.addRoute(new Route(RouteType.REQUEST).toUrl("/@routes").withRequest(Methods.GET).withClass(MangooAdminController.class).withMethod("routes"));
        Router.addRoute(new Route(RouteType.REQUEST).toUrl("/@config").withRequest(Methods.GET).withClass(MangooAdminController.class).withMethod("config"));
        Router.addRoute(new Route(RouteType.REQUEST).toUrl("/@health").withRequest(Methods.GET).withClass(MangooAdminController.class).withMethod("health"));
        Router.addRoute(new Route(RouteType.REQUEST).toUrl("/@cache").withRequest(Methods.GET).withClass(MangooAdminController.class).withMethod("cache"));
        Router.addRoute(new Route(RouteType.REQUEST).toUrl("/@metrics").withRequest(Methods.GET).withClass(MangooAdminController.class).withMethod("metrics"));
        Router.addRoute(new Route(RouteType.REQUEST).toUrl("/@scheduler").withRequest(Methods.GET).withClass(MangooAdminController.class).withMethod("scheduler"));

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
        if (!this.error) {
            this.host = this.config.getString(Key.APPLICATION_HOST, Default.APPLICATION_HOST.toString());
            this.port = this.config.getInt(Key.APPLICATION_PORT, Default.APPLICATION_PORT.toInt());

            Undertow server = Undertow.builder()
                    .addHttpListener(this.port, this.host)
                    .setHandler(Handlers.exceptionHandler(this.pathHandler).addExceptionHandler(Throwable.class, new ExceptionHandler()))
                    .build();

            server.start();
        }
    }

    private List<Module> getModules() {
        List<Module> modules = new ArrayList<>();
        if (!this.error) {
            try {
                Class<?> module = Class.forName(Default.MODULE_CLASS.toString());
                AbstractModule abstractModule;
                abstractModule = (AbstractModule) module.getConstructor().newInstance();
                modules.add(abstractModule);
                modules.add(new Modules());
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                    | NoSuchMethodException | SecurityException | ClassNotFoundException e) {
                LOG.error("Failed to load modules. Check that conf/Module.java exisits in your application", e);
                this.error = true;
            }
        }

        return modules;
    }

    public void showLogo() {
        if (!this.error) {
            StringBuilder logo = new StringBuilder(INITIAL_SIZE);
            try {
                logo.append('\n')
                     .append(FigletFont.convertOneLine("mangoo I/O"))
                     .append("\n\nhttps://mangoo.io | @mangoo_io | ")
                     .append(getVersion())
                     .append('\n');
            } catch (IOException e) {//NOSONAR
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
        if (!this.error) {
            Set<Class<?>> jobs = new Reflections(ConfigUtils.getSchedulerPackage()).getTypesAnnotatedWith(Schedule.class);
            if (jobs != null && !jobs.isEmpty() && ConfigUtils.isSchedulerAutostart()) {
                Scheduler mangooScheduler = this.injector.getInstance(Scheduler.class);
                jobs.forEach(clazz -> {
                    Schedule schedule = clazz.getDeclaredAnnotation(Schedule.class);
                    if (CronExpression.isValidExpression(schedule.cron())) {
                        JobDetail jobDetail = mangooScheduler.createJobDetail(clazz.getName(), Default.SCHEDULER_JOB_GROUP.toString(), clazz.asSubclass(Job.class));
                        Trigger trigger = mangooScheduler.createTrigger(clazz.getName() + "-trigger", Default.SCHEDULER_TRIGGER_GROUP.toString(), schedule.description(), schedule.cron());
                        mangooScheduler.schedule(jobDetail, trigger);
                        LOG.info("Successfully scheduled job " + clazz.getName() + " with cron " + schedule.cron());
                    } else {
                        LOG.error("Invalid or missing cron expression for job: " + clazz.getName());
                        this.error = true;
                    }                    
                });

                if (!this.error) {
                    mangooScheduler.start();
                }
            }
        }
    }

    private String getVersion() {
        String version = Default.VERSION.toString();
        try (InputStream inputStream = Resources.getResource(Default.VERSION_PROPERTIES.toString()).openStream()) {
            Properties properties = new Properties();
            properties.load(inputStream);
            version = String.valueOf(properties.get(Key.VERSION.toString()));
        } catch (IOException e) {
            LOG.error("Failed to get application version", e);
        }

        return version;
    }

    public boolean isApplicationStarted() {
        return !this.error;
    }
}