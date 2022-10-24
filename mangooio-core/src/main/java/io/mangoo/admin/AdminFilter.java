package io.mangoo.admin;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.apache.commons.lang3.StringUtils;

import dev.paseto.jpaseto.PasetoException;
import dev.paseto.jpaseto.Pasetos;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.enums.Default;
import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.utils.MangooUtils;

/**
 * 
 * @author svenkubiak
 *
 */
public class AdminFilter implements PerRequestFilter {
    private static final String VERSION_TAG = MangooUtils.getVersion();
    
    @Override
    public Response execute(Request request, Response response) {
        var config = Application.getInstance(Config.class);
        var cookie = request.getCookie(Default.ADMIN_COOKIE_NAME.toString());
        
        if (cookie != null) {
            String value = cookie.getValue();
            if (StringUtils.isNotBlank(value)) {
                try {
                    var paseto = Pasetos.parserBuilder()
                            .setSharedSecret(config.getApplicationSecret().getBytes(StandardCharsets.UTF_8))
                            .build()
                            .parse(value);

                    LocalDateTime expiration = LocalDateTime.ofInstant(paseto.getClaims().getExpiration(), ZoneOffset.UTC);

                    if (expiration.isAfter(LocalDateTime.now())) {
                        if (paseto.getClaims().containsKey("twofactor") && paseto.getClaims().get("twofactor", boolean.class)) {
                            return Response.withRedirect("/@admin/twofactor").andEndResponse();
                        }
                        
                        response.andContent("version", VERSION_TAG);
                        return response;
                    }
                } catch (PasetoException e) {
                    //NOSONAR Ignore catch
                }
            }
        }
        
        return Response.withRedirect("/@admin/login").andEndResponse();
    }
}