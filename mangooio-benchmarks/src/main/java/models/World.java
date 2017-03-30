package models;

import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Indexed;

import de.svenkubiak.mangooio.morphia.MorphiaModel;

/**
 *
 * @author svenkubiak
 *
 */
@Entity(value = "worlds", noClassnameStored = true)
public class World extends MorphiaModel {
    private static final long serialVersionUID = -3219780537751230815L;

    @Indexed
    private long worldId;

    private int randomNumber;

    public World() {
        //Empty constructor for injection
    }

    public World(long worldId, int randomNumber) {
        this.worldId = worldId;
        this.randomNumber = randomNumber;
    }

    public int getRandomNumber() {
        return this.randomNumber;
    }

    public void setRandomnumber(int randomNumber) {
        this.randomNumber = randomNumber;
    }

    public long getWorldId() {
        return worldId;
    }
}