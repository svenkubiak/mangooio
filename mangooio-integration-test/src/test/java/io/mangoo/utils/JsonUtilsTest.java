package io.mangoo.utils;

import tools.jackson.databind.ObjectMapper;
import io.mangoo.routing.bindings.UnprocessableContent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
class JsonUtilsTest {

    @Test
    void testToJson() {
        //given
        TestObject object = new TestObject("John", 30, true);

        //when
        String json = JsonUtils.toJson(object);

        //then
        assertThat(json, not(emptyString()));
        assertThat(json, containsString("John"));
        assertThat(json, containsString("30"));
        assertThat(json, containsString("true"));
    }

    @Test
    void testToJsonWithNulls() {
        //given
        TestObjectWithNulls object = new TestObjectWithNulls("John", null, true);

        //when
        String json = JsonUtils.toJson(object);

        //then
        assertThat(json, not(emptyString()));
        assertThat(json, containsString("John"));
        assertThat(json, containsString("true"));
        // null values should be excluded due to NON_NULL configuration
        assertThat(json, not(containsString("null")));
    }

    @Test
    void testToJsonWithDates() {
        //given
        LocalDate date = LocalDate.of(2023, 12, 25);
        LocalDateTime dateTime = LocalDateTime.of(2023, 12, 25, 14, 30, 45);
        TestObjectWithDates object = new TestObjectWithDates(date, dateTime);

        //when
        String json = JsonUtils.toJson(object);

        //then
        assertThat(json, not(emptyString()));
        assertThat(json, containsString("2023-12-25"));
        assertThat(json, containsString("14:30:45"));
    }

    @Test
    void testToJsonNullInput() {
        //given
        Object object = null;

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> JsonUtils.toJson(object));
        assertThat(exception.getMessage(), containsString("object can not be null"));
    }

    @Test
    void testToPrettyJson() {
        //given
        TestObject object = new TestObject("John", 30, true);

        //when
        String json = JsonUtils.toPrettyJson(object);

        //then
        assertThat(json, not(emptyString()));
        assertThat(json, containsString("John"));
        assertThat(json, containsString("30"));
        assertThat(json, containsString("true"));
        assertThat(json, containsString("\n"));
    }

    @Test
    void testToPrettyJsonWithNulls() {
        //given
        TestObjectWithNulls object = new TestObjectWithNulls("John", null, true);

        //when
        String json = JsonUtils.toPrettyJson(object);

        //then
        assertThat(json, not(emptyString()));
        assertThat(json, containsString("John"));
        assertThat(json, containsString("true"));
        assertThat(json, containsString("\n"));
    }

    @Test
    void testToPrettyJsonNullInput() {
        //given
        Object object = null;

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> JsonUtils.toPrettyJson(object));
        assertThat(exception.getMessage(), containsString("object can not be null"));
    }

    @Test
    void testToObject() {
        //given
        String json = "{\"name\":\"John\",\"age\":30,\"active\":true}";

        //when
        TestObject object = JsonUtils.toObject(json, TestObject.class);

        //then
        assertThat(object, not(nullValue()));
        assertThat(object.getName(), equalTo("John"));
        assertThat(object.getAge(), equalTo(30));
        assertThat(object.isActive(), equalTo(true));
    }

    @Test
    void testToObjectWithDates() {
        //given
        String json = "{\"date\":\"2023-12-25\",\"dateTime\":\"2023-12-25T14:30:45\"}";

        //when
        TestObjectWithDates object = JsonUtils.toObject(json, TestObjectWithDates.class);

        //then
        assertThat(object, not(nullValue()));
        assertThat(object.getDate(), equalTo(LocalDate.of(2023, 12, 25)));
        assertThat(object.getDateTime(), equalTo(LocalDateTime.of(2023, 12, 25, 14, 30, 45)));
    }

    @Test
    void testToObjectWithInvalidJson() {
        //given
        String json = "invalid json";

        //when
        TestObject object = JsonUtils.toObject(json, TestObject.class);

        //then
        assertThat(object, nullValue());
    }

    @Test
    void testToObjectWithNullJson() {
        //given
        String json = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JsonUtils.toObject(json, TestObject.class));
        assertThat(exception.getMessage(), containsString("json can not be null"));
    }

    @Test
    void testToObjectWithNullClass() {
        //given
        String json = "{\"name\":\"John\"}";

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> JsonUtils.toObject(json, null));
        assertThat(exception.getMessage(), containsString("class can not be null"));
    }

    @Test
    void testToObjectWithEmptyJson() {
        //given
        String json = "";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JsonUtils.toObject(json, TestObject.class));
        assertThat(exception.getMessage(), containsString("json can not be null"));
    }

    @Test
    void testToObjectWithFallback() {
        //given
        String json = "{\"name\":\"John\",\"age\":30,\"active\":true}";

        //when
        TestObject object = JsonUtils.toObjectWithFallback(json, TestObject.class);

        //then
        assertThat(object, not(nullValue()));
        assertThat(object.getName(), equalTo("John"));
        assertThat(object.getAge(), equalTo(30));
        assertThat(object.isActive(), equalTo(true));
    }

    @Test
    void testToObjectWithFallbackInvalidJson() {
        //given
        String json = "invalid json";

        //when
        TestObject object = JsonUtils.toObjectWithFallback(json, TestObject.class);

        //then
        assertThat(object, not(nullValue()));
        // Should return a new instance with default values
        assertThat(object.getName(), nullValue());
        assertThat(object.getAge(), equalTo(0));
        assertThat(object.isActive(), equalTo(false));
    }

    @Test
    void testToObjectWithFallbackClassWithoutDefaultConstructor() {
        //given
        String json = "invalid json";

        //when
        Object object = JsonUtils.toObjectWithFallback(json, TestObjectWithoutDefaultConstructor.class);

        //then
        assertThat(object, instanceOf(UnprocessableContent.class));
    }

    @Test
    void testToObjectWithFallbackNullJson() {
        //given
        String json = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JsonUtils.toObjectWithFallback(json, TestObject.class));
        assertThat(exception.getMessage(), containsString("json can not be null"));
    }

    @Test
    void testToObjectWithFallbackNullClass() {
        //given
        String json = "{\"name\":\"John\"}";

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> JsonUtils.toObjectWithFallback(json, null));
        assertThat(exception.getMessage(), containsString("class can not be null"));
    }

    @Test
    void testToFlatMap() {
        //given
        String json = "{\"name\":\"John\",\"age\":30,\"address\":{\"street\":\"Main St\",\"city\":\"New York\"}}";

        //when
        Map<String, String> flatMap = JsonUtils.toFlatMap(json);

        //then
        assertThat(flatMap.size(), equalTo(4));
        assertThat(flatMap.get("name"), equalTo("John"));
        assertThat(flatMap.get("age"), equalTo("30"));
        assertThat(flatMap.get("address.street"), equalTo("Main St"));
        assertThat(flatMap.get("address.city"), equalTo("New York"));
    }

    @Test
    void testToFlatMapWithArray() {
        //given
        String json = "{\"name\":\"John\",\"hobbies\":[\"reading\",\"swimming\",\"coding\"]}";

        //when
        Map<String, String> flatMap = JsonUtils.toFlatMap(json);

        //then
        assertThat(flatMap.size(), equalTo(4));
        assertThat(flatMap.get("name"), equalTo("John"));
        assertThat(flatMap.get("hobbies[0]"), equalTo("reading"));
        assertThat(flatMap.get("hobbies[1]"), equalTo("swimming"));
        assertThat(flatMap.get("hobbies[2]"), equalTo("coding"));
    }

    @Test
    void testToFlatMapWithNestedObjects() {
        //given
        String json = "{\"user\":{\"profile\":{\"name\":\"John\",\"age\":30},\"settings\":{\"theme\":\"dark\"}}}";

        //when
        Map<String, String> flatMap = JsonUtils.toFlatMap(json);

        //then
        assertThat(flatMap.size(), equalTo(3));
        assertThat(flatMap.get("user.profile.name"), equalTo("John"));
        assertThat(flatMap.get("user.profile.age"), equalTo("30"));
        assertThat(flatMap.get("user.settings.theme"), equalTo("dark"));
    }

    @Test
    void testToFlatMapWithEmptyObject() {
        //given
        String json = "{}";

        //when
        Map<String, String> flatMap = JsonUtils.toFlatMap(json);

        //then
        assertThat(flatMap.size(), equalTo(0));
    }

    @Test
    void testToFlatMapWithSimpleValue() {
        //given
        String json = "\"Hello, World!\"";

        //when
        Map<String, String> flatMap = JsonUtils.toFlatMap(json);

        //then
        assertThat(flatMap.size(), equalTo(1));
    }

    @Test
    void testToFlatMapWithArrayOfObjects() {
        //given
        String json = "{\"users\":[{\"name\":\"John\",\"age\":30},{\"name\":\"Jane\",\"age\":25}]}";

        //when
        Map<String, String> flatMap = JsonUtils.toFlatMap(json);

        //then
        assertThat(flatMap.size(), equalTo(4));
        assertThat(flatMap.get("users[0].name"), equalTo("John"));
        assertThat(flatMap.get("users[0].age"), equalTo("30"));
        assertThat(flatMap.get("users[1].name"), equalTo("Jane"));
        assertThat(flatMap.get("users[1].age"), equalTo("25"));
    }

    @Test
    void testToFlatMapWithNullValues() {
        //given
        String json = "{\"name\":\"John\",\"age\":null,\"address\":{\"street\":\"Main St\",\"city\":null}}";

        //when
        Map<String, String> flatMap = JsonUtils.toFlatMap(json);

        //then
        assertThat(flatMap.size(), equalTo(4));
        assertThat(flatMap.get("name"), equalTo("John"));
        assertThat(flatMap.get("age"), equalTo("null"));
        assertThat(flatMap.get("address.street"), equalTo("Main St"));
        assertThat(flatMap.get("address.city"), equalTo("null"));
    }

    @Test
    void testToFlatMapWithInvalidJson() {
        //given
        String json = "invalid json";

        //when
        Map<String, String> flatMap = JsonUtils.toFlatMap(json);

        //then
        assertThat(flatMap.size(), equalTo(0));
    }

    @Test
    void testToFlatMapNullInput() {
        //given
        String json = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JsonUtils.toFlatMap(json));
        assertThat(exception.getMessage(), containsString("json can not be null"));
    }

    @Test
    void testToFlatMapWithEmptyString() {
        //given
        String json = "";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JsonUtils.toFlatMap(json));
        assertThat(exception.getMessage(), containsString("json can not be null"));
    }

    @Test
    void testGetMapperConsistency() {
        //when
        ObjectMapper mapper1 = JsonUtils.getMapper();
        ObjectMapper mapper2 = JsonUtils.getMapper();

        //then
        assertThat(mapper1, sameInstance(mapper2));
    }

    private static class TestObject {
        private String name;
        private int age;
        private boolean active;

        public TestObject() {}

        public TestObject(String name, int age, boolean active) {
            this.name = name;
            this.age = age;
            this.active = active;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public int getAge() { return age; }
        public void setAge(int age) { this.age = age; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestObject that = (TestObject) o;
            return age == that.age && active == that.active && Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age, active);
        }
    }

    private static class TestObjectWithNulls {
        private String name;
        private Integer age;
        private Boolean active;

        public TestObjectWithNulls() {}

        public TestObjectWithNulls(String name, Integer age, Boolean active) {
            this.name = name;
            this.age = age;
            this.active = active;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Integer getAge() { return age; }
        public void setAge(Integer age) { this.age = age; }
        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }

    private static class TestObjectWithDates {
        private LocalDate date;
        private LocalDateTime dateTime;

        public TestObjectWithDates() {}

        public TestObjectWithDates(LocalDate date, LocalDateTime dateTime) {
            this.date = date;
            this.dateTime = dateTime;
        }

        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        public LocalDateTime getDateTime() { return dateTime; }
        public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
    }

    private static class TestObjectWithoutDefaultConstructor {
        private String name;

        public TestObjectWithoutDefaultConstructor(String name) {
            this.name = name;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
