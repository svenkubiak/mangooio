package jobs;

import io.mangoo.annotations.Run;

public class InfoJobEverySecond {
    @Run(at = "Every 3s")
    public void execute() {
        // Nothing to do here
    }
}