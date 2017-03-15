package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
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
    private static final String SERIALIZED = "rO0ABXNyAB9pby5tYW5nb28ucm91dGluZy5iaW5kaW5ncy5Gb3Jtr0x38MTUh5ACAARaAAVmbGFzaFoACXN1Ym1pdHRlZEwABWZpbGVzdAAQTGphdmEvdXRpbC9MaXN0O0wACXZhbHVlTGlzdHQAD0xqYXZhL3V0aWwvTWFwO3hyACRpby5tYW5nb28ucm91dGluZy5iaW5kaW5ncy5WYWxpZGF0b3LdwRhCEEkJPAIAA0wABmVycm9yc3EAfgACTAAIbWVzc2FnZXN0ABlMaW8vbWFuZ29vL2kxOG4vTWVzc2FnZXM7TAAGdmFsdWVzcQB+AAJ4cHNyABFqYXZhLnV0aWwuSGFzaE1hcAUH2sHDFmDRAwACRgAKbG9hZEZhY3RvckkACXRocmVzaG9sZHhwP0AAAAAAAAB3CAAAABAAAAAAeHNyABdpby5tYW5nb28uaTE4bi5NZXNzYWdlc2jq6LbuouS6AgABTAAIZGVmYXVsdHNxAH4AAnhwc3EAfgAGP0AAAAAAABh3CAAAACAAAAAOdAAMUkVRVUlSRURfS0VZdAAPezB9IGlzIHJlcXVpcmVkdAAJTUFUQ0hfS0VZdAASezB9IG11c3QgbWF0Y2ggezF9dAAJUkFOR0VfS0VZdAAoezB9IG11c3QgaGF2ZSBhIHNpemUgYmV0d2VlbiB7MX0gYW5kIHsyfXQACElQVjZfS0VZdAAgezB9IG11c3QgYmUgYSB2YWxpZCBJUHY2IGFkZHJlc3N0AAtOVU1FUklDX0tFWXQAG3swfSBtdXN0IGJlIGEgbnVtZXJpYyB2YWx1ZXQAB01BWF9LRVl0AB97MH0gbXVzdCBoYXZlIGEgc2l6ZSBvZiBtYXggezF9dAAPRE9NQUlOX05BTUVfS0VZdAAfezB9IG11c3QgYmUgYSB2YWxpZCBkb21haW4gbmFtZXQAD0VYQUNUX01BVENIX0tFWXQAGnswfSBtdXN0IGV4YWN0bHkgbWF0Y2ggezF9dAAJRU1BSUxfS0VZdAAhezB9IG11c3QgYmUgYSB2YWxpZCBlTWFpbCBhZGRyZXNzdAAQTUFUQ0hfVkFMVUVTX0tFWXQAHlRoZSB2YWx1ZXMgb2YgezB9IGlzIG5vdCB2YWxpZHQAB1VSTF9LRVl0ABd7MH0gbXVzdCBiZSBhIHZhbGlkIFVSTHQACElQVjRfS0VZdAAgezB9IG11c3QgYmUgYSB2YWxpZCBJUHY0IGFkZHJlc3N0AAdNSU5fS0VZdAAjezB9IG11c3QgaGF2ZSBhIGxlYXN0IGEgc2l6ZSBvZiB7MX10AAlSRUdFWF9LRVl0AA57MH0gaXMgaW52YWxpZHhzcQB+AAY/QAAAAAAADHcIAAAAEAAAAAF0AANmb290AANiYXJ4AABzcgATamF2YS51dGlsLkFycmF5TGlzdHiB0h2Zx2GdAwABSQAEc2l6ZXhwAAAAAHcEAAAAAHhzcQB+AAY/QAAAAAAAAHcIAAAAEAAAAAB4";
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
        form.addValue("foo", "bar");
        String serialized = CodecUtils.serializeToBase64(form);
        
        //then
        assertThat(serialized, not(nullValue()));
        assertThat(serialized, equalTo(SERIALIZED));
    }
    
    @Test
    public void testDeserialize() {
        //given
        Form form = CodecUtils.deserializeFromBase64(SERIALIZED);
        
        //then
        assertThat(form, not(nullValue()));
        assertThat(form, instanceOf(Form.class));
        assertThat(form.get("foo"), equalTo("bar"));
    }
}