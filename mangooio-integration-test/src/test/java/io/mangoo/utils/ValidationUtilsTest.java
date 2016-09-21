package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Map;

import org.junit.Test;

/**
 * 
 * @author svenkubia
 *
 */
public class ValidationUtilsTest {
    
    @Test
    public void testGetDefaults() {
        //when
        Map<String, String> defaults = ValidationUtils.getDefaults();
        
        //then
        assertThat(defaults, not(equalTo(nullValue())));
    }
}