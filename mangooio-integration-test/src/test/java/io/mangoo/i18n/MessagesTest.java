package io.mangoo.i18n;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Locale;

import org.junit.Test;

import io.mangoo.core.Application;
import io.mangoo.enums.Validation;

/**
 * 
 * @author svenkubiak
 *
 */
public class MessagesTest {
    
    @Test
    public void testReload() {
        //given
        Messages messages = Application.getInstance(Messages.class);
        messages.reload(Locale.GERMAN);
        
        //then
        assertThat(messages.get("welcome"), equalTo("willkommen"));
        
        //when
        messages.reload(Locale.ENGLISH);
        
        //then
        assertThat(messages.get("welcome"), equalTo("welcome"));
    }
    
    @Test
    public void testGet() {
        //given
        Messages messages = Application.getInstance(Messages.class);

        //when
        messages.reload(Locale.GERMAN);
        
        //then
        assertThat(messages.get("welcome"), equalTo("willkommen"));
    }
    
    @Test
    public void testGetWithKey() {
        //given
        Messages messages = Application.getInstance(Messages.class);
        messages.reload(Locale.ENGLISH);
        
        //then
        assertThat(messages.get(Validation.EMAIL_KEY.name(), "foo"), equalTo("foo must be a valid eMail address"));
    }
}
