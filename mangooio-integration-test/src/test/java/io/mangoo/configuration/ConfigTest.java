package io.mangoo.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;

import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.enums.Mode;

@FixMethodOrder(MethodSorters.JVM)
public class ConfigTest {

    @Test
    public void testGetApplicationName() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String applicationName = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = ImmutableMap.of("application.name", applicationName);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getApplicationName(), equalTo(applicationName));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testFlashCookieName() {
        // given
        final Config config = Application.getInstance(Config.class);

        // then
        assertThat(config.getFlashCookieName(), equalTo("test-flash"));
    }
    
    @Test
    public void testGetSessionCookieName() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String sessionCookieName = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("session.cookie.name", sessionCookieName);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getSessionCookieName(), equalTo(sessionCookieName));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSessionCookieNameDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        //given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSessionCookieName(), equalTo(Default.SESSION_COOKIE_NAME.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }      

    @Test
    public void testGetApplicationSecret() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String applicationSecret = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", applicationSecret);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getApplicationSecret(), equalTo(applicationSecret));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetAuthenticationCookieName() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String authenticationCookieName = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.cookie.name", authenticationCookieName);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getAuthenticationCookieName(), equalTo(authenticationCookieName));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetAuthenticationCookieNameDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        //given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getAuthenticationCookieName(), equalTo(Default.AUTHENTICATION_COOKIE_NAME.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }    
  
    @Test
    public void testGetAuthenticationCookieExpires() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String expires = "42";

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.cookie.expires", expires);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getAuthenticationCookieExpires(), equalTo(Long.valueOf(expires)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetAuthenticationCookieExpiresDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getAuthenticationCookieExpires(), equalTo(Default.AUTHENTICATION_COOKIE_EXPIRES.toLong()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSessionCookieExpires() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String expires = "24";

        // when
        Map<String, String> configValues = ImmutableMap.of("session.cookie.expires", expires);
        File tempConfig = createTempConfig(configValues);
        System.setProperty("application.mode", Mode.TEST.toString());
        Config config = new Config();
        
        // then
        assertThat(config.getSessionCookieExpires(), equalTo(Long.valueOf(expires)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSessionCookieExpiresDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSessionCookieExpires(), equalTo(Default.SESSION_COOKIE_EXPIRES.toLong()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testIsSessionCookieSecure() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String secure = "true";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("session.cookie.secure", secure);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.isSessionCookieSecure(), equalTo(Boolean.valueOf(secure)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testIsSessionCookieSecureDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isSessionCookieSecure(), equalTo(Default.SESSION_COOKIE_SECURE.toBoolean()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testIsAuthentcationCookieSecure() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String secure = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.cookie.secure", secure);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.isAuthenticationCookieSecure(), equalTo(Boolean.valueOf(secure)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testIsAuthenticationCookieSecureDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isSessionCookieSecure(), equalTo(Default.AUTHENTICATION_COOKIE_SECURE.toBoolean()));
        assertThat(tempConfig.delete(), equalTo(true));
    }    
 
    @Test
    public void testI18nCookieName() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String name = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("i18n.cookie.name", name);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getI18nCookieName(), equalTo(name));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testI18nCookieNameDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getI18nCookieName(), equalTo(Default.I18N_COOKIE_NAME.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testIsFlashCookieSecure() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String secure = "true";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("session.cookie.secure", secure);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.isFlashCookieSecure(), equalTo(Boolean.valueOf(secure)));
        assertThat(tempConfig.delete(), equalTo(true));
    }    
    
    @Test
    public void testAplicationLanguage() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String language = "fr";

        // when
        Map<String, String> configValues = ImmutableMap.of("application.language", language);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getApplicationLanguage(), equalTo(language));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testAplicationLanguageDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getApplicationLanguage(), equalTo(Default.APPLICATION_LANGUAGE.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }  
    
    @Test
    public void testIsSchedulerAutostart() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String autostart = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("scheduler.autostart", autostart);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.isSchedulerAutostart(), equalTo(Boolean.valueOf(autostart)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testIsSchedulerAutostartDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isSchedulerAutostart(), equalTo(Default.SCHEDULER_AUTOSTART.toBoolean()));
        assertThat(tempConfig.delete(), equalTo(true));
    } 
    
    @Test
    public void testGetApplicationAdminUsername() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String username = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.admin.username", username);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getApplicationAdminUsername(), equalTo(username));
        assertThat(tempConfig.delete(), equalTo(true));
    } 
    
    @Test
    public void testGetApplicationAdminPassword() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String password = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.admin.password", password);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getApplicationAdminPassword(), equalTo(password));
        assertThat(tempConfig.delete(), equalTo(true));
    }   
    
    @Test
    public void testGetSchedulerPackage() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String schedulerPackage = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("scheduler.package", schedulerPackage);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getSchedulerPackage(), equalTo(schedulerPackage));
        assertThat(tempConfig.delete(), equalTo(true));
    }  
    
    @Test
    public void testAuthenticationCookieRememberExpires() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String expires = "6000";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.cookie.remember.expires", expires);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getAuthenticationCookieRememberExpires(), equalTo(Long.valueOf(expires)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testAuthenticationCookieRememberExpiresDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getAuthenticationCookieRememberExpires(), equalTo(Default.AUTHENTICATION_COOKIE_REMEMBER_EXPIRES.toLong()));
        assertThat(tempConfig.delete(), equalTo(true));
    }   
    
    @Test
    public void testGetApplicationThreadpool() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String threadpool = "555";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.threadpool", threadpool);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getApplicationThreadpool(), equalTo(Integer.valueOf(threadpool)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationThreadpoolDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getApplicationThreadpool(), equalTo(Default.APPLICATION_THREADPOOL.toInt()));
        assertThat(tempConfig.delete(), equalTo(true));
    }  
    
    @Test
    public void testGetApplicationController() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String controller = UUID.randomUUID().toString();

        // when
        Map<String, String> configValues = ImmutableMap.of("application.controller", controller);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getApplicationController(), equalTo(controller));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationControllerDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getApplicationController(), equalTo(Default.APPLICATION_CONTROLLER.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }  
    
    @Test
    public void testGetApplicationtemplateEngine() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String engine = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.templateengine", engine);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getApplicationTemplateEngine(), equalTo(engine));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationtemplateEngineDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given 
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getApplicationTemplateEngine(), equalTo(Default.APPLICATION_TEMPLATEENGINE.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    } 
    
    @Test
    public void testIsApplicationMinifyJS() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String minify = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("application.minify.js", minify);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.isApplicationMinifyJS(), equalTo(Boolean.valueOf(minify)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testIsApplicationMinifyJSDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isApplicationMinifyJS(), equalTo(Default.APPLICATION_MINIFY_JS.toBoolean()));
        assertThat(tempConfig.delete(), equalTo(true));
    } 
    
    @Test
    public void testIsApplicationMinifyCSS() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String minify = "true";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = ImmutableMap.of("application.minify.css", minify);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.isApplicationMinifyCSS(), equalTo(Boolean.valueOf(minify)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testIsApplicationMinifyCSSDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isApplicationMinifyCSS(), equalTo(Default.APPLICATION_MINIFY_CSS.toBoolean()));
        assertThat(tempConfig.delete(), equalTo(true));
    } 
   
    @Test
    public void testIsApplicationPreprocessSass() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String preprocess = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("application.preprocess.sass", preprocess);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.isApplicationPreprocessSass(), equalTo(Boolean.valueOf(preprocess)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testIsApplicationPreprocessSassDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isApplicationPreprocessSass(), equalTo(Default.APPLICATION_PREPROCESS_SASS.toBoolean()));
        assertThat(tempConfig.delete(), equalTo(true));
    } 
    
    @Test
    public void testIsApplicationPreprocessLess() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String preprocess = "true";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.preprocess.less", preprocess);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.isApplicationPreprocessLess(), equalTo(Boolean.valueOf(preprocess)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testIsApplicationPreprocessLessDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isApplicationPreprocessLess(), equalTo(Default.APPLICATION_PREPROCESS_LESS.toBoolean()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testIsApplicationAdminEnable() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String enable = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("application.admin.enable", enable);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.isApplicationAdminEnable(), equalTo(Boolean.valueOf(enable)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testIsApplicationAdminEnableDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isApplicationAdminEnable(), equalTo(Default.APPLICATION_ADMIN_ENABLE.toBoolean()));
        assertThat(tempConfig.delete(), equalTo(true));
    } 
    
    @Test
    public void testGetSmptHost() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String host = "192.168.2.24";

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.host", host);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getSmtpHost(), equalTo(host));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSmptHostDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSmtpHost(), equalTo(Default.SMTP_HOST.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }  
    
    @Test
    public void testGetSmptPort() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String port = "555";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.port", port);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getSmtpPort(), equalTo(Integer.valueOf(port)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSmptPortDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSmtpPort(), equalTo(Default.SMTP_PORT.toInt()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testIsSmptSSL() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String ssl = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.ssl", ssl);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.isSmtpSSL(), equalTo(Boolean.valueOf(ssl)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testIsSmptSSLDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isSmtpSSL(), equalTo(Default.SMTP_SSL.toBoolean()));
        assertThat(tempConfig.delete(), equalTo(true));
    } 
    
    @Test
    public void testGetSmptUsername() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String username = "smtpuser";

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.username", username);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getSmtpUsername(), equalTo(username));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSmptUsernameDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSmtpUsername(), equalTo(null));
        assertThat(tempConfig.delete(), equalTo(true));
    } 
    
    @Test
    public void testGetSmptPassword() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String password = "smtppass";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.password", password);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getSmtpPassword(), equalTo(password));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSmptPasswordDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSmtpUsername(), equalTo(null));
        assertThat(tempConfig.delete(), equalTo(true));
    }   
    
    @Test
    public void testGetSmptfrom() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String from = "smtpform";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.from", from);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getSmtpFrom(), equalTo(from));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSmptfromDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSmtpFrom(), equalTo(Default.SMTP_FROM.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetConnectorAjpHost() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String host = "192.168.3.24";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("connector.ajp.host", host);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getConnectorAjpHost(), equalTo(host));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetConnectorAjpHostDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getConnectorAjpHost(), equalTo(null));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetConnectorAjpPort() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String port = "2542";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("connector.ajp.port", port);
        File tempConfig = createTempConfig(configValues);
        System.setProperty("application.mode", Mode.TEST.toString());
        Config config = new Config();
        
        // then
        assertThat(config.getConnectorAjpPort(), equalTo(Integer.valueOf(port)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetConnectorAjpPortDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getConnectorAjpPort(), equalTo(0));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetConnectorHttpHost() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String host = "192.168.2.42";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = ImmutableMap.of("connector.http.host", host);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getConnectorHttpHost(), equalTo(host));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetConnectorHttpHostDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getConnectorHttpHost(), equalTo(null));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetConnectorHttpPort() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String port = "2442";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("connector.http.port", port);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getConnectorHttpPort(), equalTo(Integer.valueOf(port)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetConnectorHttpPortDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getConnectorHttpPort(), equalTo(0));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testApplicationHeaderXssProection() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String xss = "5";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.headers.xssprotection", xss);
        File tempConfig = createTempConfig(configValues);
        System.setProperty("application.mode", Mode.TEST.toString());
        Config config = new Config();
        
        // then
        assertThat(config.getApplicationHeaderXssProection(), equalTo(Integer.valueOf(xss)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testApplicationHeaderXssProectionDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
                
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getApplicationHeaderXssProection(), equalTo(Default.APPLICATION_HEADERS_XSSPROTECTION.toInt()));
        assertThat(tempConfig.delete(), equalTo(true));
    } 
    
    @Test
    public void testGetApplicationHeadersXContentTypeOptions() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String type = "mycontenttype";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.headers.xcontenttypeoptions", type);
        File tempConfig = createTempConfig(configValues);
        System.setProperty("application.mode", Mode.TEST.toString());
        Config config = new Config();
        
        // then
        assertThat(config.getApplicationHeadersXContentTypeOptions(), equalTo(type));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationHeadersXContentTypeOptionsDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getApplicationHeadersXContentTypeOptions(), equalTo(Default.APPLICATION_HEADERS_XCONTENTTYPEOPTIONS.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    } 
    
    @Test
    public void testGetApplicationHeadersXFrameOptions() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String frame = "myframe";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.headers.xframeoptions", frame);
        File tempConfig = createTempConfig(configValues);
        System.setProperty("application.mode", Mode.TEST.toString());
        Config config = new Config();
        
        // then
        assertThat(config.getApplicationHeadersXFrameOptions(), equalTo(frame));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationHeadersXFrameOptionsDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getApplicationHeadersXFrameOptions(), equalTo(Default.APPLICATION_HEADERS_XFRAMEOPTIONS.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }     
    
    @Test
    public void testGetApplicationHeadersServer() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String server = "myservername";

        // when
        Map<String, String> configValues = ImmutableMap.of("application.headers.server", server);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getApplicationHeadersServer(), equalTo(server));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationHeadersServerDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getApplicationHeadersServer(), equalTo(Default.APPLICATION_HEADERS_SERVER.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }   
    
    @Test
    public void testGetApplicationHeadersContentSecurityPolicy() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String policy = "mypolicy";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.headers.contentsecuritypolicy", policy);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getApplicationHeadersContentSecurityPolicy(), equalTo(policy));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationHeadersContentSecurityPolicyDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getApplicationHeadersContentSecurityPolicy(), equalTo(Default.APPLICATION_HEADERS_CONTENTSECURITYPOLICY.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    } 
    
    @Test
    public void testIsCacheClusterEnable() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String enable = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("cache.cluster.enable", enable);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.isCacheCluserEnable(), equalTo(Boolean.valueOf(enable)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testIsCacheClusterEnableDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isCacheCluserEnable(), equalTo(Default.CACHE_CLUSTER_ENABLE.toBoolean()));
        assertThat(tempConfig.delete(), equalTo(true));
    } 
    
    @Test
    public void testMetricsEnable() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String enable = "true";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("metrics.enable", enable);
        File tempConfig = createTempConfig(configValues);
        System.setProperty("application.mode", Mode.TEST.toString());
        Config config = new Config();
        
        // then
        assertThat(config.isMetricsEnable(), equalTo(Boolean.valueOf(enable)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testMetricsEnableDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isMetricsEnable(), equalTo(Default.METRICS_ENABLE.toBoolean()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testAuthenticationLock() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String lock = "24";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.lock", lock);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getAuthenticationLock(), equalTo(Integer.valueOf(lock)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testAuthenticationLockDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getAuthenticationLock(), equalTo(Default.AUTHENTICATION_LOCK.toInt()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testCacheClusterUrl() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String url = "myclusterurl";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("cache.cluster.url", url);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getCacheClusterUrl(), equalTo(url));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testCacheClusterUrlDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getCacheClusterUrl(), equalTo(null));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationHeadersRefererPolicy() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String policy = "myrefererpolicy";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.headers.refererpolicy", policy);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getApplicationHeadersRefererPolicy(), equalTo(policy));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationHeadersRefererPolicyDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getApplicationHeadersRefererPolicy(), equalTo(Default.APPLICATION_HEADERS_REFERERPOLICY.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetUndertowMaxEntitySize() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String size = "4096";
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("undertow.maxentitysize", size);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getUndertowMaxEntitySize(), equalTo(Long.valueOf(size)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetUndertowMaxEntitySizeDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getUndertowMaxEntitySize(), equalTo(Default.UNDERTOW_MAX_ENTITY_SIZE.toLong()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSessionCookieSignKey() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String key = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("session.cookie.signkey", key);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getSessionCookieSignKey(), equalTo(key));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSessionCookieSignKeyDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String secret = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", secret);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSessionCookieSignKey(), equalTo(secret));
        assertThat(tempConfig.delete(), equalTo(true));
    }

    @Test
    public void testGetSessionCookieEncryptionKey() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String key = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("session.cookie.encryptionkey", key);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getSessionCookieEncryptionKey(), equalTo(key));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSessionCookieEncryptionKeyDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String secret = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", secret);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSessionCookieEncryptionKey(), equalTo(secret));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetFlashCookieEncryptionKey() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String key = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("flash.cookie.encryptionkey", key);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getFlashCookieEncryptionKey(), equalTo(key));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetFlashCookieEncryptionKeyDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String secret = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", secret);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getFlashCookieEncryptionKey(), equalTo(secret));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetAuthenticationCookieSignKey() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String key = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.cookie.signkey", key);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getAuthenticationCookieSignKey(), equalTo(key));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetAuthenticationCookieSignKeyDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String secret = UUID.randomUUID().toString();
        
        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", secret);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getAuthenticationCookieSignKey(), equalTo(secret));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetAuthenticationCookieEncryptionKey() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String key = UUID.randomUUID().toString();

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.cookie.encryptionkey", key);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getAuthenticationCookieEncryptionKey(), equalTo(key));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    public void testGetAuthenticationCookieEncryptionKeyKeyDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String secret = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", secret);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getAuthenticationCookieEncryptionKey(), equalTo(secret));
        assertThat(tempConfig.delete(), equalTo(true));
    }     
    
    @Test
    public void testGetString() {
        //given
        System.setProperty(Key.APPLICATION_CONFIG.toString(), "");
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString(Key.CONNECTOR_HTTP_PORT), equalTo("10808"));
        assertThat(config.getString(Key.CONNECTOR_HTTP_PORT.toString()), equalTo("10808"));
    }

    @Test
    public void testGetInt() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getInt(Key.CONNECTOR_HTTP_PORT), equalTo(10808));
        assertThat(config.getInt(Key.CONNECTOR_HTTP_PORT.toString()), equalTo(10808));
    }

    @Test
    public void testGetBoolean() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getBoolean(Key.APPLICATION_ADMIN_ENABLE), equalTo(true));
        assertThat(config.getBoolean(Key.APPLICATION_ADMIN_ENABLE.toString()), equalTo(true));
    }

    @Test
    public void testGetLong() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getLong(Key.CONNECTOR_HTTP_PORT), equalTo(10808L));
        assertThat(config.getLong(Key.CONNECTOR_HTTP_PORT.toString()), equalTo(10808L));
    }

    @Test
    public void testGetStringDefaultValue() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString("foo", "bar"), equalTo("bar"));
    }

    @Test
    public void testGetIntDefaultValue() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getInt("foo", 42), equalTo(42));
    }

    @Test
    public void testGetBooleanDefaultValue() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getBoolean("foo", true), equalTo(true));
        assertThat(config.getBoolean("foo", false), equalTo(false));
    }

    @Test
    public void testGetLongDefaultValue() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getLong("foo", 42), equalTo(42L));
    }

    @Test
    public void testGetAllConfigurationValues() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getAllConfigurations(), not(nullValue()));
        assertThat(config.getAllConfigurations().size(), greaterThan(12));
    }

    @Test
    public void testEnvironmentValues() {
        //given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString("smtp.username"), equalTo("bla"));
        assertThat(config.getString("smtp.port"), equalTo("3025"));
    }
    
    @Test
    public void testEncryptedValue() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString("application.foo"), equalTo("admin"));
    }
    
    @Test
    public void testEncryptedValueMultiKeyLineTwo() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString("application.bar"), equalTo("westeros"));
    }
    
    @Test
    public void testEncryptedValueMultiKeyLineThree() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString("application.foobar"), equalTo("essos"));
    }

    private File createTempConfig(Map<String, String> values) throws IOException {
        File configTestFile = new File(UUID.randomUUID().toString());
        String path = configTestFile.getAbsolutePath();
        
        List<String> lines = new ArrayList<>();
        for (Entry<String, String> entry : values.entrySet()) {
            lines.add(String.valueOf(entry.getKey()) + " = " + String.valueOf(entry.getValue()));
        }
        
        OutputStream outputStream = new FileOutputStream(configTestFile);
        IOUtils.writeLines(lines, null, outputStream, Charsets.UTF_8);
        outputStream.close();
        
        System.setProperty(Key.APPLICATION_CONFIG.toString(), path);
        
        return configTestFile;
    }
}