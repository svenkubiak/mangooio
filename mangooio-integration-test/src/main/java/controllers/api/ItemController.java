package controllers.api;

import io.mangoo.routing.Response;

import javax.inject.Singleton;

@Singleton
public class ItemController {

    public Response getItem() {
        return Response.withOk().andEmptyBody();
    }

}
