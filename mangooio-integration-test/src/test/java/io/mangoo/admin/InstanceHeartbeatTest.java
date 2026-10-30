package io.mangoo.admin;

import io.mangoo.TestExtension;
import io.mangoo.interfaces.InstanceRegistry;
import io.mangoo.models.InstanceStatus;
import io.mangoo.models.Metrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.Duration;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;

@ExtendWith({TestExtension.class})
class InstanceHeartbeatTest {

    @Test
    void testStopDoesNotWaitForAnUnavailableRegistry() {
        //given a registry that hangs like an unreachable MongoDB until the server selection times out
        InstanceRegistry unavailable = new InstanceRegistry() {
            @Override
            public void report(InstanceStatus status) {
                // Reports are accepted
            }

            @Override
            public void remove(String instanceId) {
                try {
                    Thread.sleep(Duration.ofSeconds(30));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            @Override
            public List<InstanceStatus> getInstances() {
                return List.of();
            }
        };
        var heartbeat = new InstanceHeartbeat(unavailable, new Metrics());
        heartbeat.start();

        //when
        long start = System.nanoTime();
        heartbeat.stop();
        Duration elapsed = Duration.ofNanos(System.nanoTime() - start);

        //then
        assertThat(elapsed, lessThan(Duration.ofSeconds(5)));
    }
}
