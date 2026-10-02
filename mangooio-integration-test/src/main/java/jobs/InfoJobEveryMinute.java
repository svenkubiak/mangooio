package jobs;

import io.mangoo.annotations.Run;

public class InfoJobEveryMinute {
    @Run(at = "Every 3m")
    public void execute() {
        // Nothing to do here
    }
}