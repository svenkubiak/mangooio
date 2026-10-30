package io.mangoo.admin;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.hasItem;

@ExtendWith({TestExtension.class})
class AdminFeaturesTest {

    @Test
    void testEmbeddedMongoDbWarnsInProdOnly() {
        //given
        var controller = Application.getInstance(AdminController.class);

        //when
        var prod = controller.getFeatures(true);
        var test = controller.getFeatures(false);

        //then
        assertThat(feature(prod, "Persistence", "MongoDB").get("tone"), equalTo("warn"));
        assertThat(feature(test, "Persistence", "MongoDB").get("tone"), equalTo("value"));
        assertThat(tones(test), not(hasItem("warn")));
    }

    @Test
    void testDedicatedCookieSecretsNameTheFallback() {
        //given the session cookie of the test configuration uses application.secret as secret and key
        var controller = Application.getInstance(AdminController.class);

        //when
        var feature = feature(controller.getFeatures(true), "Security", "Dedicated cookie secrets");

        //then
        assertThat(feature.get("value"), equalTo("Not Session"));
        assertThat(feature.get("tone"), equalTo("warn"));
    }

    @Test
    void testCount() {
        //given
        Map<String, Boolean> values = new LinkedHashMap<>();
        values.put("Session", true);
        values.put("Authentication", false);
        values.put("Flash", false);

        //then
        assertThat(AdminController.count(values), equalTo("Not Authentication, Flash"));
        assertThat(AdminController.count(Map.of("Session", true)), equalTo("All"));
        assertThat(AdminController.count(Map.of("Session", false)), equalTo("None"));
    }

    @Test
    void testDuration() {
        //then
        assertThat(AdminController.duration(7200), equalTo("2 h"));
        assertThat(AdminController.duration(1800), equalTo("30 min"));
        assertThat(AdminController.duration(90), equalTo("90 s"));
        assertThat(AdminController.duration(0), equalTo("0 s"));
    }

    @Test
    void testConnector() {
        //then
        assertThat(AdminController.connector("localhost", 8080), equalTo("localhost:8080"));
        assertThat(AdminController.connector(null, 8443), equalTo("Off"));
        assertThat(AdminController.connector("localhost", 0), equalTo("Off"));
    }

    private static Map<String, String> feature(Map<String, List<Map<String, String>>> features, String group, String label) {
        return features.get(group).stream()
                .filter(feature -> label.equals(feature.get("label")))
                .findFirst()
                .orElseThrow();
    }

    private static List<String> tones(Map<String, List<Map<String, String>>> features) {
        return features.values().stream()
                .flatMap(List::stream)
                .map(feature -> feature.get("tone"))
                .toList();
    }
}
