package io.mangoo.models;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.utils.CodecUtils;
import io.undertow.security.idm.Account;
import io.undertow.security.idm.PasswordCredential;

/**
 * 
 * @author svenkubiak
 *
 */
public class IdentityTest {
    private static final char [] password = {'b','a','r'};
    
    @Test
    public void testValidVerify() {
        //given
        Identity identity = new Identity("foo", CodecUtils.hexJBcrypt("bar"));
        PasswordCredential credential = new PasswordCredential(password);

        //when
        Account account = identity.verify("foo", credential);
        
        //then
        assertThat(account, not(nullValue()));
        assertThat(account.getPrincipal().getName(), equalTo("foo"));
    }
    
    @Test
    public void testNonValidVerify() {
        //given
        Identity identity = new Identity("foo", CodecUtils.hexJBcrypt("abar"));
        PasswordCredential credential = new PasswordCredential(password);

        //when
        Account account = identity.verify("foo", credential);
        
        //then
        assertThat(account, nullValue());
    }
}
