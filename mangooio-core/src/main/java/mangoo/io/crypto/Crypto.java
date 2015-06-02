package mangoo.io.crypto;

import mangoo.io.configuration.Config;
import mangoo.io.enums.Key;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Convenient class for encryption and decryption
 *
 * @author svenkubiak
 *
 */
@Singleton
public class Crypto {
    private static final Logger LOG = LoggerFactory.getLogger(Crypto.class);
    private static final int KEYINDEX_START = 0;
    private static final int KEYLENGTH_16 = 16;
    private static final int KEYLENGTH_24 = 24;
    private static final int KEYLENGTH_32 = 32;
    private PaddedBufferedBlockCipher cipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(new AESEngine()));
    private CipherParameters cipherParameters;
    private Config config;

    @Inject
    public Crypto(Config config) {
        this.config = config;
    }

    /**
     * Decrypts an given encrypted text using the application secret property (application.secret) as key
     * 
     * @param encrytedText The encrypted text
     * @return The clear text or null if decryption failed
     */
    public String decrypt(String encrytedText) {
        return decrypt(encrytedText, getSizedKey(this.config.getString(Key.APPLICATION_SECRET)));
    }

    /**
     * Decrypts an given encrypted text using the given key 
     * 
     * @param encrytedText The encrypted text
     * @param key The encryption key
     * @return The clear text or null if decryption failed
     */
    public String decrypt(String encrytedText, String key) {
        this.cipherParameters = new ParametersWithIV(new KeyParameter(getSizedKey(key).getBytes(Charsets.UTF_8)), new byte[KEYLENGTH_16]);

        String plainText = null;
        this.cipher.init(false, this.cipherParameters);
        plainText = new String(cipherData(Base64.decode(encrytedText)), Charsets.UTF_8);

        return plainText;
    }

    /**
     * Encrypts a given plain text using the application secret property (application.secret) as key
     * 
     * Encryption is done by using AES and CBC Cipher and a key length of 128/192/256 bit depending on
     * the size of the application.secret property length (16/24/32 characters)
     * 
     * @param plainText The plain text to encrypt
     * @return The encrypted text or null if encryption failed
     */
    public String encrypt(String plainText) {
        return encrypt(plainText, getSizedKey(this.config.getString(Key.APPLICATION_SECRET)));
    }
    
    /**
     * Encrypts a given plain text using the given key
     * 
     * Encryption is done by using AES and CBC Cipher and a key length of 128/192/256 bit depending on
     * the size of the application.secret property length (16/24/32 characters)
     * 
     * @param plainText The plain text to encrypt
     * @return The encrypted text or null if encryption failed
     */
    public String encrypt(String plainText, String key) {
        this.cipherParameters = new ParametersWithIV(new KeyParameter(getSizedKey(key).getBytes(Charsets.UTF_8)), new byte[KEYLENGTH_16]);

        String encrytedText = null;
        this.cipher.init(true, this.cipherParameters);
        encrytedText = new String(Base64.encode(cipherData(plainText.getBytes(Charsets.UTF_8))), Charsets.UTF_8);

        return encrytedText;
    }

    private byte[] cipherData(byte[] data) {
        byte[] result = null;
        try {
            byte[] buffer = new byte[this.cipher.getOutputSize(data.length)];

            int processedBytes = this.cipher.processBytes(data, 0, data.length, buffer, 0);
            int finalBytes = this.cipher.doFinal(buffer, processedBytes);

            result = new byte[processedBytes + finalBytes];
            System.arraycopy(buffer, 0, result, 0, result.length);
        } catch (CryptoException e) {
            LOG.error("Failed to encrypt/decrypt", e);
        }

        return result;
    }

    private String getSizedKey(String secret) {
        String key = null;
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