package io.mangoo.filters;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.google.inject.Inject;

import io.jsonwebtoken.Jwts;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.enums.Header;
import io.mangoo.enums.Required;
import io.mangoo.interfaces.MangooFilter;
import io.mangoo.models.JsonWebToken;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

/**
 * @deprecated As of 4.10.0, will be removed in 5.0.0 without replacement
 * @author svenkubiak
 *
 */
@Deprecated
public class JsonWebTokenFilter implements MangooFilter {
    private Config config;
    
    @Inject
    public JsonWebTokenFilter(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
    }
    
    @Override
    public Response execute(Request request, Response response) {
        String bearer = request.getHeader(Header.AUTHORIZATION.toHttpString());
        String signKey = this.config.getJwtsSignKey();

        if (StringUtils.isNotBlank(signKey) && StringUtils.isNotBlank(bearer)) {
            bearer = StringUtils.replace(bearer, "Bearer", "");
            bearer = bearer.trim();

            try {
                if (this.config.isJwtsEncrypted()) {
                    bearer = Application.getInstance(Crypto.class).decrypt(bearer, this.config.getJwtsEncryptionKey());
                }
                
                Jwts.parser().setSigningKey(signKey).parseClaimsJws(bearer);
                request.setJsonWebToken(new JsonWebToken(Jwts.parser().setSigningKey(signKey), bearer));
            } catch (Exception e) { //NOSONAR
                return Response.withUnauthorized().end();
            }
            
            return response;
        }
        
        return Response.withBadRequest().end();
    }
}