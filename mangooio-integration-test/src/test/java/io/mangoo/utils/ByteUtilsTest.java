package io.mangoo.utils;

import io.mangoo.TestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith({TestExtension.class})
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
