package io.mangoo.configuration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

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

import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Jvm;
import io.mangoo.enums.Key;
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
    
    @Test
    public void testFlashCookieName() {
        // given
        final Config config = Application.getInstance(Config.class);

        // then
        assertThat(config.getFlashCookieName(), equalTo(Default.FLASH_COOKIE_NAME.toString()));
    }
    
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
        assertThat(config.getApplicationTemplateEngine(), equalTo(engine));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationtemplateEngineDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getApplicationTemplateEngine(), equalTo(Default.APPLICATION_TEMPLATEENGINE.toString()));
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
    
    @Test
    public void testIsApplicationAdminEnable() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String enable = "true";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("admin", ImmutableMap.of("enable", enable)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.isApplicationAdminEnable(), equalTo(Boolean.valueOf(enable)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testIsApplicationAdminEnableDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.isApplicationAdminEnable(), equalTo(Default.APPLICATION_ADMIN_ENABLE.toBoolean()));
        assertThat(yaml.delete(), equalTo(true));
    } 
    
    @Test
    public void testGetSmptHost() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String host = "192.168.2.24";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("smtp", ImmutableMap.of("host", host));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getSmtpHost(), equalTo(host));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSmptHostDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getSmtpHost(), equalTo(Default.SMTP_HOST.toString()));
        assertThat(yaml.delete(), equalTo(true));
    }  
    
    @Test
    public void testGetSmptPort() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String port = "555";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("smtp", ImmutableMap.of("port", port));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getSmtpPort(), equalTo(Integer.valueOf(port)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSmptPortDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getSmtpPort(), equalTo(Default.SMTP_PORT.toInt()));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testIsSmptSSL() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String ssl = "true";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("smtp", ImmutableMap.of("ssl", ssl));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.isSmtpSSL(), equalTo(Boolean.valueOf(ssl)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testIsSmptSSLDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.isSmtpSSL(), equalTo(Default.SMTP_SSL.toBoolean()));
        assertThat(yaml.delete(), equalTo(true));
    } 
    
    @Test
    public void testGetSmptUsername() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String username = "smtpuser";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("smtp", ImmutableMap.of("username", username));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getSmtpUsername(), equalTo(username));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSmptUsernameDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getSmtpUsername(), equalTo(null));
        assertThat(yaml.delete(), equalTo(true));
    } 
    
    @Test
    public void testGetSmptPassword() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String password = "smtppass";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("smtp", ImmutableMap.of("password", password));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getSmtpPassword(), equalTo(password));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSmptPasswordDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getSmtpUsername(), equalTo(null));
        assertThat(yaml.delete(), equalTo(true));
    }   
    
    @Test
    public void testGetSmptfrom() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String from = "smtpform";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("smtp", ImmutableMap.of("from", from));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getSmtpFrom(), equalTo(from));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSmptfromDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getSmtpFrom(), equalTo(Default.SMTP_FROM.toString()));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetConnectorAjpHost() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String host = "192.168.3.24";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("connector", ImmutableMap.of("ajp", ImmutableMap.of("host", host)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getConnectorAjpHost(), equalTo(host));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetConnectorAjpHostDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getConnectorAjpHost(), equalTo(null));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetConnectorAjpPort() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String port = "2542";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("connector", ImmutableMap.of("ajp", ImmutableMap.of("port", port)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getConnectorAjpPort(), equalTo(Integer.valueOf(port)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetConnectorAjpPortDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getConnectorAjpPort(), equalTo(0));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetConnectorHttpHost() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String host = "192.168.2.42";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("connector", ImmutableMap.of("http", ImmutableMap.of("host", host)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getConnectorHttpHost(), equalTo(host));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetConnectorHttpHostDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getConnectorHttpHost(), equalTo(null));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetConnectorHttpPort() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String port = "2442";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("connector", ImmutableMap.of("http", ImmutableMap.of("port", port)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getConnectorHttpPort(), equalTo(Integer.valueOf(port)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetConnectorHttpPortDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getConnectorHttpPort(), equalTo(0));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testApplicationHeaderXssProection() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String xss = "5";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("headers", ImmutableMap.of("xssprotection", xss)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getApplicationHeaderXssProection(), equalTo(Integer.valueOf(xss)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testApplicationHeaderXssProectionDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getApplicationHeaderXssProection(), equalTo(Default.APPLICATION_HEADERS_XSSPROTECTION.toInt()));
        assertThat(yaml.delete(), equalTo(true));
    } 
    
    @Test
    public void testGetApplicationHeadersXContentTypeOptions() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String type = "mycontenttype";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("headers", ImmutableMap.of("xcontenttypeoptions", type)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getApplicationHeadersXContentTypeOptions(), equalTo(type));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationHeadersXContentTypeOptionsDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getApplicationHeadersXContentTypeOptions(), equalTo(Default.APPLICATION_HEADERS_XCONTENTTYPEOPTIONS.toString()));
        assertThat(yaml.delete(), equalTo(true));
    } 
    
    @Test
    public void testGetApplicationHeadersXFrameOptions() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String frame = "myframe";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("headers", ImmutableMap.of("xframeoptions", frame)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getApplicationHeadersXFrameOptions(), equalTo(frame));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationHeadersXFrameOptionsDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getApplicationHeadersXFrameOptions(), equalTo(Default.APPLICATION_HEADERS_XFRAMEOPTIONS.toString()));
        assertThat(yaml.delete(), equalTo(true));
    }     
    
    @Test
    public void testGetApplicationHeadersServer() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String server = "myservername";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("headers", ImmutableMap.of("server", server)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getApplicationHeadersServer(), equalTo(server));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationHeadersServerDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getApplicationHeadersServer(), equalTo(Default.APPLICATION_HEADERS_SERVER.toString()));
        assertThat(yaml.delete(), equalTo(true));
    }   
    
    @Test
    public void testGetApplicationHeadersContentSecurityPolicy() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String policy = "mypolicy";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("headers", ImmutableMap.of("contentsecuritypolicy", policy)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getApplicationHeadersContentSecurityPolicy(), equalTo(policy));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationHeadersContentSecurityPolicyDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getApplicationHeadersContentSecurityPolicy(), equalTo(Default.APPLICATION_HEADERS_CONTENTSECURITYPOLICY.toString()));
        assertThat(yaml.delete(), equalTo(true));
    } 
    
    @Test
    public void testIsCacheClusterEnable() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String enable = "true";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("cache", ImmutableMap.of("cluster", ImmutableMap.of("enable", enable)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.isCacheCluserEnable(), equalTo(Boolean.valueOf(enable)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testIsCacheClusterEnableDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.isCacheCluserEnable(), equalTo(Default.CACHE_CLUSTER_ENABLE.toBoolean()));
        assertThat(yaml.delete(), equalTo(true));
    } 
    
    @Test
    public void testMetricsEnable() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String enable = "true";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("metrics", ImmutableMap.of("enable", true));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.isMetricsEnable(), equalTo(Boolean.valueOf(enable)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testMetricsEnableDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.isMetricsEnable(), equalTo(Default.METRICS_ENABLE.toBoolean()));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testAuthenticationLock() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String lock = "24";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("authentication", ImmutableMap.of("lock", lock));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getAuthenticationLock(), equalTo(Integer.valueOf(lock)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testAuthenticationLockDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getAuthenticationLock(), equalTo(Default.AUTHENTICATION_LOCK.toInt()));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testCacheClusterUrl() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String url = "myclusterurl";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("cache", ImmutableMap.of("cluster", ImmutableMap.of("url", url)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getCacheClusterUrl(), equalTo(url));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testCacheClusterUrlDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getCacheClusterUrl(), equalTo(null));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationHeadersRefererPolicy() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String policy = "myrefererpolicy";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("headers", ImmutableMap.of("refererpolicy", policy)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getApplicationHeadersRefererPolicy(), equalTo(policy));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetApplicationHeadersRefererPolicyDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getApplicationHeadersRefererPolicy(), equalTo(Default.APPLICATION_HEADERS_REFERERPOLICY.toString()));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetUndertowMaxEntitySize() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String size = "4096";

        // when
        Map<Object, Object> configValues = ImmutableMap.of("undertow", ImmutableMap.of("maxentitysize", size));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getUndertowMaxEntitySize(), equalTo(Long.valueOf(size)));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetUndertowMaxEntitySizeDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // when
        Map<Object, Object> configValues = new HashMap<>();
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getUndertowMaxEntitySize(), equalTo(Default.UNDERTOW_MAX_ENTITY_SIZE.toLong()));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSessionCookieSignKey() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String key = UUID.randomUUID().toString();

        // when
        Map<Object, Object> configValues = ImmutableMap.of("session", ImmutableMap.of("cookie", ImmutableMap.of("signkey", key)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getSessionCookieSignKey(), equalTo(key));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSessionCookieSignKeyDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String secret = UUID.randomUUID().toString();
        
        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("secret", secret));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getSessionCookieSignKey(), equalTo(secret));
        assertThat(yaml.delete(), equalTo(true));
    }

    @Test
    public void testGetSessionCookieEncryptionKey() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String key = UUID.randomUUID().toString();

        // when
        Map<Object, Object> configValues = ImmutableMap.of("session", ImmutableMap.of("cookie", ImmutableMap.of("encryptionkey", key)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getSessionCookieEncryptionKey(), equalTo(key));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetSessionCookieEncryptionKeyDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String secret = UUID.randomUUID().toString();
        
        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("secret", secret));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getSessionCookieEncryptionKey(), equalTo(secret));
        assertThat(yaml.delete(), equalTo(true));
    } 
    
    @Test
    public void testGetAuthenticationCookieSignKey() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String key = UUID.randomUUID().toString();

        // when
        Map<Object, Object> configValues = ImmutableMap.of("authentication", ImmutableMap.of("cookie", ImmutableMap.of("signkey", key)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getAuthenticationCookieSignKey(), equalTo(key));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetAuthenticationCookieSignKeyDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String secret = UUID.randomUUID().toString();
        
        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("secret", secret));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getAuthenticationCookieSignKey(), equalTo(secret));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetAuthenticationCookieEncryptionKey() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String key = UUID.randomUUID().toString();

        // when
        Map<Object, Object> configValues = ImmutableMap.of("authentication", ImmutableMap.of("cookie", ImmutableMap.of("encryptionkey", key)));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);
        
        // then
        assertThat(config.getAuthenticationCookieEncryptionKey(), equalTo(key));
        assertThat(yaml.delete(), equalTo(true));
    }
    
    @Test
    public void testGetAuthenticationCookieEncryptionKeyKeyDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String secret = UUID.randomUUID().toString();
        
        // when
        Map<Object, Object> configValues = ImmutableMap.of("application", ImmutableMap.of("secret", secret));
        File yaml = createTempYaml(configValues);
        Config config = new Config(yaml.getAbsolutePath(), Mode.TEST);

        // then
        assertThat(config.getAuthenticationCookieEncryptionKey(), equalTo(secret));
        assertThat(yaml.delete(), equalTo(true));
    }     
    
    
    @Test
    public void testGetString() {
        //given
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
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString("smtp.username"), equalTo(""));
        assertThat(config.getString("smtp.port"), equalTo("3055"));
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
    
    @Test
    public void testGetMasterKey() {
        //given
        final Config config = Application.getInstance(Config.class);
        System.setProperty(Jvm.APPLICATION_MASTERKEY.toString(), "thisismymasterkey");

        //then
        assertThat(config.getMasterKeys().get(0), equalTo("thisismymasterkey"));
    }

    private File createTempYaml(Map<Object, Object> values) throws JsonGenerationException, JsonMappingException, IOException {
        Map<Object, Object> defaults = new HashMap<>();
        defaults.put("default", values);

        File configTestFile = new File(UUID.randomUUID().toString());
        System.setProperty(Jvm.APPLICATION_CONFIG.toString(), configTestFile.getAbsolutePath());
        new ObjectMapper(new YAMLFactory()).writeValue(configTestFile, defaults);

        return configTestFile;
    }
}