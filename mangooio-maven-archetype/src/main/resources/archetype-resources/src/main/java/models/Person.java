package models;

import java.io.Serializable;

import dev.morphia.annotations.Entity;
import io.mangoo.persistence.BaseModel;

@Entity(value = "persons", noClassnameStored = true)
public class Person extends BaseModel implements Serializable {
    private static final long serialVersionUID = 4343854859740893649L;
    private final String firstname;
    private final String lastname;
    private final int age;

    public Person() {
        this.firstname = "";
        this.lastname = "";
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