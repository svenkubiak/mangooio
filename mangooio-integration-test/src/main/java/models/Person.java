package models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.mangoo.annotations.Collection;
import io.mangoo.annotations.Indexed;
import io.mangoo.enums.Sort;
import io.mangoo.persistence.Entity;

/**
 * 
 * @author svenkubiak
 *
 */
@Collection(name = "people")
public class Person extends Entity {
    @Indexed(sort = Sort.ASCENDING)
    private final String firstname;

    @Indexed(sort = Sort.DESCENDING)
    private final String lastname;

    @Indexed(sort = Sort.DESCENDING)
    private String address;

    @Indexed(sort = Sort.DESCENDING)
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}