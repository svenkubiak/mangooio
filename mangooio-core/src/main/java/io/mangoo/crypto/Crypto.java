package io.mangoo.crypto;

import io.mangoo.constants.NotNull;
import io.mangoo.exceptions.MangooEncryptionException;
import io.mangoo.utils.CodecUtils;
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

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
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
    private static final int KEY_LENGTH = 3072;
    private static final int KEY_INDEX_START = 0;
    private static final int MAX_KEY_LENGTH = 32;
    private final PaddedBufferedBlockCipher paddedBufferedBlockCipher = new PaddedBufferedBlockCipher(CBCBlockCipher.newInstance(new AESLightEngine()));

    public Crypto() {
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Decrypts a given encrypted text using the given key
     *
     * @param encryptedText The encrypted text
     * @param key The encryption key
     * @return The clear text or null if decryption fails
     */
    public String decrypt(String encryptedText, String key) {
        Objects.requireNonNull(encryptedText, NotNull.ENCRYPTED_TEXT);
        Objects.requireNonNull(key, NotNull.KEY);

        CipherParameters cipherParameters = new ParametersWithRandom(new KeyParameter(getSizedSecret(key).getBytes(StandardCharsets.UTF_8)));
        paddedBufferedBlockCipher.init(false, cipherParameters);

        return new String(cipherData(CodecUtils.decodeFromBase64(encryptedText)), StandardCharsets.UTF_8);
    }

    /**
     * Encrypts a given plain text using the given key
     * <p>
     * Encryption is done by using AES and CBC Cipher and a key length of 256 bit
     *
     * @param plainText The plain text to encrypt
     * @param key The key to use for encryption
     * @return The encrypted text or null if encryption fails
     */
    public String encrypt(String plainText, String key) {
        Objects.requireNonNull(plainText, NotNull.PLAIN_TEXT);
        Objects.requireNonNull(key, NotNull.KEY);

        CipherParameters cipherParameters = new ParametersWithRandom(new KeyParameter(getSizedSecret(key).getBytes(StandardCharsets.UTF_8)));
        paddedBufferedBlockCipher.init(true, cipherParameters);
        
        return new String(CodecUtils.encodeToBase64(cipherData(plainText.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
    }

    /**
     * Encrypts or decrypts a given byte array of data
     *
     * @param data The data to encrypt or decrypt
     * @return A clear text or encrypted byte array
     */
    private byte[] cipherData(byte[] data) {
        byte[] result = null;
        try {
            final var buffer = new byte[paddedBufferedBlockCipher.getOutputSize(data.length)];

            final int processedBytes = paddedBufferedBlockCipher.processBytes(data, 0, data.length, buffer, 0);
            final int finalBytes = paddedBufferedBlockCipher.doFinal(buffer, processedBytes);

            result = new byte[processedBytes + finalBytes];
            System.arraycopy(buffer, 0, result, 0, result.length);
        } catch (final CryptoException e) {
            LOG.error("Failed to encrypt/decrypt data array", e);
        }

        return result;
    }

    public String getSizedSecret(String secret) {
        Objects.requireNonNull(secret, NotNull.SECRET);
        
        String key = RegExUtils.replaceAll(secret, "[^\\x00-\\x7F]", "");
        return key.length() < MAX_KEY_LENGTH ? key : key.substring(KEY_INDEX_START, MAX_KEY_LENGTH);
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
        Objects.requireNonNull(text, NotNull.PLAIN_TEXT);
        Objects.requireNonNull(text, NotNull.PUBLIC_KEY);
        
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
        Objects.requireNonNull(text, NotNull.PLAIN_TEXT);
        Objects.requireNonNull(text, NotNull.PUBLIC_KEY);
        
        var encrypt = "";
        try {
            byte[] cipherText = encrypt(text.getBytes(StandardCharsets.UTF_8), key);
            encrypt = new String(CodecUtils.encodeToBase64(cipherText), StandardCharsets.UTF_8);
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
        Objects.requireNonNull(text, NotNull.ENCRYPTED_TEXT);
        Objects.requireNonNull(text, NotNull.PRIVATE_KEY);

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
        Objects.requireNonNull(text, NotNull.ENCRYPTED_TEXT);
        Objects.requireNonNull(text, NotNull.PRIVATE_KEY);
        
        var decrypt = "";
        try {
            byte[] decryptText = decrypt(CodecUtils.decodeFromBase64(text), key);
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
        Objects.requireNonNull(key, NotNull.KEY);
        
        return new String(CodecUtils.encodeToBase64(key.getEncoded()), StandardCharsets.UTF_8);
    }

    /**
     * Generates Private Key from Base64 encoded string
     * 
     * @param key Base64 encoded string which represents the key

     * @return The PrivateKey
     * @throws MangooEncryptionException if getting private key from string fails
     */
    public PrivateKey getPrivateKeyFromString(String key) throws MangooEncryptionException {
        Objects.requireNonNull(key, NotNull.KEY);
        
        try {
            return KeyFactory.getInstance(ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(CodecUtils.decodeFromBase64(key)));
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
        Objects.requireNonNull(key, NotNull.KEY);
        
        try {
            return KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(CodecUtils.decodeFromBase64(key)));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new MangooEncryptionException("Failed to get public key from string", e);
        }
    }
}