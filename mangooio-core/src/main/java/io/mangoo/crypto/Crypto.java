package io.mangoo.crypto;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.encoders.Base64;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.configuration.Config;
import io.mangoo.enums.Key;
import io.mangoo.utils.ConfigUtils;

/**
 * Convenient class for encryption and decryption
 *
 * @author svenkubiak
 *
 */
@Singleton
public class Crypto {
    private static final Logger LOG = LogManager.getLogger(Crypto.class);
    private static final int KEYINDEX_START = 0;
    private static final int KEYLENGTH_16 = 16;
    private static final int KEYLENGTH_24 = 24;
    private static final int KEYLENGTH_32 = 32;
    private final PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
    private CipherParameters cipherParameters;
    private final Config config;

    @Inject
    public Crypto(Config config) {
        Preconditions.checkNotNull(config, "config can not be null");

        this.config = config;
    }

    /**
     * Decrypts an given encrypted text using the application secret property (application.secret) as key
     *
     * @param encrytedText The encrypted text
     * @return The clear text or null if decryption fails
     */
    public String decrypt(String encrytedText) {
        Preconditions.checkNotNull(encrytedText, "encrytedText can not be null");

        return decrypt(encrytedText, getSizedKey(this.config.getString(Key.APPLICATION_SECRET)));
    }

    /**
     * Decrypts an given encrypted text using the given key
     *
     * @param encrytedText The encrypted text
     * @param key The encryption key
     * @return The clear text or null if decryption fails
     */
    public String decrypt(String encrytedText, String key) {
        Preconditions.checkNotNull(encrytedText, "encrytedText can not be null");
        Preconditions.checkNotNull(key, "key can not be null");

        this.cipherParameters = new ParametersWithIV(new KeyParameter(getSizedKey(key).getBytes(Charsets.UTF_8)), new byte[KEYLENGTH_16]);

        this.cipher.init(false, this.cipherParameters);
        final String plainText = new String(cipherData(Base64.decode(encrytedText)), Charsets.UTF_8);

        return plainText;
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
        Preconditions.checkNotNull(plainText, "plainText can not be null");

        return encrypt(plainText, getSizedKey(ConfigUtils.getApplicationSecret()));
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
        Preconditions.checkNotNull(plainText, "plainText can not be null");
        Preconditions.checkNotNull(key, "key can not be null");

        this.cipherParameters = new ParametersWithIV(new KeyParameter(getSizedKey(key).getBytes(Charsets.UTF_8)), new byte[KEYLENGTH_16]);

        this.cipher.init(true, this.cipherParameters);
        final String encrytedText = new String(Base64.encode(cipherData(plainText.getBytes(Charsets.UTF_8))), Charsets.UTF_8);

        return encrytedText;
    }

    /**
     * Encrypts or decrypts a given byte array of data
     *
     * @param data The data to encrypt or decrypt
     * @return A cleartext or encrypted byte array
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
     *
     * @param secret A given secret to trim
     * @return A stirng secret with 16, 24 or 32 characters
     */
    private String getSizedKey(String secret) {
        String key = "";
        if (StringUtils.isNotBlank(secret)) {
            if (secret.length() >= KEYLENGTH_32) {
                key = secret.substring(KEYINDEX_START, KEYLENGTH_32);
            } else if (secret.length() >= KEYLENGTH_24) {
                key = secret.substring(KEYINDEX_START, KEYLENGTH_24);
            } else if (secret.length() >= KEYLENGTH_16) {
                key = secret.substring(KEYINDEX_START, KEYLENGTH_16);
            }
        }

        return key;
    }
}