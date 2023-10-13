package io.mangoo.core;

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
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;

import io.mangoo.TestExtension;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.enums.Mode;

@ExtendWith({TestExtension.class})
class ConfigTest {
    
    @Test
    void testFlashCookieName() {
        // given
        final Config config = Application.getInstance(Config.class);

        // then
        assertThat(config.getFlashCookieName(), equalTo("test-flash"));
    }
    
    @Test
    void testGetSessionCookieName() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetSessionCookieNameDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetApplicationSecret() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetAuthenticationCookieName() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetAuthenticationCookieNameDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetAuthenticationCookieExpires() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String expires = "42";

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.cookie.expires", expires);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.isAuthenticationCookieExpires(), equalTo(Boolean.valueOf(expires)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetAuthenticationCookieExpiresDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isAuthenticationCookieExpires(), equalTo(Default.AUTHENTICATION_COOKIE_EXPIRES.toBoolean()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetSessionCookieExpires() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String expires = "60";

        // when
        Map<String, String> configValues = ImmutableMap.of("session.cookie.expires", expires);
        File tempConfig = createTempConfig(configValues);
        System.setProperty("application.mode", Mode.TEST.toString());
        Config config = new Config();
        
        // then
        assertThat(config.getSessionCookieTokenExpires(), equalTo(Long.valueOf(expires)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetSessionCookieExpiresDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSessionCookieTokenExpires(), equalTo(Default.SESSION_COOKIE_TOKEN_EXPIRES.toLong()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testIsSessionCookieSecure() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testIsSessionCookieSecureDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testIsAuthentcationCookieSecure() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testIsAuthenticationCookieSecureDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testI18nCookieName() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testI18nCookieNameDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testIsFlashCookieSecure() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testAplicationLanguage() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testAplicationLanguageDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testIsSchedulerEnabled() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String enabled = "false";

        // when
        Map<String, String> configValues = ImmutableMap.of("scheduler.enable", enabled);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.isSchedulerEnabled(), equalTo(Boolean.valueOf(enabled)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testIsSchedulerEnabledDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isSchedulerEnabled(), equalTo(Default.SCHEDULER_ENABLE.toBoolean()));
        assertThat(tempConfig.delete(), equalTo(true));
    } 
    
    @Test
    void testGetApplicationAdminUsername() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetApplicationAdminSecret() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String secret = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.admin.secret", secret);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getApplicationAdminSecret(), equalTo(secret));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetApplicationAdminSecretDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getApplicationAdminSecret(), equalTo(null));
        assertThat(tempConfig.delete(), equalTo(true));
    } 
    
    @Test
    void testGetApplicationAdminPassword() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testAuthenticationCookieRememberExpires() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testAuthenticationCookieRememberExpiresDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetApplicationController() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetApplicationControllerDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetApplicationtemplateEngine() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetApplicationtemplateEngineDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testValueFromSystemPropertyInProfile() {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Config config = Application.getInstance(Config.class);
        String value = config.getString("application.test");
        
        // then
        assertThat(value, equalTo("valuefromarg"));
    }
    
    @Test
    void testValueFromSystemProperty() {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Config config = Application.getInstance(Config.class);
        String applicationName = config.getApplicationName();
        
        // then
        assertThat(applicationName, equalTo("namefromarg"));
    }
    
    @Test
    void testValueFromSystemPropertyInProfileEncrypted() {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Config config = Application.getInstance(Config.class);
        String value = config.getString("application.profil");
        
        // then
        assertThat(value, equalTo("admin"));
    }
    
    @Test
    void testValueFromSystemPropertyEncrypted() {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Config config = Application.getInstance(Config.class);
        String value = config.getString("application.encrypted");
        
        // then
        assertThat(value, equalTo("admin"));
    }
    
    @Test
    void testIsApplicationAdminEnable() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testIsApplicationAdminEnableDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetSmptHost() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetSmptHostDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetSmptPort() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetSmptPortDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testIsAuthentication() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String ssl = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.authentication", ssl);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.isSmtpAuthentication(), equalTo(Boolean.valueOf(ssl)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testIsSmptAuthenticationDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isSmtpAuthentication(), equalTo(Default.SMTP_AUTHENTICATION.toBoolean()));
        assertThat(tempConfig.delete(), equalTo(true));
    } 
    
    @Test
    void testGetSmptUsername() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetSmptUsernameDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetSmptPassword() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetSmptPasswordDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetSmptfrom() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetSmptfromDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetConnectorAjpHost() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetConnectorAjpHostDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetConnectorAjpPort() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetConnectorAjpPortDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetConnectorHttpHost() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetConnectorHttpHostDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetConnectorHttpPort() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetConnectorHttpPortDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testIsCacheClusterEnable() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testIsCacheClusterEnableDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testMetricsEnable() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testMetricsEnableDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testAuthenticationLock() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testAuthenticationLockDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testCacheClusterUrl() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testCacheClusterUrlDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetUndertowMaxEntitySize() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetUndertowMaxEntitySizeDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
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
    void testGetSessionCookieSecret() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String key = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("session.cookie.secret", key);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getSessionCookieSecret(), equalTo(key));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetSessionCookieeSecretDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String secret = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", secret);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSessionCookieSecret(), equalTo(secret));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetFlashCookieSecret() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String key = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());

        // when
        Map<String, String> configValues = ImmutableMap.of("flash.cookie.secret", key);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getFlashCookieSecret(), equalTo(key));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetFlashCookieEncryptionKeyDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String secret = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", secret);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getFlashCookieSecret(), equalTo(secret));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetAuthenticationCookieSignKeyDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String secret = UUID.randomUUID().toString();
        
        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", secret);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getAuthenticationCookieSecret(), equalTo(secret));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetAuthenticationCookieSecret() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String key = UUID.randomUUID().toString();

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.cookie.secret", key);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getAuthenticationCookieSecret(), equalTo(key));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetAuthenticationCookieSecretDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        String secret = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", secret);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getAuthenticationCookieSecret(), equalTo(secret));
        assertThat(tempConfig.delete(), equalTo(true));
    }     
    
    @Test
    void testGetString() {
        //given
        System.setProperty(Key.APPLICATION_CONFIG.toString(), "");
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString(Key.CONNECTOR_HTTP_PORT), equalTo("10808"));
        assertThat(config.getString(Key.CONNECTOR_HTTP_PORT.toString()), equalTo("10808"));
    }

    @Test
    void testGetInt() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getInt(Key.CONNECTOR_HTTP_PORT), equalTo(10808));
        assertThat(config.getInt(Key.CONNECTOR_HTTP_PORT.toString()), equalTo(10808));
    }

    @Test
    void testGetBoolean() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getBoolean(Key.APPLICATION_ADMIN_ENABLE), equalTo(true));
        assertThat(config.getBoolean(Key.APPLICATION_ADMIN_ENABLE.toString()), equalTo(true));
    }

    @Test
    void testGetLong() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getLong(Key.CONNECTOR_HTTP_PORT), equalTo(10808L));
        assertThat(config.getLong(Key.CONNECTOR_HTTP_PORT.toString()), equalTo(10808L));
    }

    @Test
    void testGetStringDefaultValue() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString("foo", "bar"), equalTo("bar"));
    }

    @Test
    void testGetIntDefaultValue() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getInt("foo", 42), equalTo(42));
    }

    @Test
    void testGetBooleanDefaultValue() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getBoolean("foo", true), equalTo(true));
        assertThat(config.getBoolean("foo", false), equalTo(false));
    }

    @Test
    void testGetLongDefaultValue() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getLong("foo", 42), equalTo(42L));
    }

    @Test
    void testGetAllConfigurationValues() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getAllConfigurations(), not(nullValue()));
        assertThat(config.getAllConfigurations().size(), greaterThan(12));
    }

    @Test
    void testEnvironmentValues() {
        //given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString("smtp.username"), equalTo("bla"));
        assertThat(config.getString("smtp.port"), equalTo("3025"));
    }
    
    @Test
    void testEncryptedValue() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString("application.foo"), equalTo("admin"));
    }
    
    @Test
    void testEncryptedValueMultiKeyLineTwo() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString("application.bar"), equalTo("westeros"));
    }
    
    @Test
    void testEncryptedValueMultiKeyLineThree() {
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
    
    @Test
    void testIsSmtpDebug() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String debug = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.debug", debug);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.isSmtpDebug(), equalTo(Boolean.valueOf(debug)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testIsSmtpDebugDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isSmtpDebug(), equalTo(Default.SMTP_DEBUG.toBoolean()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetSmtpProtocol() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String protocol = "smtptls";

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.protocol", protocol);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getSmtpProtocol(), equalTo(protocol));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetSmtpProtocolDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSmtpProtocol(), equalTo(Default.SMTP_PROTOCOL.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetMongoAuthDB() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String authDb = "admin";

        // when
        Map<String, String> configValues = ImmutableMap.of("persistence.mongo.authdb", authDb);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getMongoAuthDB("persistence."), equalTo(authDb));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetMongoDBName() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String mongodb = "mongodb";

        // when
        Map<String, String> configValues = ImmutableMap.of("persistence.mongo.dbname", mongodb);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getMongoDbName("persistence."), equalTo(mongodb));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetMongoDBNameDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getMongoDbName("persistence."), equalTo(Default.PERSISTENCE_MONGO_DBNAME.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetMongoHost() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String host = "127.0.0.5";

        // when
        Map<String, String> configValues = ImmutableMap.of("persistence.mongo.host", host);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getMongoHost("persistence."), equalTo(host));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetMongoHostDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getMongoHost(""), equalTo(Default.PERSISTENCE_MONGO_HOST.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    } 
    
    @Test
    void testGetMongoPort() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String port = "47235";

        // when
        Map<String, String> configValues = ImmutableMap.of("persistence.mongo.port", port);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getMongoPort("persistence."), equalTo(Integer.parseInt(port)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetMongoPortDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getMongoPort("persistence."), equalTo(Default.PERSISTENCE_MONGO_PORT.toInt()));
        assertThat(tempConfig.delete(), equalTo(true));
    } 
    
    @Test
    void testGetMongoPassword() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String password = "thisismypassword";

        // when
        Map<String, String> configValues = ImmutableMap.of("persistence.mongo.password", password);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getMongoPassword("persistence."), equalTo(password));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetMongoUsername() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String username = "thisismyusername";

        // when
        Map<String, String> configValues = ImmutableMap.of("persistence.mongo.username", username);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getMongoUsername("persistence."), equalTo(username));
        assertThat(tempConfig.delete(), equalTo(true));
    }

    @Test
    void testCorsEnable() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String cors = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.enable", cors);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.isCorsEnable(), equalTo(Boolean.valueOf(cors)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testCorsEnableDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isCorsEnable(), equalTo(Default.CORS_ENABLE.toBoolean()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetCorsAllowOrigin() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String origin = "^http(s)?://(www.)?example.(com|org)$";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.alloworigin", origin);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getCorsAllowOrigin().toString(), equalTo(Pattern.compile(origin).toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetCorsAllowOriginDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getCorsAllowOrigin().toString(), equalTo(Pattern.compile(Default.CORS_ALLOWORIGIN.toString()).toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetCorsHeadersAllowCredentials() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String credentials = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.headers.allowcredentials", credentials);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getCorsHeadersAllowCredentials(), equalTo(credentials));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetCorsHeadersAllowCredentialsDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getCorsHeadersAllowCredentials(), equalTo(Default.CORS_HEADERS_ALLOWCREDENTIALS.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetCorsHeadersAllowHeaders() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String headers = "Authorization,Content-Type";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.headers.allowheaders", headers);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getCorsHeadersAllowHeaders(), equalTo(headers));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetCorsHeadersAllowHeadersDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getCorsHeadersAllowHeaders(), equalTo(Default.CORS_HEADERS_ALLOWHEADERS.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetCorsHeadersAllowMethods() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String headers = "GET,POST";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.headers.allowmethods", headers);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getCorsHeadersAllowMethods(), equalTo(headers));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetCorsHeadersAllowMethodsDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getCorsHeadersAllowMethods(), equalTo(Default.CORS_HEADERS_ALLOWMETHODS.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetCorsHeadersExposeHeaders() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String headers = "Accept-Ranges,Content-Length";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.headers.exposeheaders", headers);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getCorsHeadersExposeHeaders(), equalTo(headers));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetCorsHeadersExposeHeadersDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getCorsHeadersExposeHeaders(), equalTo(Default.CORS_HEADERS_EXPOSEHEADERS.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetCorsHeadersMaxAge() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String maxage = "86400";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.headers.maxage", maxage);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getCorsHeadersMaxAge(), equalTo(maxage));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetCorsHeadersMaxAgeDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getCorsHeadersMaxAge(), equalTo(Default.CORS_HEADERS_MAXAGE.toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetCorsUrlPattern() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String pattern = "^http(s)?://([^/]+)(:([^/]+))?(/([^/])+)?/api(/.*)?$";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.urlpattern", pattern);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getCorsUrlPattern().toString(), equalTo(Pattern.compile(pattern).toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetCorsUrlPatternDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getCorsUrlPattern().toString(), equalTo(Pattern.compile(Default.CORS_URLPATTERN.toString()).toString()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetApplicationAdminHealthToken() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String token = UUID.randomUUID().toString();

        // when
        Map<String, String> configValues = ImmutableMap.of("application.admin.health.token", token);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.getApplicationAdminHealthToken(), equalTo(token));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetApplicationAdminHealthEnable() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        String enable = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("application.admin.health.enable", enable);
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();
        
        // then
        assertThat(config.isApplicationAdminHealthEnable(), equalTo(Boolean.parseBoolean(enable)));
        assertThat(tempConfig.delete(), equalTo(true));
    }
    
    @Test
    void testGetApplicationAdminHealthEnableDefaultValue() throws JsonGenerationException, JsonMappingException, IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE.toString(), Mode.TEST.toString());
        
        // when
        Map<String, String> configValues = new HashMap<>();
        File tempConfig = createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isApplicationAdminHealthEnable(), equalTo(Default.APPLICATION_ADMIN_HEALTH_ENABLE.toBoolean()));
        assertThat(tempConfig.delete(), equalTo(true));
    }
}