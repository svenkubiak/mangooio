package io.mangoo.utils;

import io.mangoo.TestExtension;
import io.mangoo.models.Car;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.llorllale.cactoos.matchers.RunsInThreads;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class JsonUtilsTest {
    private static final String expectedJson = "{\"brand\":null,\"doors\":0,\"foo\":\"blablabla\"}";

    @Test
    void testToJson() {
        //given
        Car car = new Car();
        
        //when
        String json = JsonUtils.toJson(car);
        
        //then
        assertThat(json, not(nullValue()));
        assertThat(json, equalTo(expectedJson));
    }
    
    @Test
    void testConcurrentToJson() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String uuid = UUID.randomUUID().toString();
            Car car = new Car(uuid);
            
            //when
            String json = JsonUtils.toJson(car);
            
            // then
            return json.equals("{\"brand\":null,\"doors\":0,\"foo\":\"blablabla\",\"id\":\"" + uuid + "\"}");
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }

    @Test
    void testFromJsonToClass() {
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
    void testConcurrentFromJsonToClass() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            //given
            String uuid = UUID.randomUUID().toString(); 
            String json = "{\"brand\":null,\"doors\":0,\"foo\":\"" + uuid + "\"}";
            
            ///when
            Car car = JsonUtils.fromJson(json, Car.class);
            
            // then
            return car.brand == null && car.doors == 0 && car.foo.equals(uuid);
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
}