package io.mangoo.models;

import java.io.Serializable;

import io.mangoo.persistence.annotations.Collection;

@Collection(name = "test")
public class TestModel {

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