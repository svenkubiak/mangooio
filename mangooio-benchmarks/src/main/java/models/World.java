package models;

import java.io.Serializable;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Field;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Index;
import org.mongodb.morphia.annotations.IndexOptions;
import org.mongodb.morphia.annotations.Indexes;

/**
 *
 * @author svenkubiak
 *
 */
@Indexes(@Index(fields = {@Field(value = "id")}, options = @IndexOptions(unique = true)))
public class World implements Serializable {
    private static final long serialVersionUID = -3219780537751230815L;

    @Id
    protected ObjectId objectId;

    private long id;

    private int randomNumber;

    public World() {
    }

    public World(long id, int randomNumber) {
        this.id = id;
        this.randomNumber = randomNumber;
    }

    public int getRandomNumber() {
        return this.randomNumber;
    }

    public void setRandomnumber(int randomNumber) {
        this.randomNumber = randomNumber;
    }

    public long getId() {
        return id;
    }
}