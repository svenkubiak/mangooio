package io.mangoo.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;

/**
 * 
 * @author sven.kubiak
 *
 */
@ExtendWith({TestExtension.class})
public class JobTest {
    @Test
    public void testCreateJob() {
        //given
        Job job = new Job(true, "foo", "bar", new Date(), new Date());
        
        //then
        assertThat(job, not(nullValue()));
        assertThat(job.getDescription(), equalTo("bar"));
        assertThat(job.getName(), equalTo("foo"));
        assertThat(job.getNextFireTime(), not(nullValue()));
        assertThat(job.getPreviousFireTime(), not(nullValue()));
    }
}
