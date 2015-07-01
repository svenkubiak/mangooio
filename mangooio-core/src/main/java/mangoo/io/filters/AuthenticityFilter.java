package mangoo.io.filters;

import io.undertow.util.StatusCodes;
import mangoo.io.enums.Template;
import mangoo.io.interfaces.MangooControllerFilter;
import mangoo.io.routing.bindings.Exchange;

/**
 *
 * @author svenkubiak
 *
 */
public class AuthenticityFilter implements MangooControllerFilter {

    @Override
    public boolean filter(Exchange exchange) {
        if (!exchange.authenticityMatches()) {
            exchange.getHttpServerExchange().setResponseCode(StatusCodes.FORBIDDEN);
            exchange.getHttpServerExchange().getResponseSender().send(Template.DEFAULT.forbidden());

            return false;
        }

        return true;
    }
}