package io.mangoo.utils.internal;

import com.google.common.io.Resources;
import com.google.common.reflect.ClassPath;
import com.nimbusds.jwt.JWTClaimsSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.constants.ClaimKey;
import io.mangoo.constants.Default;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.crypto.Argon2Hash;
import io.mangoo.exceptions.MangooJwtException;
import io.mangoo.exceptions.MangooTranslationException;
import io.mangoo.i18n.Messages;
import io.mangoo.interfaces.AttemptStore;
import io.mangoo.interfaces.OneTimeStore;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Request;
import io.mangoo.routing.bindings.Validator;
import io.mangoo.utils.CommonUtils;
import io.mangoo.utils.DateUtils;
import io.mangoo.utils.JwtUtils;
import io.mangoo.utils.TotpUtils;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.executable.ExecutableValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.fory.Fory;
import org.apache.fory.ThreadSafeFory;
import org.apache.fory.config.Language;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static io.mangoo.core.Application.getInstance;

public final class MangooUtils {
    private static final Logger LOG = LogManager.getLogger(MangooUtils.class);
    /** Budget of the admin password step, separate from the second factor, so a successful password login does not reset it. */
    public static final String ADMIN_PASSWORD = "admin:pwd";
    public static final String ADMIN_SECOND_FACTOR = "admin:2fa";
    private static final String ADMIN_IDENTIFIER = "admin";
    private static final String ADMIN_PRE_AUTH = "admin-pre-auth:";
    private static final int ADMIN_LOGIN_MAX_RETRIES = 10;
    private static final Duration ADMIN_LOCK_DURATION = Duration.ofMinutes(60);
    private static final int ADMIN_COOKIE_TTL = 1800;
    private static final int ADMIN_PRE_AUTH_COOKIE_TTL = 120;
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
        String stored = getInstance(Config.class).getApplicationAdminPassword();

        if (!StringUtils.isNoneBlank(username, stored)) {
            return false;
        }

        boolean usernameMatch = MessageDigest.isEqual(
                username.getBytes(StandardCharsets.UTF_8),
                StringUtils.defaultString(form.get("username")).getBytes(StandardCharsets.UTF_8));
        boolean passwordMatch = adminPasswordMatches(StringUtils.defaultString(form.get("password")), stored);

        // Non-short-circuiting '&' so a wrong username still pays the Argon2 cost
        return usernameMatch & passwordMatch;
    }

    /**
     * {@code stored} is the Argon2id PHC string from {@code application.admin.password}. The salt lives in that string.
     * A blank submission is rejected without hashing. {@code MangooHashingException} propagates so an overloaded hasher is not counted as a failed login.
     */
    private static boolean adminPasswordMatches(String submitted, String stored) {
        if (StringUtils.isBlank(submitted) || !Argon2Hash.isPhcFormat(stored)) {
            if (!Argon2Hash.isPhcFormat(stored)) {
                LOG.warn("application.admin.password is not an Argon2id hash, admin login is rejected");
            }
            return false;
        }

        try {
            String salt = new String(Argon2Hash.parse(stored).salt(), StandardCharsets.UTF_8);
            return CommonUtils.matchArgon2(submitted, salt, stored);
        } catch (IllegalArgumentException e) {
            LOG.warn("application.admin.password is not a valid Argon2id hash, admin login is rejected");
            return false;
        }
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
        return parseAdminCookie(cookie == null ? null : cookie.getValue());
    }

    /** Parses the value of the admin cookie, e.g. of a request that is not dispatched to a controller. */
    public static Optional<JWTClaimsSet> parseAdminCookie(String value) {
        if (StringUtils.isBlank(value)) {
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
            return Optional.of(JwtUtils.parseJwt(value, jwtData));
        } catch (MangooJwtException e) {
            LOG.error("Failed to parse admin cookie -> {}", e.getCause(), e);
            return Optional.empty();
        }
    }

    public static boolean isTwoFactorPending(JWTClaimsSet claims) {
        Objects.requireNonNull(claims, Required.CLAIMS);
        return ("true").equals(claims.getClaim(ClaimKey.TWO_FACTOR));
    }

    // One-time use on every instance, so a captured pre-authentication cookie cannot be replayed; the claim outlives
    // the cookie by the JWT clock skew, as the cookie is still accepted until then
    public static boolean consumePreAuthentication(JWTClaimsSet claims) {
        Objects.requireNonNull(claims, Required.CLAIMS);

        String jwtId = claims.getJWTID();
        if (!isTwoFactorPending(claims) || StringUtils.isBlank(jwtId) || claims.getExpirationTime() == null) {
            return false;
        }

        Instant expiresAt = claims.getExpirationTime().toInstant().plusSeconds(Default.JWT_CLOCK_SKEW);
        return getInstance(OneTimeStore.class).claim(ADMIN_PRE_AUTH + jwtId, expiresAt);
    }

    public static String getAdminCookieName() {
        return Application.inProdMode() ? "__Host-" + Default.APPLICATION_ADMIN_COOKIE_NAME : Default.APPLICATION_ADMIN_COOKIE_NAME;
    }

    /** Reserves an attempt of ADMIN_PASSWORD or ADMIN_SECOND_FACTOR before it is verified; false while the step is locked. */
    public static boolean acquireAdminAttempt(String step) {
        return getInstance(AttemptStore.class).tryAcquire(step, ADMIN_LOGIN_MAX_RETRIES, ADMIN_LOCK_DURATION);
    }

    public static void releaseAdminAttempt(String step) {
        getInstance(AttemptStore.class).release(step, ADMIN_LOGIN_MAX_RETRIES);
    }

    public static void resetAdminAttempts(String step) {
        getInstance(AttemptStore.class).reset(step);
    }

    public static boolean isAdminLocked(String step) {
        return getInstance(AttemptStore.class).isLocked(step);
    }

    public static boolean isValidAdminSecondFactor(String secret, String code) {
        return TotpUtils.verifyTotp(ADMIN_IDENTIFIER, secret, code);
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
        Objects.requireNonNull(prefix, Required.PREFIX);
        Objects.requireNonNull(flatMap, Required.FLAT_MAP);

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
