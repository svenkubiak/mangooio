package io.mangoo.routing.handlers;

import io.mangoo.core.Application;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Form;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData.FormValue;
import io.undertow.server.handlers.form.FormParserFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Deque;

public class FormHandler implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        final Attachment attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());
        if (attachment.getForm() == null) {
            attachment.setForm(getForm(exchange));   
        }

        exchange.putAttachment(RequestUtils.getAttachmentKey(), attachment);
        nextHandler(exchange);
    }

    /**
     * Retrieves the form parameter from a request
     *
     * @param exchange The Undertow HttpServerExchange
     *
     * @throws IOException If form parsing fails
     */
    protected Form getForm(HttpServerExchange exchange) throws IOException {
        final var form = Application.getInstance(Form.class);
        if (RequestUtils.isPostPutPatch(exchange)) {
            var builder = FormParserFactory.builder();
            builder.setDefaultCharset(StandardCharsets.UTF_8.name());
            try (var formDataParser = builder.build().createParser(exchange)) {
                if (formDataParser != null) {
                    exchange.startBlocking();
                    var formData = formDataParser.parseBlocking();
                    for (String name : formData) {
                        Deque<FormValue> deque = formData.get(name);
                        if (deque != null) {
                            var formValue = deque.element();
                            if (formValue != null) {
                                if (formValue.isFileItem()) {
                                    form.addFile(name, formValue.getFileItem().getInputStream());
                                } else if (!name.contains("[]")) {
                                    form.addValue(name, formValue.getValue());
                                }
                            }
                        }
                    }
                }
            }
            
            form.setSubmitted(true);
        }

        return form;
    }

    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(RequestHandler.class).handleRequest(exchange);
    }
}