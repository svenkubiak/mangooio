package io.mangoo.routing.bindings;

import io.mangoo.TestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({TestExtension.class})
class SessionTest {
    private static final String BAR = "bar";
    private static final String FOO = "foo";

    @Test
    void testNoContent() {
        //given
        final Session session = Session.create();

        //then
        assertThat(session.hasContent(), equalTo(false));
    }

    @Test
    void testContent() {
        //given
        final Session session = Session.create();

        //when
        session.put(FOO, BAR);

        //then
        assertThat(session.hasContent(), equalTo(true));
        assertThat(session.get(FOO), equalTo(BAR));
        assertThat(session.isKept(), equalTo(false));
    }

    @Test
    void testRemove() {
        //given
        final Session session = Session.create();

        //when
        session.put(FOO, BAR);
        session.remove(FOO);

        //then
        assertThat(session.hasContent(), equalTo(false));
        assertThat(session.get(FOO), equalTo(null));
        assertThat(session.isKept(), equalTo(false));
    }

    @Test
    void testClear() {
        //given
        final Session session = Session.create();

        //when
        session.put(FOO, BAR);
        session.clear();

        //then
        assertThat(session.hasContent(), equalTo(false));
        assertThat(session.get(FOO), equalTo(null));
        assertThat(session.isKept(), equalTo(false));
    }

    @Test
    void testFormerSeparatorCharactersAreStored() {
        //given
        final Session session = Session.create();

        //when
        session.put("|", FOO);
        session.put(FOO, " ");
        session.put("text", "a|b: c&d");

        //then
        assertThat(session.get("|"), equalTo(FOO));
        assertThat(session.get(FOO), equalTo(" "));
        assertThat(session.get("text"), equalTo("a|b: c&d"));
    }

    @Test
    void testReservedKeysAreRejected() {
        //given
        final Session session = Session.create();

        //then
        for (String key : new String[] {"sub", "exp", "jti", "x-csrf-token"}) {
            assertThrows(IllegalArgumentException.class, () -> session.put(key, FOO));
        }
        assertThat(session.hasContent(), equalTo(false));
    }
}