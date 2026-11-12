package io.mangoo.controllers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.mangoo.TestExtension;
import io.mangoo.constants.Default;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith({TestExtension.class})
class ThreadDispatchTest {
    private static final String JSON = "application/json";

    @Test
    void testGetStaysOnTheIoThread() {
        assertIoThread(TestRequest.get("/thread").execute());
    }

    @Test
    void testGetMarkedNonBlockingRunsOnAWorker() {
        assertWorker(TestRequest.get("/thread/worker").execute());
    }

    @Test
    void testSmallJsonPostStaysOnTheIoThread() {
        assertIoThread(TestRequest.post("/thread/json").withContentType(JSON).withStringBody(body(128)).execute());
    }

    @Test
    void testSmallJsonPutStaysOnTheIoThread() {
        assertIoThread(TestRequest.put("/thread/json").withContentType(JSON).withStringBody(body(128)).execute());
    }

    @Test
    void testSmallJsonPatchStaysOnTheIoThread() {
        assertIoThread(TestRequest.patch("/thread/json").withContentType(JSON).withStringBody(body(128)).execute());
    }

    @Test
    void testJsonAtTheBufferLimitStaysOnTheIoThread() {
        assertIoThread(TestRequest.post("/thread/json").withContentType(JSON).withStringBody(body(Default.SMALL_JSON_MAX_BYTES)).execute());
    }

    @Test
    void testJsonAboveTheBufferLimitRunsOnAWorker() {
        assertWorker(TestRequest.post("/thread/json").withContentType(JSON).withStringBody(body(Default.SMALL_JSON_MAX_BYTES + 1)).execute());
    }

    @Test
    void testSmallJsonMarkedNonBlockingRunsOnAWorker() {
        assertWorker(TestRequest.post("/thread/json-worker").withContentType(JSON).withStringBody(body(128)).execute());
    }

    @Test
    void testFormPostRunsOnAWorker() {
        Multimap<String, String> form = ArrayListMultimap.create();
        form.put("username", "sven");

        assertWorker(TestRequest.post("/thread/form").withForm(form).execute());
    }

    @Test
    void testPlainPostRunsOnAWorker() {
        assertWorker(TestRequest.post("/thread/text").withContentType("text/plain").withStringBody("hello").execute());
    }

    private static String body(int bytes) {
        return "x".repeat(bytes);
    }

    private static void assertIoThread(TestResponse response) {
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), containsString("I/O"));
    }

    private static void assertWorker(TestResponse response) {
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), containsString("task"));
    }
}
