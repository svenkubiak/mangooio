package io.mangoo.utils;

import io.mangoo.TestExtension;
import io.mangoo.test.concurrent.ConcurrentRunner;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Execution(ExecutionMode.CONCURRENT)
class MangooUtilsTest {
    @Test
    void testGetVersion() throws InterruptedException {
        //then
        assertThat(MangooUtils.getVersion(), not(nullValue()));
    } 
    
    @Test
    void testCopyMap() {
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
    void testCopyMapConcurrent() {
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
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }

    @Test
    void testRandomString() {
        //given
        String string = MangooUtils.randomString(32);
        
        //then
        assertThat(string, not(nullValue()));
        assertThat(string.length(), equalTo(32));
    }

    @Test
    void testGetLanguages() {
        //given
        Set<String> languages = MangooUtils.getLanguages();

        //then
        assertThat(languages, not(nullValue()));
        assertThat(languages.size(), equalTo(3));
        assertThat(languages.contains("de"), equalTo(true));
        assertThat(languages.contains("en"), equalTo(true));
        assertThat(languages.contains("fr"), equalTo(true));
        assertThat(languages.contains("es"), equalTo(false));
    }
    
    @Test
    void testConcurrentRandomString() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            // given
            int size = (int) (Math.random() * (64 - 16)) + 16;
            String secret = MangooUtils.randomString(size);
            
            // then
            return secret.length() == size;
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test()
    void testInvalidMinRandomString() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            //given
            MangooUtils.randomString(0);
        }, "Failed to test invalid min number of random string");
    }
    
    @Test()
    void testInvalidMaxRandomString() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            MangooUtils.randomString(257);
        }, "Failed to test invalid max number of random string");
    }
    
    @Test
    void testCloseQuietly() throws IOException {
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
    void testReadableFileSize() {
        //given
        long size = 25165824;

        //when
        String readableSize = MangooUtils.readableFileSize(size);
        
        //then
        assertThat(readableSize, not(nullValue()));
        assertThat(readableSize, equalTo("24 MB"));
    }
    
    @Test
    void testResourceExists() {
        //when
        boolean exists = MangooUtils.resourceExists("attachment.txt");
        
        //then
        assertThat(exists, equalTo(true));
    }
    
    @Test
    void testResourceExistsConcurrent() {
        MatcherAssert.assertThat(t -> {
            return MangooUtils.resourceExists("attachment.txt");
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    void testResourceNotExists() {
        //when
        boolean exists = MangooUtils.resourceExists("foo.txt");
        
        //then
        assertThat(exists, equalTo(false));
    }
    
    @Test
    void testReadFileToStringPath() throws IOException {
        //given
        File file = new File(UUID.randomUUID().toString());
        String uuid = UUID.randomUUID().toString();
        Files.write(file.toPath(), uuid.getBytes());
        
        //when
        String content = MangooUtils.readFileToString(file.toPath());
        
        //then
        assertThat(content, equalTo(uuid));
        assertThat(file.delete(), equalTo(true));
    }
    
    @Test
    void testReadFileToStringString() throws IOException {
        //given
        File file = new File(UUID.randomUUID().toString());
        String uuid = UUID.randomUUID().toString();
        Files.write(file.toPath(), uuid.getBytes());
        
        //when
        String content = MangooUtils.readFileToString(file.getAbsolutePath());
        
        //then
        assertThat(content, equalTo(uuid));
        assertThat(file.delete(), equalTo(true));
    }
    
    @Test
    void testReadResourceToString() {
        //given
        String resource = "attachment.txt";
        
        //when
        String content = MangooUtils.readResourceToString(resource);
        
        //then
        assertThat(content, equalTo("This is an attachment"));
    }
    
    @Test
    void testReadFileToStringPathConcurrent() {
        MatcherAssert.assertThat(t -> {
            //given
            File file = new File(UUID.randomUUID().toString());
            String uuid = CodecUtils.uuidV6();
            Files.write(file.toPath(), uuid.getBytes());
            
            //when
            String content = MangooUtils.readFileToString(file.toPath());
            
            // then
            return content.equals(uuid) && file.delete();
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    void testReadFileToStringStringConcurrent() {
        MatcherAssert.assertThat(t -> {
            //given
            File file = new File(UUID.randomUUID().toString());
            String uuid = UUID.randomUUID().toString();
            Files.write(file.toPath(), uuid.getBytes());
            
            //when
            String content = MangooUtils.readFileToString(file.getAbsolutePath());
            
            // then
            return content.equals(uuid) && file.delete();
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }

    @Test
    void testUUID() {
        //when
        String uuid = CodecUtils.uuidV6();

        //then
        assertThat(uuid, not(nullValue()));
    }

    @Test
    void testUUIDConcurrent() {
        MatcherAssert.assertThat(t -> {
            //when
            String uuid = CodecUtils.uuidV6();

            // then
            return StringUtils.isNotBlank(uuid);
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }
}