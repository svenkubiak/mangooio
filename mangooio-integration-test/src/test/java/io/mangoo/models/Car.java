package io.mangoo.models;

import org.boon.json.annotations.JsonInclude;
import org.boon.json.annotations.JsonIgnore;

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

    public Car() {}
}