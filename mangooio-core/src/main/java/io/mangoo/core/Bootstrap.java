package io.mangoo.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lalyos.jfiglet.FigletFont;
import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import io.mangoo.admin.MangooAdminController;
import io.mangoo.annotations.Schedule;
import io.mangoo.configuration.Config;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.enums.Mode;
import io.mangoo.enums.RouteType;
import io.mangoo.interfaces.MangooLifecycle;
import io.mangoo.interfaces.MangooRoutes;
import io.mangoo.routing.Route;
import io.mangoo.routing.Router;
import io.mangoo.routing.handlers.DispatcherHandler;
import io.mangoo.routing.handlers.ExceptionHandler;
import io.mangoo.routing.handlers.FallbackHandler;
import io.mangoo.routing.handlers.WebSocketHandler;
import io.mangoo.scheduler.MangooScheduler;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.server.handlers.resource.ResourceHandler;
import io.undertow.util.Methods;

/**
 * Convenient methods for everything to start up the framework
 * 
 * @author svenkubiak
 *
 */
public class Bootstrap {
    private static final Logger LOG = LoggerFactory.getLogger(Bootstrap.class);
    private static final int INITIAL_SIZE = 255;
    private LocalDateTime start;
    private PathHandler pathHandler;
    private ResourceHandler resourceHandler;
    private Mode mode;
    private Undertow undertow;
    private GreenMail fakeSMTP;
    private Injector injector;
    private Config config;
    private String host;
    private boolean error;
    private int port;

    public void prepareApplication() {
        this.start = LocalDateTime.now();
    }

    public void prepareMode() {
        String property = System.getProperty(Key.APPLICATION_MODE.toString());
        if (StringUtils.isNotBlank(property)) {
            switch (property.toLowerCase(Locale.ENGLISH)) {
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
    }

    public void prepareInjector() {
        this.injector = Guice.createInjector(Stage.PRODUCTION, getModules());
        this.config = this.injector.getInstance(Config.class);
        this.injector.getInstance(MangooLifecycle.class).applicationInitialized();
    }

    public void prepareConfig() {
        if (!this.config.hasValidSecret()) {
            LOG.error("Please make sure that your application.yaml has an application.secret property which has at least 16 characters");
            this.error = true;
        }
    }

    public void prepareRoutes() {
        if (!this.error) {
            try {
                MangooRoutes mangooRoutes = (MangooRoutes) this.injector.getInstance(Class.forName(Default.ROUTES_CLASS.toString()));
                mangooRoutes.routify();
            } catch (ClassNotFoundException e) {
                LOG.error("Failed to load routes. Please check, that conf/Routes.java exisits in your application", e);
                this.error = true;
            }

            for (Route route : Router.getRoutes()) {
                if (RouteType.REQUEST.equals(route.getRouteType())) {
                    Class<?> controllerClass = route.getControllerClass();
                    checkRoute(route, controllerClass);
                }
            }

            if (!this.error) {
                initPathHandler();
            }
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
        for (Route route : Router.getRoutes()) {
            if (RouteType.WEBSOCKET.equals(route.getRouteType())) {
                this.pathHandler.addExactPath(route.getUrl(), Handlers.websocket(new WebSocketHandler(route.getControllerClass())));
            } else if (RouteType.RESOURCE_PATH.equals(route.getRouteType())) {
                this.pathHandler.addPrefixPath(route.getUrl(), getResourceHandler(route.getUrl()));
            }
        }
    }

    private RoutingHandler initRoutingHandler() {
        RoutingHandler routingHandler = Handlers.routing();
        routingHandler.setFallbackHandler(new FallbackHandler());

        Router.mapRequest(Methods.GET).toUrl("/@routes").onClassAndMethod(MangooAdminController.class, "routes");
        Router.mapRequest(Methods.GET).toUrl("/@config").onClassAndMethod(MangooAdminController.class, "config");
        Router.mapRequest(Methods.GET).toUrl("/@health").onClassAndMethod(MangooAdminController.class, "health");
        Router.mapRequest(Methods.GET).toUrl("/@cache").onClassAndMethod(MangooAdminController.class, "cache");
        Router.mapRequest(Methods.GET).toUrl("/@metrics").onClassAndMethod(MangooAdminController.class, "metrics");
        Router.mapRequest(Methods.GET).toUrl("/@scheduler").onClassAndMethod(MangooAdminController.class, "scheduler");

        for (Route route : Router.getRoutes()) {
            if (RouteType.REQUEST.equals(route.getRouteType())) {
                routingHandler.add(route.getRequestMethod(), route.getUrl(), new DispatcherHandler(route.getControllerClass(), route.getControllerMethod()));
            } else if (RouteType.RESOURCE_FILE.equals(route.getRouteType())) {
                routingHandler.add(Methods.GET, route.getUrl(), getResourceHandler(null));
            }
        }

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

    public void startServer() {
        if (!this.error) {
            this.host = this.config.getString(Key.APPLICATION_HOST, Default.APPLICATION_HOST.toString());
            this.port = this.config.getInt(Key.APPLICATION_PORT, Default.APPLICATION_PORT.toInt());

            Undertow server = Undertow.builder()
                    .addHttpListener(this.port, this.host)
                    .setHandler(Handlers.exceptionHandler(this.pathHandler).addExceptionHandler(Throwable.class, new ExceptionHandler()))
                    .build();

            server.start();

            this.undertow = server;
        }
    }

    private List<Module> getModules() {
        List<Module> modules = new ArrayList<Module>();
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

    public void applicationStarted() {
        if (!this.error) {
            StringBuilder logo = new StringBuilder(INITIAL_SIZE);
            try {
                logo.append("\n").append(FigletFont.convertOneLine("mangoo I/O")).append("\n\n").append("https://mangoo.io | @mangoo_io | " + Application.getVersion() + "\n");
            } catch (IOException e) {//NOSONAR
                //intentionally left blank
            }

            LOG.info(logo.toString());
            LOG.info("mangoo I/O application started @{}:{} in {} ms in {} mode. Enjoy.", this.host, this.port, ChronoUnit.MILLIS.between(this.start, LocalDateTime.now()), this.mode.toString());
            this.injector.getInstance(MangooLifecycle.class).applicationStarted();
        }
    }

    public void startFakeSMTP() {
        if (!this.error && !Mode.PROD.equals(Application.getMode())) {
            GreenMail greenMail = new GreenMail(new ServerSetup(
                    this.config.getInt(Key.SMTP_PORT, Default.SMTP_PORT.toInt()),
                    this.config.getString(Key.SMTP_HOST, Default.LOCALHOST.toString()), Default.FAKE_SMTP_PROTOCOL.toString()));
            greenMail.start();

            this.fakeSMTP = greenMail;
        }
    }
    
    public void startScheduler() {
        if (!this.error) {
            Set<Class<?>> jobs = new Reflections().getTypesAnnotatedWith(Schedule.class);
            if (jobs != null && !jobs.isEmpty() && this.config.isSchedulerAutostart()) {
                MangooScheduler mangooScheduler = this.injector.getInstance(MangooScheduler.class);
                for (Class<?> clazz : jobs) {
                    Schedule schedule = clazz.getDeclaredAnnotation(Schedule.class);
                    if (CronExpression.isValidExpression(schedule.cron())) {
                        JobDetail jobDetail = mangooScheduler.getJobDetail(clazz.asSubclass(Job.class), clazz.getName(), Default.SCHEDULER_JOB_GROUP.toString());
                        Trigger trigger = mangooScheduler.getTrigger(clazz.getSimpleName() + "-trigger", schedule.cron(), Default.SCHEDULER_TRIGGER_GROUP.toString(), schedule.description()); 
                        mangooScheduler.schedule(jobDetail, trigger);      
                        LOG.info("Successfully scheduled job " + clazz.getName() + " with cron " + schedule.cron());
                    } else {
                        LOG.error("Invalid or missing cron expression for job: " + clazz.getName());
                        this.error = true;
                    }
                }
                if (!this.error) {
                    mangooScheduler.start();                    
                }
            }
        }
    }

    public void prepareLogging() {
        if (Mode.PROD.equals(this.mode)) {
            LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
            try {
                URL resource = Resources.getResource(Default.LOGBACK_PROD_FILE.toString());
                if (resource != null) {
                    JoranConfigurator configurator = new JoranConfigurator();
                    configurator.setContext(context);
                    context.reset();
                    configurator.doConfigure(resource);
                }
            } catch (JoranException | IllegalArgumentException e) { //NOSONAR
                //intentionally left blank
            }
        }
    }

    public Mode getMode() {
        return this.mode;
    }

    public boolean isStarted() {
        return !this.error;
    }

    public Injector getInjector() {
        return this.injector;
    }

    public GreenMail getFakeSMTP() {
        return this.fakeSMTP;
    }

    public Undertow getServer() {
        return this.undertow;
    }
}