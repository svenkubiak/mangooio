package io.mangoo.models;

import io.advantageous.boon.json.annotations.JsonIgnore;
import io.advantageous.boon.json.annotations.JsonInclude;

/**
 * 
 * @author svenkubiak
 *
 */
public class Car {
    @JsonInclude
    public String brand = null;
    
    @JsonInclude
    public int doors = 0;

    @JsonIgnore
    public String comment = "blablabla";
    
    public String foo = "blablabla";
    public String id;

    public Car() {}
    public Car(String id) {
        this.id = id;
    }
}