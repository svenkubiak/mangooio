package io.mangoo.routing.handlers;

import io.mangoo.constants.Default;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Form;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData.FormValue;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Deque;
import java.util.Objects;

public class FormHandler implements HttpHandler {
    private static final Logger LOG = LogManager.getLogger(FormHandler.class);
    private final long maxFileSize;

    @Inject
    public FormHandler(Config config) {
        Objects.requireNonNull(config, Required.CONFIG);
        this.maxFileSize = config.getFormMaxFileSize();
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        final Attachment attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());

        var form = attachment.getForm();
        if (form == null) {
            form = getForm(exchange);
        }

        // Always use the messages of this request, also for a form restored from the flash cookie,
        // so that validation errors are rendered in the locale of the current request.
        form.withMessages(attachment.getMessages());
        attachment.setForm(form);

        exchange.putAttachment(RequestUtils.getAttachmentKey(), attachment);
        nextHandler(exchange);
    }

    protected Form getForm(HttpServerExchange exchange) throws IOException {
        final Form form = Application.getInstance(Form.class);
        if (!RequestUtils.isPostPutPatch(exchange)) {
            return form;
        }

        exchange.startBlocking();

        var formParserBuilder = FormParserFactory.builder();
        formParserBuilder.setDefaultCharset(StandardCharsets.UTF_8.name());

        try (FormDataParser parser = formParserBuilder.build().createParser(exchange)) {
            if (parser == null) {
                return form;
            }

            var formData = parser.parseBlocking();
            var parameterCount = 0;
            var fileCount = 0;
            for (String name : formData) {
                if (name == null || name.isBlank() || name.length() > 200) {
                    throw rejected(exchange, "Invalid parameter name");
                }

                Deque<FormValue> values = formData.get(name);
                if (values == null || values.isEmpty()) {
                    continue;
                }

                for (FormValue value : values) {
                    parameterCount++;
                    if (parameterCount > Default.FORM_MAX_PARAMETERS) {
                        throw rejected(exchange, "Too many parameters, limit is " + Default.FORM_MAX_PARAMETERS);
                    }

                    if (value.isFileItem()) {
                        fileCount++;
                        if (fileCount > Default.FORM_MAX_FILES) {
                            throw rejected(exchange, "Too many file uploads, limit is " + Default.FORM_MAX_FILES);
                        }

                        var fileItem = value.getFileItem();
                        var size = fileItem.getFileSize();
                        if (size > maxFileSize) {
                            throw rejected(exchange, "Uploaded file too large: parameter '" + name + "' has "
                                    + size + " bytes, limit is " + maxFileSize
                                    + " bytes, raise form.maxfilesize to allow it");
                        }

                        form.addFile(name, fileItem.getInputStream());
                    } else {
                        String val = value.getValue();
                        if (val == null) {
                            continue;
                        }

                        if (val.length() > Default.FORM_MAX_VALUE_LENGTH) {
                            throw rejected(exchange, "Parameter value too long: parameter '" + name + "' has "
                                    + val.length() + " characters, limit is " + Default.FORM_MAX_VALUE_LENGTH);
                        }

                        form.addValue(name, val);
                    }
                }
            }

            form.setSubmitted(true);
        }

        return form;
    }

    // Logged here, as the rejection happens before any controller runs.
    private IOException rejected(HttpServerExchange exchange, String reason) {
        LOG.warn("Rejected form of request {} {}: {}",
                exchange.getRequestMethod(), exchange.getRequestURI(), reason);

        return new IOException(reason);
    }

    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(RequestHandler.class).handleRequest(exchange);
    }
}
