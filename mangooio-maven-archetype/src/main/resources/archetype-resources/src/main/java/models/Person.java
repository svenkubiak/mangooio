package models;

import dev.morphia.annotations.Entity;
import io.mangoo.persistence.BaseModel;

import java.io.Serializable;

@Entity(value = "persons", useDiscriminator = false)
public class Person extends BaseModel implements Serializable {
    private static final long serialVersionUID = 4343854859740893649L;
    private final String firstname;
    private final String lastname;
    private final int age;

    public Person() {
        this.firstname = Strings.EMPTY;
        this.lastname = Strings.EMPTY;
        this.age = 0;
    }

    public Person(String firstname, String lastname, int age) {
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