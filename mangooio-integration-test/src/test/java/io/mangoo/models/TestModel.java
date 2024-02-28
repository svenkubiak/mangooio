package io.mangoo.models;

import io.mangoo.persistence.Entity;
import io.mangoo.annotations.Collection;

@Collection(name = "tests")
public class TestModel extends Entity {
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