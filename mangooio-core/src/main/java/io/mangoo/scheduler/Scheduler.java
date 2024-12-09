package io.mangoo.scheduler;

import jakarta.inject.Singleton;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Singleton
public class Scheduler {
    private final Set<Schedule> schedules = new HashSet<>();

    public Scheduler() {
    }

    public void addSchedule(Schedule schedule) {
        Objects.requireNonNull(schedule, "can not be null");

        schedules.add(schedule);
    }

    public Set<Schedule> getSchedules() {
        return schedules;
    }
}
