package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
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
class CodecUtilsTest {
    private static final String SERIALIZED = "Av8AAE8RTONuVa1FGgBpby5tYW5nb28ucm91dGluZy5iaW5kaW5ncwCin0YlwLEkYgQARm9ybSBNvIoAAAAAAN46bVxOsy7bDgBpby5tYW5nb28uaTE4bgCPK9eKCyHXPQgATWVzc2FnZXP3WdtnAAEiABEAABB2YWxpZGF0aW9uLm1hdGNoAAASezB9IG11c3QgbWF0Y2ggezF9AAAQdmFsaWRhdGlvbi5lbWFpbAAAIXswfSBtdXN0IGJlIGEgdmFsaWQgZU1haWwgYWRkcmVzcwAAD3ZhbGlkYXRpb24uaXB2NAAAIHswfSBtdXN0IGJlIGEgdmFsaWQgSVB2NCBhZGRyZXNzAAAPdmFsaWRhdGlvbi5pcHY2AAAgezB9IG11c3QgYmUgYSB2YWxpZCBJUHY2IGFkZHJlc3MAABd2YWxpZGF0aW9uLnJhbmdlLmxlbmd0aAAAKHswfSBtdXN0IGJlIGEgbGVuZ3RoIGJldHdlZW4gezF9IGFuZCB7Mn0AABV2YWxpZGF0aW9uLmV4YWN0bWF0Y2gAABp7MH0gbXVzdCBleGFjdGx5IG1hdGNoIHsxfQAAE3ZhbGlkYXRpb24ucmVxdWlyZWQAABd7MH0gaXMgYSByZXF1aXJlZCB2YWx1ZQAAFnZhbGlkYXRpb24ucmFuZ2UudmFsdWUAACV7MH0gbXVzdCBiZSB2YWx1ZSBiZXR3ZWVuIHsxfSBhbmQgezJ9AAASdmFsaWRhdGlvbi5udW1lcmljAAAbezB9IG11c3QgYmUgYSBudW1lcmljIHZhbHVlAAAQdmFsaWRhdGlvbi5yZWdleAAAF3swfSBpcyBhbiBpbnZhbGlkIHZhbHVlAAAUdmFsaWRhdGlvbi5taW4udmFsdWUAACV7MH0gbXVzdCBiZSBhIHZhbHVlIG5vdCBsZXNzIHRoYW4gezF9AAAUdmFsaWRhdGlvbi5tYXgudmFsdWUAACh7MH0gbXVzdCBiZSBhIHZhbHVlIG5vdCBncmVhdGVyIHRoYW4gezF9AAAVdmFsaWRhdGlvbi5kb21haW5uYW1lAAAfezB9IG11c3QgYmUgYSB2YWxpZCBkb21haW4gbmFtZQAAFXZhbGlkYXRpb24ubWluLmxlbmd0aAAALHswfSBtdXN0IGJlIGEgdmFsdWUgd2l0aCBhIG1pbiBsZW5ndGggb2YgezF9AAAOdmFsaWRhdGlvbi51cmwAABd7MH0gbXVzdCBiZSBhIHZhbGlkIFVSTAAAFXZhbGlkYXRpb24ubWF4Lmxlbmd0aAAALHswfSBtdXN0IGJlIGEgdmFsdWUgd2l0aCBhIG1heCBsZW5ndGggb2YgezF9AAAWdmFsaWRhdGlvbi5tYXRjaHZhbHVlcwAAHlRoZSB2YWx1ZXMgb2YgezB9IGlzIG5vdCB2YWxpZAABIgAAAAEiAAAAASIAAQAAA2ZvbwAAA2Jhcg==";
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
        byte[] base64 = CodecUtils.encodeBase64(foo);
                
        //then
        assertThat(base64, not(nullValue()));
    }
    
    @Test
    void testBase64EncoderConcurrent() {
        MatcherAssert.assertThat(t -> {
            //given
            String foo = UUID.randomUUID().toString();

            //when
            byte[] base64 = CodecUtils.encodeBase64(foo);
            
            return base64 != null;
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    void testBase64Dencoder() {
        //given
        String foo = UUID.randomUUID().toString();

        //when
        byte[] base64Encoded = CodecUtils.encodeBase64(foo);
        byte[] base64Decoded = CodecUtils.decodeBase64(new String(base64Encoded, StandardCharsets.UTF_8));
                
        //then
        assertThat(new String(base64Decoded, StandardCharsets.UTF_8), equalTo(foo));
    }
    
    @Test
    void testBase64DencoderConcurrent() {
        MatcherAssert.assertThat(t -> {
            //given
            String foo = UUID.randomUUID().toString();

            //when
            byte[] base64Encoded = CodecUtils.encodeBase64(foo);
            byte[] base64Decoded = CodecUtils.decodeBase64(new String(base64Encoded, StandardCharsets.UTF_8));
            
            return new String(base64Decoded, StandardCharsets.UTF_8).equals(foo);
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
}