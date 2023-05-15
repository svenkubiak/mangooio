package jobs;

import io.mangoo.scheduler.annotations.Run;

public class InfoJobEveryHour {
    @Run(at = "Every 3h")
    public void execute() {
        //Do nothing for now
    }
}