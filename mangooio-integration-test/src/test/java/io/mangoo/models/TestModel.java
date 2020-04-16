package io.mangoo.models;

import java.io.Serializable;

import dev.morphia.annotations.Entity;
import io.mangoo.persistence.BaseModel;

@Entity(value = "test", noClassnameStored = true)
public class TestModel extends BaseModel implements Serializable {
    private static final long serialVersionUID = -3974611906988154231L;
    private String name;
    
    public TestModel(){
    }
    
    public TestModel(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}