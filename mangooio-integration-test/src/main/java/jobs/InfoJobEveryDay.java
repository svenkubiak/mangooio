package jobs;

import io.mangoo.scheduler.annotations.Run;

public class InfoJobEveryDay {
    @Run(at = "Every 3d")
    public void execute(){
        //Do nothing for now
    }
}