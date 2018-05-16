package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.llorllale.cactoos.matchers.RunsInThreads;

import io.mangoo.TestSuite;
import io.mangoo.core.Application;
import io.mangoo.routing.bindings.Form;

/**
 * 
 * @author svenkubiak
 *
 */
public class CodecUtilsTest {
    private static final String SERIALIZED = "rO0ABXNyAB9pby5tYW5nb28ucm91dGluZy5iaW5kaW5ncy5Gb3Jtr0x38MTUh5ACAARaAAVmbGFzaFoACXN1Ym1pdHRlZEwABWZpbGVzdAAQTGphdmEvdXRpbC9MaXN0O0wACHZhbHVlTWFwdAAPTGphdmEvdXRpbC9NYXA7eHIAJGlvLm1hbmdvby5yb3V0aW5nLmJpbmRpbmdzLlZhbGlkYXRvct3BGEIQSQk8AgADTAAGZXJyb3JzcQB+AAJMAAhtZXNzYWdlc3QAGUxpby9tYW5nb28vaTE4bi9NZXNzYWdlcztMAAZ2YWx1ZXNxAH4AAnhwc3IAEWphdmEudXRpbC5IYXNoTWFwBQfawcMWYNEDAAJGAApsb2FkRmFjdG9ySQAJdGhyZXNob2xkeHA/QAAAAAAAAHcIAAAAEAAAAAB4c3IAF2lvLm1hbmdvby5pMThuLk1lc3NhZ2VzaOrotu6i5LoCAAFMAAhkZWZhdWx0c3EAfgACeHBzcQB+AAY/QAAAAAAAGHcIAAAAIAAAAA50AAxSRVFVSVJFRF9LRVl0AA97MH0gaXMgcmVxdWlyZWR0AAlNQVRDSF9LRVl0ABJ7MH0gbXVzdCBtYXRjaCB7MX10AAlSQU5HRV9LRVl0ACh7MH0gbXVzdCBoYXZlIGEgc2l6ZSBiZXR3ZWVuIHsxfSBhbmQgezJ9dAAISVBWNl9LRVl0ACB7MH0gbXVzdCBiZSBhIHZhbGlkIElQdjYgYWRkcmVzc3QAC05VTUVSSUNfS0VZdAAbezB9IG11c3QgYmUgYSBudW1lcmljIHZhbHVldAAHTUFYX0tFWXQAH3swfSBtdXN0IGhhdmUgYSBzaXplIG9mIG1heCB7MX10AA9ET01BSU5fTkFNRV9LRVl0AB97MH0gbXVzdCBiZSBhIHZhbGlkIGRvbWFpbiBuYW1ldAAPRVhBQ1RfTUFUQ0hfS0VZdAAaezB9IG11c3QgZXhhY3RseSBtYXRjaCB7MX10AAlFTUFJTF9LRVl0ACF7MH0gbXVzdCBiZSBhIHZhbGlkIGVNYWlsIGFkZHJlc3N0ABBNQVRDSF9WQUxVRVNfS0VZdAAeVGhlIHZhbHVlcyBvZiB7MH0gaXMgbm90IHZhbGlkdAAHVVJMX0tFWXQAF3swfSBtdXN0IGJlIGEgdmFsaWQgVVJMdAAISVBWNF9LRVl0ACB7MH0gbXVzdCBiZSBhIHZhbGlkIElQdjQgYWRkcmVzc3QAB01JTl9LRVl0ACN7MH0gbXVzdCBoYXZlIGEgbGVhc3QgYSBzaXplIG9mIHsxfXQACVJFR0VYX0tFWXQADnswfSBpcyBpbnZhbGlkeHNxAH4ABj9AAAAAAAAMdwgAAAAQAAAAAXQAA2Zvb3QAA2JhcngAAHNyABNqYXZhLnV0aWwuQXJyYXlMaXN0eIHSHZnHYZ0DAAFJAARzaXpleHAAAAAAdwQAAAAAeHNxAH4ABj9AAAAAAAAAdwgAAAAQAAAAAHg=";
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
    public void testConcurrentHexSHA512() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String hex = CodecUtils.hexSHA512(PLAIN);
            
            // then
            return hex.equals("39e668e353a0b4caf7e8e3c7093e30be8c0a29db739bf86bd5243d11d1bfe040ad2a712be1a96b405233ce13cbd7c3db9bcc40f2f2e70c6a344a0898208347e4");
        }, new RunsInThreads<>(new AtomicInteger(), TestSuite.THREADS));
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
    public void testConcurrentHexSHA512WithSalt() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String salt = "this is a salt";
            String hex = CodecUtils.hexSHA512(PLAIN, salt);
            
            // then
            return hex.equals("e3558a2c97cecf01e7dbe39e5ec3156bf55b38dee69de17f2ef2fac60e1fc4e67b85413849c6b1d5b67adc94d10684f066127c444ac17830267fd816dd49bc8e");
        }, new RunsInThreads<>(new AtomicInteger(), TestSuite.THREADS));
    }
    
    @Test
    public void testCheckJBcrypt() {
        //given
        boolean valid = CodecUtils.checkJBCrypt(PLAIN, JBCRYPT_HASH);
        
        //then
        assertThat(valid, equalTo(true));
    }
    
    @Test
    public void testConcurrentCheckJBcrypt() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            boolean valid = CodecUtils.checkJBCrypt(PLAIN, JBCRYPT_HASH);
            
            // then
            return valid;
        }, new RunsInThreads<>(new AtomicInteger(), TestSuite.THREADS));
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
    public void testConcurrentSerialize() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            Form form = Application.getInstance(Form.class);
            form.addValue("foo", "bar");
            String serialized = CodecUtils.serializeToBase64(form);
            
            // then
            return serialized.equals(SERIALIZED);
        }, new RunsInThreads<>(new AtomicInteger(), TestSuite.THREADS));
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
    
    @Test
    public void testConcurrentDeserialize() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            Form form = CodecUtils.deserializeFromBase64(SERIALIZED);
            
            // then
            return form.get("foo").equals("bar");
        }, new RunsInThreads<>(new AtomicInteger(), TestSuite.THREADS));
    }
}