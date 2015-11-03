package io.mangoo.bindings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringWhiteSpace;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.routing.bindings.Flash;

/**
 * 
 * @author svenkubiak
 *
 */
public class FlashTest {
    private static final String MYMESSAGE = "mymessage";
    private static final String CUSTOM_MESSAGE = "This is my custom message";
    private static final String SUCCESS_MESSAGE = "This is a success message!";
    private static final String WARNING_MESSAGE = "This is a warning message!";
    private static final String ERROR_MESSAGE = "This is an error message!";

    @Test
    public void testSuccessMessage() {
        //given
        Flash flash = new Flash();
        
        //when
        flash.setSuccess(SUCCESS_MESSAGE);

        //then
        assertThat(flash.get("success"), equalToIgnoringWhiteSpace(SUCCESS_MESSAGE));
    }

    @Test
    public void testWarningMessage() {
        //given
        Flash flash = new Flash();

        //when
        flash.setWarning(WARNING_MESSAGE);

        //then
        assertThat(flash.get("warning"), equalToIgnoringWhiteSpace(WARNING_MESSAGE));
    }

    @Test
    public void testErrorMessage() {
        //given
        Flash flash = new Flash();
        
        //when
        flash.setError(ERROR_MESSAGE);

        //then
        assertThat(flash.get("error"), equalToIgnoringWhiteSpace(ERROR_MESSAGE));
    }

    @Test
    public void testNoContent() {
        //given
        Flash flash = new Flash();
        
        //then
        assertThat(flash.hasContent(), equalTo(false));
    }
    
    @Test
    public void testContent() {
        //given
        Flash flash = new Flash();
        
        //when
        flash.add(MYMESSAGE, CUSTOM_MESSAGE);

        //then
        assertThat(flash.hasContent(), equalTo(true));
        assertThat(flash.get(MYMESSAGE), equalToIgnoringWhiteSpace(CUSTOM_MESSAGE));
    }
    
    @Test
    public void testInvalidCharacters() {
        //given
        Flash flash = new Flash();

        //when
        flash.add("|", "foo");
        flash.add(":", "foo");
        flash.add("&", "foo");
        flash.add(" ", "foo");
        flash.add("foo", "|");
        flash.add("foo", ":");
        flash.add("foo", "&");
        flash.add("foo", " ");
        
        //then
        assertThat(flash.hasContent(), equalTo(false));
    }
    
    @Test
    public void testNoDiscard() {
        //given
        Flash flash = new Flash();
        
        //then
        assertThat(flash.isDiscard(), equalTo(false));
    }
    
    @Test
    public void testValues() {
        //given
        Flash flash = new Flash();
        
        //then
        assertThat(flash.getValues(), not(nullValue()));
    }
    
    @Test
    public void testDiscard() {
        //given
        Flash flash = new Flash();
        
        //when
        flash.setDiscard(true);
        
        //then
        assertThat(flash.isDiscard(), equalTo(true));
    }
}