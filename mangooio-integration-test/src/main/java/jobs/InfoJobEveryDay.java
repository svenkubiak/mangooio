package jobs;

import io.mangoo.annotations.Schedule;

public class InfoJobEveryDay {
    @Schedule(at = "Every 3d")
    public void execute(){
        //Do nothing for now
    }
}