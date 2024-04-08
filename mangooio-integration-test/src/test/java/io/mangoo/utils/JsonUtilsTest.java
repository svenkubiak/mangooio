package io.mangoo.utils;

import io.mangoo.TestExtension;
import io.mangoo.models.Car;
import io.mangoo.test.concurrent.ConcurrentRunner;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class JsonUtilsTest {
    private static final String EXPECTED_JSON = "{\"brand\":null,\"doors\":0,\"foo\":\"blablabla\"}";
    private static final String EXPECTED_PRETTY_JSON = "{\n  \"brand\" : null,\n  \"doors\" : 0,\n  \"foo\" : \"blablabla\"\n}";

    private static final String JSON = """
            {"widget": {
                "debug": "on",
                "window": {
                    "title": "Sample Konfabulator Widget",
                    "name": "main_window",
                    "width": 500,
                    "height": 500
                },
                "image": {
                    "src": "Images/Sun.png",
                    "name": "sun1",
                    "hOffset": 250,
                    "vOffset": 250,
                    "alignment": "center"
                },
                "text": {
                    "data": "Click Here",
                    "size": 36,
                    "style": "bold",
                    "name": "text1",
                    "hOffset": 250,
                    "vOffset": 100,
                    "alignment": "center"
                }
            }}
            """;

    @Test
    void testToJson() {
        //given
        Car car = new Car();
        
        //when
        String json = JsonUtils.toJson(car);
        
        //then
        assertThat(json, not(nullValue()));
        assertThat(json, equalTo(EXPECTED_JSON));
    }

    @Test
    void testToPrettyJson() {
        //given
        Car car = new Car();

        //when
        String json = JsonUtils.toPrettyJson(car);

        //then
        assertThat(json, not(nullValue()));
        assertThat(json, equalTo(EXPECTED_PRETTY_JSON));
    }

    @Test
    void testFlatMap() {
        //when
        Map<String, String> map = JsonUtils.toFlatMap(JSON);

        //then
        assertThat(map, not(nullValue()));
        assertThat(map.get("widget.debug"), equalTo("on"));
        assertThat(map.get("widget.image.name"), equalTo("sun1"));
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
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }

    @Test
    void testFromJsonToClass() {
        //given
        String json = "{\"brand\":null,\"doors\":0,\"foo\":\"blablabla\"}";
        
        //when
        Car car = JsonUtils.toObject(json, Car.class);
        
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
            Car car = JsonUtils.toObject(json, Car.class);
            
            // then
            return car.brand == null && car.doors == 0 && car.foo.equals(uuid);
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }
}