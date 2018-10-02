package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.llorllale.cactoos.matchers.RunsInThreads;

import io.mangoo.TestExtension;
import io.mangoo.enums.Default;

/**
 * 
 * @author svenkubiak
 *
 */
public class MangooUtilsTest {
    @Test
    public void testGetVersion() throws InterruptedException {
        //then
        assertThat(MangooUtils.getVersion(), not(nullValue()));
    } 
    
    @Test
    public void testCopyMap() {
        //given
        String value1 = UUID.randomUUID().toString();
        String value2 = UUID.randomUUID().toString();
        
        //when
        Map<String, String> map = new HashMap<>();
        map.put("value1", value1);
        map.put("value2", value2);
        Map<String, String> copy = MangooUtils.copyMap(map);
        
        //then
        assertThat(copy.get("value1"), equalTo(value1));
        assertThat(copy.get("value2"), equalTo(value2));
    }
    
    @Test
    public void testCopyMapConcurrent() {
        MatcherAssert.assertThat(t -> {
            //given
            String value1 = UUID.randomUUID().toString();
            String value2 = UUID.randomUUID().toString();
            
            //when
            Map<String, String> map = new HashMap<>();
            map.put("value1", value1);
            map.put("value2", value2);
            Map<String, String> copy = MangooUtils.copyMap(map);
            
            // then
            return copy.get("value1").equals(value1) && copy.get("value2").equals(value2);
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }

    @Test
    public void testRandomString() {
        //given
        String string = MangooUtils.randomString(32);
        
        //then
        assertThat(string, not(nullValue()));
        assertThat(string.length(), equalTo(32));
    }
    
    @Test
    public void testConcurrentRandomString() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            // given
            int size = (int) (Math.random() * (64 - 16)) + 16;
            String secret = MangooUtils.randomString(size);
            
            // then
            return secret.length() == size;
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test()
    public void testInvalidMinRandomString() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            //given
            MangooUtils.randomString(0);
          });
    }
    
    @Test()
    public void testInvalidMaxRandomString() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            MangooUtils.randomString(257);
          });
    }
    
    @Test
    public void testCloseQuietly() throws IOException {
        //given
        File file = new File(UUID.randomUUID().toString());
        file.createNewFile();
        InputStream inputStream = new FileInputStream(file);
        
        //when
        MangooUtils.closeQuietly(inputStream);
        
        //then
        file.delete();
        assertThat(file.exists(), equalTo(false));
    } 
    
    @Test
    public void testReadableFileSize() {
        //given
        long size = 25165824;

        //when
        String readableSize = MangooUtils.readableFileSize(size);
        
        //then
        assertThat(readableSize, not(nullValue()));
        assertThat(readableSize, equalTo("24 MB"));
    }
    
    @Test
    public void testResourceExists() {
        //when
        boolean exists = MangooUtils.resourceExists(Default.MODEL_CONF.toString());
        
        //then
        assertThat(exists, equalTo(true));
    }
    
    @Test
    public void testResourceExistsConcurrent() {
        MatcherAssert.assertThat(t -> {
            return MangooUtils.resourceExists(Default.MODEL_CONF.toString());
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    public void testResourceNotExists() {
        //when
        boolean exists = MangooUtils.resourceExists("foo.txt");
        
        //then
        assertThat(exists, equalTo(false));
    }
}