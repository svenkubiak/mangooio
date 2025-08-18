package io.mangoo.crypto;

import io.mangoo.constants.Key;
import io.mangoo.constants.NotNull;
import io.mangoo.core.Config;
import io.mangoo.utils.MangooUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Objects;

@Singleton
public class Vault {
    private static final Logger LOG = LogManager.getLogger(Vault.class);
    private static final String FILENAME = "vault.p12";
    private final Config config;
    private Path path;
    private KeyStore keyStore;
    private char[] secret;

    @Inject
    public Vault(Config config) {
        Security.addProvider(new BouncyCastleProvider());
        this.config = Objects.requireNonNull(config, NotNull.CONFIG);
        try {
            this.keyStore = KeyStore.getInstance("pkcs12");
        } catch (KeyStoreException e) {
            LOG.error("Failed to get instance of KeyStore", e);
        }
        loadPath();
        loadSecret();
        loadKeyStore();
    }

    private void loadKeyStore() {
        if (Files.exists(path)) {
            try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
                keyStore.load(inputStream, secret);
                LOG.info("Successfully loaded existing key store from {}", path);
            } catch (Exception e) {
                LOG.error("Failed to load key store", e);
            }
        } else {
            try (OutputStream outputStream = Files.newOutputStream(path)) {
                keyStore.load(null, secret);
                keyStore.store(outputStream, secret);

                createCertificate();
                createSecrets();
                LOG.info("Successfully created new key store at {}", path);
            } catch (Exception e) {
                LOG.error("Failed to create key store", e);
            }
        }
    }

    private void loadSecret() {
        String secret = System.getProperty(Key.APPLICATION_VAULT_SECRET);

        if (StringUtils.isBlank(secret)) {
            secret = config.getApplicationVaultSecret();
        }

        if (StringUtils.isBlank(secret)) {
            secret = config.getApplicationSecret();
        }

        this.secret = secret.toCharArray();
    }

    private void loadPath() {
        String path = System.getProperty(Key.APPLICATION_VAULT_PATH);

        if (StringUtils.isBlank(path)) {
            path = config.getApplicationVaultPath();
        }

        if (StringUtils.isNotBlank(path)) {
            path = path + "/" + FILENAME;
        } else {
            path = FILENAME;
        }

        this.path = Path.of(path);
    }

    private void createSecrets() {
        put("dev." + Key.AUTHENTICATION_COOKIE_SECRET, MangooUtils.randomString(64));
        put("dev." + Key.AUTHENTICATION_COOKIE_KEY, MangooUtils.randomString(64));
        put("test." + Key.AUTHENTICATION_COOKIE_SECRET, MangooUtils.randomString(64));
        put("test." + Key.AUTHENTICATION_COOKIE_KEY, MangooUtils.randomString(64));
        put("prod." + Key.AUTHENTICATION_COOKIE_SECRET, MangooUtils.randomString(64));
        put("prod." + Key.AUTHENTICATION_COOKIE_KEY, MangooUtils.randomString(64));

        put("dev." + Key.SESSION_COOKIE_SECRET, MangooUtils.randomString(64));
        put("dev." + Key.SESSION_COOKIE_KEY, MangooUtils.randomString(64));
        put("test." + Key.SESSION_COOKIE_SECRET, MangooUtils.randomString(64));
        put("test." + Key.SESSION_COOKIE_KEY, MangooUtils.randomString(64));
        put("prod." + Key.SESSION_COOKIE_SECRET, MangooUtils.randomString(64));
        put("prod." + Key.SESSION_COOKIE_KEY, MangooUtils.randomString(64));

        put("dev." + Key.FLASH_COOKIE_SECRET, MangooUtils.randomString(64));
        put("dev." + Key.FLASH_COOKIE_KEY, MangooUtils.randomString(64));
        put("test." + Key.FLASH_COOKIE_SECRET, MangooUtils.randomString(64));
        put("test." + Key.FLASH_COOKIE_KEY, MangooUtils.randomString(64));
        put("prod." + Key.FLASH_COOKIE_SECRET, MangooUtils.randomString(64));
        put("prod." + Key.FLASH_COOKIE_KEY, MangooUtils.randomString(64));
    }

    public String get(String key) {
        byte[] raw = null;
        try {
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry)
                    keyStore.getEntry(key, new KeyStore.PasswordProtection(secret));
            raw = entry.getSecretKey().getEncoded();
            return new String(raw, StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOG.error("Failed to get key", e);
        }

        return null;
    }

    public void remove(String key) {
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
        try (OutputStream outputStream = Files.newOutputStream(path)) {
            SecretKey secretKey = new SecretKeySpec(value.getBytes(), "AES");

            KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
            KeyStore.ProtectionParameter protectionParam = new KeyStore.PasswordProtection(secret);

            keyStore.setEntry(key, secretKeyEntry, protectionParam);
            keyStore.store(outputStream, secret);
        } catch (Exception e) {
            LOG.error("Failed to add key", e);
        }
    }

    public SSLContext getSSLContext() {
        SSLContext sslContext = null;
        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, secret);

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyStoreException | UnrecoverableKeyException | KeyManagementException e) {
            LOG.error("Failed to create SSLContext", e);
        }

        return sslContext;
    }

    private void createCertificate() throws NoSuchAlgorithmException, NoSuchProviderException, CertIOException, OperatorCreationException, CertificateException, KeyStoreException {
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

        keyStore.setKeyEntry("localhost", keyPair.getPrivate(), secret, new X509Certificate[]{certificate});
    }
}