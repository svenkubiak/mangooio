package io.mangoo.models;

import java.util.Date;

/**
 * Base class that holds scheduled job information
 *
 * @author svenkubiak
 *
 */
public class Job {
    private final boolean active;
    private final String name;
    private final String description;
    private final Date nextFireTime;
    private final Date previousFireTime;

    public Job(boolean active, String name, String description, Date nextFireTime, Date previousFireTime) {
        this.active = active;
        this.name = name;
        this.nextFireTime = (nextFireTime == null) ? null : (Date) nextFireTime.clone();
        this.previousFireTime =  (previousFireTime == null) ? null : (Date) previousFireTime.clone();
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public Date getNextFireTime() {
        return (nextFireTime == null) ? null : (Date) nextFireTime.clone();
    }

    public Date getPreviousFireTime() {
        return (previousFireTime == null) ? null : (Date) previousFireTime.clone();
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }
}