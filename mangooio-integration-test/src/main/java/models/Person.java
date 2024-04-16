package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.mangoo.annotations.Collection;
import io.mangoo.annotations.Indexed;
import io.mangoo.enums.Order;
import io.mangoo.persistence.Entity;

/**
 * 
 * @author svenkubiak
 *
 */
@Collection(name = "people")
public class Person extends Entity {
    //@Indexed(order = "asc")
    private final String firstname;

    //@Indexed(order = "desc")
    private final String lastname;

    @Indexed(order = Order.DESCENDING)
    private final int age;

    @JsonCreator
    public Person(@JsonProperty("firstname") String firstname, @JsonProperty("lastname") String lastname, @JsonProperty("age") int age) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.age = age;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public int getAge() {
        return age;
    }
}