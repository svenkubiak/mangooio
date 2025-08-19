package io.mangoo.crypto;

import io.mangoo.constants.Const;
import io.mangoo.constants.Default;
import io.mangoo.constants.Key;
import io.mangoo.constants.NotNull;
import io.mangoo.core.Application;
import io.mangoo.enums.Mode;
import io.mangoo.utils.ConfigUtils;
import io.mangoo.utils.MangooUtils;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.yaml.snakeyaml.Yaml;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Stream;

@Singleton
public class Vault {
    private static final Logger LOG = LogManager.getLogger(Vault.class);
    private static final String PKCS_12 = "pkcs12";
    private final KeyStore keyStore;
    private Path path;
    private String prefix = Strings.EMPTY;
    private char[] secret;
    private Map<String, String> config = new HashMap<>();

    public Vault() {
        Security.addProvider(new BouncyCastleProvider());
        try {
            this.keyStore = KeyStore.getInstance(PKCS_12);
        } catch (KeyStoreException e) {
            throw new IllegalStateException("Failed to acquire PKCS12 keystore", e);
        }
        loadConfig();
        loadPath();
        loadSecret();
        loadKeyStore();
        loadPrefix();

        cleanUp();
    }

    private void loadKeyStore() {
        if (Files.exists(path)) {
            try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
                keyStore.load(inputStream, secret);
                LOG.info("Successfully loaded existing key store from {}", path);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load existing keystore", e);
            }
        } else {
            try (OutputStream outputStream = Files.newOutputStream(path,
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
                try {
                    Set<PosixFilePermission> perms = EnumSet.of(
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE);
                    Files.setPosixFilePermissions(path, perms);
                } catch (UnsupportedOperationException ignored) {
                    // likely running on Windows
                }

                keyStore.load(null, secret);
                keyStore.store(outputStream, secret);

                createCertificate();
                createSecrets();
                LOG.info("Successfully created new key store at {}", path);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to create keystore", e);
            }
        }
    }

    private void cleanUp() {
        config = new HashMap<>();
    }

    private void loadSecret() {
        String providedSecret = System.getProperty(Key.APPLICATION_VAULT_SECRET);

        if (StringUtils.isBlank(providedSecret)) {
            providedSecret = config.get(Key.APPLICATION_VAULT_SECRET);
        }

        if (StringUtils.isBlank(providedSecret)) {
            providedSecret = config.get(Key.APPLICATION_SECRET);
        }

        if (StringUtils.isBlank(providedSecret) || providedSecret.length() < 64) {
            throw new IllegalStateException(
                    "Keystore password (Vault secret) must be provided and at least 64 characters long."
            );
        }

        this.secret = providedSecret.toCharArray();
    }

    private void loadPath() {
        String path;
        if (!Application.inProdMode()) {
            path = MangooUtils.getRootFolder();
        } else {
            path = System.getProperty(Key.APPLICATION_VAULT_PATH);

            if (StringUtils.isBlank(path)) {
                path = config.get(Key.APPLICATION_VAULT_PATH);
            }
        }

        if (StringUtils.isBlank(path)) {
            path = Const.KEYSTORE_FILENAME;
        } else if (path.charAt(path.length() - 1) != File.separatorChar) {
            path += File.separator + Const.KEYSTORE_FILENAME;
        } else {
            path = path + Const.KEYSTORE_FILENAME;
        }

        this.path = Path.of(path);
    }

    @SuppressWarnings("unchecked")
    private void loadConfig() {
        var yaml = new Yaml();
        Map<String, Object> loaded = yaml.load(MangooUtils.readResourceToString(Const.CONFIG_FILE));

        Map<String, Object> defaultConfig = (Map<String, Object>) loaded.get("default");
        Map<String, Object> environments = (Map<String, Object>) loaded.get("environments");

        String activeEnv = Application.getMode().toString().toLowerCase(Locale.ENGLISH);

        Map<String, Object> activeEnvironment = (Map<String, Object>) environments.get(activeEnv);
        if (activeEnvironment != null) {
            Map<String, Object> mergedConfig = new HashMap<>(defaultConfig);
            ConfigUtils.mergeMaps(mergedConfig, activeEnvironment);

            this.config = ConfigUtils.flattenMap(mergedConfig);
        }
    }

    private void loadPrefix() {
        this.prefix = Application.getMode().toString().toLowerCase() + ".";
    }

    private void createSecrets() {
        Stream.of(Mode.values())
               .forEach(value -> {
                   String mode = value.toString().toLowerCase(Locale.ENGLISH) + ".";
                   put(mode + Key.AUTHENTICATION_COOKIE_SECRET, MangooUtils.randomString(64));
                   put(mode + Key.AUTHENTICATION_COOKIE_KEY, MangooUtils.randomString(64));
                   put(mode + Key.SESSION_COOKIE_SECRET, MangooUtils.randomString(64));
                   put(mode + Key.SESSION_COOKIE_KEY, MangooUtils.randomString(64));
                   put(mode + Key.FLASH_COOKIE_SECRET, MangooUtils.randomString(64));
                   put(mode + Key.FLASH_COOKIE_KEY, MangooUtils.randomString(64));
               });
    }

    public String get(String key) {
        Objects.requireNonNull(key, NotNull.KEY);
        key = prefix + key;

        try {
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry)
                    keyStore.getEntry(key, new KeyStore.PasswordProtection(secret));
            if (entry != null) {
                byte[] raw = entry.getSecretKey().getEncoded();
                return new String(raw, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            LOG.error("Failed to get value for key: {}", key, e);
        }

        return null;
    }

    public void remove(String key) {
        Objects.requireNonNull(key, NotNull.KEY);
        key = prefix + key;

        try {
            if (keyStore.containsAlias(key)) {
                keyStore.deleteEntry(key);
                try (OutputStream outputStream = Files.newOutputStream(path)) {
                    keyStore.store(outputStream, secret);
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to remove key", e);
        }
    }

    public void put(String key, String value) {
        Objects.requireNonNull(key, NotNull.KEY);
        Objects.requireNonNull(value, NotNull.VALUE);
        key = prefix + key;

        try (OutputStream outputStream = Files.newOutputStream(path)) {
            SecretKey secretKey = new SecretKeySpec(value.getBytes(StandardCharsets.UTF_8), "AES");

            KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
            KeyStore.ProtectionParameter protectionParam = new KeyStore.PasswordProtection(secret);

            keyStore.setEntry(key, secretKeyEntry, protectionParam);
            keyStore.store(outputStream, secret);
        } catch (Exception e) {
            LOG.error("Failed to add key", e);
        }
    }

    public SSLContext getSSLContext(String alias) {
          try {
            java.security.Key key = keyStore.getKey(alias, secret);
            Certificate[] chain = keyStore.getCertificateChain(alias);

            KeyStore tempKeyStore = KeyStore.getInstance("PKCS12");
            tempKeyStore.load(null, null);
            tempKeyStore.setKeyEntry(alias, key, secret, chain);

            KeyManagerFactory keyManagerFactory =
                    KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(tempKeyStore, secret);

            SSLContext sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create SSLContext", e);
        }
    }

    private void createCertificate()
            throws NoSuchAlgorithmException, NoSuchProviderException, CertIOException,
            OperatorCreationException, CertificateException, KeyStoreException {

        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
        keyPairGen.initialize(2048, new SecureRandom());
        KeyPair keyPair = keyPairGen.generateKeyPair();

        X500Name dnName = new X500Name("CN=localhost");
        BigInteger certSerialNumber = BigInteger.valueOf(System.currentTimeMillis());
        Date startDate = new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24);
        Date endDate = new Date(System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)); // 1 year validity

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                dnName,
                certSerialNumber,
                startDate,
                endDate,
                dnName,
                keyPair.getPublic()
        );

        certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("BC")
                .build(keyPair.getPrivate());

        X509CertificateHolder certHolder = certBuilder.build(contentSigner);

        X509Certificate certificate = new JcaX509CertificateConverter()
                .setProvider("BC")
                .getCertificate(certHolder);

        String alias = Optional.ofNullable(config.get(Key.CONNECTOR_HTTPS_CERTIFICATE_ALIAS)).orElse(Default.CONNECTOR_HTTPS_CERTIFICATE_ALIAS);

        keyStore.setKeyEntry(alias, keyPair.getPrivate(), secret, new X509Certificate[]{certificate});
    }
}
