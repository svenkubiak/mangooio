package io.mangoo.routing.handlers;

import io.mangoo.core.Application;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Form;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData.FormValue;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.util.HttpString;
import org.apache.commons.lang3.Strings;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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
                    for (String data : formData) {
                        Deque<FormValue> deque = formData.get(data);
                        if (deque != null) {
                            var formValue = deque.element();
                            if (formValue != null) {
                                if (formValue.isFileItem() && formValue.getFileItem().getFile() != null) {
                                    form.addFile(Files.newInputStream(formValue.getFileItem().getFile()));
                                } else {
                                    if (data.contains("[]")) {
                                        var key = Strings.CI.replace(data, "[]", "");
                                        for (FormValue value : deque) {
                                            form.addValueList(new HttpString(key).toString(), value.getValue());
                                        }
                                    } else {
                                        form.addValue(new HttpString(data).toString(), formValue.getValue());
                                    }
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