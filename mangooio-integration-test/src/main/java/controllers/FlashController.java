package controllers;

import mangoo.io.routing.Response;
import mangoo.io.routing.bindings.Flash;

public class FlashController {

    public Response flash(Flash flash) {
        flash.add("simple", "simple");
        flash.setError("error");
        flash.setWarn("warning");
        flash.setSuccess("info");

        return Response.withRedirect("/flashed");
    }

    public Response flashed(Flash flash) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(flash.get("simple")).append(flash.get("error")).append(flash.get("warn")).append(flash.get("success"));

        return Response.withOk().andTextBody(buffer.toString());
    }
}