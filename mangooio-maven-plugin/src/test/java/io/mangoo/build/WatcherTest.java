package io.mangoo.build;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

class WatcherTest {
    // The JDK watch service polls on macOS and reports changes with a delay of several seconds
    private static final Duration TIMEOUT = Duration.ofSeconds(45);

    @TempDir
    Path tempDir;

    @Test
    void testDeletedOutputDirectoryIsWatchedAgainOnceRecreated() throws Exception {
        //given
        Path target = tempDir.resolve("target");
        Path classes = Files.createDirectories(target.resolve("classes"));
        // The trigger thread is never started, so the runner is never asked to restart
        var trigger = new Trigger(new Runner("none", "", tempDir.toFile(), 0, null));
        var watcher = new Watcher(Set.of(classes), Set.of(), Set.of(), trigger);
        var thread = Thread.ofPlatform().daemon().start(watcher);

        try {
            //when the output directory is deleted as by mvn clean
            deleteRecursively(target);
            Thread.sleep(TIMEOUT.toMillis() / 3);

            //then the watcher keeps running
            assertThat(thread.isAlive(), equalTo(true));

            //when the output directory is recreated
            int beforeRecreate = trigger.getAccumulatedTriggerCount();
            Files.createDirectories(classes);

            //then a restart is triggered
            await().atMost(TIMEOUT).untilAsserted(() -> assertThat(trigger.getAccumulatedTriggerCount(), greaterThan(beforeRecreate)));

            //when a class file is written into the recreated directory
            int beforeWrite = trigger.getAccumulatedTriggerCount();
            Files.writeString(classes.resolve("Controller.class"), "compiled");

            //then the change is noticed, so the directory is watched again
            await().atMost(TIMEOUT).untilAsserted(() -> assertThat(trigger.getAccumulatedTriggerCount(), greaterThan(beforeWrite)));
        } finally {
            watcher.doShutdown();
            thread.interrupt();
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        try (Stream<Path> paths = Files.walk(path)) {
            for (Path file : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.delete(file);
            }
        }
    }
}
