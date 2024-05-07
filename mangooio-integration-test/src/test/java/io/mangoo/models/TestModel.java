package io.mangoo.models;

import io.mangoo.annotations.Collection;
import io.mangoo.annotations.Indexed;
import io.mangoo.enums.Sort;
import io.mangoo.persistence.Entity;

import java.time.LocalDateTime;

@Collection(name = "tests")
public class TestModel extends Entity {
    private String name;
    @Indexed(sort = Sort.ASCENDING)
    private LocalDateTime timestamp;
    
    public TestModel(){
    }
    
    public TestModel(String name) {
        this.name = name;
        this.timestamp = LocalDateTime.now();
    }

    public TestModel(String name, LocalDateTime timestamp) {
        this.name = name;
        this.timestamp = timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}