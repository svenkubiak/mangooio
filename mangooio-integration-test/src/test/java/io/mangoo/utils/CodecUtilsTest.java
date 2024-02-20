package io.mangoo.utils;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.routing.bindings.Form;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.llorllale.cactoos.matchers.RunsInThreads;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class CodecUtilsTest {
    private static final String SERIALIZED = "Av8BAE8RTONuVa1FGgBpby5tYW5nb28ucm91dGluZy5iaW5kaW5ncwCin0YlwLEkYgQARm9ybQAAAAEA3jptXE6zLtsOAGlvLm1hbmdvby5pMThuAI8r14oLIdc9CABNZXNzYWdlcwBEEQAAEHZhbGlkYXRpb24ubWF0Y2gAABJ7MH0gbXVzdCBtYXRjaCB7MX0AABB2YWxpZGF0aW9uLmVtYWlsAAAhezB9IG11c3QgYmUgYSB2YWxpZCBlTWFpbCBhZGRyZXNzAAAPdmFsaWRhdGlvbi5pcHY0AAAgezB9IG11c3QgYmUgYSB2YWxpZCBJUHY0IGFkZHJlc3MAAA92YWxpZGF0aW9uLmlwdjYAACB7MH0gbXVzdCBiZSBhIHZhbGlkIElQdjYgYWRkcmVzcwAAF3ZhbGlkYXRpb24ucmFuZ2UubGVuZ3RoAAAoezB9IG11c3QgYmUgYSBsZW5ndGggYmV0d2VlbiB7MX0gYW5kIHsyfQAAFXZhbGlkYXRpb24uZXhhY3RtYXRjaAAAGnswfSBtdXN0IGV4YWN0bHkgbWF0Y2ggezF9AAATdmFsaWRhdGlvbi5yZXF1aXJlZAAAF3swfSBpcyBhIHJlcXVpcmVkIHZhbHVlAAAWdmFsaWRhdGlvbi5yYW5nZS52YWx1ZQAAJXswfSBtdXN0IGJlIHZhbHVlIGJldHdlZW4gezF9IGFuZCB7Mn0AABJ2YWxpZGF0aW9uLm51bWVyaWMAABt7MH0gbXVzdCBiZSBhIG51bWVyaWMgdmFsdWUAABB2YWxpZGF0aW9uLnJlZ2V4AAAXezB9IGlzIGFuIGludmFsaWQgdmFsdWUAABR2YWxpZGF0aW9uLm1pbi52YWx1ZQAAJXswfSBtdXN0IGJlIGEgdmFsdWUgbm90IGxlc3MgdGhhbiB7MX0AABR2YWxpZGF0aW9uLm1heC52YWx1ZQAAKHswfSBtdXN0IGJlIGEgdmFsdWUgbm90IGdyZWF0ZXIgdGhhbiB7MX0AABV2YWxpZGF0aW9uLmRvbWFpbm5hbWUAAB97MH0gbXVzdCBiZSBhIHZhbGlkIGRvbWFpbiBuYW1lAAAVdmFsaWRhdGlvbi5taW4ubGVuZ3RoAAAsezB9IG11c3QgYmUgYSB2YWx1ZSB3aXRoIGEgbWluIGxlbmd0aCBvZiB7MX0AAA52YWxpZGF0aW9uLnVybAAAF3swfSBtdXN0IGJlIGEgdmFsaWQgVVJMAAAVdmFsaWRhdGlvbi5tYXgubGVuZ3RoAAAsezB9IG11c3QgYmUgYSB2YWx1ZSB3aXRoIGEgbWF4IGxlbmd0aCBvZiB7MX0AABZ2YWxpZGF0aW9uLm1hdGNodmFsdWVzAAAeVGhlIHZhbHVlcyBvZiB7MH0gaXMgbm90IHZhbGlkAEQAAEQAAEQBAAADZm9vAAADYmFy";
    private static final String PLAIN = "this is a plain text";
    
    @Test
    void testHexSHA512() {
        //given
        String hex = CodecUtils.hexSHA512(PLAIN);
        
        //then
        assertThat(hex, not(nullValue()));
        assertThat(hex, equalTo("39e668e353a0b4caf7e8e3c7093e30be8c0a29db739bf86bd5243d11d1bfe040ad2a712be1a96b405233ce13cbd7c3db9bcc40f2f2e70c6a344a0898208347e4"));
    }
    
    @Test
    void testConcurrentHexSHA512() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String hex = CodecUtils.hexSHA512(PLAIN);
            
            // then
            return hex.equals("39e668e353a0b4caf7e8e3c7093e30be8c0a29db739bf86bd5243d11d1bfe040ad2a712be1a96b405233ce13cbd7c3db9bcc40f2f2e70c6a344a0898208347e4");
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    void testHashArgon2() {
        //given
        String hash = CodecUtils.hashArgon2(PLAIN, MangooUtils.randomString(24));
        
        //then
        assertThat(hash, not(nullValue()));
    }
    
    @Test
    void testConcurrentHashArgon2() {
        MatcherAssert.assertThat(t -> {
            //given
            String hash = CodecUtils.hashArgon2(PLAIN, MangooUtils.randomString(24));
            
            // then
            return StringUtils.isNotBlank(hash);
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    void testMatchArgon2() {
        //given
        String salt = MangooUtils.randomString(24);
        String hash = CodecUtils.hashArgon2(PLAIN, salt);
        
        //when
        boolean valid = CodecUtils.matchArgon2(PLAIN, salt, hash);
        
        //then
        assertThat(valid, equalTo(true));
    }
    
    @Test
    void testNonMatchSaltArgon2() {
        //given
        String salt = MangooUtils.randomString(24);
        String hash = CodecUtils.hashArgon2(PLAIN, MangooUtils.randomString(24));
        
        //when
        boolean valid = CodecUtils.matchArgon2(PLAIN, salt, hash);
        
        //then
        assertThat(valid, equalTo(false));
    }
    
    @Test
    void testNonMatchHashArgon2() {
        //given
        String salt = MangooUtils.randomString(24);
        String hash = CodecUtils.hashArgon2(PLAIN, salt);
        
        //when
        boolean valid = CodecUtils.matchArgon2("foobar", salt, hash);
        
        //then
        assertThat(valid, equalTo(false));
    }
    
    @Test
    void testConcurrentMatchArgon2() throws InterruptedException {
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
    void testSerialize() {
        //given
        Form form = Application.getInstance(Form.class);
        form.addValue("foo", "bar");
        String serialized = CodecUtils.serializeToBase64(form);
        
        //then
        assertThat(serialized, not(nullValue()));
        assertThat(serialized, equalTo(SERIALIZED));
    }
    
    @Test
    @Disabled
    void testConcurrentSerialize() throws Exception {
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
    void testDeserialize() {
        //given
        Form form = CodecUtils.deserializeFromBase64(SERIALIZED);
        
        //then
        assertThat(form, not(nullValue()));
        assertThat(form, instanceOf(Form.class));
        assertThat(form.get("foo"), equalTo("bar"));
    }
    
    @Test
    @Disabled
    void testConcurrentDeserialize() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            Form form = CodecUtils.deserializeFromBase64(SERIALIZED);
            
            // then
            return form.get("foo").equals("bar");
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    void testBase64Encoder() {
        //given
        String foo = UUID.randomUUID().toString();

        //when
        byte[] base64 = CodecUtils.encodeToBase64(foo);
                
        //then
        assertThat(base64, not(nullValue()));
    }
    
    @Test
    void testBase64EncoderConcurrent() {
        MatcherAssert.assertThat(t -> {
            //given
            String foo = UUID.randomUUID().toString();

            //when
            byte[] base64 = CodecUtils.encodeToBase64(foo);
            
            return base64 != null;
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    void testBase64Decoder() {
        //given
        String foo = UUID.randomUUID().toString();

        //when
        byte[] base64Encoded = CodecUtils.encodeToBase64(foo);
        byte[] base64Decoded = CodecUtils.decodeFromBase64(new String(base64Encoded, StandardCharsets.UTF_8));
                
        //then
        assertThat(new String(base64Decoded, StandardCharsets.UTF_8), equalTo(foo));
    }
    
    @Test
    void testBase64DecoderConcurrent() {
        MatcherAssert.assertThat(t -> {
            //given
            String foo = UUID.randomUUID().toString();

            //when
            byte[] base64Encoded = CodecUtils.encodeToBase64(foo);
            byte[] base64Decoded = CodecUtils.decodeFromBase64(new String(base64Encoded, StandardCharsets.UTF_8));
            
            return new String(base64Decoded, StandardCharsets.UTF_8).equals(foo);
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
}