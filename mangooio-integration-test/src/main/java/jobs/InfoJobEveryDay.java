package jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.inject.Singleton;

import io.mangoo.annotations.Schedule;

@Singleton
@Schedule(cron = "Every 3d", description = "This is a job description")
public class InfoJobEveryDay implements Job {
    @Override
    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException {
        //Do nothing for now
    }
}