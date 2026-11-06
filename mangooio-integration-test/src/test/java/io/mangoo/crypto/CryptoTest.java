package io.mangoo.crypto;

import io.mangoo.TestExtension;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import static io.mangoo.test.hamcrest.RegexMatcher.matches;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
@SuppressWarnings("unchecked")
@Execution(ExecutionMode.SAME_THREAD)
class CryptoTest {
    private static final String base64Pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
    private static final String plainText = "This is a super secret message!";
    private static final String key33 = "123456789012345678901234567890123";
    private static final String key32 = "12345678901234567890123456789012";
    private static final String key31 = "1234567890123456789012345678901";

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
    void testGetPublicKeyFromString() throws Exception {
        //given
        Crypto crypto = Application.getInstance(Crypto.class);

        //when
        KeyPair keyPair = crypto.generateKeyPair();
        String privateKey = crypto.getKeyAsString(keyPair.getPrivate());
        String publicKey = crypto.getKeyAsString(keyPair.getPublic());
        
        //then
        assertThat(crypto.getPublicKeyFromString(publicKey), not(nullValue()));
        assertThat(crypto.getPrivateKeyFromString(privateKey), not(nullValue()));
    }

    @Test
    void testTamperedCiphertextIsRejected() {
        //given
        Crypto crypto = Application.getInstance(Crypto.class);
        byte[] payload = Base64.getDecoder().decode(crypto.encrypt(plainText, key32));
        payload[payload.length - 1] ^= 0x01;

        //when
        String decrypt = crypto.decrypt(Base64.getEncoder().encodeToString(payload), key32);

        //then
        assertThat(decrypt, nullValue());
    }

    @Test
    void testTamperedIvIsRejected() {
        //given
        Crypto crypto = Application.getInstance(Crypto.class);
        byte[] payload = Base64.getDecoder().decode(crypto.encrypt(plainText, key32));
        payload[0] ^= 0x01;

        //when
        String decrypt = crypto.decrypt(Base64.getEncoder().encodeToString(payload), key32);

        //then
        assertThat(decrypt, nullValue());
    }

    @Test
    void testWrongKeyIsRejected() {
        //given
        Crypto crypto = Application.getInstance(Crypto.class);
        String encrypt = crypto.encrypt(plainText, key32);

        //when
        String decrypt = crypto.decrypt(encrypt, "abcdefghijklmnopqrstuvwxyz123456");

        //then
        assertThat(decrypt, nullValue());
    }

    @Test
    void testKeyIsNotTruncated() {
        //given
        Crypto crypto = Application.getInstance(Crypto.class);
        String encrypt = crypto.encrypt(plainText, key33);

        //then
        assertThat(crypto.decrypt(encrypt, key33), equalTo(plainText));
        assertThat(crypto.decrypt(encrypt, key32), nullValue());
    }

    @Test
    void testEncryptionUsesARandomIv() {
        //given
        Crypto crypto = Application.getInstance(Crypto.class);

        //when
        String first = crypto.encrypt(plainText, key32);
        String second = crypto.encrypt(plainText, key32);

        //then
        assertThat(first, not(equalTo(second)));
        assertThat(crypto.decrypt(first, key32), equalTo(plainText));
        assertThat(crypto.decrypt(second, key32), equalTo(plainText));
    }

    @Test
    void testPayloadWithoutCiphertextIsRejected() {
        //given
        String ivOnly = Base64.getEncoder().encodeToString(new byte[12]);

        //when
        String decrypt = Application.getInstance(Crypto.class).decrypt(ivOnly, key32);

        //then
        assertThat(decrypt, nullValue());
    }

    @Test
    void testNullPublicKeyIsRejected() {
        //given
        Crypto crypto = Application.getInstance(Crypto.class);

        //when
        NullPointerException exception = Assertions.assertThrows(NullPointerException.class,
                () -> crypto.encrypt(plainText, (PublicKey) null));

        //then
        assertThat(exception.getMessage(), equalTo(Required.PUBLIC_KEY));
    }

    @Test
    void testNullPrivateKeyIsRejected() {
        //given
        Crypto crypto = Application.getInstance(Crypto.class);

        //when
        NullPointerException exception = Assertions.assertThrows(NullPointerException.class,
                () -> crypto.decrypt("text", (PrivateKey) null));

        //then
        assertThat(exception.getMessage(), equalTo(Required.PRIVATE_KEY));
    }
}
