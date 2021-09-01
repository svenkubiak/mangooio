package jobs;

import io.mangoo.annotations.Run;

public class InfoJobCron {
    @Run(at = "0/1 * * * *")
    public void execute() {
        //do nothing for now
    }
}