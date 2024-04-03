package models;

import io.mangoo.annotations.Collection;
import io.mangoo.persistence.Entity;
import org.apache.logging.log4j.util.Strings;

import java.io.Serial;
import java.io.Serializable;

@Collection(name = "persons")
public class Person extends Entity implements Serializable {
    @Serial
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