package io.mangoo.crypto;

import static io.mangoo.test.hamcrest.RegexMatcher.matches;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.llorllale.cactoos.matchers.RunsInThreads;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.utils.MangooUtils;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
@SuppressWarnings("unchecked")
class CryptoTest {
    private static final String base64Pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
    private static final String plainText = "This is a super secret message!";
    private static final String key33 = "123456789012345678901234567890123";
    private static final String key32 = "12345678901234567890123456789012";
    private static final String key31 = "1234567890123456789012345678901";
    
    @Test
    void testEncryption() {
        //when
        String encrypt = Application.getInstance(Crypto.class).encrypt(plainText);

        //then
        assertThat(encrypt, not(nullValue()));
        assertThat(encrypt, matches(base64Pattern));
        assertThat(encrypt, not(equalTo(plainText)));
    }
    
    @Test
    void testEncryptionConcurrent() {
        MatcherAssert.assertThat(t -> {
            //given
            String text = MangooUtils.randomString(32);
            
            //when
            String encrypt = Application.getInstance(Crypto.class).encrypt(text);
            String decrypt = Application.getInstance(Crypto.class).decrypt(encrypt);
           
            //when
            return decrypt.equals(text);
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    void testLongKey() {
        //when
        String encrypt = Application.getInstance(Crypto.class).encrypt(plainText, key33);

        //then
        assertThat(encrypt, not(nullValue()));
        assertThat(encrypt, matches(base64Pattern));
        assertThat(encrypt, not(equalTo(plainText)));
    }
    
    @Test
    void testShortKey() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            Application.getInstance(Crypto.class).encrypt(plainText, key31);
        }, "Failed to add a key that is short than required");
    }
    
    @Test
    void testEncryptionWithKey() {
        //when
        String encrypt = Application.getInstance(Crypto.class).encrypt(plainText, key32);
        
        //then
        assertThat(encrypt, not(nullValue()));
        assertThat(encrypt, matches(base64Pattern));
        assertThat(encrypt, not(equalTo(plainText)));
    }
    
    @Test
    void testDecryption() {
        //given
        String encrypt = Application.getInstance(Crypto.class).encrypt(plainText);
        
        //when
        String decrypt = Application.getInstance(Crypto.class).decrypt(encrypt);

        //then
        assertThat(decrypt, not(nullValue()));
        assertThat(decrypt, equalTo(plainText));
    }
    
    @Test
    void testDecryptionWithKey() {
        //given
        String encrypt = Application.getInstance(Crypto.class).encrypt(plainText, key32);

        //when
        String decrypt = Application.getInstance(Crypto.class).decrypt(encrypt, key32);

        //then
        assertThat(decrypt, not(nullValue()));
        assertThat(decrypt, equalTo(plainText));
    }
    
    @Test
    void testGenerateKeyPair() throws NoSuchAlgorithmException {
        //given
        Crypto crypto = Application.getInstance(Crypto.class);

        //when
        KeyPair keyPair = crypto.generateKeyPair();

        //then
        assertThat(keyPair.getPrivate(), not(nullValue()));
        assertThat(keyPair.getPublic(), not(nullValue()));
    }
    
    @Test
    void testEncryptWithPublicAndPrivateKey() throws Exception {
        //given
        Crypto crypto = Application.getInstance(Crypto.class);

        //when
        KeyPair keyPair = crypto.generateKeyPair();
        String encryptedText = crypto.encrypt(plainText, keyPair.getPublic());
        
        //then
        assertThat(encryptedText, not(nullValue()));
        assertThat(encryptedText, not(equalTo(plainText)));
    }
    
    @Test
    void testDecryptWithPublicAndPrivateKey() throws Exception {
        //given
        Crypto crypto = Application.getInstance(Crypto.class);

        //when
        KeyPair keyPair = crypto.generateKeyPair();
        String encryptedText = crypto.encrypt(plainText, keyPair.getPublic());
        
        //then
        assertThat(encryptedText, not(nullValue()));
        assertThat(encryptedText, not(equalTo(plainText)));
        
        //when
        String decryptedText = crypto.decrypt(encryptedText, keyPair.getPrivate());
        
        //then
        assertThat(decryptedText, not(nullValue()));
        assertThat(decryptedText, equalTo(plainText));
    }
    
    @Test
    void testGetKeyAsString() throws Exception {
        //given
        Crypto crypto = Application.getInstance(Crypto.class);

        //when
        KeyPair keyPair = crypto.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();
        PublicKey publicKey = keyPair.getPublic();
        
        //then
        assertThat(privateKey, not(nullValue()));
        assertThat(publicKey, not(nullValue()));
        assertThat(crypto.getKeyAsString(publicKey), not(nullValue()));
        assertThat(crypto.getKeyAsString(privateKey), not(nullValue()));
    }
    
    @Test
    void testGetPublicKeyFromString() throws Exception {
        //given
        Crypto crypto = Application.getInstance(Crypto.class);

        //when
        KeyPair keyPair = crypto.generateKeyPair();
        String privateKey = crypto.getKeyAsString(keyPair.getPrivate());
        String publicKey = crypto.getKeyAsString(keyPair.getPublic());
        
        //then
        assertThat(privateKey, not(nullValue()));
        assertThat(publicKey, not(nullValue()));
        assertThat(crypto.getPublicKeyFromString(publicKey), not(nullValue()));
        assertThat(crypto.getPrivateKeyFromString(privateKey), not(nullValue()));
    }
}