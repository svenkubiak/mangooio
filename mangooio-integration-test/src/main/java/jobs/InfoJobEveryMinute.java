package jobs;

import io.mangoo.scheduler.annotations.Run;

public class InfoJobEveryMinute {
    @Run(at = "Every 3m")
    public void execute() {
        //Do nothing for now
    }
}