package io.mangoo.routing.handlers;

import java.io.IOException;

import com.google.common.base.Charsets;

import io.mangoo.core.Application;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Form;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.FormParserFactory;
import io.undertow.server.handlers.form.FormParserFactory.Builder;
import io.undertow.util.HttpString;

/**
 * 
 * @author svenkubiak
 *
 */
public class FormHandler implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        final Attachment requestAttachment = exchange.getAttachment(RequestUtils.ATTACHMENT_KEY);
        requestAttachment.setForm(getForm(exchange));

        exchange.putAttachment(RequestUtils.ATTACHMENT_KEY, requestAttachment);
        nextHandler(exchange);
    }

    /**
     * Retrieves the form parameter from a request
     *
     * @param exchange The Undertow HttpServerExchange
     *
     * @throws IOException
     */
    @SuppressWarnings("all")
    private Form getForm(HttpServerExchange exchange) throws IOException {
        final Form form = Application.getInstance(Form.class);
        if (RequestUtils.isPostOrPut(exchange)) {
            final Builder builder = FormParserFactory.builder();
            builder.setDefaultCharset(Charsets.UTF_8.name());

            final FormDataParser formDataParser = builder.build().createParser(exchange);
            if (formDataParser != null) {
                exchange.startBlocking();
                final FormData formData = formDataParser.parseBlocking();

                formData.forEach(data -> {
                    formData.get(data).forEach(formValue -> {
                        if (formValue.isFile()) {
                            form.addFile(formValue.getPath().toFile());
                        } else {
                            form.addValue(new HttpString(data).toString(), formValue.getValue());
                        }
                    });
                });

                form.setSubmitted(true);
            }
        }

        return form;
    }

    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    @SuppressWarnings("all")
    private void nextHandler(HttpServerExchange exchange) throws Exception {
        new RequestHandler().handleRequest(exchange);
    }
}