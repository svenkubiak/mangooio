package io.mangoo.routing.handlers;

import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;

import com.google.common.base.Charsets;
import com.google.inject.Inject;

import io.mangoo.core.Application;
import io.mangoo.enums.Required;
import io.mangoo.helpers.RequestHelper;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Form;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormData.FormValue;
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
    private final RequestHelper requestHelper;
    
    @Inject
    public FormHandler(RequestHelper requestHelper) {
        this.requestHelper = Objects.requireNonNull(requestHelper, Required.REQUEST_HELPER.toString());
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        final Attachment attachment = exchange.getAttachment(RequestHelper.ATTACHMENT_KEY);
        if (attachment.getForm() == null) {
            attachment.setForm(getForm(exchange));   
        }

        exchange.putAttachment(RequestHelper.ATTACHMENT_KEY, attachment);
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
    protected Form getForm(HttpServerExchange exchange) throws IOException {
        final Form form = Application.getInstance(Form.class);
        if (this.requestHelper.isPostPutPatch(exchange)) {
            final Builder builder = FormParserFactory.builder();
            builder.setDefaultCharset(Charsets.UTF_8.name());

            final FormDataParser formDataParser = builder.build().createParser(exchange);
            if (formDataParser != null) {
                exchange.startBlocking();
                final FormData formData = formDataParser.parseBlocking();
                formData.forEach(data -> {
                    Deque<FormValue> deque = formData.get(data);
                    if (deque != null) {
                        FormValue formValue = deque.element();
                        if (formValue != null) {
                            if (formValue.isFile() && formValue.getPath() != null) {
                                form.addFile(formValue.getPath().toFile());
                            } else {
                                if (data.contains("[]")) {
                                    String key = data.replace("[]", "");
                                    for (Iterator iterator = deque.iterator(); iterator.hasNext();)  {
                                        form.addValueList(new HttpString(key).toString(), ((FormValue) iterator.next()).getValue());
                                    }
                                } else {
                                    form.addValue(new HttpString(data).toString(), formValue.getValue());
                                }
                            }    
                        }
                    }
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
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(RequestHandler.class).handleRequest(exchange);
    }
}