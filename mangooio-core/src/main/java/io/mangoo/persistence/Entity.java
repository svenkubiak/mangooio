package io.mangoo.persistence;

import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

public class Entity implements BaseEntity {
    @BsonId
    protected ObjectId id;

    @Override
    public ObjectId getId() {
        return id;
    }

    @Override
    public void setId(ObjectId id) {
        this.id = id;
    }
}
