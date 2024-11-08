package io.mangoo.models;

import io.mangoo.TestExtension;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.PasswordCredential;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class IdentityTest {
    private static final char [] password = {'b','a','r'};
    
    @Test
    void testValidVerify() {
        //given
        Identity identity = new Identity("foo", "bar");
        PasswordCredential credential = new PasswordCredential(password);

        //when
        Account account = identity.verify("foo", credential);
        
        //then
        assertThat(account, not(nullValue()));
        assertThat(account.getPrincipal().getName(), equalTo("foo"));
    }
    
    @Test
    void testNonValidVerify() {
        //given
        Identity identity = new Identity("foo", "abar");
        PasswordCredential credential = new PasswordCredential(password);

        //when
        Account account = identity.verify("foo", credential);
        
        //then
        assertThat(account, nullValue());
    }
}
