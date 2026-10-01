package io.mangoo.crypto;

import io.mangoo.constants.Const;
import io.mangoo.constants.Default;
import io.mangoo.constants.Key;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.enums.Mode;
import io.mangoo.utils.Argument;
import io.mangoo.utils.CommonUtils;
import io.mangoo.utils.internal.MangooUtils;
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
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.yaml.snakeyaml.Yaml;

import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@Singleton
public class Vault {
    private static final Logger LOG = LogManager.getLogger(Vault.class);
    private static final String KEYSTORE_TYPE = "PKCS12";
    private static final String[] KEYS;
    static {
        KEYS = new String[] {
                Key.AUTHENTICATION_COOKIE_SECRET,
                Key.AUTHENTICATION_COOKIE_KEY,
                Key.SESSION_COOKIE_SECRET,
                Key.SESSION_COOKIE_KEY,
                Key.FLASH_COOKIE_SECRET,
                Key.FLASH_COOKIE_KEY
        };
    }

    private KeyStore keyStore;
    private Path path;
    private String prefix = Strings.EMPTY;
    private char[] secret;
    private Map<String, String> config = new HashMap<>();

    public Vault() {
        try {
            loadConfig();
            if (enabled()) {
                Security.addProvider(new BouncyCastleProvider());
                this.keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
                loadPath();
                loadSecret();
                loadKeyStore();
                loadPrefix();

                boolean changed = createSecrets();
                changed |= createCertificate();
                if (changed) {
                    store();
                }

                cleanUp();
            }
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("Failed to init keystore", e);
        }
    }

    private boolean enabled() {
        return config.get(Key.APPLICATION_VAULT_ENABLE) != null && ("true").equals(config.get(Key.APPLICATION_VAULT_ENABLE));
    }

    private boolean exists(String key) {
        Objects.requireNonNull(key, Required.KEY);
        try {
            return keyStore.containsAlias(key);
        } catch (KeyStoreException e) {
            //Intentionally throwing no exception
            return false;
        }
    }

    private void loadKeyStore() throws IOException, GeneralSecurityException {
        if (Files.exists(path) && Files.size(path) == 0) {
            // A vault file without content holds no secrets, e.g. left behind by an earlier
            // version that failed while creating the vault, it is therefore created anew
            LOG.warn("Found empty vault at {}, creating a new vault", path);
        }

        if (Files.exists(path) && Files.size(path) > 0) {
            try (var inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
                keyStore.load(inputStream, secret);
                LOG.info("Loaded existing vault from {}", path);
            } catch (IllegalStateException | IOException | NoSuchAlgorithmException | CertificateException e) {
                LOG.error("Failed to load vault from {}", path, e); //NOSONAR
                throw e;
            }
        } else {
            try {
                keyStore.load(null, secret);
                store();
                LOG.info("Created new vault at {}", path);
            } catch (IOException | GeneralSecurityException e) {
                LOG.error("Failed to create vault at {}", path, e); //NOSONAR
                throw e;
            }
        }
    }

    private void cleanUp() {
        config = new HashMap<>();
    }

    private void loadSecret() {
        String providedSecret = System.getenv("APPLICATION_VAULT_SECRET");

        if (Strings.isBlank(providedSecret)) {
            providedSecret = System.getProperty(Key.APPLICATION_VAULT_SECRET);
        }

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
        String vaultPath;
        if (!Application.inProdMode()) {
            vaultPath = MangooUtils.getRootFolder();
        } else {
            vaultPath = System.getenv("APPLICATION_VAULT_PATH");

            if (Strings.isBlank(vaultPath)) {
                vaultPath = System.getProperty(Key.APPLICATION_VAULT_PATH);
            }

            if (StringUtils.isBlank(vaultPath)) {
                vaultPath = config.get(Key.APPLICATION_VAULT_PATH);
            }
        }

        if (StringUtils.isBlank(vaultPath)) {
            vaultPath = Const.KEYSTORE_FILENAME;
        } else if (vaultPath.charAt(vaultPath.length() - 1) != File.separatorChar) {
            vaultPath += File.separator + Const.KEYSTORE_FILENAME;
        } else {
            vaultPath = vaultPath + Const.KEYSTORE_FILENAME;
        }

        this.path = Path.of(vaultPath);
    }

    @SuppressWarnings("unchecked")
    private void loadConfig() {
        var yaml = new Yaml();
        Map<String, Object> loaded = yaml.load(CommonUtils.readResourceToString(Const.CONFIG_FILE));

        Map<String, Object> defaultConfig = (Map<String, Object>) loaded.get("default");
        Map<String, Object> environments = (Map<String, Object>) loaded.get("environments");

        String activeEnv = Application.getMode().toString().toLowerCase(Locale.ENGLISH);

        Map<String, Object> activeEnvironment = (Map<String, Object>) environments.get(activeEnv);
        if (activeEnvironment != null) {
            Map<String, Object> mergedConfig = new HashMap<>(defaultConfig);
            MangooUtils.mergeMaps(mergedConfig, activeEnvironment);

            this.config = MangooUtils.flattenMap(mergedConfig);
        }
    }

    private void loadPrefix() {
        this.prefix = Application.getMode().toString().toLowerCase() + ".";
    }

    /**
     * Creates the cookie secrets and keys for every mode if they do not exist yet
     *
     * @return True if the keystore has been changed and needs to be stored, false otherwise
     */
    private boolean createSecrets() throws KeyStoreException {
        var changed = false;
        for (Mode mode : Mode.values()) {
            for (String key : KEYS) {
                String alias = mode.toString().toLowerCase(Locale.ENGLISH) + "." + key;
                if (!exists(alias)) {
                    keyStore.setEntry(alias, secretKeyEntry(CommonUtils.randomString(64)), new KeyStore.PasswordProtection(secret));
                    changed = true;
                }
            }
        }

        return removeMisplacedSecrets() || changed;
    }

    /**
     * Removes the secrets that earlier versions created with a doubled mode prefix,
     * e.g. dev.prod.session.cookie.secret. These were never read.
     *
     * @return True if the keystore has been changed, false otherwise
     */
    private boolean removeMisplacedSecrets() throws KeyStoreException {
        var changed = false;
        for (Mode outer : Mode.values()) {
            for (Mode inner : Mode.values()) {
                for (String key : KEYS) {
                    String alias = outer.toString().toLowerCase(Locale.ENGLISH) + "." + inner.toString().toLowerCase(Locale.ENGLISH) + "." + key;
                    if (exists(alias)) {
                        keyStore.deleteEntry(alias);
                        changed = true;
                    }
                }
            }
        }

        return changed;
    }

    private static KeyStore.SecretKeyEntry secretKeyEntry(String value) {
        return new KeyStore.SecretKeyEntry(new SecretKeySpec(value.getBytes(StandardCharsets.UTF_8), "AES"));
    }

    /**
     * Stores the keystore atomically. The keystore is written to a temporary file in the
     * same directory first, which then replaces the vault file. The existing vault file is
     * therefore never truncated or left incomplete, even if storing fails or the process
     * is killed.
     */
    private void store() throws IOException, GeneralSecurityException {
        Path tempFile = createTempFile(path.toAbsolutePath().getParent());
        try {
            try (var channel = FileChannel.open(tempFile, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                 var outputStream = Channels.newOutputStream(channel)) {
                keyStore.store(outputStream, secret);
                channel.force(true);
            }

            try {
                Files.move(tempFile, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempFile, path, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    public String get(String key) {
        Objects.requireNonNull(key, Required.KEY);
        if (keyStore == null) {
            return null;
        }
        String prefixed = prefix + key;

        try {
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry)
                    keyStore.getEntry(prefixed, new KeyStore.PasswordProtection(secret));
            if (entry != null) {
                byte[] raw = entry.getSecretKey().getEncoded();
                return new String(raw, StandardCharsets.UTF_8);
            }
            entry = (KeyStore.SecretKeyEntry) keyStore.getEntry(key, new KeyStore.PasswordProtection(secret));
            if (entry != null) {
                byte[] raw = entry.getSecretKey().getEncoded();
                return new String(raw, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            LOG.error("Failed to get environment value for key: {} or default value for key {}", key, prefixed, e);
        }

        return null;
    }

    /**
     * Stores a value in the vault. The vault file is replaced atomically, if storing fails
     * the vault file and the in-memory vault keep their previous state.
     *
     * @param key The key of the value
     * @param value The value, must not be blank
     *
     * @throws IllegalArgumentException if key or value is blank
     * @throws IllegalStateException if the vault is not enabled or the value could not be stored
     */
    public void put(String key, String value) {
        Argument.requireNonBlank(key, Required.KEY);
        Argument.requireNonBlank(value, Required.VALUE);
        if (keyStore == null) {
            throw new IllegalStateException("Vault is not enabled");
        }

        String alias = prefix + key;
        var protection = new KeyStore.PasswordProtection(secret);
        KeyStore.Entry previous = null;
        try {
            previous = keyStore.getEntry(alias, protection);
            keyStore.setEntry(alias, secretKeyEntry(value), protection);
            store();
        } catch (IOException | GeneralSecurityException e) {
            restore(alias, previous, protection);
            throw new IllegalStateException("Failed to store key '" + key + "' in vault", e);
        }
    }

    private void restore(String alias, KeyStore.Entry previous, KeyStore.PasswordProtection protection) {
        try {
            if (previous == null) {
                if (keyStore.containsAlias(alias)) {
                    keyStore.deleteEntry(alias);
                }
            } else {
                keyStore.setEntry(alias, previous, protection);
            }
        } catch (KeyStoreException e) {
            LOG.error("Failed to restore previous value of key '{}' in vault", alias, e);
        }
    }

    /**
     * Creates the temporary file for storing the vault. On file systems with POSIX
     * permissions the file is readable and writable by the owner only from the moment it
     * is created. Other file systems, e.g. NTFS, do not support POSIX permissions, the file
     * then inherits the access rights of its directory.
     */
    private static Path createTempFile(Path directory) throws IOException {
        if (Files.getFileStore(directory).supportsFileAttributeView(PosixFileAttributeView.class)) {
            return Files.createTempFile(directory, ".vault", ".tmp",
                    PosixFilePermissions.asFileAttribute(EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE)));
        }

        return Files.createTempFile(directory, ".vault", ".tmp");
    }

    Path getPath() {
        return path;
    }

    public SSLContext getSSLContext(String alias) {
          try {
            var key = keyStore.getKey(alias, secret);
            Certificate[] chain = keyStore.getCertificateChain(alias);

            var tempKeyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            tempKeyStore.load(null, null);
            tempKeyStore.setKeyEntry(alias, key, secret, chain);

            var keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(tempKeyStore, secret);

            var sslContext = SSLContext.getInstance("TLSv1.3");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
            return sslContext;
        } catch (IllegalStateException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException |
                 IOException | CertificateException | KeyManagementException e) {
              LOG.error("Failed to create SSLContext", e);
        }

          return null;
    }

    /**
     * Creates a self-signed certificate if it does not exist yet
     *
     * @return True if the keystore has been changed and needs to be stored, false otherwise
     */
    private boolean createCertificate() {
        String alias = Optional
                .ofNullable(config.get(Key.CONNECTOR_HTTPS_CERTIFICATE_ALIAS))
                .orElse(Default.CONNECTOR_HTTPS_CERTIFICATE_ALIAS);

        if (!exists(alias)) {
            try {
                var keyPairGen = KeyPairGenerator.getInstance("RSA", "BC");
                keyPairGen.initialize(2048, new SecureRandom());
                var keyPair = keyPairGen.generateKeyPair();

                var dnName = new X500Name("CN=localhost");
                var certSerialNumber = BigInteger.valueOf(System.currentTimeMillis());
                var startDate = new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24);
                var endDate = new Date(System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)); // 1 year validity

                var certBuilder = new JcaX509v3CertificateBuilder(
                        dnName,
                        certSerialNumber,
                        startDate,
                        endDate,
                        dnName,
                        keyPair.getPublic()
                );

                certBuilder.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));

                var contentSigner = new JcaContentSignerBuilder("SHA256withRSA")
                        .setProvider("BC")
                        .build(keyPair.getPrivate());

                X509CertificateHolder certHolder = certBuilder.build(contentSigner);

                X509Certificate certificate = new JcaX509CertificateConverter()
                        .setProvider("BC")
                        .getCertificate(certHolder);

                keyStore.setKeyEntry(alias, keyPair.getPrivate(), secret, new X509Certificate[]{certificate});
                return true;
            } catch (CertIOException | OperatorCreationException | CertificateException | KeyStoreException |
                     NoSuchAlgorithmException | NoSuchProviderException e) {
                LOG.error("Failed to create certificate", e);
            }
        }

        return false;
    }
}
