package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.llorllale.cactoos.matchers.RunsInThreads;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.routing.bindings.Form;

@ExtendWith({TestExtension.class})
public class CodecUtilsTest {
    private static final String SERIALIZED = "rO0ABXNyAB9pby5tYW5nb28ucm91dGluZy5iaW5kaW5ncy5Gb3Jtr0x38MTUh5ACAANaAARrZWVwWgAJc3VibWl0dGVkTAAIdmFsdWVNYXB0AA9MamF2YS91dGlsL01hcDt4cgAkaW8ubWFuZ29vLnJvdXRpbmcuYmluZGluZ3MuVmFsaWRhdG9y3cEYQhBJCTwCAANMAAZlcnJvcnNxAH4AAUwACG1lc3NhZ2VzdAAZTGlvL21hbmdvby9pMThuL01lc3NhZ2VzO0wABnZhbHVlc3EAfgABeHBzcgARamF2YS51dGlsLkhhc2hNYXAFB9rBwxZg0QMAAkYACmxvYWRGYWN0b3JJAAl0aHJlc2hvbGR4cD9AAAAAAAAAdwgAAAAQAAAAAHhzcgAXaW8ubWFuZ29vLmkxOG4uTWVzc2FnZXNo6ui27qLkugIAAUwACGRlZmF1bHRzcQB+AAF4cHNxAH4ABT9AAAAAAAAYdwgAAAAgAAAADnQADFJFUVVJUkVEX0tFWXQAD3swfSBpcyByZXF1aXJlZHQACU1BVENIX0tFWXQAEnswfSBtdXN0IG1hdGNoIHsxfXQACVJBTkdFX0tFWXQAKHswfSBtdXN0IGhhdmUgYSBzaXplIGJldHdlZW4gezF9IGFuZCB7Mn10AAhJUFY2X0tFWXQAIHswfSBtdXN0IGJlIGEgdmFsaWQgSVB2NiBhZGRyZXNzdAALTlVNRVJJQ19LRVl0ABt7MH0gbXVzdCBiZSBhIG51bWVyaWMgdmFsdWV0AAdNQVhfS0VZdAAfezB9IG11c3QgaGF2ZSBhIHNpemUgb2YgbWF4IHsxfXQAD0RPTUFJTl9OQU1FX0tFWXQAH3swfSBtdXN0IGJlIGEgdmFsaWQgZG9tYWluIG5hbWV0AA9FWEFDVF9NQVRDSF9LRVl0ABp7MH0gbXVzdCBleGFjdGx5IG1hdGNoIHsxfXQACUVNQUlMX0tFWXQAIXswfSBtdXN0IGJlIGEgdmFsaWQgZU1haWwgYWRkcmVzc3QAEE1BVENIX1ZBTFVFU19LRVl0AB5UaGUgdmFsdWVzIG9mIHswfSBpcyBub3QgdmFsaWR0AAdVUkxfS0VZdAAXezB9IG11c3QgYmUgYSB2YWxpZCBVUkx0AAhJUFY0X0tFWXQAIHswfSBtdXN0IGJlIGEgdmFsaWQgSVB2NCBhZGRyZXNzdAAHTUlOX0tFWXQAI3swfSBtdXN0IGhhdmUgYSBsZWFzdCBhIHNpemUgb2YgezF9dAAJUkVHRVhfS0VZdAAOezB9IGlzIGludmFsaWR4c3EAfgAFP0AAAAAAAAx3CAAAABAAAAABdAADZm9vdAADYmFyeAAAc3EAfgAFP0AAAAAAAAB3CAAAABAAAAAAeA==";
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
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
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
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
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