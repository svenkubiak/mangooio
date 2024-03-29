package io.mangoo.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.mangoo.persistence.annotations.Collection;

@Collection(name = "test")
class SourceTest {

    @Test
    void isCause() {
        //given
        Source source = new Source(true, 24, "foo");

        //then
        assertThat(source.cause(), equalTo(true));
    }

    @Test
    void getLine() {
        //given
        Source source = new Source(true, 24, "foo");

        //then
        assertThat(source.line(), equalTo(24));
    }

    @Test
    void getContent() {
        //given
        Source source = new Source(true, 24, "foo");

        //then
        assertThat(source.content(), equalTo("foo"));
    }
}