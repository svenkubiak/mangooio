package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.llorllale.cactoos.matchers.RunsInThreads;

import io.mangoo.TestSuite;

public class ByteUtilsTest {
    
    @Test
    public void testBitLengthAsByte() {
        //given
        String string = "jhjfksHjKSHjdkhsjdk2222";
        
        //then
        assertThat(ByteUtils.bitLength(string.getBytes()), equalTo(184));
    }
    
    @Test
    public void testBitLengthAsString() {
        //given
        String string = "jhjfksHjKSHjdfdfdsfdsfskhsjdk2222";
        
        //then
        assertThat(ByteUtils.bitLength(string), equalTo(264));
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
        Map<String, String> copy = ByteUtils.copyMap(map);
        
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
            Map<String, String> copy = ByteUtils.copyMap(map);
            
            // then
            return copy.get("value1").equals(value1) && copy.get("value2").equals(value2);
        }, new RunsInThreads<>(new AtomicInteger(), TestSuite.THREADS));
    }
}
