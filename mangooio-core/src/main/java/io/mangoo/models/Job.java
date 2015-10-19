package io.mangoo.models;

import java.util.Date;

public class Job {
    private boolean active;
    private String name;
    private String description;
    private Date nextFireTime;
    private Date previousFireTime;

    public Job(boolean active, String name, String description, Date nextFireTime, Date previousFireTime) {
        this.active = active;
        this.name = name;
        this.nextFireTime = (nextFireTime != null) ? (Date) nextFireTime.clone() : null;
        this.previousFireTime = (previousFireTime != null) ? (Date) previousFireTime.clone() : null;
        this.description = description;
    }

    public String getName() {
        return this.name;
    }

    public Date getNextFireTime() {
        return (this.nextFireTime != null) ? (Date) nextFireTime.clone() : null;
    }

    public Date getPreviousFireTime() {
        return (this.previousFireTime != null) ? (Date) previousFireTime.clone() : null;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return active;
    }
}