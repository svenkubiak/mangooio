package jobs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.mangoo.annotations.Schedule;

public class InfoJobCron {
    private static final Logger LOG = LogManager.getLogger(InfoJobCron.class);
    
    @Schedule(at = "0/1 * * * *")
    public void execute() {
        LOG.info("now");
    }
}