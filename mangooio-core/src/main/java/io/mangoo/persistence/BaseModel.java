package io.mangoo.persistence;

import java.io.Serializable;

import org.bson.types.ObjectId;

import dev.morphia.annotations.Id;

public class BaseModel implements Serializable {
    private static final long serialVersionUID = 1117892356160010435L;
    
    @Id
    protected ObjectId objectId;

    public ObjectId getId() {
        return objectId;
    }
}