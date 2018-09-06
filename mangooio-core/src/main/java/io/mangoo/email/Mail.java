package io.mangoo.email;

import java.util.Map;
import java.util.Objects;

import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooMailerException;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.interfaces.MangooTemplateEngine;
import io.mangoo.services.EventBusService;
import jodd.mail.Email;

/**
 * 
 * @author svenkubiak
 *
 */
public class Mail {
    private Email email;

    /**
     * Creates a new mail instance
     * @return A mail object instance
     */
    public static Mail build() {
        return new Mail();
    }

    /**
     * Sets the org.Jodd.Email instance that the email is based on
     * 
     * @param email The Email instance
     * @return A mail object instance
     */
    public Mail withBuilder(Email email) {
        Objects.requireNonNull(email, Required.EMAIL.toString());
        this.email = email;
        
        return this;
    }

    /**
     * Sets a template to be rendered for the email. Using a template
     * will make it a HTML Email by default.
     *
     * @param template The template to use
     * @param content The content for the template
     * @return A mail object instance
     * @throws MangooTemplateEngineException if rendering of template fails
     */
    public Mail templateMessage(String template, Map<String, Object> content) throws MangooTemplateEngineException {
        Objects.requireNonNull(template, Required.TEMPLATE.toString());
        Objects.requireNonNull(content, Required.CONTENT.toString());

        if (template.charAt(0) == '/' || template.startsWith("\\")) {
            template = template.substring(1, template.length());
        } 
        this.email.htmlMessage(Application.getInstance(MangooTemplateEngine.class).render("", template, content), Default.ENCODING.toString());

        return this;
    }
    
    /**
     * Sends the mail
     * 
     * @throws MangooMailerException when sending the mail failed
     */
    public void send() throws MangooMailerException {
        Application.getInstance(EventBusService.class).publish(this.email);
    }
}