package io.mangoo.routing.bindings;

import io.mangoo.TestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
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
        assertThat(session.hasChanged(), equalTo(true));
    }

    @Test
    void testRemove() {
        //given
        final Session session = Session.create().withContent(new HashMap<>(Map.of(FOO, BAR)));

        //when
        session.remove(FOO);

        //then
        assertThat(session.hasContent(), equalTo(false));
        assertThat(session.get(FOO), equalTo(null));
        assertThat(session.hasChanged(), equalTo(true));
    }

    @Test
    void testClear() {
        //given
        final Session session = Session.create().withContent(new HashMap<>(Map.of(FOO, BAR)));

        //when
        session.clear();

        //then
        assertThat(session.hasContent(), equalTo(false));
        assertThat(session.get(FOO), equalTo(null));
        assertThat(session.isInvalid(), equalTo(true));
    }

    @Test
    void testCsrfIsCreatedOnFirstRead() {
        //given
        final Session session = Session.create();

        //when
        String first = session.getCsrf();
        String second = session.getCsrf();

        //then
        assertThat(first.length(), equalTo(32));
        assertThat(second, equalTo(first));
    }

    @Test
    void testExplicitCsrfIsKept() {
        //given
        final Session session = Session.create().withCsrf("existing-token");

        //then
        assertThat(session.getCsrf(), equalTo("existing-token"));
    }

    @Test
    void testRestoredSessionKeepsItsCsrf() {
        //given
        final Session session = Session.create().withCsrf("from-cookie").withRestored();

        //then
        assertThat(session.getCsrf(), equalTo("from-cookie"));
    }

    @Test
    void testRestoredSessionDoesNotReplaceABlankCsrf() {
        //given
        final Session session = Session.create().withCsrf(" ").withRestored();

        //then
        assertThat(session.getCsrf(), equalTo(" "));
        assertThat(session.getCsrf(), not(equalTo(Session.create().getCsrf())));
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