package io.mangoo.crypto;

import com.google.common.base.Preconditions;
import io.mangoo.constants.Required;
import io.mangoo.exceptions.MangooEncryptionException;
import io.mangoo.utils.CommonUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;

public class Crypto {
    private static final Logger LOG = LogManager.getLogger(Crypto.class);
    private static final String TRANSFORMATION = "RSA/None/OAEPWITHSHA-512ANDMGF1PADDING";
    private static final String ALGORITHM = "RSA";
    private static final String AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding";
    private static final String KEY_DERIVATION_ALGORITHM = "SHA-256";
    private static final int KEY_LENGTH = 3072;
    private static final int MIN_KEY_LENGTH = 32;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    public Crypto() {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Decrypts a given Base64 encoded, AES-256-GCM encrypted text using the given key.
     * <p>
     * The random IV is expected to be prepended to the ciphertext. Decryption fails
     * (returns null) if the authentication tag does not verify, i.e. the ciphertext
     * has been tampered with.
     *
     * @param encryptedText The encrypted text
     * @param key The encryption key
     * @return The clear text or null if decryption fails
     */
    public String decrypt(String encryptedText, String key) {
        Objects.requireNonNull(encryptedText, Required.ENCRYPTED_TEXT);
        Objects.requireNonNull(key, Required.KEY);

        try {
            byte[] combined = CommonUtils.decodeFromBase64(encryptedText);
            if (combined.length <= GCM_IV_LENGTH) {
                throw new IllegalArgumentException("Invalid encrypted payload");
            }

            var iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);

            var cipherText = new byte[combined.length - GCM_IV_LENGTH];
            System.arraycopy(combined, GCM_IV_LENGTH, cipherText, 0, cipherText.length);

            var cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, deriveKey(key), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));

            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            LOG.error("Failed to decrypt data", e);
        }

        return null;
    }

    /**
     * Encrypts a given plain text using the given key.
     * <p>
     * Encryption is done using AES-256 in GCM mode (authenticated encryption). A fresh
     * random 96-bit IV is generated for every call and prepended to the ciphertext, so
     * encrypting the same plain text twice yields different results.
     *
     * @param plainText The plain text to encrypt
     * @param key The key to use for encryption
     * @return The encrypted text (Base64 encoded) or null if encryption fails
     */
    public String encrypt(String plainText, String key) {
        Objects.requireNonNull(plainText, Required.PLAIN_TEXT);
        Objects.requireNonNull(key, Required.KEY);

        try {
            var iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);

            var cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, deriveKey(key), new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            var combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

            return new String(CommonUtils.encodeToBase64(combined), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) {
            LOG.error("Failed to encrypt data", e);
        }

        return null;
    }

    /**
     * Derives a 256-bit AES key from the given secret.
     * <p>
     * The secret must be at least {@value #MIN_KEY_LENGTH} characters long. Instead of
     * silently truncating the secret, the full (ASCII) secret is hashed with SHA-256 so
     * that all of its entropy contributes to the key.
     *
     * @param secret The secret to derive the key from
     * @return A 256-bit AES {@link SecretKey}
     */
    private SecretKey deriveKey(String secret) {
        Objects.requireNonNull(secret, Required.SECRET);

        String sanitized = RegExUtils.replaceAll(secret, "[^\\x00-\\x7F]", "");
        Preconditions.checkArgument(sanitized.length() >= MIN_KEY_LENGTH,
                "Encryption key must be at least " + MIN_KEY_LENGTH + " characters");

        try {
            var digest = MessageDigest.getInstance(KEY_DERIVATION_ALGORITHM);
            byte[] keyBytes = digest.digest(sanitized.getBytes(StandardCharsets.UTF_8));
            return new SecretKeySpec(keyBytes, "AES");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to derive encryption key", e);
        }
    }
    
    /**
     * Generate key which contains a pair of private and public key using 4096 bytes
     * 
     * @return key pair 
     */
    public KeyPair generateKeyPair() {
        KeyPair keyPair = null;
        try {
            var keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KEY_LENGTH, new SecureRandom());
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Failed to create public/private key pair", e);
        }
        
        return keyPair;
    }
    
    /**
     * Encrypt a text using public key
     * 
     * @param text The plain text
     * @param key The public key
     * 
     * @return Encrypted text
     * @throws MangooEncryptionException if encryption fails
     */
    public byte[] encrypt(byte[] text, PublicKey key) throws MangooEncryptionException {
        Objects.requireNonNull(text, Required.PLAIN_TEXT);
        Objects.requireNonNull(text, Required.PUBLIC_KEY);
        
        byte[] encrypt = null;
        try {
            var cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            encrypt = cipher.doFinal(text);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            throw new MangooEncryptionException("Failed to encrypt clear text with public key", e);
        }

        return encrypt;
    }
    
    /**
     * Encrypt a text using public key. The result is encoded to Base64.
     * 
     * @param text The plain text
     * @param key The public key 
     * 
     * @return Encrypted string as base64
     * @throws MangooEncryptionException if encryption fails
     */
    public String encrypt(String text, PublicKey key) throws MangooEncryptionException {
        Objects.requireNonNull(text, Required.PLAIN_TEXT);
        Objects.requireNonNull(text, Required.PUBLIC_KEY);
        
        var encrypt = "";
        try {
            byte[] cipherText = encrypt(text.getBytes(StandardCharsets.UTF_8), key);
            encrypt = new String(CommonUtils.encodeToBase64(cipherText), StandardCharsets.UTF_8);
        } catch (MangooEncryptionException e) {
            throw new MangooEncryptionException("Failed to encrypt clear text with public key", e);
        }
        
        return encrypt;
    }

    /**
     * Decrypt text using private key
     * 
     * @param text The encrypted text
     * @param key The private key
     * 
     * @return The unencrypted text
     * @throws MangooEncryptionException if decryption fails
     */
    public byte[] decrypt(byte[] text, PrivateKey key) throws MangooEncryptionException {
        Objects.requireNonNull(text, Required.ENCRYPTED_TEXT);
        Objects.requireNonNull(text, Required.PRIVATE_KEY);

        byte[] decrypt = null;
        try {
            var cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key);
            decrypt = cipher.doFinal(text);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            throw new MangooEncryptionException("Failed to decrypt encrypted text with private key", e);
        }
        
        return decrypt;
    }
    
    /**
     * Decrypt Base64 encoded text using private key
     * 
     * @param text The encrypted text, encoded as Base64
     * @param key The private key
     * 
     * @return The plain text encoded as UTF8
     * @throws MangooEncryptionException if decryption fails
     */
    public String decrypt(String text, PrivateKey key) throws MangooEncryptionException {
        Objects.requireNonNull(text, Required.ENCRYPTED_TEXT);
        Objects.requireNonNull(text, Required.PRIVATE_KEY);
        
        var decrypt = "";
        try {
            byte[] decryptText = decrypt(CommonUtils.decodeFromBase64(text), key);
            decrypt = new String(decryptText, StandardCharsets.UTF_8);
        } catch (MangooEncryptionException e) {
            throw new MangooEncryptionException("Failed to decrypt encrypted text with private key", e);
        }

        return decrypt;
    }

    /**
     * Convert a Key to string encoded as Base64
     * 
     * @param key The key (private or public)
     * @return A string representation of the key
     */
    public String getKeyAsString(Key key) {
        Objects.requireNonNull(key, Required.KEY);
        
        return new String(CommonUtils.encodeToBase64(key.getEncoded()), StandardCharsets.UTF_8);
    }

    /**
     * Generates Private Key from Base64 encoded string
     * 
     * @param key Base64 encoded string which represents the key

     * @return The PrivateKey
     * @throws MangooEncryptionException if getting private key from string fails
     */
    public PrivateKey getPrivateKeyFromString(String key) throws MangooEncryptionException {
        Objects.requireNonNull(key, Required.KEY);
        
        try {
            return KeyFactory.getInstance(ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(CommonUtils.decodeFromBase64(key)));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new MangooEncryptionException("Failed to get private key from string", e);
        }
    }

    /**
     * Generates Public Key from Base64 encoded string
     * 
     * @param key Base64 encoded string which represents the key

     * @return The PublicKey
     * @throws MangooEncryptionException if getting public key from string fails
     */
    public PublicKey getPublicKeyFromString(String key) throws MangooEncryptionException {
        Objects.requireNonNull(key, Required.KEY);
        
        try {
            return KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(CommonUtils.decodeFromBase64(key)));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new MangooEncryptionException("Failed to get public key from string", e);
        }
    }
}