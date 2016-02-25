package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import com.jayway.jsonpath.ReadContext;

import io.advantageous.boon.json.JsonSerializer;
import io.advantageous.boon.json.JsonSerializerFactory;
import io.mangoo.models.Car;

/**
 * 
 * @author svenkubiak
 *
 */
public class JsonUtilsTest {
    private final String expectedJson = "{\"brand\":null,\"doors\":0,\"foo\":\"blablabla\"}";

    @Test
    public void toJsonTest() {
        //given
        Car car = new Car();
        
        //when
        String json = JsonUtils.toJson(car);
        
        //then
        assertThat(json, not(nullValue()));
        assertThat(json, equalTo(expectedJson));
    }
    
    @Test
    public void fromJsonTest2() {
        //given
        String json = "{\"brand\":null,\"doors\":0,\"foo\":\"blablabla\"}";
        
        //when
        ReadContext readContext = JsonUtils.fromJson(json);
        
        //then
        assertThat(readContext, not(nullValue()));
    }
    
    @Test
    public void fromJsonClassTest() {
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
    public void customSerializerTest(){
        //given
        JsonSerializerFactory jsonSerializerFactory = new JsonSerializerFactory();
        jsonSerializerFactory.useAnnotations();
        JsonSerializer serializer = jsonSerializerFactory.create();
        
        //then
        JsonUtils.withJsonSerializer(serializer);
    }
}