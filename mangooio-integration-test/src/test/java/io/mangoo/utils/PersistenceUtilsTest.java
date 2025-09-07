package io.mangoo.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.lang.reflect.Field;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
class PersistenceUtilsTest {

    private Map<String, String> originalCollections;

    @BeforeEach
    void setUp() throws Exception {
        // Save the original state of the COLLECTIONS map
        Field collectionsField = PersistenceUtils.class.getDeclaredField("COLLECTIONS");
        collectionsField.setAccessible(true);
        originalCollections = new java.util.concurrent.ConcurrentHashMap<>((Map<String, String>) collectionsField.get(null));
        
        // Clear the collections map for each test
        ((Map<String, String>) collectionsField.get(null)).clear();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Restore the original state of the COLLECTIONS map
        Field collectionsField = PersistenceUtils.class.getDeclaredField("COLLECTIONS");
        collectionsField.setAccessible(true);
        Map<String, String> collections = (Map<String, String>) collectionsField.get(null);
        collections.clear();
        collections.putAll(originalCollections);
    }

    @Test
    void testAddCollection() {
        //given
        String key = "test-key";
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
        String key1 = "key1";
        String value1 = "value1";
        String key2 = "key2";
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
    void testAddCollectionWithSameKey() {
        //given
        String key = "java.lang.String";
        String value1 = "value1";
        String value2 = "value2";
        
        //when
        PersistenceUtils.addCollection(key, value1);
        PersistenceUtils.addCollection(key, value2);
        
        //then
        String retrievedValue = PersistenceUtils.getCollectionName(String.class);
        assertThat(retrievedValue, equalTo(value2)); // Should be the last value
    }

    @Test
    void testAddCollectionWithEmptyKey() {
        //given
        String key = "";
        String value = "test-value";
        
        //when
        PersistenceUtils.addCollection(key, value);
        
        //then
        // Should not throw exception, empty string is valid
        String retrievedValue = PersistenceUtils.getCollectionName(String.class);
        assertThat(retrievedValue, equalTo(value));
    }

    @Test
    void testAddCollectionWithEmptyValue() {
        //given
        String key = "test-key";
        String value = "";
        
        //when
        PersistenceUtils.addCollection(key, value);
        
        //then
        // Should not throw exception, empty string is valid
        String retrievedValue = PersistenceUtils.getCollectionName(String.class);
        assertThat(retrievedValue, equalTo(value));
    }

    @Test
    void testAddCollectionWithNullKey() {
        //given
        String key = null;
        String value = "test-value";
        
        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class, 
                () -> PersistenceUtils.addCollection(key, value));
        assertThat(exception.getMessage(), containsString("key can not be null"));
    }

    @Test
    void testAddCollectionWithNullValue() {
        //given
        String key = "test-key";
        String value = null;
        
        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class, 
                () -> PersistenceUtils.addCollection(key, value));
        assertThat(exception.getMessage(), containsString("value can not be null"));
    }

    @Test
    void testAddCollectionWithBothNull() {
        //given
        String key = null;
        String value = null;
        
        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class, 
                () -> PersistenceUtils.addCollection(key, value));
        // Should throw for the first null parameter (key)
        assertThat(exception.getMessage(), containsString("key can not be null"));
    }

    @Test
    void testAddCollectionWithSpecialCharacters() {
        //given
        String key = "test-key-with-special-chars!@#$%^&*()";
        String value = "value-with-special-chars!@#$%^&*()";
        
        //when
        PersistenceUtils.addCollection(key, value);
        
        //then
        String retrievedValue = PersistenceUtils.getCollectionName(String.class);
        assertThat(retrievedValue, equalTo(value));
    }

    @Test
    void testAddCollectionWithUnicodeCharacters() {
        //given
        String key = "test-key-with-unicode-测试";
        String value = "value-with-unicode-测试";
        
        //when
        PersistenceUtils.addCollection(key, value);
        
        //then
        String retrievedValue = PersistenceUtils.getCollectionName(String.class);
        assertThat(retrievedValue, equalTo(value));
    }

    @Test
    void testAddCollectionWithLongStrings() {
        //given
        String key = "a".repeat(1000);
        String value = "b".repeat(1000);
        
        //when
        PersistenceUtils.addCollection(key, value);
        
        //then
        String retrievedValue = PersistenceUtils.getCollectionName(String.class);
        assertThat(retrievedValue, equalTo(value));
    }

    @Test
    void testGetCollectionName() {
        //given
        String key = String.class.getName();
        String value = "string-collection";
        PersistenceUtils.addCollection(key, value);
        
        //when
        String collectionName = PersistenceUtils.getCollectionName(String.class);
        
        //then
        assertThat(collectionName, equalTo(value));
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
    void testGetCollectionNameWithPrimitiveClass() {
        //given
        String key = int.class.getName();
        String value = "int-collection";
        PersistenceUtils.addCollection(key, value);
        
        //when
        String collectionName = PersistenceUtils.getCollectionName(int.class);
        
        //then
        assertThat(collectionName, equalTo(value));
    }

    @Test
    void testGetCollectionNameWithArrayClass() {
        //given
        String key = String[].class.getName();
        String value = "string-array-collection";
        PersistenceUtils.addCollection(key, value);
        
        //when
        String collectionName = PersistenceUtils.getCollectionName(String[].class);
        
        //then
        assertThat(collectionName, equalTo(value));
    }

    @Test
    void testGetCollectionNameWithInnerClass() {
        //given
        String key = InnerTestClass.class.getName();
        String value = "inner-class-collection";
        PersistenceUtils.addCollection(key, value);
        
        //when
        String collectionName = PersistenceUtils.getCollectionName(InnerTestClass.class);
        
        //then
        assertThat(collectionName, equalTo(value));
    }

    @Test
    void testGetCollectionNameWithAnonymousClass() {
        //given
        Runnable anonymousRunnable = new Runnable() {
            @Override
            public void run() {}
        };
        String key = anonymousRunnable.getClass().getName();
        String value = "anonymous-class-collection";
        PersistenceUtils.addCollection(key, value);
        
        //when
        String collectionName = PersistenceUtils.getCollectionName(anonymousRunnable.getClass());
        
        //then
        assertThat(collectionName, equalTo(value));
    }

    @Test
    void testGetCollectionNameWithLambdaClass() {
        //given
        Runnable lambdaRunnable = () -> {};
        String key = lambdaRunnable.getClass().getName();
        String value = "lambda-class-collection";
        PersistenceUtils.addCollection(key, value);
        
        //when
        String collectionName = PersistenceUtils.getCollectionName(lambdaRunnable.getClass());
        
        //then
        assertThat(collectionName, equalTo(value));
    }

    @Test
    void testAddCollectionAndGetCollectionNameRoundTrip() {
        //given
        String key = Double.class.getName();
        String value = "double-collection";
        
        //when
        PersistenceUtils.addCollection(key, value);
        String retrievedValue = PersistenceUtils.getCollectionName(Double.class);
        
        //then
        assertThat(retrievedValue, equalTo(value));
    }

    @Test
    void testMultipleAddCollectionAndGetCollectionName() {
        //given
        String key1 = String.class.getName();
        String value1 = "string-collection";
        String key2 = Integer.class.getName();
        String value2 = "integer-collection";
        String key3 = Boolean.class.getName();
        String value3 = "boolean-collection";
        
        //when
        PersistenceUtils.addCollection(key1, value1);
        PersistenceUtils.addCollection(key2, value2);
        PersistenceUtils.addCollection(key3, value3);
        
        String retrievedValue1 = PersistenceUtils.getCollectionName(String.class);
        String retrievedValue2 = PersistenceUtils.getCollectionName(Integer.class);
        String retrievedValue3 = PersistenceUtils.getCollectionName(Boolean.class);
        
        //then
        assertThat(retrievedValue1, equalTo(value1));
        assertThat(retrievedValue2, equalTo(value2));
        assertThat(retrievedValue3, equalTo(value3));
    }

    @Test
    void testAddCollectionOverwriteExisting() {
        //given
        String key = String.class.getName();
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
    void testGetCollectionNameWithEmptyMap() {
        //given
        // Map is already empty from setUp()
        
        //when
        String collectionName = PersistenceUtils.getCollectionName(String.class);
        
        //then
        assertThat(collectionName, nullValue());
    }

    @Test
    void testConcurrentAccess() throws InterruptedException {
        //given
        String key = String.class.getName();
        String value = "concurrent-value";
        
        //when
        Thread thread1 = new Thread(() -> PersistenceUtils.addCollection(key, value));
        Thread thread2 = new Thread(() -> {
            try {
                Thread.sleep(10); // Small delay to ensure thread1 starts first
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            String retrievedValue = PersistenceUtils.getCollectionName(String.class);
            assertThat(retrievedValue, not(nullValue()));
        });
        
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        
        //then
        String finalValue = PersistenceUtils.getCollectionName(String.class);
        assertThat(finalValue, equalTo(value));
    }

    @Test
    void testAddCollectionWithWhitespaceOnly() {
        //given
        String key = "   ";
        String value = "   ";
        
        //when
        PersistenceUtils.addCollection(key, value);
        
        //then
        // Should not throw exception, whitespace-only strings are valid
        String retrievedValue = PersistenceUtils.getCollectionName(String.class);
        assertThat(retrievedValue, equalTo(value));
    }

    @Test
    void testAddCollectionWithNewlineCharacters() {
        //given
        String key = "key\nwith\nnewlines";
        String value = "value\nwith\nnewlines";
        
        //when
        PersistenceUtils.addCollection(key, value);
        
        //then
        String retrievedValue = PersistenceUtils.getCollectionName(String.class);
        assertThat(retrievedValue, equalTo(value));
    }

    @Test
    void testAddCollectionWithTabCharacters() {
        //given
        String key = "key\twith\ttabs";
        String value = "value\twith\ttabs";
        
        //when
        PersistenceUtils.addCollection(key, value);
        
        //then
        String retrievedValue = PersistenceUtils.getCollectionName(String.class);
        assertThat(retrievedValue, equalTo(value));
    }

    @Test
    void testAddCollectionWithCarriageReturnCharacters() {
        //given
        String key = "key\rwith\rcarriage\rreturns";
        String value = "value\rwith\rcarriage\rreturns";
        
        //when
        PersistenceUtils.addCollection(key, value);
        
        //then
        String retrievedValue = PersistenceUtils.getCollectionName(String.class);
        assertThat(retrievedValue, equalTo(value));
    }

    // Helper inner class for testing
    private static class InnerTestClass {
        // Empty class for testing
    }
}
