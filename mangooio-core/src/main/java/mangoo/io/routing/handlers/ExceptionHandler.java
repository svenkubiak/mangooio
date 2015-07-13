package mangoo.io.routing.handlers;

import java.io.FileNotFoundException;
import java.io.IOException;

import freemarker.template.TemplateException;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import mangoo.io.core.Application;
import mangoo.io.enums.Default;
import mangoo.io.enums.Header;
import mangoo.io.enums.Template;
import mangoo.io.templating.TemplateEngine;

/**
 *
 * @author svenkubiak
 *
 */
public class ExceptionHandler implements HttpHandler {
    public static final AttachmentKey<Throwable> THROWABLE = AttachmentKey.create(Throwable.class);

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        exchange.getResponseHeaders().put(Header.X_XSS_PPROTECTION.toHttpString(), Default.XSS_PROTECTION.toInt());
        exchange.getResponseHeaders().put(Header.X_CONTENT_TYPE_OPTIONS.toHttpString(), Default.NOSNIFF.toString());
        exchange.getResponseHeaders().put(Header.X_FRAME_OPTIONS.toHttpString(), Default.SAMEORIGIN.toString());
        exchange.getResponseHeaders().put(Headers.SERVER, Default.SERVER.toString());
        exchange.setResponseCode(StatusCodes.INTERNAL_SERVER_ERROR);

        if (Application.inDevMode()) {
            Throwable throwable = exchange.getAttachment(THROWABLE);
            if (throwable == null) {
                exchange.getResponseSender().send(Template.DEFAULT.internalServerError());
            } else {
                exchange.getResponseSender().send(renderException(exchange, throwable.getCause()));
            }
        } else {
            exchange.getResponseSender().send(Template.DEFAULT.internalServerError());
        }
    }

    @SuppressWarnings("all")
    private String renderException(HttpServerExchange exchange, Throwable cause) throws FileNotFoundException, IOException, TemplateException {
        TemplateEngine templateEngine = Application.getInjector().getInstance(TemplateEngine.class);
        return templateEngine.renderException(exchange, cause);
    }
}