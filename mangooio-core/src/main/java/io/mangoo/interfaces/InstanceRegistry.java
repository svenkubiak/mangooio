package io.mangoo.interfaces;

import io.mangoo.models.InstanceStatus;

import java.util.List;

public interface InstanceRegistry {
    void report(InstanceStatus status);
    void remove(String instanceId);
    /** Instances that reported within the last minute, sorted by id. */
    List<InstanceStatus> getInstances();
}
