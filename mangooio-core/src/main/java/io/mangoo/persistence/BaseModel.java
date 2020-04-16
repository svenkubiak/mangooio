package io.mangoo.persistence;

import java.io.Serializable;

import org.bson.types.ObjectId;

import dev.morphia.annotations.Id;

public class BaseModel implements Serializable {
    private static final long serialVersionUID = -3141621127850129919L;

    @Id
    protected ObjectId objectId;

    public ObjectId getId() {
        return this.objectId;
    }
}
