package io.mangoo.models;

import java.util.Date;

/**
 * Base class that holds scheduled job information
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
        this.nextFireTime = (nextFireTime == null) ? null : (Date) nextFireTime.clone();
        this.previousFireTime =  (previousFireTime == null) ? null : (Date) previousFireTime.clone();
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