package io.mangoo.routing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import io.mangoo.core.Application;
import io.mangoo.core.Bootstrap;
import io.mangoo.crypto.Crypto;
import io.mangoo.i18n.Messages;
import io.mangoo.templating.TemplateEngine;

/**
 * 
 * @author svenkubiak
 *
 */
public class AttachmentTest {

    @Test
    public void testCreateAttachment() {
        //given
        Attachment attachment = Attachment.build();
        
        //when
        attachment.withClassAnnotations(new ArrayList<Annotation>());
        attachment.withControllerClass(Bootstrap.class);
        attachment.withControllerClassName("foo");
        attachment.withControllerInstance(new String());
        attachment.withControllerMethodName("bar");
        attachment.withCrypto(Application.getInstance(Crypto.class));
        attachment.withLimit(23);
        attachment.withMessages(Application.getInstance(Messages.class));
        attachment.withMethodAnnotations(new ArrayList<Annotation>());
        attachment.withMethodParameterCount(42);
        attachment.withMethodParameters(new HashMap<>());
        attachment.withPassword("foobar");
        attachment.withRequestFilter(true);
        attachment.withRequestParameter(new HashMap<>());
        attachment.withTemplateEngine(Application.getInstance(TemplateEngine.class));
        attachment.withTimer(true);
        attachment.withUsername("peter");
        
        //then
        assertThat(attachment.getClassAnnotations(), instanceOf(ArrayList.class));
        assertThat(attachment.getControllerClass(), instanceOf(Class.class));
        assertThat(attachment.getControllerClassName(), equalTo("foo"));
        assertThat(attachment.getControllerInstance(), instanceOf(String.class));
        assertThat(attachment.getControllerMethodName(), equalTo("bar"));
        assertThat(attachment.getCrypto(), instanceOf(Crypto.class));
        assertThat(attachment.getLimit(), equalTo(23));
        assertThat(attachment.getMessages(), instanceOf(Messages.class));
        assertThat(attachment.getMethodAnnotations(), instanceOf(ArrayList.class));
        assertThat(attachment.getMethodParametersCount(), equalTo(42));
        assertThat(attachment.getMethodParameters(), instanceOf(HashMap.class));
        assertThat(attachment.getPassword(), equalTo("foobar"));
        assertThat(attachment.hasRequestFilter(), equalTo(true));
        assertThat(attachment.getRequestParameter(), instanceOf(HashMap.class));
        assertThat(attachment.getTemplateEngine(), instanceOf(TemplateEngine.class));
        assertThat(attachment.hasTimer(), equalTo(true));
        assertThat(attachment.getUsername(), equalTo("peter"));
        assertThat(attachment.hasAuthentication(), equalTo(true));
    }
}