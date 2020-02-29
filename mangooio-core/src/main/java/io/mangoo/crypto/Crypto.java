package io.mangoo.crypto;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Objects;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.lang3.RegExUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.engines.AESLightEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.google.inject.Inject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.core.Config;
import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooEncryptionException;

/**
 * Convenient class for encryption and decryption
 *
 * @author svenkubiak
 *
 */
@SuppressFBWarnings(value = "java:S5542", justification = "Compliant is SHA-256, SHA-512 is used")
public class Crypto {
    private static final Logger LOG = LogManager.getLogger(Crypto.class);
    private final PaddedBufferedBlockCipher paddedBufferedBlockCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESLightEngine()));
    private static final Base64.Encoder base64Encoder = Base64.getEncoder();
    private static final Base64.Decoder base64Decoder = Base64.getDecoder();
    private static final String TRANSFORMATION = "RSA/None/OAEPWITHSHA-512ANDMGF1PADDING";
    private static final String ALGORITHM = "RSA";
    private static final int KEYLENGTH = 2048;
    private static final int KEYINDEX_START = 0;
    private static final int MAX_KEY_LENGTH = 32;
    private Config config;
    
    @Inject
    public Crypto(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
        Security.addProvider(new BouncyCastleProvider());
    }
    
    /**
     * Decrypts an given encrypted text using the application secret property (application.secret) as key
     *
     * @param encrytedText The encrypted text
     * @return The clear text or null if decryption fails
     */
    public String decrypt(String encrytedText) {
        Objects.requireNonNull(encrytedText, Required.ENCRYPTED_TEXT.toString());
        return decrypt(encrytedText, getSizedSecret(this.config.getApplicationSecret()));
    }

    /**
     * Decrypts an given encrypted text using the given key
     *
     * @param encrytedText The encrypted text
     * @param key The encryption key
     * @return The clear text or null if decryption fails
     */
    public String decrypt(String encrytedText, String key) {
        Objects.requireNonNull(encrytedText, Required.ENCRYPTED_TEXT.toString());
        Objects.requireNonNull(key, Required.KEY.toString());

        CipherParameters cipherParameters = new ParametersWithRandom(new KeyParameter(getSizedSecret(key).getBytes(StandardCharsets.UTF_8)));
        this.paddedBufferedBlockCipher.init(false, cipherParameters);
        
        return new String(cipherData(base64Decoder.decode(encrytedText)), StandardCharsets.UTF_8);
    }

    /**
     * Encrypts a given plain text using the application secret property (application.secret) as key
     *
     * Encryption is done by using AES and CBC Cipher and a key length of 256 bit
     *
     * @param plainText The plain text to encrypt
     * @return The encrypted text or null if encryption fails
     */
    public String encrypt(String plainText) {
        Objects.requireNonNull(plainText, Required.PLAIN_TEXT.toString());

        return encrypt(plainText, getSizedSecret(this.config.getApplicationSecret()));
    }

    /**
     * Encrypts a given plain text using the given key
     *
     * Encryption is done by using AES and CBC Cipher and a key length of 256 bit
     *
     * @param plainText The plain text to encrypt
     * @param key The key to use for encryption
     * @return The encrypted text or null if encryption fails
     */
    public String encrypt(final String plainText, final String key) {
        Objects.requireNonNull(plainText, Required.PLAIN_TEXT.toString());
        Objects.requireNonNull(key, Required.KEY.toString());

        CipherParameters cipherParameters = new ParametersWithRandom(new KeyParameter(getSizedSecret(key).getBytes(StandardCharsets.UTF_8)));
        this.paddedBufferedBlockCipher.init(true, cipherParameters);
        
        return new String(base64Encoder.encode(cipherData(plainText.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
    }

    /**
     * Encrypts or decrypts a given byte array of data
     *
     * @param data The data to encrypt or decrypt
     * @return A clear text or encrypted byte array
     */
    private byte[] cipherData(final byte[] data) {
        byte[] result = null;
        try {
            final byte[] buffer = new byte[this.paddedBufferedBlockCipher.getOutputSize(data.length)];

            final int processedBytes = this.paddedBufferedBlockCipher.processBytes(data, 0, data.length, buffer, 0);
            final int finalBytes = this.paddedBufferedBlockCipher.doFinal(buffer, processedBytes);

            result = new byte[processedBytes + finalBytes];
            System.arraycopy(buffer, 0, result, 0, result.length);
        } catch (final CryptoException e) {
            LOG.error("Failed to encrypt/decrypt data array", e);
        }

        return result;
    }

    public String getSizedSecret(String secret) {
        Objects.requireNonNull(secret, Required.SECRET.toString());
        
        String key = RegExUtils.replaceAll(secret, "[^\\x00-\\x7F]", "");
        return key.length() < MAX_KEY_LENGTH ? key : key.substring(KEYINDEX_START, MAX_KEY_LENGTH);
    }
    
    /**
     * Generate key which contains a pair of private and public key using 4096 bytes
     * 
     * @return key pair 
     */
    public KeyPair generateKeyPair() {
        KeyPair keyPair = null;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
            keyPairGenerator.initialize(KEYLENGTH);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            LOG.error("Failed to create publi/private key pair", e);
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
        Objects.requireNonNull(text, Required.PLAIN_TEXT.toString());
        Objects.requireNonNull(text, Required.PUBLIC_KEY.toString());
        
        byte[] encrypt = null;
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
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
        Objects.requireNonNull(text, Required.PLAIN_TEXT.toString());
        Objects.requireNonNull(text, Required.PUBLIC_KEY.toString());
        
        String encrypt = null;
        try {
            byte[] cipherText = encrypt(text.getBytes(StandardCharsets.UTF_8), key);
            encrypt = encodeBase64(cipherText);
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
        Objects.requireNonNull(text, Required.ENCRYPTED_TEXT.toString());
        Objects.requireNonNull(text, Required.PRIVATE_KEY.toString());

        byte[] decrypt = null;
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
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
        Objects.requireNonNull(text, Required.ENCRYPTED_TEXT.toString());
        Objects.requireNonNull(text, Required.PRIVATE_KEY.toString());
        
        String decrypt = null;
        try {
            byte[] dectyptedText = decrypt(decodeBase64(text), key);
            decrypt = new String(dectyptedText, StandardCharsets.UTF_8);
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
        Objects.requireNonNull(key, Required.KEY.toString());
        
        return encodeBase64(key.getEncoded());
    }

    /**
     * Generates Private Key from Base64 encoded string
     * 
     * @param key Base64 encoded string which represents the key

     * @return The PrivateKey
     * @throws MangooEncryptionException if getting private key from string fails
     */
    public PrivateKey getPrivateKeyFromString(String key) throws MangooEncryptionException {
        Objects.requireNonNull(key, Required.KEY.toString());
        
        try {
            return KeyFactory.getInstance(ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(decodeBase64(key)));
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
        Objects.requireNonNull(key, Required.KEY.toString());
        
        try {
            return KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(decodeBase64(key)));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new MangooEncryptionException("Failed to get pulbic key from string", e);
        }
    }

    /**
     * Encode bytes array to Base64 string
     * 
     * @param bytes The bytes
     * @return Encoded string
     */
    private String encodeBase64(byte[] bytes) {
        Objects.requireNonNull(bytes, Required.BYTES.toString());
        return org.apache.commons.codec.binary.Base64.encodeBase64String(bytes);
    }

    /**
     * Decode Base64 encoded string to bytes array
     * 
     * @param text The string
     * @return Bytes array
     */
    private byte[] decodeBase64(String text) {
        Objects.requireNonNull(text, Required.PLAIN_TEXT.toString());
        return org.apache.commons.codec.binary.Base64.decodeBase64(text);
    }
}