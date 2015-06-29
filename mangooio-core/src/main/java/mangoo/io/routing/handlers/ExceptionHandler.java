package mangoo.io.routing.handlers;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import mangoo.io.core.Application;
import mangoo.io.enums.Default;
import mangoo.io.enums.Templates;
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
        exchange.getResponseHeaders().put(Headers.SERVER, Default.SERVER.toString());
        exchange.setResponseCode(StatusCodes.INTERNAL_SERVER_ERROR);
        
        if (Application.inDevMode()) {
        	Throwable throwable = exchange.getAttachment(THROWABLE);
        	exchange.getResponseSender().send(getExceptionTemplate(exchange, throwable.getCause()));
        } else {
        	exchange.getResponseSender().send(Templates.DEFAULT.internalServerError());
        }
    }

	private String getExceptionTemplate(HttpServerExchange exchange, Throwable cause) throws Exception {
	    TemplateEngine templateEngine = Application.getInjector().getInstance(TemplateEngine.class);
	    return templateEngine.renderException(exchange, cause);
	}
}