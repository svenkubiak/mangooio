package controllers;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Flash;

public class FlashController {
    private static final String SUCCESS = "success";
    private static final String WARNING = "warning";
    private static final String ERROR = "error";
    private static final String SIMPLE = "simple";

    public Response flash(Flash flash) {
        flash.put(SIMPLE, SIMPLE);
        flash.setError(ERROR);
        flash.setWarning(WARNING);
        flash.setSuccess(SUCCESS);

        return Response.withRedirect("/flashed");
    }

    public Response flashed(Flash flash) {
        final StringBuilder buffer = new StringBuilder();
        buffer.append(flash.get(SIMPLE)).append(flash.get(ERROR)).append(flash.get(WARNING)).append(flash.get(SUCCESS));

        return Response.withOk().andTextBody(buffer.toString());
    }
}