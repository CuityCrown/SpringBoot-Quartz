package com.crown.quartz.tamedtask;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.stereotype.Component;

/**
 * description:
 *
 * @author 刘一博
 * @version V1.0
 * @date 2019/10/17 14:55
 */
@Component
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
public class RefreshCardDataCacheTimedTask implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext){


    }
}
