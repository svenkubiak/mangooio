package io.mangoo.templating;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class TemplateEngineTest {
    @Test
    void testGetTemplateName() {
        //given
        TemplateEngine templateEngine = Application.getInstance(TemplateEngine.class);
        
        //when
        String templateWithSuffix = templateEngine.getTemplateName("template.ftl");
        String templateWithoutSuffix = templateEngine.getTemplateName("template");
        
        //then
        assertThat(templateWithSuffix, equalTo("template.ftl"));
        assertThat(templateWithoutSuffix, equalTo("template.ftl"));
    }
}