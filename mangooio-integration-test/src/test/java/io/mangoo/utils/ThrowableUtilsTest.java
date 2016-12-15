package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.io.File;

import org.junit.Test;

/**
 * 
 * @author svenkubiak
 *
 */
public class ThrowableUtilsTest {

    @Test
    public void testGetSourceCodePath() {
        //given
        String separator = File.separator;
        StackTraceElement stackTraceElement = new StackTraceElement("io.mangoo.core.Bootstrap", "parseRoutes", "Bootstrap.java", 173);
        
        //when
        String sourceCodePath = ThrowableUtils.getSourceCodePath(stackTraceElement);
        
        //then
        assertThat(sourceCodePath, not(equalTo(nullValue())));
        assertThat(sourceCodePath, equalTo("io" + separator + "mangoo" + separator + "core" + separator + "Bootstrap.java"));
    }
}