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
    private List<Item> items = new ArrayList<>();

    public Response getItem() {
        return Response.withOk().andJsonBody(items);
    }

    public Response createItem(Item item) {
        items.add(item);
        return Response.withOk().andEmptyBody();
    }

    public Response updateItem(String name, boolean status) {
        items.stream().filter(i -> i.getName().equals(name))
                .findFirst()
                .ifPresent(i -> i.setActive(status));
        return Response.withOk().andEmptyBody();
    }

    public Response deleteItem(String name){
        items.removeIf(i->i.getName().equals(name));
        return Response.withOk().andEmptyBody();
    }
}
