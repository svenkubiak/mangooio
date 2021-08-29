package jobs;

import io.mangoo.annotations.Schedule;

public class InfoJob {
    @Schedule(at = "Every 3m")
    public void execute() {
        //Do nothing for now
    }
}