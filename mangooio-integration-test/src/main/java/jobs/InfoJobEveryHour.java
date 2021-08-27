package jobs;

import io.mangoo.annotations.Schedule;

public class InfoJobEveryHour {
    @Schedule(rate = "Every 3h")
    public void execute() {
        //Do nothing for now
    }
}