package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.llorllale.cactoos.matchers.RunsInThreads;

import com.jayway.jsonpath.ReadContext;

import io.mangoo.TestSuite;
import io.mangoo.models.Car;

/**
 * 
 * @author svenkubiak
 *
 */
public class JsonUtilsTest {
    private static final String expectedJson = "{\"brand\":null,\"doors\":0,\"foo\":\"blablabla\"}";

    @Test
    public void testToJson() {
        //given
        Car car = new Car();
        
        //when
        String json = JsonUtils.toJson(car);
        
        //then
        assertThat(json, not(nullValue()));
        assertThat(json, equalTo(expectedJson));
    }
    
    @Test
    public void testConcurrentToJson() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String uuid = UUID.randomUUID().toString();
            Car car = new Car(uuid);
            
            //when
            String json = JsonUtils.toJson(car);
            
            // then
            return json.equals("{\"brand\":null,\"doors\":0,\"foo\":\"blablabla\",\"id\":\"" + uuid + "\"}");
        }, new RunsInThreads<>(new AtomicInteger(), TestSuite.THREADS));
    }
    
    @Test
    public void testFromJson() {
        //given
        String json = "{\"brand\":null,\"doors\":0,\"foo\":\"blablabla\"}";
        
        //when
        ReadContext readContext = JsonUtils.fromJson(json);
        
        //then
        assertThat(readContext, not(nullValue()));
        assertThat(readContext.read("$.foo"), equalTo("blablabla"));
    }
    
    @Test
    public void testConcurrentFromJson() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String uuid = UUID.randomUUID().toString(); 
            String json = "{\"brand\":null,\"doors\":0,\"foo\":\"" + uuid + "\"}";
            
            //when
            ReadContext readContext = JsonUtils.fromJson(json);
            
            // then
            return readContext.read("$.foo").equals(uuid);
        }, new RunsInThreads<>(new AtomicInteger(), TestSuite.THREADS));
    }
    
    @Test
    public void testFromJsonToClass() {
        //given
        String json = "{\"brand\":null,\"doors\":0,\"foo\":\"blablabla\"}";
        
        //when
        Car car = JsonUtils.fromJson(json, Car.class);
        
        //then
        assertThat(car, not(nullValue()));
        assertThat(car.brand, equalTo(null));
        assertThat(car.doors, equalTo(0));
        assertThat(car.foo, equalTo("blablabla"));
    }
    
    @Test
    public void testConcurrentFromJsonToClass() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String uuid = UUID.randomUUID().toString(); 
            String json = "{\"brand\":null,\"doors\":0,\"foo\":\"" + uuid + "\"}";
            
            ///when
            Car car = JsonUtils.fromJson(json, Car.class);
            
            // then
            return car.brand == null && car.doors == 0 && car.foo.equals(uuid);
        }, new RunsInThreads<>(new AtomicInteger(), TestSuite.THREADS));
    }
}