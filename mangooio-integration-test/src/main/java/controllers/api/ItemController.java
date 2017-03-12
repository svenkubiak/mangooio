package controllers.api;

import io.mangoo.routing.Response;
import models.Item;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by gowthaman on 12/3/17.
 */
@Singleton
public class ItemController {
    private static final List<Item> items = new ArrayList<>();

    public Response getItem() {
        return Response.withOk().andJsonBody(items);
    }

    public Response createItem(Item item) {
        return Response.withOk().andEmptyBody();
    }

    public Response updateItem() {
        return Response.withOk().andEmptyBody();
    }

    public Response deleteItem(String name) {
        return Response.withOk().andEmptyBody();
    }
}
