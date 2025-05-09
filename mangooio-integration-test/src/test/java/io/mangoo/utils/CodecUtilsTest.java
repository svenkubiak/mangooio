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
    private static final String SERIALIZED = "Av9FBFLOOFjLjhCh2mAaZztRdSaGm0FDRoaaQAYDFdFgAAD/LQIQd8YAaMcd8Rr4aAwDsJKQDEkA/0QRJBFAdmFsaWRhdGlvbi5tYXRjaEh7MH0gbXVzdCBtYXRjaCB7MX1AdmFsaWRhdGlvbi5lbWFpbIQBezB9IG11c3QgYmUgYSB2YWxpZCBlTWFpbCBhZGRyZXNzPHZhbGlkYXRpb24uaXB2NIABezB9IG11c3QgYmUgYSB2YWxpZCBJUHY0IGFkZHJlc3M8dmFsaWRhdGlvbi5pcHY2gAF7MH0gbXVzdCBiZSBhIHZhbGlkIElQdjYgYWRkcmVzc1x2YWxpZGF0aW9uLnJhbmdlLmxlbmd0aKABezB9IG11c3QgYmUgYSBsZW5ndGggYmV0d2VlbiB7MX0gYW5kIHsyfVR2YWxpZGF0aW9uLmV4YWN0bWF0Y2hoezB9IG11c3QgZXhhY3RseSBtYXRjaCB7MX1MdmFsaWRhdGlvbi5yZXF1aXJlZFx7MH0gaXMgYSByZXF1aXJlZCB2YWx1ZVh2YWxpZGF0aW9uLnJhbmdlLnZhbHVllAF7MH0gbXVzdCBiZSB2YWx1ZSBiZXR3ZWVuIHsxfSBhbmQgezJ9SHZhbGlkYXRpb24ubnVtZXJpY2x7MH0gbXVzdCBiZSBhIG51bWVyaWMgdmFsdWVAdmFsaWRhdGlvbi5yZWdleFx7MH0gaXMgYW4gaW52YWxpZCB2YWx1ZVB2YWxpZGF0aW9uLm1pbi52YWx1ZZQBezB9IG11c3QgYmUgYSB2YWx1ZSBub3QgbGVzcyB0aGFuIHsxfVB2YWxpZGF0aW9uLm1heC52YWx1ZaABezB9IG11c3QgYmUgYSB2YWx1ZSBub3QgZ3JlYXRlciB0aGFuIHsxfVR2YWxpZGF0aW9uLmRvbWFpbm5hbWV8ezB9IG11c3QgYmUgYSB2YWxpZCBkb21haW4gbmFtZVR2YWxpZGF0aW9uLm1pbi5sZW5ndGiwAXswfSBtdXN0IGJlIGEgdmFsdWUgd2l0aCBhIG1pbiBsZW5ndGggb2YgezF9OHZhbGlkYXRpb24udXJsXHswfSBtdXN0IGJlIGEgdmFsaWQgVVJMVHZhbGlkYXRpb24ubWF4Lmxlbmd0aLABezB9IG11c3QgYmUgYSB2YWx1ZSB3aXRoIGEgbWF4IGxlbmd0aCBvZiB7MX1YdmFsaWRhdGlvbi5tYXRjaHZhbHVlc3hUaGUgdmFsdWVzIG9mIHswfSBpcyBub3QgdmFsaWT/RAD/RAD/RAEkAQxmb28MYmFy";
    private static final String PLAIN = "this is a plain text";

    @Test
    void testHexSHA512() {
        //given
        String hex = CodecUtils.hexSHA512(PLAIN);
        
        //then
        assertThat(hex, not(nullValue()));
        assertThat(hex, equalTo("131674e01e84e7dc3a1cb190440a0c730c2ec800d03a24cb63587a38f05aa827ee6458af8f3a503f633159af4eedaeca6bd2165f9ef918c0b7223d5e1fcdfb06"));
    }
    
    @Test
    void testConcurrentHexSHA512() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String hex = CodecUtils.hexSHA512(PLAIN);
            
            // then
            return hex.equals("131674e01e84e7dc3a1cb190440a0c730c2ec800d03a24cb63587a38f05aa827ee6458af8f3a503f633159af4eedaeca6bd2165f9ef918c0b7223d5e1fcdfb06");
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
    void testHashArgon2WithApplicationSecret() {
        //given
        String hash = CodecUtils.hashArgon2(PLAIN);

        //then
        assertThat(hash, not(nullValue()));
    }

    @Test
    void testUuid() {
        //given
        String uuid;
        String uuid2;

        //when
        uuid = CodecUtils.uuid();
        uuid2 = CodecUtils.uuid();

        //then
        assertThat(uuid, not(nullValue()));
        assertThat(uuid2, not(nullValue()));
        assertThat(uuid, not(equalTo(uuid2)));
        assertThat(UUID.fromString(uuid).version(), equalTo(6));
        assertThat(UUID.fromString(uuid).variant(), equalTo(2));
        assertThat(UUID.fromString(uuid2).version(), equalTo(6));
        assertThat(UUID.fromString(uuid2).variant(), equalTo(2));
    }

    @Test
    void testConcurrentUuid() {
        MatcherAssert.assertThat(t -> {
            //given
            String uuid;
            String uuid2;

            //when
            uuid = CodecUtils.uuid();
            uuid2 = CodecUtils.uuid();

            return StringUtils.isNotBlank(uuid) && StringUtils.isNotBlank(uuid2) && !uuid.equals(uuid2) && UUID.fromString(uuid).variant() == 2 && UUID.fromString(uuid).version() == 6;
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
    void testMatchArgon2WithApplicationSecret() {
        //given
        String hash = CodecUtils.hashArgon2(PLAIN);

        //when
        boolean valid = CodecUtils.matchArgon2(PLAIN, hash);

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