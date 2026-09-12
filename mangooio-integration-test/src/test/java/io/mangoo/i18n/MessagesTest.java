package io.mangoo.i18n;

import io.mangoo.TestExtension;
import io.mangoo.constants.Validation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Locale;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 *
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class MessagesTest {

    @Test
    void testGet() {
        //given
        Messages messages = new Messages(Locale.GERMAN);

        //then
        assertThat(messages.get("welcome"), equalTo("willkommen"));
    }

    @Test
    void testGetWithKey() {
        //given
        Messages messages = new Messages(Locale.ENGLISH);

        //then
        assertThat(messages.get(Validation.EMAIL_KEY, "foo"), equalTo("foo must be a valid eMail address"));
    }

    @Test
    void testInstancesAreIsolated() {
        //given
        Messages german = new Messages(Locale.GERMAN);
        Messages english = new Messages(Locale.ENGLISH);

        //then creating and using one instance must never change another one
        assertThat(german.get("welcome"), equalTo("willkommen"));
        assertThat(english.get("welcome"), equalTo("welcome"));
        assertThat(german.get("welcome"), equalTo("willkommen"));
        assertThat(english.get("welcome"), equalTo("welcome"));
    }

    @Test
    void testDoesNotChangeDefaultLocale() {
        //given
        Locale before = Locale.getDefault();

        //when
        assertThat(new Messages(Locale.FRENCH).get("welcome"), equalTo("willkommen"));

        //then resolving a bundle must not mutate JVM global state
        assertThat(Locale.getDefault(), equalTo(before));
    }

    @Test
    void testUnknownLocaleFallsBackToBaseBundle() {
        //given a locale without a bundle must not fall back to the JVM default locale
        Messages messages = new Messages(Locale.forLanguageTag("zz"));

        //then
        assertThat(messages.get("welcome"), equalTo("willkommen"));
        assertThat(messages.getLocale(), equalTo(Locale.forLanguageTag("zz")));
    }
}
