package jobs;

import io.mangoo.scheduler.annotations.Run;

public class InfoJobEverySecond {
    @Run(at = "Every 3s")
    public void execute() {
        //Do nothing for now
    }
}