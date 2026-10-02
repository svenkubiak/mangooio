package jobs;

import annotations.Label;
import io.mangoo.annotations.Run;

public class AnnotatedJob {
    @Run(at = "Every 1h")
    @Label("job")
    public void execute() {
        // Nothing to do here
    }

    @Deprecated
    @Label("helper")
    public void helper() {
        // Nothing to do here
    }
}
