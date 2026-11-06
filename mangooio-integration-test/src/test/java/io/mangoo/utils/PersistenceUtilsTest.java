package io.mangoo.utils;

import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@TestMethodOrder(MethodOrderer.MethodName.class)
class PersistenceUtilsTest {
    private Map<String, String> originalCollections;

    @BeforeEach
    void setUp() throws Exception {
        Field collectionsField = PersistenceUtils.class.getDeclaredField("COLLECTIONS");
        collectionsField.setAccessible(true);
        originalCollections = new java.util.concurrent.ConcurrentHashMap<>((Map<String, String>) collectionsField.get(null));
        
        ((Map<String, String>) collectionsField.get(null)).clear();
    }

    @AfterEach
    void tearDown() throws Exception {
        Field collectionsField = PersistenceUtils.class.getDeclaredField("COLLECTIONS");
        collectionsField.setAccessible(true);
        Map<String, String> collections = (Map<String, String>) collectionsField.get(null);
        collections.clear();
        collections.putAll(originalCollections);
    }

    @Test
    void testAddCollection() {
        //given
        String key = "java.lang.String";
        String value = "test-value";
        
        //when
        PersistenceUtils.addCollection(key, value);
        
        //then
        String retrievedValue = PersistenceUtils.getCollectionName(String.class);
        assertThat(retrievedValue, equalTo(value));
    }

    @Test
    void testAddCollectionWithDifferentKeys() {
        //given
        String key1 = "java.lang.String";
        String value1 = "value1";
        String key2 = "java.lang.Integer";
        String value2 = "value2";
        
        //when
        PersistenceUtils.addCollection(key1, value1);
        PersistenceUtils.addCollection(key2, value2);
        
        //then
        String retrievedValue1 = PersistenceUtils.getCollectionName(String.class);
        String retrievedValue2 = PersistenceUtils.getCollectionName(Integer.class);
        assertThat(retrievedValue1, equalTo(value1));
        assertThat(retrievedValue2, equalTo(value2));
    }

    @Test
    void testAddCollectionWithEmptyKey() {
        //given
        String key = "";
        String value = "test-value";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PersistenceUtils.addCollection(key, value));
        assertThat(exception.getMessage(), containsString("key can not be null"));
    }

    @Test
    void testAddCollectionWithEmptyValue() {
        //given
        String key = "test-key";
        String value = "";

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PersistenceUtils.addCollection(key, value));
        assertThat(exception.getMessage(), containsString("value can not be null or blank"));
    }

    @Test
    void testAddCollectionWithNullKey() {
        //given
        String key = null;
        String value = "test-value";
        
        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PersistenceUtils.addCollection(key, value));
        assertThat(exception.getMessage(), containsString("key can not be null"));
    }

    @Test
    void testAddCollectionWithNullValue() {
        //given
        String key = "test-key";
        String value = null;
        
        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PersistenceUtils.addCollection(key, value));
        assertThat(exception.getMessage(), containsString("value can not be null"));
    }

    @Test
    void testGetCollectionNameWithNonExistentClass() {
        //given
        // No collection added for this class
        
        //when
        String collectionName = PersistenceUtils.getCollectionName(Integer.class);
        
        //then
        assertThat(collectionName, nullValue());
    }

    @Test
    void testGetCollectionNameWithNullClass() {
        //given
        Class<?> clazz = null;
        
        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class, 
                () -> PersistenceUtils.getCollectionName(clazz));
        assertThat(exception.getMessage(), containsString("class can not be null"));
    }

    @Test
    void testAddCollectionOverwriteExisting() {
        //given
        String key = "java.lang.String";
        String value1 = "original-value";
        String value2 = "updated-value";
        
        //when
        PersistenceUtils.addCollection(key, value1);
        String retrievedValue1 = PersistenceUtils.getCollectionName(String.class);
        PersistenceUtils.addCollection(key, value2);
        String retrievedValue2 = PersistenceUtils.getCollectionName(String.class);
        
        //then
        assertThat(retrievedValue1, equalTo(value1));
        assertThat(retrievedValue2, equalTo(value2));
    }

    @Test
    void testAddCollectionWithWhitespaceOnly() {
        //given
        String key = "   ";
        String value = "   ";
        
        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> PersistenceUtils.addCollection(key, value));
        assertThat(exception.getMessage(), containsString("key can not be null"));
    }
}
