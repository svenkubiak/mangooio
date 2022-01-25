package io.mangoo.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.PasswordCredential;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class IdentityTest {
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
