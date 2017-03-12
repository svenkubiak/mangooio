package models;

/**
 * Created by gowthaman on 12/3/17.
 */
public class Item {
    private String name;
    private boolean active;

    public Item() {
    }

    public Item(String name) {
        this.name = name;
        this.active = true;
    }

    public String getName() {
        return name;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "Item{" +
                "name='" + name + '\'' +
                ", active=" + active +
                '}';
    }
}
