package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.llorllale.cactoos.matchers.RunsInThreads;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.routing.bindings.Form;

@ExtendWith({TestExtension.class})
public class CodecUtilsTest {
    private static final String SERIALIZED = "rO0ABXNyAB9pby5tYW5nb28ucm91dGluZy5iaW5kaW5ncy5Gb3JtHu23i2raFFUCAANaAARrZWVwWgAJc3VibWl0dGVkTAAIdmFsdWVNYXB0AA9MamF2YS91dGlsL01hcDt4cgAkaW8ubWFuZ29vLnJvdXRpbmcuYmluZGluZ3MuVmFsaWRhdG9y9hXwxmUKKmMCAANMAAZlcnJvcnNxAH4AAUwACG1lc3NhZ2VzdAAZTGlvL21hbmdvby9pMThuL01lc3NhZ2VzO0wABnZhbHVlc3EAfgABeHBzcgARamF2YS51dGlsLkhhc2hNYXAFB9rBwxZg0QMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD9AAAAAAAAAdwgAAAAQAAAAAHhzcgAXaW8ubWFuZ29vLmkxOG4uTWVzc2FnZXPoOUNDl2xQ4wIAAUwACGRlZmF1bHRzcQB+AAF4cHNxAH4ABT9AAAAAAAAYdwgAAAAgAAAAEXQAEHZhbGlkYXRpb24ubWF0Y2h0ABJ7MH0gbXVzdCBtYXRjaCB7MX10ABB2YWxpZGF0aW9uLmVtYWlsdAAhezB9IG11c3QgYmUgYSB2YWxpZCBlTWFpbCBhZGRyZXNzdAAPdmFsaWRhdGlvbi5pcHY0dAAgezB9IG11c3QgYmUgYSB2YWxpZCBJUHY0IGFkZHJlc3N0AA92YWxpZGF0aW9uLmlwdjZ0ACB7MH0gbXVzdCBiZSBhIHZhbGlkIElQdjYgYWRkcmVzc3QAF3ZhbGlkYXRpb24ucmFuZ2UubGVuZ3RodAAoezB9IG11c3QgYmUgYSBsZW5ndGggYmV0d2VlbiB7MX0gYW5kIHsyfXQAFXZhbGlkYXRpb24uZXhhY3RtYXRjaHQAGnswfSBtdXN0IGV4YWN0bHkgbWF0Y2ggezF9dAATdmFsaWRhdGlvbi5yZXF1aXJlZHQAF3swfSBpcyBhIHJlcXVpcmVkIHZhbHVldAAWdmFsaWRhdGlvbi5yYW5nZS52YWx1ZXQAJXswfSBtdXN0IGJlIHZhbHVlIGJldHdlZW4gezF9IGFuZCB7Mn10ABJ2YWxpZGF0aW9uLm51bWVyaWN0ABt7MH0gbXVzdCBiZSBhIG51bWVyaWMgdmFsdWV0ABB2YWxpZGF0aW9uLnJlZ2V4dAAXezB9IGlzIGFuIGludmFsaWQgdmFsdWV0ABR2YWxpZGF0aW9uLm1pbi52YWx1ZXQAJXswfSBtdXN0IGJlIGEgdmFsdWUgbm90IGxlc3MgdGhhbiB7MX10ABR2YWxpZGF0aW9uLm1heC52YWx1ZXQAKHswfSBtdXN0IGJlIGEgdmFsdWUgbm90IGdyZWF0ZXIgdGhhbiB7MX10ABV2YWxpZGF0aW9uLmRvbWFpbm5hbWV0AB97MH0gbXVzdCBiZSBhIHZhbGlkIGRvbWFpbiBuYW1ldAAVdmFsaWRhdGlvbi5taW4ubGVuZ3RodAAsezB9IG11c3QgYmUgYSB2YWx1ZSB3aXRoIGEgbWluIGxlbmd0aCBvZiB7MX10AA52YWxpZGF0aW9uLnVybHQAF3swfSBtdXN0IGJlIGEgdmFsaWQgVVJMdAAVdmFsaWRhdGlvbi5tYXgubGVuZ3RodAAsezB9IG11c3QgYmUgYSB2YWx1ZSB3aXRoIGEgbWF4IGxlbmd0aCBvZiB7MX10ABZ2YWxpZGF0aW9uLm1hdGNodmFsdWVzdAAeVGhlIHZhbHVlcyBvZiB7MH0gaXMgbm90IHZhbGlkeHNxAH4ABT9AAAAAAAAMdwgAAAAQAAAAAXQAA2Zvb3QAA2JhcngAAHNxAH4ABT9AAAAAAAAAdwgAAAAQAAAAAHg=";
    private static final String PLAIN = "this is a plain text";
    
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
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    public void testHashArgon2() {
        //given
        String hash = CodecUtils.hashArgon2(PLAIN, MangooUtils.randomString(24));
        
        //then
        assertThat(hash, not(nullValue()));
    }
    
    @Test
    public void testConcurrentHashArgon2() {
        MatcherAssert.assertThat(t -> {
            //given
            String hash = CodecUtils.hashArgon2(PLAIN, MangooUtils.randomString(24));
            
            // then
            return StringUtils.isNotBlank(hash);
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    public void testMatchArgon2() {
        //given
        String salt = MangooUtils.randomString(24);
        String hash = CodecUtils.hashArgon2(PLAIN, salt);
        
        //when
        boolean valid = CodecUtils.matchArgon2(PLAIN, salt, hash);
        
        //then
        assertThat(valid, equalTo(true));
    }
    
    @Test
    public void testNonMatchSaltArgon2() {
        //given
        String salt = MangooUtils.randomString(24);
        String hash = CodecUtils.hashArgon2(PLAIN, MangooUtils.randomString(24));
        
        //when
        boolean valid = CodecUtils.matchArgon2(PLAIN, salt, hash);
        
        //then
        assertThat(valid, equalTo(false));
    }
    
    @Test
    public void testNonMatchHashArgon2() {
        //given
        String salt = MangooUtils.randomString(24);
        String hash = CodecUtils.hashArgon2(PLAIN, salt);
        
        //when
        boolean valid = CodecUtils.matchArgon2("foobar", salt, hash);
        
        //then
        assertThat(valid, equalTo(false));
    }
    
    @Test
    public void testConcurrentMatchArgon2() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String salt = MangooUtils.randomString(24);
            String hash = CodecUtils.hashArgon2(PLAIN, salt);
            
            //when
            boolean valid = CodecUtils.matchArgon2(PLAIN, salt, hash);
            
            // then
            return valid;
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
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
    public void testConcurrentSerialize() throws Exception {
        MatcherAssert.assertThat(t -> {
            //given
            Form form = Application.getInstance(Form.class);
            form.addValue("foo", "bar");
            String serialized = CodecUtils.serializeToBase64(form);
            
            // then
            return serialized.equals(SERIALIZED);
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
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
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
}