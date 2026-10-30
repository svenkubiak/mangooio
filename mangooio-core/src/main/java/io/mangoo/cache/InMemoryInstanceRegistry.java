package io.mangoo.cache;

import io.mangoo.constants.Required;
import io.mangoo.interfaces.InstanceRegistry;
import io.mangoo.models.InstanceStatus;
import io.mangoo.utils.Argument;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/** InstanceRegistry without persistence, which only knows the instance it runs in. */
@Singleton
public class InMemoryInstanceRegistry implements InstanceRegistry {
    private final AtomicReference<InstanceStatus> status = new AtomicReference<>();

    @Override
    public void report(InstanceStatus status) {
        this.status.set(Objects.requireNonNull(status, "status can not be null"));
    }

    @Override
    public void remove(String instanceId) {
        Argument.requireNonBlank(instanceId, Required.ID);
        status.updateAndGet(current -> current != null && current.id().equals(instanceId) ? null : current);
    }

    @Override
    public List<InstanceStatus> getInstances() {
        InstanceStatus current = status.get();
        return current == null ? List.of() : List.of(current);
    }
}
