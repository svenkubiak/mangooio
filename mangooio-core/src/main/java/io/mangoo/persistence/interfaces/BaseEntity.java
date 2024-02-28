package io.mangoo.persistence.interfaces;

import org.bson.types.ObjectId;

public interface BaseEntity {
    ObjectId getId();

    void setId(ObjectId id);
}
