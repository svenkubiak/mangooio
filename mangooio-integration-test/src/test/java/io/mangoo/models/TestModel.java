package io.mangoo.models;

import java.io.Serializable;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Indexed;
import io.mangoo.persistence.BaseModel;

@Entity(value = "test")
public class TestModel extends BaseModel implements Serializable {
    private static final long serialVersionUID = -3974611906988154231L;
    
    @Indexed
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