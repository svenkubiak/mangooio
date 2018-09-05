package io.mangoo.routing.handlers;

import java.io.IOException;
import java.util.Deque;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Charsets;

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
     * @throws IOException
     */
    @SuppressWarnings("all")
    protected Form getForm(HttpServerExchange exchange) throws IOException {
        final Form form = Application.getInstance(Form.class);
        if (RequestUtils.isPostPutPatch(exchange)) {
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
                                    String key = StringUtils.replace(data, "[]", "");
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