package io.mangoo.interfaces.persistence;

import org.bson.types.ObjectId;

public interface BaseEntity {
    ObjectId getId();

    void setId(ObjectId id);
}
