package io.mangoo.models;

import java.util.Date;
import java.util.Optional;

/**
 * 
 * @author svenkubiak
 *
 */
public class Job {
    private boolean active;
    private String name;
    private String description;
    private Date nextFireTime;
    private Date previousFireTime;

    public Job(boolean active, String name, String description, Date nextFireTime, Date previousFireTime) {
        this.active = active;
        this.name = name;
        this.nextFireTime = Optional.ofNullable(nextFireTime).orElse((Date) (nextFireTime.clone()));
        this.previousFireTime = Optional.ofNullable(previousFireTime).orElse((Date) (nextFireTime.clone()));
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public Date getNextFireTime() {
        return (this.nextFireTime == null) ? null : (Date) this.nextFireTime.clone();
    }

    public Date getPreviousFireTime() {
        return (this.previousFireTime == null) ? null : (Date) this.previousFireTime.clone();
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isActive() {
        return this.active;
    }
}