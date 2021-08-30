package jobs;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.mangoo.annotations.Run;

public class InfoJobCron {
    private static final Logger LOG = LogManager.getLogger(InfoJobCron.class);
    
    @Run(at = "0/1 * * * *")
    public void execute() {
        LOG.info("now");
    }
}