package io.mangoo.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
public class ArgumentsTest {

    @Test
    public void testRequireNonBlankWithValidString() {
        // Given
        String validString = "valid";
        String message = "message";
        
        // When & Then
        assertDoesNotThrow(() -> Argument.requireNonBlank(validString, message));
    }

    @Test
    public void testRequireNonBlankWithNullString() {
        // Given
        String nullString = null;
        String message = "message";
        
        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> Argument.requireNonBlank(nullString, message));
        
        // Then
        assert exception.getMessage().equals("message");
    }

    @Test
    public void testRequireNonBlankWithEmptyString() {
        // Given
        String emptyString = "";
        String message = "message";
        
        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> Argument.requireNonBlank(emptyString, message));
        
        // Then
        assert exception.getMessage().equals("message");
    }

    @Test
    public void testRequireNonBlankWithBlankString() {
        // Given
        String blankString = "   ";
        String message = "message";
        
        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> Argument.requireNonBlank(blankString, message));
        
        // Then
        assert exception.getMessage().equals("message");
    }

    @Test
    public void testRequireNonBlankWithValidStrings() {
        // Given
        String message = "message";
        String[] validStrings = {"valid1", "valid2", "valid3"};
        
        // When & Then
        assertDoesNotThrow(() -> Argument.requireNonBlank(message, validStrings));
    }

    @Test
    public void testRequireNonBlankWithNullInStrings() {
        // Given
        String message = "message";
        String[] stringsWithNull = {"valid1", null, "valid3"};
        
        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> Argument.requireNonBlank(message, stringsWithNull));
        
        // Then
        assert exception.getMessage().equals("message");
    }

    @Test
    public void testRequireNonBlankWithEmptyInStrings() {
        // Given
        String message = "message";
        String[] stringsWithEmpty = {"valid1", "", "valid3"};
        
        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> Argument.requireNonBlank(message, stringsWithEmpty));
        
        // Then
        assert exception.getMessage().equals("message");
    }

    @Test
    public void testRequireNonBlankWithBlankInStrings() {
        // Given
        String message = "message";
        String[] stringsWithBlank = {"valid1", "   ", "valid3"};
        
        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> Argument.requireNonBlank(message, stringsWithBlank));
        
        // Then
        assert exception.getMessage().equals("message");
    }

    @Test
    public void testRequireNonBlankWithEmptyStringArray() {
        // Given
        String message = "message";
        
        // When & Then
        assertDoesNotThrow(() -> Argument.requireNonBlank(message));
    }
}
