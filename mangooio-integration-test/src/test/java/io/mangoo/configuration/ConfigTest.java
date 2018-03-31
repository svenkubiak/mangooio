package io.mangoo.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableMap;

import io.mangoo.enums.Default;
import io.mangoo.enums.Jvm;
import io.mangoo.enums.Mode;

@FixMethodOrder(MethodSorters.JVM)
public class ConfigTest {

    @Test
    public void testGetApplicationName() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String applicationName = UUID.randomUUID().toString();

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("name", applicationName));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getApplicationName(), equalTo(applicationName));
        assertThat(yaml.delete(), equalTo(true));
    }
    
//    @Test
//    public void testFlashCookieName() {
//        // given
//        final Config config = Application.getInstance(Config.class);
//
//        // then
//        assertThat(config.getFlashCookieName(), equalTo(Default.FLASH_COOKIE_NAME.toString()));
//    }
    
    @Test
    public void testGetSessionCookieName() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String sessionCookieName = UUID.randomUUID().toString();

        // when
        Map<Object, Object> configValues = ImmutableMap.of("session", ImmutableMap.of("cookie", ImmutableMap.of("name", sessionCookieName)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getSessionCookieName(), equalTo(sessionCookieName));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSessionCookieNameDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getSessionCookieName(), equalTo(Default.SESSION_COOKIE_NAME.toString()));
        assertThat(yaml.delete(), equalTo(true));
    }      

    @Test
    public void testGetApplicationSecret() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String applicationSecret = UUID.randomUUID().toString();

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("secret", applicationSecret));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getApplicationSecret(), equalTo(applicationSecret));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetAuthenticationCookieName() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String authenticationCookieName = UUID.randomUUID().toString();

        // when
        Map<Object, Object> configValues = ImmutableMap.of("authentication", ImmutableMap.of("cookie", ImmutableMap.of("name", authenticationCookieName)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getAuthenticationCookieName(), equalTo(authenticationCookieName));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetAuthenticationCookieNameDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getAuthenticationCookieName(), equalTo(Default.AUTHENTICATION_COOKIE_NAME.toString()));
        assertThat(yaml.delete(), equalTo(true));
    }    
  
    @Test
    public void testGetAuthenticationCookieExpires() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String expires = "42";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("authentication", ImmutableMap.of("cookie", ImmutableMap.of("expires", expires)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getAuthenticationCookieExpires(), equalTo(Long.valueOf(expires)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetAuthenticationCookieExpiresDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getAuthenticationCookieExpires(), equalTo(Default.AUTHENTICATION_COOKIE_EXPIRES.toLong()));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSessionCookieExpires() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String expires = "24";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("session", ImmutableMap.of("cookie", ImmutableMap.of("expires", expires)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getSessionCookieExpires(), equalTo(Long.valueOf(expires)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSessionCookieExpiresDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getSessionCookieExpires(), equalTo(Default.SESSION_COOKIE_EXPIRES.toLong()));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testIsSessionCookieSecure() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String secure = "true";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("session", ImmutableMap.of("cookie", ImmutableMap.of("secure", secure)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.isSessionCookieSecure(), equalTo(Boolean.valueOf(secure)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testIsSessionCookieSecureDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.isSessionCookieSecure(), equalTo(Default.SESSION_COOKIE_SECURE.toBoolean()));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testIsAuthentcationCookieSecure() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String secure = "true";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("authentication", ImmutableMap.of("cookie", ImmutableMap.of("secure", secure)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.isAuthenticationCookieSecure(), equalTo(Boolean.valueOf(secure)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testIsAuthenticationCookieSecureDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.isSessionCookieSecure(), equalTo(Default.AUTHENTICATION_COOKIE_SECURE.toBoolean()));
        assertThat(yaml.delete(), equalTo(true));
    }    
 
    @Test
    public void testI18nCookieName() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String name = UUID.randomUUID().toString();

        // when
        Map<Object, Object> configValues = ImmutableMap.of("i18n", ImmutableMap.of("cookie", ImmutableMap.of("name", name)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getI18nCookieName(), equalTo(name));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testI18nCookieNameDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getI18nCookieName(), equalTo(Default.I18N_COOKIE_NAME.toString()));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testIsFlashCookieSecure() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String secure = "true";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("session", ImmutableMap.of("cookie", ImmutableMap.of("secure", secure)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.isFlashCookieSecure(), equalTo(Boolean.valueOf(secure)));
        assertThat(yaml.delete(), equalTo(true));
    }    
    
    @Test
    public void testAplicationLanguage() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String language = "fr";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("language", language));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getApplicationLanguage(), equalTo(language));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testAplicationLanguageDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getApplicationLanguage(), equalTo(Default.APPLICATION_LANGUAGE.toString()));
        assertThat(yaml.delete(), equalTo(true));
    }  
    
    @Test
    public void testIsAuthentcationCookieEncrypt() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String encrypt = "true";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("authentication", ImmutableMap.of("cookie", ImmutableMap.of("encrypt", encrypt)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.isAuthenticationCookieEncrypt(), equalTo(Boolean.valueOf(encrypt)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testIsAuthentcationCookieEncryptDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.isAuthenticationCookieEncrypt(), equalTo(Default.AUTHENTICATION_COOKIE_ENCRYPT.toBoolean()));
        assertThat(yaml.delete(), equalTo(true));
    } 

    @Test
    public void testGetAuthenticationCookieVersion() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String version = "4";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("authentication", ImmutableMap.of("cookie", ImmutableMap.of("version", version)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getAuthenticationCookieVersion(), equalTo(version));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetAuthenticationCookieVersionDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getAuthenticationCookieVersion(), equalTo(Default.AUTHENTICATION_COOKIE_VERSION.toString()));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSessionCookieVersion() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String version = "7";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("session", ImmutableMap.of("cookie", ImmutableMap.of("version", version)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getSessionCookieVersion(), equalTo(version));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSessionCookieVersionDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getAuthenticationCookieVersion(), equalTo(Default.SESSION_COOKIE_VERSION.toString()));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testIsSchedulerAutostart() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String autostart = "true";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("scheduler", ImmutableMap.of("autostart", autostart));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.isSchedulerAutostart(), equalTo(Boolean.valueOf(autostart)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testIsSchedulerAutostartDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.isSchedulerAutostart(), equalTo(Default.SCHEDULER_AUTOSTART.toBoolean()));
        assertThat(yaml.delete(), equalTo(true));
    } 
    
    @Test
    public void testGetApplicationAdminUsername() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String username = UUID.randomUUID().toString();

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("admin", ImmutableMap.of("username", username)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getApplicationAdminUsername(), equalTo(username));
        assertThat(yaml.delete(), equalTo(true));
    } 
    
    @Test
    public void testGetApplicationAdminPassword() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String password = UUID.randomUUID().toString();

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("admin", ImmutableMap.of("password", password)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getApplicationAdminPassword(), equalTo(password));
        assertThat(yaml.delete(), equalTo(true));
    }   
    
    @Test
    public void testGetSchedulerPackage() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String schedulerPackage = UUID.randomUUID().toString();

        // when
        Map<Object, Object> configValues = ImmutableMap.of("scheduler", ImmutableMap.of("package", schedulerPackage));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getSchedulerPackage(), equalTo(schedulerPackage));
        assertThat(yaml.delete(), equalTo(true));
    }  
    
    @Test
    public void testIsSessionCookieEncrypt() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String encrypt = "true";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("session", ImmutableMap.of("cookie", ImmutableMap.of("encrypt", encrypt)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.isSessionCookieEncrypt(), equalTo(Boolean.valueOf(encrypt)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testIsSessionCookieEncryptDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.isSessionCookieEncrypt(), equalTo(Default.SESSION_COOKIE_ENCRYPT.toBoolean()));
        assertThat(yaml.delete(), equalTo(true));
    }  
    
    @Test
    public void testAuthenticationCookieRememberExpires() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String expires = "6000";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("authentication", ImmutableMap.of("cookie", ImmutableMap.of("remember", ImmutableMap.of("expires", expires))));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getAuthenticationCookieRememberExpires(), equalTo(Long.valueOf(expires)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testAuthenticationCookieRememberExpiresDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getAuthenticationCookieRememberExpires(), equalTo(Default.AUTHENTICATION_COOKIE_REMEMBER_EXPIRES.toLong()));
        assertThat(yaml.delete(), equalTo(true));
    }   
    
    @Test
    public void testGetApplicationThreadpool() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String threadpool = "555";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("threadpool", threadpool));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getApplicationThreadpool(), equalTo(Integer.valueOf(threadpool)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationThreadpoolDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getApplicationThreadpool(), equalTo(Default.APPLICATION_THREADPOOL.toInt()));
        assertThat(yaml.delete(), equalTo(true));
    }  
    
    @Test
    public void testGetApplicationController() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String controller = UUID.randomUUID().toString();

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("controller", controller));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getApplicationController(), equalTo(controller));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationControllerDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getApplicationController(), equalTo(Default.APPLICATION_CONTROLLER.toString()));
        assertThat(yaml.delete(), equalTo(true));
    }  
    
    @Test
    public void testGetApplicationtemplateEngine() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String engine = UUID.randomUUID().toString();

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("templateengine", engine));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getApplicationTemplateEnginge(), equalTo(engine));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationtemplateEngineDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getApplicationTemplateEnginge(), equalTo(Default.APPLICATION_TEMPLATEENGINE.toString()));
        assertThat(yaml.delete(), equalTo(true));
    } 
    
    @Test
    public void testIsApplicationMinifyJS() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String minify = "true";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("minify", ImmutableMap.of("js", minify)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.isApplicationMinifyJS(), equalTo(Boolean.valueOf(minify)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testIsApplicationMinifyJSDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.isApplicationMinifyJS(), equalTo(Default.APPLICATION_MINIFY_JS.toBoolean()));
        assertThat(yaml.delete(), equalTo(true));
    } 
    
    @Test
    public void testIsApplicationMinifyCSS() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String minify = "true";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("minify", ImmutableMap.of("css", minify)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.isApplicationMinifyCSS(), equalTo(Boolean.valueOf(minify)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testIsApplicationMinifyCSSDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.isApplicationMinifyCSS(), equalTo(Default.APPLICATION_MINIFY_CSS.toBoolean()));
        assertThat(yaml.delete(), equalTo(true));
    } 
   
    @Test
    public void testIsApplicationPreprocessSass() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String preprocess = "true";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("preprocess", ImmutableMap.of("sass", preprocess)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.isApplicationPreprocessSass(), equalTo(Boolean.valueOf(preprocess)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testIsApplicationPreprocessSassDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.isApplicationPreprocessSass(), equalTo(Default.APPLICATION_PREPROCESS_SASS.toBoolean()));
        assertThat(yaml.delete(), equalTo(true));
    } 
    
    @Test
    public void testIsApplicationPreprocessLess() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String preprocess = "true";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("preprocess", ImmutableMap.of("less", preprocess)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.isApplicationPreprocessLess(), equalTo(Boolean.valueOf(preprocess)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testIsApplicationPreprocessLessDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.isApplicationPreprocessLess(), equalTo(Default.APPLICATION_PREPROCESS_LESS.toBoolean()));
        assertThat(yaml.delete(), equalTo(true));
    }     
    
//    @Test
//    public void testGetString() {
//        //given
//        final Config config = Application.getInstance(Config.class);
//
//        //then
//        assertThat(config.getString(Key.CONNECTOR_HTTP_PORT), equalTo("10808"));
//        assertThat(config.getString(Key.CONNECTOR_HTTP_PORT.toString()), equalTo("10808"));
//    }
//
//    @Test
//    public void testGetInt() {
//        //given
//        final Config config = Application.getInstance(Config.class);
//
//        //then
//        assertThat(config.getInt(Key.CONNECTOR_HTTP_PORT), equalTo(10808));
//        assertThat(config.getInt(Key.CONNECTOR_HTTP_PORT.toString()), equalTo(10808));
//    }
//
//    @Test
//    public void testGetBoolean() {
//        //given
//        final Config config = Application.getInstance(Config.class);
//
//        //then
//        assertThat(config.getBoolean(Key.APPLICATION_ADMIN_ENABLE), equalTo(true));
//        assertThat(config.getBoolean(Key.APPLICATION_ADMIN_ENABLE.toString()), equalTo(true));
//    }
//
//    @Test
//    public void testGetLong() {
//        //given
//        final Config config = Application.getInstance(Config.class);
//
//        //then
//        assertThat(config.getLong(Key.CONNECTOR_HTTP_PORT), equalTo(10808L));
//        assertThat(config.getLong(Key.CONNECTOR_HTTP_PORT.toString()), equalTo(10808L));
//    }
//
//    @Test
//    public void testGetStringDefaultValue() {
//        //given
//        final Config config = Application.getInstance(Config.class);
//
//        //then
//        assertThat(config.getString("foo", "bar"), equalTo("bar"));
//    }
//
//    @Test
//    public void testGetIntDefaultValue() {
//        //given
//        final Config config = Application.getInstance(Config.class);
//
//        //then
//        assertThat(config.getInt("foo", 42), equalTo(42));
//    }
//
//    @Test
//    public void testGetBooleanDefaultValue() {
//        //given
//        final Config config = Application.getInstance(Config.class);
//
//        //then
//        assertThat(config.getBoolean("foo", true), equalTo(true));
//        assertThat(config.getBoolean("foo", false), equalTo(false));
//    }
//
//    @Test
//    public void testGetLongDefaultValue() {
//        //given
//        final Config config = Application.getInstance(Config.class);
//
//        //then
//        assertThat(config.getLong("foo", 42), equalTo(42L));
//    }
//
//    @Test
//    public void testGetAllConfigurationValues() {
//        //given
//        final Config config = Application.getInstance(Config.class);
//
//        //then
//        assertThat(config.getAllConfigurations(), not(nullValue()));
//        assertThat(config.getAllConfigurations().size(), greaterThan(12));
//    }
//
//    @Test
//    public void testEnvironmentValues() {
//        //given
//        final Config config = Application.getInstance(Config.class);
//
//        //then
//        assertThat(config.getString("smtp.username"), equalTo(""));
//        assertThat(config.getString("smtp.port"), equalTo("3055"));
//    }
//
//    @Test
//    public void testGetLocaleCookieName() {
//        //given
//        final Config config = Application.getInstance(Config.class);
//
//        //then
//        assertThat(config.getI18nCookieName(), equalTo(Default.I18N_COOKIE_NAME.toString()));
//    }
//    
//    @Test
//    public void testEncryptedValue() {
//        //given
//        final Config config = Application.getInstance(Config.class);
//
//        //then
//        assertThat(config.getString("application.foo"), equalTo("admin"));
//    }
//    
//    @Test
//    public void testEncryptedValueMultiKeyLineTwo() {
//        //given
//        final Config config = Application.getInstance(Config.class);
//
//        //then
//        assertThat(config.getString("application.bar"), equalTo("westeros"));
//    }
//    
//    @Test
//    public void testEncryptedValueMultiKeyLineThree() {
//        //given
//        final Config config = Application.getInstance(Config.class);
//
//        //then
//        assertThat(config.getString("application.foobar"), equalTo("essos"));
//    }
//    
//    @Test
//    public void testGetMasterKey() {
//        //given
//        final Config config = Application.getInstance(Config.class);
//        System.setProperty(Jvm.APPLICATION_MASTERKEY.toString(), "thisismymasterkey");
//
//        //then
//        assertThat(config.getMasterKeys().get(0), equalTo("thisismymasterkey"));
//    }
//    
//    @Test
//    public void testApplicationName() {
//        //given
//        final Config config = Application.getInstance(Config.class);
//
//        //then
//        assertThat(config.getApplicationName(), equalTo("TEST"));
//    }
//    
//    @Test
//    public void testGetFlashCookieName() {
//        //given
//        final Config config = Application.getInstance(Config.class);
//
//        //then
//        assertThat(config.getFlashCookieName(), equalTo(Default.FLASH_COOKIE_NAME.toString()));
//    }
    
    private File createTempYaml(Map<Object, Object> values) throws JsonGenerationException, JsonMappingException, IOException {
        Map<Object, Object> defaults = new HashMap<>();
        defaults.put("default", values);

        File configTestFile = new File(UUID.randomUUID().toString());
        System.setProperty(Jvm.APPLICATION_CONFIG.toString(), configTestFile.getAbsolutePath());
        new ObjectMapper(new YAMLFactory()).writeValue(configTestFile, defaults);

        return configTestFile;
    }
}