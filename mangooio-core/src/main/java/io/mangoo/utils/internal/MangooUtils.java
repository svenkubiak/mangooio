package io.mangoo.utils.internal;

import com.google.common.io.Resources;
import com.google.common.reflect.ClassPath;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.cache.Cache;
import io.mangoo.constants.Default;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooJwtException;
import io.mangoo.routing.bindings.Form;
import io.mangoo.utils.CommonUtils;
import io.mangoo.utils.DateUtils;
import io.mangoo.utils.JwtUtils;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
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
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.mangoo.core.Application.getInstance;

public final class MangooUtils {
    private static final Logger LOG = LogManager.getLogger(MangooUtils.class);
    private static final int ADMIN_LOGIN_MAX_RETRIES = 10;
    private static final String MANGOOIO_ADMIN_LOCKED_UNTIL = "mangooio-admin-locked-until";
    private static final String MANGOOIO_ADMIN_LOCK_COUNT = "mangooio-admin-lock-count";
    private static final String VERSION_PROPERTIES = "version.properties";
    private static final String VERSION_UNKNOWN = "unknown";

    private MangooUtils() {}

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

    public static Set<String> getLanguages() {
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
            throw new RuntimeException(e);
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
                    new InputSource(new StringReader("")));

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

        return StringUtils.isNoneBlank(username, password) &&
                username.equals(form.get("username")) && password.equals(form.get("password"));
    }

    public static Cookie getAdminCookie(boolean requireTwoFactor) throws MangooJwtException {
        Config config = getInstance(Config.class);
        Map<String, String> claims = new HashMap<>();
        claims.put("uuid", CommonUtils.randomString(32));

        if (requireTwoFactor && StringUtils.isNotBlank(config.getApplicationAdminSecret())) {
            claims.put("twofactor", "true");
        }

        try {
            var jwtData = JwtUtils.JwtData.create()
                    .withKey(config.getApplicationSecret())
                    .withSecret(config.getApplicationSecret().getBytes(StandardCharsets.UTF_8))
                    .withIssuer(config.getApplicationName())
                    .withAudience(getAdminCookieName())
                    .withSubject(CommonUtils.uuidV6())
                    .withTtlSeconds(1800)
                    .withClaims(claims);

            var jwt = JwtUtils.createJwt(jwtData);

            return new CookieImpl(getAdminCookieName())
                    .setValue(jwt)
                    .setHttpOnly(true)
                    .setSecure(Application.inProdMode())
                    .setExpires(DateUtils.localDateTimeToDate(LocalDateTime.now().plusSeconds(1800)))
                    .setPath("/")
                    .setSameSiteMode("Strict");
        } catch (MangooJwtException e) {
            LOG.error("Failed to create admin cookie", e);
            throw new MangooJwtException(e);
        }
    }

    public static String getAdminCookieName() {
        return Application.inProdMode() ? "__Host-" + Default.APPLICATION_ADMIN_COOKIE_NAME : Default.APPLICATION_ADMIN_COOKIE_NAME;
    }

    public static void invalidAuthentication() {
        AtomicInteger counter = getInstance(Cache.class).getAndIncrementCounter(MANGOOIO_ADMIN_LOCK_COUNT);
        if (counter.intValue() >= ADMIN_LOGIN_MAX_RETRIES) {
            getInstance(Cache.class).put(MANGOOIO_ADMIN_LOCKED_UNTIL, LocalDateTime.now().plusMinutes(60));
        }

        getInstance(Cache.class).put(MANGOOIO_ADMIN_LOCK_COUNT, counter);
    }

    public static boolean isNotLocked() {
        LocalDateTime lockedUntil = getInstance(Cache.class).get(MANGOOIO_ADMIN_LOCKED_UNTIL);
        return lockedUntil == null || lockedUntil.isBefore(LocalDateTime.now());
    }

    public static void resetLockCounter() {
        getInstance(Cache.class).resetCounter(MANGOOIO_ADMIN_LOCK_COUNT);
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
}
