package io.mangoo.persistence;

import java.io.Serial;
import java.io.Serializable;

import org.bson.types.ObjectId;

import dev.morphia.annotations.Id;

/**
 * 
 * @author svenkubiak
 *
 */
public class BaseModel implements Serializable {
    @Serial
    private static final long serialVersionUID = -3141621127850129919L;

    @Id
    protected ObjectId objectId;

    public ObjectId getId() {
        return objectId;
    }
}