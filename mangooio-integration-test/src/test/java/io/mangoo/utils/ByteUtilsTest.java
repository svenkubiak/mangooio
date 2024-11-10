package io.mangoo.utils;

import io.mangoo.constants.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
class ByteUtilsTest {
    
    @Test
    void testBitLengthAsByte() {
        //given
        String string = "jhjfksHjKSHjdkhsjdk2222";
        
        //then
        assertThat(ByteUtils.bitLength(string.getBytes()), equalTo(184));
    }

    @Test
    void testBitLengthAsStringNotNull() {
        //given
        String string = null;

        //then
        Exception exception = assertThrows(NullPointerException.class, () -> {
            ByteUtils.bitLength(string);
        });

        //then
        String actualMessage = exception.getMessage();
        assertThat(actualMessage, containsString(NotNull.STRING));
    }

    @Test
    void testBitLengthAsString() {
        //given
        String string = "jhjfksHjKSHjdfdfdsfdsfskhsjdk2222";
        
        //then
        assertThat(ByteUtils.bitLength(string), equalTo(264));
    }
}
