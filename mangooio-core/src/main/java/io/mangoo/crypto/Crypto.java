package io.mangoo.crypto;

import java.util.Base64;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.engines.AESLightEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithRandom;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Key;
import io.mangoo.enums.Required;

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
    private static final int KEYLENGTH_32 = 32;
    private static final Base64.Encoder base64Encoder = Base64.getEncoder();
    private static final Base64.Decoder base64Decoder = Base64.getDecoder();
    private final PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESLightEngine()));
    
    /**
     * Decrypts an given encrypted text using the application secret property (application.secret) as key
     *
     * @param encrytedText The encrypted text
     * @return The clear text or null if decryption fails
     */
    public String decrypt(String encrytedText) {
        Objects.requireNonNull(encrytedText, Required.ENCRYPTED_TEXT.toString());

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
        Objects.requireNonNull(encrytedText, Required.ENCRYPTED_TEXT.toString());
        Objects.requireNonNull(key, Required.KEY.toString());

        CipherParameters cipherParameters = new ParametersWithRandom(new KeyParameter(getSizedKey(key).getBytes(Charsets.UTF_8)));
        this.cipher.init(false, cipherParameters);
        
        return new String(cipherData(base64Decoder.decode(encrytedText)), Charsets.UTF_8);
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

        return encrypt(plainText, getSizedKey(CONFIG.getApplicationSecret()));
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
    public String encrypt(String plainText, String key) {
        Objects.requireNonNull(plainText, Required.PLAIN_TEXT.toString());
        Objects.requireNonNull(key, Required.KEY.toString());

        CipherParameters cipherParameters = new ParametersWithRandom(new KeyParameter(getSizedKey(key).getBytes(Charsets.UTF_8)));
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
     * of 32 characters, corresponding to 256 Bits
     * 
     * If the provided secret has more than 32 characters it will be trimmed
     * to 32 characters
     *
     * @param secret A given secret to trim
     * @return A secret with at least 32 characters
     */
    private String getSizedKey(String secret) {
        Objects.requireNonNull(secret, Required.SECRET.toString());
        String key = secret.replaceAll("[^\\x00-\\x7F]", "");

        Preconditions.checkArgument(key.length() >= KEYLENGTH_32, "encryption key must be at least 32 characters");
        
        return key.substring(KEYINDEX_START, KEYLENGTH_32);
    }
}
