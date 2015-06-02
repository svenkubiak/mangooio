package mangoo.crypto;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;

import mangoo.io.core.Application;
import mangoo.io.crypto.Crypto;

import org.junit.Before;
import org.junit.Test;

public class CryptoTest {
    private static Crypto crypto;
    private static final String base64Pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
    private static final Pattern pattern = Pattern.compile(base64Pattern);
    private static final String plainText = "super secret message!!!";
    private static final String key = "myvoiceismypassword";
    
    @Before
    public void init() {
        crypto = Application.getInjector().getInstance(Crypto.class);
    }
    
    @Test
    public void encryptTest() {
        String encrypt = crypto.encrypt(plainText);
        
        assertNotNull(encrypt);
        assertTrue(pattern.matcher(encrypt).matches());
        assertFalse(encrypt.equalsIgnoreCase(plainText));
    }
    
    @Test
    public void encryptWithKeyTest() {
        String encrypt = crypto.encrypt(plainText, key);
        
        assertNotNull(encrypt);
        assertTrue(pattern.matcher(encrypt).matches());
        assertFalse(encrypt.equalsIgnoreCase(plainText));
    }
    
    @Test
    public void decryptTest() {
        String encrypt = crypto.encrypt(plainText);
        assertNotNull(encrypt);
        assertTrue(pattern.matcher(encrypt).matches());
        
        String decrypt = crypto.decrypt(encrypt);
        assertNotNull(decrypt);
        assertEquals(decrypt, plainText);
    }
    
    @Test
    public void decryptWithKeyTest() {
        String encrypt = crypto.encrypt(plainText, key);
        assertNotNull(encrypt);
        assertTrue(pattern.matcher(encrypt).matches());
        
        String decrypt = crypto.decrypt(encrypt, key);
        assertNotNull(decrypt);
        assertEquals(decrypt, plainText);
    }
}