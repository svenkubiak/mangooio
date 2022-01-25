package io.mangoo.i18n;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.enums.Validation;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class MessagesTest {
    
    @Test
    void testReload() {
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
    void testGet() {
        //given
        Messages messages = Application.getInstance(Messages.class);

        //when
        messages.reload(Locale.GERMAN);
        
        //then
        assertThat(messages.get("welcome"), equalTo("willkommen"));
    }
    
    @Test
    void testGetWithKey() {
        //given
        Messages messages = Application.getInstance(Messages.class);
        messages.reload(Locale.ENGLISH);
        
        //then
        assertThat(messages.get(Validation.EMAIL_KEY.toString(), "foo"), equalTo("foo must be a valid eMail address"));
    }
}
