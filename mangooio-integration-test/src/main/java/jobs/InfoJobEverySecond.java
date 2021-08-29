package jobs;

import io.mangoo.annotations.Schedule;

public class InfoJobEverySecond {
    @Schedule(at = "Every 3s")
    public void execute() {
        //Do nothing for now
    }
}