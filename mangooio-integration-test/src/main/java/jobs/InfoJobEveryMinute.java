package jobs;

import io.mangoo.annotations.Schedule;

public class InfoJobEveryMinute {
    @Schedule(rate = "Every 3m")
    public void execute() {
        //Do nothing for now
    }
}