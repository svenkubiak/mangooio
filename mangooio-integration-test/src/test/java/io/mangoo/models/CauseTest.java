package io.mangoo.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

/**
 * 
 * @author svenkubiak
 *
 */
public class CauseTest {

    @Test
    public void testCreateCause() {
        //given
        Source source = new Source(true, 42, "foo");
        
        //then
        assertThat(source, not(nullValue()));
        assertThat(source.getContent(), equalTo("foo"));
        assertThat(source.getLine(), equalTo(42));
        assertThat(source.isCause(), equalTo(true));
    }
}