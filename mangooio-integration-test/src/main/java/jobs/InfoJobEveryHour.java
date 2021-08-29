package jobs;

import io.mangoo.annotations.Schedule;

public class InfoJobEveryHour {
    @Schedule(at = "Every 3h")
    public void execute() {
        //Do nothing for now
    }
}