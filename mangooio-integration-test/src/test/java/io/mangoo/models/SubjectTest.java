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
public class SubjectTest {
    @Test
    public void testCreateSubject() {
        //given
        Subject subject = new Subject("foo", true);
        
        //then
        assertThat(subject, not(nullValue()));
        assertThat(subject.getUsername(), equalTo("foo"));
        assertThat(subject.isAuthenticated(), equalTo(true));
    }
}