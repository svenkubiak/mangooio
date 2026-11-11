package io.mangoo.templating;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

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

    @Test
    void testRenderTemplateUtf8MatchesTheString() throws Exception {
        //given
        TemplateEngine templateEngine = Application.getInstance(TemplateEngine.class);
        String value = "äöü" + "x".repeat(12_000);
        var context = new TemplateContext(Map.of("king", value)).withTemplatePath("emails/simple.ftl");

        //when
        String rendered = templateEngine.renderTemplate(context);
        byte[] bytes = templateEngine.renderTemplateUtf8(context);

        //then
        assertThat(rendered, containsString(value));
        assertThat(rendered.length(), greaterThan(4_096));
        assertArrayEquals(rendered.getBytes(StandardCharsets.UTF_8), bytes);
    }
}