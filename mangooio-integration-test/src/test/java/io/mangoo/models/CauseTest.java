package io.mangoo.models;

import io.mangoo.TestExtension;
import io.mangoo.records.Source;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class CauseTest {
    @Test
    void testCreateCause() {
        //given
        Source source = new Source(true, 42, "foo");
        
        //then
        assertThat(source, not(nullValue()));
        assertThat(source.content(), equalTo("foo"));
        assertThat(source.line(), equalTo(42));
        assertThat(source.cause(), equalTo(true));
    }
}