package io.mangoo.routing.handlers;

import io.mangoo.constants.Default;
import io.mangoo.core.Application;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Form;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormData.FormValue;
import io.undertow.server.handlers.form.FormDataParser;
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
        final Form form = Application.getInstance(Form.class);

        if (!RequestUtils.isPostPutPatch(exchange)) {
            return form;
        }

        exchange.startBlocking();

        FormParserFactory.Builder builder = FormParserFactory.builder();
        builder.setDefaultCharset(StandardCharsets.UTF_8.name());

        try (FormDataParser parser = builder.build().createParser(exchange)) {
            if (parser == null) {
                return form;
            }

            FormData formData = parser.parseBlocking();

            int parameterCount = 0;
            int fileCount = 0;

            for (String name : formData) {
                if (name == null || name.isBlank() || name.length() > 200) {
                    throw new IOException("Invalid parameter name");
                }

                Deque<FormValue> values = formData.get(name);
                if (values == null || values.isEmpty()) {
                    continue;
                }

                for (FormValue value : values) {
                    parameterCount++;
                    if (parameterCount > Default.FORM_MAX_PARAMETERS) {
                        throw new IOException("Too many parameters");
                    }

                    if (value.isFileItem()) {
                        fileCount++;
                        if (fileCount > Default.FORM_MAX_FILES) {
                            throw new IOException("Too many file uploads");
                        }

                        FormData.FileItem fileItem = value.getFileItem();

                        long size = fileItem.getFileSize();
                        if (size > Default.FORM_MAX_FILE_SIZE) {
                            throw new IOException("Uploaded file too large");
                        }

                        form.addFile(name, fileItem.getInputStream());
                    } else {
                        String val = value.getValue();
                        if (val == null) {
                            continue;
                        }

                        if (val.length() > Default.FORM_MAX_VALUE_LENGTH) {
                            throw new IOException("Parameter value too long");
                        }

                        form.addValue(name, val);
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