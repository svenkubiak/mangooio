package io.mangoo.templating;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Version;
import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.MonthDay;
import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JavaTimeObjectWrapperTest {
    private static final Version VERSION = new Version(2, 3, 35);

    @Test
    void testPrintsIsoAndPattern() throws Exception {
        var rendered = render("${value} ${value.format('dd.MM.yyyy HH:mm:ss')}", LocalDateTime.of(2026, 10, 28, 11, 56, 0));

        assertEquals("2026-10-28T11:56 28.10.2026 11:56:00", rendered);
    }

    @Test
    void testPlusDaysUsesJavaMethod() throws Exception {
        var rendered = render("${value.plusDays(1)}", LocalDateTime.of(2026, 10, 28, 11, 56, 0));

        assertEquals("2026-10-29T11:56", rendered);
    }

    @Test
    void testFormatWithoutPatternUsesToString() throws Exception {
        assertEquals("2026-10-28T10:15:30Z", render("${value.format()}", Instant.parse("2026-10-28T10:15:30Z")));
        assertEquals("--12-03", render("${value.format()}", MonthDay.of(12, 3)));
    }

    private static String render(String source, Object value) throws Exception {
        var loader = new StringTemplateLoader();
        loader.putTemplate("template", source);
        var configuration = new Configuration(VERSION);
        configuration.setObjectWrapper(new JavaTimeObjectWrapper(VERSION));
        configuration.setTemplateLoader(loader);
        configuration.setLocale(Locale.GERMANY);

        var writer = new StringWriter();
        configuration.getTemplate("template").process(Map.of("value", value), writer);
        return writer.toString();
    }
}
