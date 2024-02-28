package io.mangoo.persistence;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.mangoo.persistence.interfaces.BaseEntity;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

public class Entity implements BaseEntity {
    @BsonId
    @JsonSerialize(using = ToStringSerializer.class)
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
