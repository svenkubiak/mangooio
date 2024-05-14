package io.mangoo.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
    void testBitLengthAsString() {
        //given
        String string = "jhjfksHjKSHjdfdfdsfdsfskhsjdk2222";
        
        //then
        assertThat(ByteUtils.bitLength(string), equalTo(264));
    }
}
