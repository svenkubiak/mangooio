package io.mangoo.utils.internal;

import com.google.common.io.Resources;
import com.google.common.reflect.ClassPath;
import com.nimbusds.jwt.JWTClaimsSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.cache.Cache;
import io.mangoo.constants.ClaimKey;
import io.mangoo.constants.Default;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooJwtException;
import io.mangoo.exceptions.MangooTranslationException;
import io.mangoo.i18n.Messages;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Request;
import io.mangoo.routing.bindings.Validator;
import io.mangoo.utils.CommonUtils;
import io.mangoo.utils.DateUtils;
import io.mangoo.utils.JwtUtils;
import org.apache.fory.Fory;
import org.apache.fory.ThreadSafeFory;
import org.apache.fory.config.Language;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.executable.ExecutableValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Base64;

import static io.mangoo.core.Application.getInstance;

public final class MangooUtils {
    private static final Logger LOG = LogManager.getLogger(MangooUtils.class);
    private static final int ADMIN_LOGIN_MAX_RETRIES = 10;
    private static final int ADMIN_COOKIE_TTL = 1800;
    private static final int ADMIN_PRE_AUTH_COOKIE_TTL = 120;
    private static final String MANGOOIO_ADMIN_LOCKED_UNTIL = "mangooio-admin-locked-until";
    private static final String MANGOOIO_ADMIN_LOCK_COUNT = "mangooio-admin-lock-count";
    private static final String MANGOOIO_ADMIN_TWO_FACTOR_LOCKED_UNTIL = "mangooio-admin-twofactor-locked-until";
    private static final String MANGOOIO_ADMIN_TWO_FACTOR_LOCK_COUNT = "mangooio-admin-twofactor-lock-count";
    private static final String MANGOOIO_ADMIN_PRE_AUTH = "mangooio-admin-pre-auth-";
    private static final String VERSION_PROPERTIES = "version.properties";
    private static final String VERSION_UNKNOWN = "unknown";
    private static final Set<String> VALID_TIMEZONES = ZoneId.getAvailableZoneIds();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();
    private static final ThreadSafeFory FLASH_FORM_FORY = Fory.builder()
            .withLanguage(Language.JAVA)
            .requireClassRegistration(true)
            .buildThreadSafeFory();
    private static final ExecutableValidator executableValidator;
    static {
        FLASH_FORM_FORY.register(Form.class);
        FLASH_FORM_FORY.register(Validator.class);
        FLASH_FORM_FORY.register(Messages.class);

        var configuration = Validation.byDefaultProvider().configure();
        var englishInterpolator = new EnglishMessageInterpolator(configuration.getDefaultMessageInterpolator());
        try (var validatorFactory = configuration
                .messageInterpolator(englishInterpolator)
                .buildValidatorFactory()) {

            executableValidator = validatorFactory.getValidator().forExecutables();
        }
    }

    private MangooUtils() {}

    public static ExecutableValidator validator() {
        return executableValidator;
    }

    @SuppressFBWarnings(justification = "Only used to retrieve the version of mangoo I/O", value = "URLCONNECTION_SSRF_FD")
    public static String getVersion() {
        var version = VERSION_UNKNOWN;
        try (var inputStream = Resources.getResource(VERSION_PROPERTIES).openStream()) {
            final var properties = new Properties();
            properties.load(inputStream);
            version = String.valueOf(properties.get("version"));
        } catch (final IOException e) {
            LOG.error("Failed to get application version", e);
        }

        return version;
    }

    public static Set<String> getLanguages() throws MangooTranslationException {
        var classLoader = Thread.currentThread().getContextClassLoader();
        Set<String> languages = new HashSet<>();

        try {
            var classPath = ClassPath.from(classLoader);
            for (ClassPath.ResourceInfo resourceInfo : classPath.getResources()) {
                String resourceName = resourceInfo.getResourceName();
                if (resourceName.startsWith("translations/") && resourceName.endsWith(".properties")) {
                    String fileName = resourceName.replace("translations/", "");
                    var langCode = StringUtils.substringBetween(fileName, "messages_", ".properties");
                    if (StringUtils.isNotBlank(langCode)) {
                        languages.add(langCode);
                    }
                }
            }
        } catch (IOException e) {
            throw new MangooTranslationException(e);
        }

        return languages;
    }

    private static File findRoot(File current) {
        while (current != null) {
            var pom = current.toPath().resolve("pom.xml").toFile();
            if (pom.exists() && isRootPom(pom)) {
                return current;
            }
            current = current.getParentFile();
        }
        return null;
    }

    private static boolean isRootPom(File pomFile) {
        try {
            var dbf = DocumentBuilderFactory.newInstance();

            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setFeature("http://xml.org/sax/features/external-general-entities", false);
            dbf.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            dbf.setXIncludeAware(false);
            dbf.setExpandEntityReferences(false);
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            var dBuilder = dbf.newDocumentBuilder();
            dBuilder.setEntityResolver((publicId, systemId) ->
                    new InputSource(Reader.of("")));

            var path = pomFile.toPath();
            try (var in = Files.newInputStream(path)) {
                Document doc = dBuilder.parse(in);
                NodeList modules = doc.getElementsByTagName("modules");
                NodeList parentNodes = doc.getElementsByTagName("parent");
                return modules.getLength() > 0 || parentNodes.getLength() == 0;
            }
        } catch (Exception e) {
            LOG.error("Failed to find pom.xml", e);
        }

        return false;
    }

    public static String getRootFolder() {
        var startDir = Path.of(System.getProperty("user.dir")).toFile();
        var root = findRoot(startDir);

        return root!= null ? root.getAbsolutePath() : StringUtils.EMPTY;
    }

    public static boolean isValidAuthentication(Form form) {
        String username = getInstance(Config.class).getApplicationAdminUsername();
        String password = getInstance(Config.class).getApplicationAdminPassword();

        if (!StringUtils.isNoneBlank(username, password)) {
            return false;
        }

        boolean usernameMatch = MessageDigest.isEqual(
                username.getBytes(StandardCharsets.UTF_8),
                StringUtils.defaultString(form.get("username")).getBytes(StandardCharsets.UTF_8));
        boolean passwordMatch = MessageDigest.isEqual(
                password.getBytes(StandardCharsets.UTF_8),
                StringUtils.defaultString(form.get("password")).getBytes(StandardCharsets.UTF_8));

        // Non-short-circuiting '&' to avoid leaking which field matched via timing
        return usernameMatch & passwordMatch;
    }

    public static Cookie getAdminCookie(boolean requireTwoFactor) throws MangooJwtException {
        Config config = getInstance(Config.class);
        boolean preAuthentication = requireTwoFactor && StringUtils.isNotBlank(config.getApplicationAdminSecret());

        Map<String, String> claims = new HashMap<>();
        if (preAuthentication) {
            claims.put(ClaimKey.TWO_FACTOR, "true");
        }

        // A cookie that only passed the first factor is short-lived, as it is solely used to verify the second factor
        int ttl = preAuthentication ? ADMIN_PRE_AUTH_COOKIE_TTL : ADMIN_COOKIE_TTL;
        String subject = CommonUtils.uuidV6();

        try {
            var jwtData = JwtUtils.JwtData.create()
                    .withKey(config.getApplicationSecret().getBytes(StandardCharsets.UTF_8))
                    .withSecret(config.getApplicationSecret().getBytes(StandardCharsets.UTF_8))
                    .withIssuer(config.getApplicationName())
                    .withAudience(getAdminCookieName())
                    .withSubject(subject)
                    .withTtlSeconds(ttl)
                    .withClaims(claims);

            var jwt = JwtUtils.createJwt(jwtData);

            if (preAuthentication) {
                getInstance(Cache.class).put(MANGOOIO_ADMIN_PRE_AUTH + subject, Boolean.TRUE, ttl, ChronoUnit.SECONDS);
            }

            return new CookieImpl(getAdminCookieName())
                    .setValue(jwt)
                    .setHttpOnly(true)
                    .setSecure(Application.inProdMode())
                    .setExpires(DateUtils.localDateTimeToDate(LocalDateTime.now().plusSeconds(ttl)))
                    .setPath("/")
                    .setSameSiteMode("Strict");
        } catch (MangooJwtException e) {
            LOG.error("Failed to create admin cookie", e);
            throw new MangooJwtException(e);
        }
    }

    public static Optional<JWTClaimsSet> parseAdminCookie(Request request) {
        Objects.requireNonNull(request, Required.REQUEST);

        var cookie = request.getCookie(getAdminCookieName());
        if (cookie == null || StringUtils.isBlank(cookie.getValue())) {
            return Optional.empty();
        }

        Config config = getInstance(Config.class);
        var jwtData = JwtUtils.JwtData.create()
                .withKey(config.getApplicationSecret().getBytes(StandardCharsets.UTF_8))
                .withSecret(config.getApplicationSecret().getBytes(StandardCharsets.UTF_8))
                .withIssuer(config.getApplicationName())
                .withAudience(getAdminCookieName())
                .withTtlSeconds(ADMIN_COOKIE_TTL);

        try {
            return Optional.of(JwtUtils.parseJwt(cookie.getValue(), jwtData));
        } catch (MangooJwtException e) {
            LOG.error("Failed to parse admin cookie -> {}", e.getCause(), e);
            return Optional.empty();
        }
    }

    public static boolean isTwoFactorPending(JWTClaimsSet claims) {
        Objects.requireNonNull(claims, Required.CLAIMS);
        return ("true").equals(claims.getClaim(ClaimKey.TWO_FACTOR));
    }

    // One-time use, so a captured pre-authentication cookie cannot be replayed
    public static boolean consumePreAuthentication(JWTClaimsSet claims) {
        Objects.requireNonNull(claims, Required.CLAIMS);

        String subject = claims.getSubject();
        if (!isTwoFactorPending(claims) || StringUtils.isBlank(subject)) {
            return false;
        }

        Cache cache = getInstance(Cache.class);
        String key = MANGOOIO_ADMIN_PRE_AUTH + subject;
        if (cache.get(key) == null) {
            return false;
        }

        cache.remove(key);
        return true;
    }

    public static String getAdminCookieName() {
        return Application.inProdMode() ? "__Host-" + Default.APPLICATION_ADMIN_COOKIE_NAME : Default.APPLICATION_ADMIN_COOKIE_NAME;
    }

    public static void invalidAuthentication() {
        invalidAttempt(MANGOOIO_ADMIN_LOCK_COUNT, MANGOOIO_ADMIN_LOCKED_UNTIL);
    }

    public static boolean isNotLocked() {
        return isNotLocked(MANGOOIO_ADMIN_LOCKED_UNTIL);
    }

    public static void resetLockCounter() {
        resetLock(MANGOOIO_ADMIN_LOCK_COUNT, MANGOOIO_ADMIN_LOCKED_UNTIL);
    }

    // Separate budget from the password step, so a successful password login does not reset it
    public static void invalidSecondFactor() {
        invalidAttempt(MANGOOIO_ADMIN_TWO_FACTOR_LOCK_COUNT, MANGOOIO_ADMIN_TWO_FACTOR_LOCKED_UNTIL);
    }

    public static boolean isSecondFactorNotLocked() {
        return isNotLocked(MANGOOIO_ADMIN_TWO_FACTOR_LOCKED_UNTIL);
    }

    public static void resetSecondFactorLockCounter() {
        resetLock(MANGOOIO_ADMIN_TWO_FACTOR_LOCK_COUNT, MANGOOIO_ADMIN_TWO_FACTOR_LOCKED_UNTIL);
    }

    private static void invalidAttempt(String countKey, String lockedUntilKey) {
        AtomicInteger counter = getInstance(Cache.class).getAndIncrementCounter(countKey);
        if (counter.intValue() >= ADMIN_LOGIN_MAX_RETRIES) {
            getInstance(Cache.class).put(lockedUntilKey, LocalDateTime.now().plusMinutes(60));
        }

        getInstance(Cache.class).put(countKey, counter);
    }

    private static boolean isNotLocked(String lockedUntilKey) {
        LocalDateTime lockedUntil = getInstance(Cache.class).get(lockedUntilKey);
        return lockedUntil == null || lockedUntil.isBefore(LocalDateTime.now());
    }

    private static void resetLock(String countKey, String lockedUntilKey) {
        getInstance(Cache.class).resetCounter(countKey);
        getInstance(Cache.class).remove(lockedUntilKey);
    }

    public static void mergeMaps(Map<String, Object> baseMap, Map<String, Object> overrideMap) {
        Objects.requireNonNull(baseMap, Required.MAP);
        Objects.requireNonNull(overrideMap, Required.MAP);

        overrideMap.forEach((key, value) -> {
            if (value instanceof Map && baseMap.get(key) instanceof Map) {
                mergeMaps((Map<String, Object>) baseMap.get(key), (Map<String, Object>) value);
            } else {
                baseMap.put(key, value);
            }
        });
    }

    public static Map<String, String> flattenMap(Map<String, Object> map) {
        Objects.requireNonNull(map, Required.MAP);

        Map<String, String> flatMap = new HashMap<>();
        flattenMapHelper(map, "", flatMap);
        return flatMap;
    }

    @SuppressWarnings("unchecked")
    public static void flattenMapHelper(Map<String, Object> map, String prefix, Map<String, String> flatMap) {
        Objects.requireNonNull(map, Required.MAP);
        Objects.requireNonNull(prefix, Required.MAP);
        Objects.requireNonNull(map, Required.MAP);

        map.forEach((key, value) -> {
            String newKey = prefix.isEmpty() ? key : prefix + "." + key;

            if (value instanceof Map) {
                flattenMapHelper((Map<String, Object>) value, newKey, flatMap);
            } else {
                flatMap.put(newKey, value != null ? value.toString() : Strings.EMPTY);
            }
        });
    }

    public static boolean isValidTimeZone(String timezone) {
        return StringUtils.isNotBlank(timezone) && VALID_TIMEZONES.contains(timezone);
    }

    public static String serializeFlashFormToBase64(Form form) {
        Objects.requireNonNull(form, Required.OBJECT);

        byte[] serialized = FLASH_FORM_FORY.serialize(form);
        return BASE64_ENCODER.encodeToString(serialized);
    }

    public static Form deserializeFlashFormFromBase64(String data) {
        Objects.requireNonNull(data, Required.DATA);

        byte[] bytes = BASE64_DECODER.decode(data);
        Object deserialized = FLASH_FORM_FORY.deserialize(bytes);
        if (!(deserialized instanceof Form form)) {
            throw new IllegalArgumentException("Unexpected flash form type: "
                    + (deserialized == null ? "null" : deserialized.getClass().getName()));
        }

        return form;
    }

    private static class EnglishMessageInterpolator implements MessageInterpolator {
        private final MessageInterpolator delegate;

        public EnglishMessageInterpolator(MessageInterpolator delegate) {
            this.delegate = delegate;
        }

        @Override
        public String interpolate(String messageTemplate, Context context) {
            return delegate.interpolate(messageTemplate, context, Locale.ENGLISH);
        }

        @Override
        public String interpolate(String messageTemplate, Context context, Locale locale) {
            return delegate.interpolate(messageTemplate, context, Locale.ENGLISH);
        }
    }
}
