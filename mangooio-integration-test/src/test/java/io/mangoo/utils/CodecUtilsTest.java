package io.mangoo.utils;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.routing.bindings.Form;
import io.mangoo.test.concurrent.ConcurrentRunner;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
@Execution(ExecutionMode.SAME_THREAD)
class CodecUtilsTest {
    private static final String SERIALIZED = "Av9FBFLOOFjLjhCh2mAaZztRdSaGm0FDRoaaQAYDO6GfKQCzbxXRYAAAAC0CqWE3GdWnsBB3xgBoxx3xGvhoDAOiTLCd9+8/sJKQDEkAAEQRAEB2YWxpZGF0aW9uLm1hdGNoAEh7MH0gbXVzdCBtYXRjaCB7MX0AQHZhbGlkYXRpb24uZW1haWwAhAF7MH0gbXVzdCBiZSBhIHZhbGlkIGVNYWlsIGFkZHJlc3MAPHZhbGlkYXRpb24uaXB2NACAAXswfSBtdXN0IGJlIGEgdmFsaWQgSVB2NCBhZGRyZXNzADx2YWxpZGF0aW9uLmlwdjYAgAF7MH0gbXVzdCBiZSBhIHZhbGlkIElQdjYgYWRkcmVzcwBcdmFsaWRhdGlvbi5yYW5nZS5sZW5ndGgAoAF7MH0gbXVzdCBiZSBhIGxlbmd0aCBiZXR3ZWVuIHsxfSBhbmQgezJ9AFR2YWxpZGF0aW9uLmV4YWN0bWF0Y2gAaHswfSBtdXN0IGV4YWN0bHkgbWF0Y2ggezF9AEx2YWxpZGF0aW9uLnJlcXVpcmVkAFx7MH0gaXMgYSByZXF1aXJlZCB2YWx1ZQBYdmFsaWRhdGlvbi5yYW5nZS52YWx1ZQCUAXswfSBtdXN0IGJlIHZhbHVlIGJldHdlZW4gezF9IGFuZCB7Mn0ASHZhbGlkYXRpb24ubnVtZXJpYwBsezB9IG11c3QgYmUgYSBudW1lcmljIHZhbHVlAEB2YWxpZGF0aW9uLnJlZ2V4AFx7MH0gaXMgYW4gaW52YWxpZCB2YWx1ZQBQdmFsaWRhdGlvbi5taW4udmFsdWUAlAF7MH0gbXVzdCBiZSBhIHZhbHVlIG5vdCBsZXNzIHRoYW4gezF9AFB2YWxpZGF0aW9uLm1heC52YWx1ZQCgAXswfSBtdXN0IGJlIGEgdmFsdWUgbm90IGdyZWF0ZXIgdGhhbiB7MX0AVHZhbGlkYXRpb24uZG9tYWlubmFtZQB8ezB9IG11c3QgYmUgYSB2YWxpZCBkb21haW4gbmFtZQBUdmFsaWRhdGlvbi5taW4ubGVuZ3RoALABezB9IG11c3QgYmUgYSB2YWx1ZSB3aXRoIGEgbWluIGxlbmd0aCBvZiB7MX0AOHZhbGlkYXRpb24udXJsAFx7MH0gbXVzdCBiZSBhIHZhbGlkIFVSTABUdmFsaWRhdGlvbi5tYXgubGVuZ3RoALABezB9IG11c3QgYmUgYSB2YWx1ZSB3aXRoIGEgbWF4IGxlbmd0aCBvZiB7MX0AWHZhbGlkYXRpb24ubWF0Y2h2YWx1ZXMAeFRoZSB2YWx1ZXMgb2YgezB9IGlzIG5vdCB2YWxpZABEAABEAABEAQAMZm9vAAxiYXI=";
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
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    void testHashArgon2() {
        //given
        String hash = CodecUtils.hashArgon2(PLAIN, MangooUtils.randomString(24));
        
        //then
        assertThat(hash, not(nullValue()));
    }

    @Test
    void testUuid() {
        //given
        String uuid;

        //when
        uuid = CodecUtils.uuid();

        //then
        assertThat(uuid, not(nullValue()));
        assertThat(UUID.fromString(uuid).version(), equalTo(5));
        assertThat(UUID.fromString(uuid).variant(), equalTo(2));
    }

    @Test
    void testConcurrentUuid() {
        MatcherAssert.assertThat(t -> {
            //given
            String uuid;

            //when
            uuid = CodecUtils.uuid();

            return StringUtils.isNotBlank(uuid) && UUID.fromString(uuid).variant() == 2 && UUID.fromString(uuid).version() == 5;
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    void testConcurrentHashArgon2() {
        MatcherAssert.assertThat(t -> {
            //given
            String hash = CodecUtils.hashArgon2(PLAIN, MangooUtils.randomString(24));
            
            // then
            return StringUtils.isNotBlank(hash);
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
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
            
            // then
            return CodecUtils.matchArgon2(PLAIN, salt, hash);
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
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
    void testConcurrentSerialize() throws Exception {
        MatcherAssert.assertThat(t -> {
            //given
            Form form = Application.getInstance(Form.class);
            form.addValue("foo", "bar");
            String serialized = CodecUtils.serializeToBase64(form);

            // then
            return serialized.equals(SERIALIZED);
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
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
    void testConcurrentDeserialize() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            Form form = CodecUtils.deserializeFromBase64(SERIALIZED);

            // then
            return form.get("foo").equals("bar");
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
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
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
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
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }
}