package io.mangoo.models;

import java.io.Serializable;

import io.mangoo.persistence.BaseModel;
import io.mangoo.persistence.annotations.Collection;

@Collection(name = "test")
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