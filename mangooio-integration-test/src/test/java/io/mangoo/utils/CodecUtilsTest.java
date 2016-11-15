package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import com.google.common.base.Charsets;

import io.mangoo.core.Application;
import io.mangoo.routing.bindings.Form;

/**
 * 
 * @author svenkubiak
 *
 */
public class CodecUtilsTest {
    private static final String BASE64 = "dGhpcyBpcyBhIHBsYWluIHRleHQ=";
    private static final String PLAIN = "this is a plain text";
    private static final String JBCRYPT_HASH = "$2a$12$I.tRIbGLB82DDLUHTz.IUOSGeHCwUgX/MnGj67SRFvfzoNZzx2je6";

    @Test
    public void testHexJBcrypt() {
        //given
        String hex = CodecUtils.hexJBcrypt(PLAIN);
        
        //then
        assertThat(hex, not(nullValue()));
    }
    
    @Test
    public void testHexSHA512() {
        //given
        String hex = CodecUtils.hexSHA512(PLAIN);
        
        //then
        assertThat(hex, not(nullValue()));
        assertThat(hex, equalTo("39e668e353a0b4caf7e8e3c7093e30be8c0a29db739bf86bd5243d11d1bfe040ad2a712be1a96b405233ce13cbd7c3db9bcc40f2f2e70c6a344a0898208347e4"));
    }
    
    @Test
    public void testHexSHA512WithSalt() {
        //given
        String salt = "this is a salt";
        String hex = CodecUtils.hexSHA512(PLAIN, salt);
        
        //then
        assertThat(hex, not(nullValue()));
        assertThat(hex, equalTo("e3558a2c97cecf01e7dbe39e5ec3156bf55b38dee69de17f2ef2fac60e1fc4e67b85413849c6b1d5b67adc94d10684f066127c444ac17830267fd816dd49bc8e"));
    }
    
    @Test
    public void testCheckJBcrypt() {
        //given
        boolean valid = CodecUtils.checkJBCrypt(PLAIN, JBCRYPT_HASH);
        
        //then
        assertThat(valid, equalTo(true));
    }
    
    @Test
    public void testEncodeBase64String() {
        //given
        String base64 = CodecUtils.encodeBase64(PLAIN);
        
        //then
        assertThat(base64, not(nullValue()));
        assertThat(base64, equalTo(BASE64));
    }
    
    @Test
    public void testDencodeBase64String() {
        //given
        String base64 = CodecUtils.decodeBase64(BASE64);
        
        //then
        assertThat(base64, not(nullValue()));
        assertThat(base64, equalTo(PLAIN));
    }
    
    @Test
    public void testEncodeBase64ByteArray() {
        //given
        String base64 = CodecUtils.encodeBase64(PLAIN.getBytes(Charsets.UTF_8));
        
        //then
        assertThat(base64, not(nullValue()));
        assertThat(base64, equalTo(BASE64));
    }
    
    @Test
    public void testDencodeBase64ByteArray() {
        //given
        String base64 = CodecUtils.decodeBase64(BASE64.getBytes(Charsets.UTF_8));
        
        //then
        assertThat(base64, not(nullValue()));
        assertThat(base64, equalTo(PLAIN));
    }
    
    @Test
    public void testSerialize() {
        //given
        Form form = Application.getInstance(Form.class);
        String base64 = CodecUtils.serializeToString(form);

        //then
        assertThat(base64, not(nullValue()));
    }
}