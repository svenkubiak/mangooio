package io.mangoo.routing.bindings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToCompressingWhiteSpace;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;

/**
 *
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class FlashTest {
    private static final String MYMESSAGE = "mymessage";
    private static final String CUSTOM_MESSAGE = "This is my custom message";
    private static final String SUCCESS_MESSAGE = "This is a success message!";
    private static final String WARNING_MESSAGE = "This is a warning message!";
    private static final String ERROR_MESSAGE = "This is an error message!";

    @Test
    void testSuccessMessage() {
        //given
        final Flash flash = new Flash();

        //when
        flash.setSuccess(SUCCESS_MESSAGE);

        //then
        assertThat(flash.get("success"), equalToCompressingWhiteSpace(SUCCESS_MESSAGE));
    }

    @Test
    void testWarningMessage() {
        //given
        final Flash flash = new Flash();

        //when
        flash.setWarning(WARNING_MESSAGE);

        //then
        assertThat(flash.get("warning"), equalToCompressingWhiteSpace(WARNING_MESSAGE));
    }

    @Test
    void testErrorMessage() {
        //given
        final Flash flash = new Flash();

        //when
        flash.setError(ERROR_MESSAGE);

        //then
        assertThat(flash.get("error"), equalToCompressingWhiteSpace(ERROR_MESSAGE));
    }

    @Test
    void testNoContent() {
        //given
        final Flash flash = new Flash();

        //then
        assertThat(flash.hasContent(), equalTo(false));
    }

    @Test
    void testContent() {
        //given
        final Flash flash = new Flash();

        //when
        flash.put(MYMESSAGE, CUSTOM_MESSAGE);

        //then
        assertThat(flash.hasContent(), equalTo(true));
        assertThat(flash.get(MYMESSAGE), equalToCompressingWhiteSpace(CUSTOM_MESSAGE));
    }

    @Test
    void testInvalidCharacters() {
        //given
        final Flash flash = new Flash();

        //when
        flash.put("|", "foo");
        flash.put(":", "foo");
        flash.put("&", "foo");
        flash.put(" ", "foo");
        flash.put("foo", "|");
        flash.put("foo", ":");
        flash.put("foo", "&");
        flash.put("foo", " ");

        //then
        assertThat(flash.hasContent(), equalTo(false));
    }

    @Test
    void testNoDiscard() {
        //given
        final Flash flash = new Flash();

        //then
        assertThat(flash.isDiscard(), equalTo(false));
    }

    @Test
    void testValues() {
        //given
        final Flash flash = new Flash();

        //then
        assertThat(flash.getValues(), not(nullValue()));
    }

    @Test
    void testDiscard() {
        //given
        final Flash flash = new Flash();

        //when
        flash.setDiscard(true);

        //then
        assertThat(flash.isDiscard(), equalTo(true));
    }
}