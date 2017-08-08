package io.mangoo.templating;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import io.mangoo.core.Application;
import io.mangoo.interfaces.MangooTemplateEngine;

/**
 * 
 * @author svenkubiak
 *
 */
public class TemplateEngineFreemarkerTest {

    @Test
    public void testGetTemplateName() {
        //given
        MangooTemplateEngine templateEngine = Application.getInstance(MangooTemplateEngine.class);
        
        //when
        String templateWithSuffix = templateEngine.getTemplateName("template.ftl");
        String templateWithoutSuffix = templateEngine.getTemplateName("template");
        
        //then
        assertThat(templateWithSuffix, equalTo("template.ftl"));
        assertThat(templateWithoutSuffix, equalTo("template.ftl"));
    }
}