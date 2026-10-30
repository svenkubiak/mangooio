package io.mangoo.routing.bindings;

import io.mangoo.TestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void testReservedKeysAreRejected() {
        //given
        final Flash flash = new Flash();

        //then
        for (String key : new String[] {"exp", "sub", "form"}) {
            assertThrows(IllegalArgumentException.class, () -> flash.put(key, "foo"));
        }
        assertThat(flash.hasContent(), equalTo(false));
    }
}