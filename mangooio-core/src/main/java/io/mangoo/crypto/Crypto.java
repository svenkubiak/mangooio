package io.mangoo.crypto;

import java.util.Base64;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import com.google.common.base.Charsets;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Key;

/**
 * Convenient class for encryption and decryption
 *
 * @author svenkubiak
 *
 */
public class Crypto {
    private static final Logger LOG = LogManager.getLogger(Crypto.class);
    private static final Config CONFIG = Application.getConfig();
    private static final int KEYINDEX_START = 0;
    private static final int BLOCK_SIZE = 16;
    private static final int KEYLENGTH_16 = 16;
    private static final int KEYLENGTH_24 = 24;
    private static final int KEYLENGTH_32 = 32;
    private static final Base64.Encoder base64Encoder = Base64.getEncoder();
    private static final Base64.Decoder base64Decoder = Base64.getDecoder();
    private final PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESFastEngine()));
    
    /**
     * Decrypts an given encrypted text using the application secret property (application.secret) as key
     *
     * @param encrytedText The encrypted text
     * @return The clear text or null if decryption fails
     */
    public String decrypt(String encrytedText) {
        Objects.requireNonNull(encrytedText, "encrytedText can not be null");

        return decrypt(encrytedText, getSizedKey(CONFIG.getString(Key.APPLICATION_SECRET)));
    }

    /**
     * Decrypts an given encrypted text using the given key
     *
     * @param encrytedText The encrypted text
     * @param key The encryption key
     * @return The clear text or null if decryption fails
     */
    public String decrypt(String encrytedText, String key) {
        Objects.requireNonNull(encrytedText, "encrytedText can not be null");
        Objects.requireNonNull(key, "key can not be null");

        CipherParameters cipherParameters = new ParametersWithIV(new KeyParameter(getSizedKey(key).getBytes(Charsets.UTF_8)), new byte[BLOCK_SIZE]);
        this.cipher.init(false, cipherParameters);
        
        return new String(cipherData(base64Decoder.decode(encrytedText)), Charsets.UTF_8);
    }

    /**
     * Encrypts a given plain text using the application secret property (application.secret) as key
     *
     * Encryption is done by using AES and CBC Cipher and a key length of 128/192/256 bit depending on
     * the size of the application.secret property length (16/24/32 characters)
     *
     * @param plainText The plain text to encrypt
     * @return The encrypted text or null if encryption fails
     */
    public String encrypt(String plainText) {
        Objects.requireNonNull(plainText, "plainText can not be null");

        return encrypt(plainText, getSizedKey(CONFIG.getApplicationSecret()));
    }

    /**
     * Encrypts a given plain text using the given key
     *
     * Encryption is done by using AES and CBC Cipher and a key length of 128/192/256 bit depending on
     * the size of the application.secret property length (16/24/32 characters)
     *
     * @param plainText The plain text to encrypt
     * @param key The key to use for encryption
     * @return The encrypted text or null if encryption fails
     */
    public String encrypt(String plainText, String key) {
        Objects.requireNonNull(plainText, "plainText can not be null");
        Objects.requireNonNull(key, "key can not be null");

        CipherParameters cipherParameters = new ParametersWithIV(new KeyParameter(getSizedKey(key).getBytes(Charsets.UTF_8)), new byte[BLOCK_SIZE]);
        this.cipher.init(true, cipherParameters);

        return new String(base64Encoder.encode(cipherData(plainText.getBytes(Charsets.UTF_8))), Charsets.UTF_8);
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
            final byte[] buffer = new byte[this.cipher.getOutputSize(data.length)];

            final int processedBytes = this.cipher.processBytes(data, 0, data.length, buffer, 0);
            final int finalBytes = this.cipher.doFinal(buffer, processedBytes);

            result = new byte[processedBytes + finalBytes];
            System.arraycopy(buffer, 0, result, 0, result.length);
        } catch (final CryptoException e) {
            LOG.error("Failed to encrypt/decrypt", e);
        }

        return result;
    }

    /**
     * Creates a secret for encrypt or decryption which has a length
     * of 16, 24 or 32 characters, corresponding to 128, 192 or 256 Bits
     *
     * @param secret A given secret to trim
     * @return A secret with 16, 24 or 32 characters
     */
    private String getSizedKey(String secret) {
        Objects.requireNonNull(secret, "secret can not be null");
        secret = secret.replaceAll("[^\\x00-\\x7F]", "");
        
        String key = "";
        if (secret.length() >= KEYLENGTH_32) {
            key = secret.substring(KEYINDEX_START, KEYLENGTH_32);
        } else if (secret.length() >= KEYLENGTH_24) {
            key = secret.substring(KEYINDEX_START, KEYLENGTH_24);
        } else if (secret.length() >= KEYLENGTH_16) {
            key = secret.substring(KEYINDEX_START, KEYLENGTH_16);
        }

        return key;
    }
}