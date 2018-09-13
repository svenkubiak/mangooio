package io.mangoo.templating;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import io.mangoo.TestExtension;
import io.mangoo.i18n.Messages;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Session;
import io.mangoo.templating.directives.FormDirective;
import io.mangoo.templating.directives.TokenDirective;
import io.mangoo.templating.methods.I18nMethod;
import io.mangoo.templating.methods.LocationMethod;
import io.mangoo.templating.methods.PrettyTimeMethod;

@ExtendWith({TestExtension.class})
public class TemplateContextTest {
    @Test
    public void testCreation() {
        //given
        Map<String, Object> content = new HashMap<>();
        content.put("foo", "bar");
        
        TemplateContext templateContext = new TemplateContext(content);
        Form form = Mockito.mock(Form.class);
        Flash flash = Mockito.mock(Flash.class);
        Session session = Mockito.mock(Session.class);
        Messages messages = Mockito.mock(Messages.class);
        String controller = "MyController";
        Locale locale = Locale.getDefault();
        String path = "MyPath";
        
        //when
        templateContext.withForm(form);
        templateContext.withFlash(flash);
        templateContext.withSession(session);
        templateContext.withMessages(messages);
        templateContext.withController(controller);
        templateContext.withPrettyTime(locale);
        templateContext.withTemplatePath(path);
        templateContext.withAuthenticity(session);
        templateContext.withAuthenticityForm(session);
        
        //then
        assertThat(templateContext.getContent().get("foo"), equalTo("bar"));
        assertThat(templateContext.getContent().get("form"), equalTo(form));
        assertThat(templateContext.getContent().get("flash"), equalTo(flash));
        assertThat(templateContext.getContent().get("session"), equalTo(session));
        assertThat(templateContext.getContent().get("i18n"), instanceOf(I18nMethod.class));
        assertThat(templateContext.getContent().get("location"), instanceOf(LocationMethod.class));
        assertThat(templateContext.getContent().get("authenticity"), instanceOf(TokenDirective.class));
        assertThat(templateContext.getContent().get("authenticityForm"), instanceOf(FormDirective.class));
        assertThat(templateContext.getContent().get("prettytime"), instanceOf(PrettyTimeMethod.class));
    }
}
