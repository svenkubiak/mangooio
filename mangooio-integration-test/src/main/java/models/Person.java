package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.mangoo.persistence.annotations.Collection;

/**
 * 
 * @author svenkubiak
 *
 */
@Collection(name = "people")
public class Person {
    private final String firstname;
    private final String lastname;
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