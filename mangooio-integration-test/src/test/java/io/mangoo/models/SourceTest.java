package io.mangoo.models;

import dev.morphia.annotations.Entity;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Entity(value = "test")
class SourceTest {

    @Test
    void isCause() {
        //given
        Source source = new Source(true, 24, "foo");

        //then
        assertThat(source.isCause(), equalTo(true));
    }

    @Test
    void getLine() {
        //given
        Source source = new Source(true, 24, "foo");

        //then
        assertThat(source.getLine(), equalTo(24));
    }

    @Test
    void getContent() {
        //given
        Source source = new Source(true, 24, "foo");

        //then
        assertThat(source.getContent(), equalTo("foo"));
    }
}