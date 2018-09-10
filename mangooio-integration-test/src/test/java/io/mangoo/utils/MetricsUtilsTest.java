package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author svenkubiak
 *
 */
public class MetricsUtilsTest {
    @Test
    public void testReadableFileSize() {
        //given
        long size = 25165824;

        //when
        String readableSize = MetricsUtils.readableFileSize(size);
        
        //then
        assertThat(readableSize, not(nullValue()));
        assertThat(readableSize, equalTo("24 MB"));
    }
}