package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.UUID;

import org.junit.Test;

import com.jayway.jsonpath.ReadContext;

import io.mangoo.models.Car;
import io.mangoo.test.utils.ConcurrentRunner;

/**
 * 
 * @author svenkubiak
 *
 */
public class JsonUtilsTest {
    private final String expectedJson = "{\"brand\":null,\"doors\":0,\"foo\":\"blablabla\"}";

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
        Runnable runnable = () -> {
            for (int j=0; j < 50; j++) {
                //given
                String uuid = UUID.randomUUID().toString();
                Car car = new Car(uuid);
                
                //when
                String json = JsonUtils.toJson(car);
                
                //then
                assertThat(json, not(nullValue()));
                assertThat(json, equalTo("{\"brand\":null,\"doors\":0,\"foo\":\"blablabla\",\"id\":\"" + uuid + "\"}"));   
            }
        };
        
        ConcurrentRunner.create()
            .withRunnable(runnable)
            .withThreads(50)
            .run();
    }
    
    @Test
    public void testFromJson() {
        //given
        String json = "{\"brand\":null,\"doors\":0,\"foo\":\"blablabla\"}";
        
        //when
        ReadContext readContext = JsonUtils.fromJson(json);
        
        //then
        assertThat(readContext, not(nullValue()));
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
}