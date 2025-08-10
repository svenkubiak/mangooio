package io.mangoo.filters;

import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.undertow.util.Methods;
import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class CsrfFilter implements PerRequestFilter {

    @Override
    public Response execute(Request request, Response response) {
        var method = request.getMethod();
        if (Methods.POST == method || Methods.PUT == method || Methods.DELETE == method || Methods.PATCH == method) {
            String csrfTokenFromRequest = request.getHeader("X-CSRF-Token");
            String csrfTokenFromForm = parseFormEncodedString(request.getBody()).get("X-CSRF-Token");
            String csrfTokenFromSession = request.getSession().get("csrf-token");

            if (StringUtils.isNotBlank(csrfTokenFromSession)
                    && (!csrfTokenFromSession.equals(csrfTokenFromRequest) || !csrfTokenFromSession.equals(csrfTokenFromForm)))
            {
                return Response.forbidden().bodyText("CSRF validation failed").end();
            }
        }

        return response;
    }

    private Map<String, String> parseFormEncodedString(String body) {
        Map<String, String> params = new LinkedHashMap<>();
        if (body == null || body.isEmpty()) {
            return params;
        }

        String[] pairs = body.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            String key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8) : pair;
            String value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8) : "";
            params.put(key, value);
        }

        return params;
    }
}

