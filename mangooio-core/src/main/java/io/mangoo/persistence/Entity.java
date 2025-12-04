package io.mangoo.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.mangoo.persistence.interfaces.BaseEntity;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;

import java.io.Serial;
import java.io.Serializable;

public class Entity implements BaseEntity, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @BsonId
    @BsonProperty("_id")
    @JsonIgnore
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
