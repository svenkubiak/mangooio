package jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.inject.Singleton;

@Singleton
public class InfoJob implements Job {

    @Override
    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //Do nothing for now
    }
}