package io.mangoo.persistence;

import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

public class BaseModel {
    @BsonProperty("id")
    private ObjectId id;

    public ObjectId getId() {
        return id;
    }
}