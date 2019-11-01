package com.crown.quartz.config;

import com.crown.quartz.tamedtask.RefreshCardDataCacheTimedTask;
import com.crown.quartz.factory.AutoWiredSpringBeanToJobFactory;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.Properties;
/**
 * description:
 *
 * @author 刘一博
 * @version V1.0
 * @date 2019/10/30 10:02
 */
@Configuration
public class QuartzConfig {
    //TODO 计划用数据库存储定时任务信息，并提供可视化以方便界面操作定时任务
    /**
     * 配置文件路径
     */
    private static final String QUARTZ_CONFIG = "/application-quartz.properties";

    @Autowired
    private DataSource dataSource;

    @Value("${quartz.cronExpression}")
    private String cronExpression;

    /**
     * 从quartz.properties文件中读取Quartz配置属性
     * @return
     * @throws IOException
     */
    @Bean
    public Properties quartzProperties() throws IOException {
        PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
        propertiesFactoryBean.setLocation(new ClassPathResource(QUARTZ_CONFIG));
        propertiesFactoryBean.afterPropertiesSet();
        return propertiesFactoryBean.getObject();
    }

    /**
     * JobFactory与schedulerFactoryBean中的JobFactory相互依赖,注意bean的名称
     * 在这里为JobFactory注入了Spring上下文
     *
     * @param applicationContext
     * @return
     */
    @Bean
    public JobFactory buttonJobFactory(ApplicationContext applicationContext) {
        AutoWiredSpringBeanToJobFactory jobFactory = new AutoWiredSpringBeanToJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    /**
     *
     * @param buttonJobFactory  为SchedulerFactory配置JobFactory
     * @param cronJobTrigger
     * @return
     * @throws IOException
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(JobFactory buttonJobFactory, Trigger... cronJobTrigger) throws IOException {
        SchedulerFactoryBean factory = new SchedulerFactoryBean();
        factory.setJobFactory(buttonJobFactory);
        factory.setOverwriteExistingJobs(true);
        // 设置自行启动
        factory.setAutoStartup(true);
        factory.setQuartzProperties(quartzProperties());
        factory.setTriggers(cronJobTrigger);
        // 使用应用的dataSource替换quartz的dataSource
        factory.setDataSource(dataSource);
        return factory;
    }

    /**
     * 配置JobDetailFactory
     * JobDetailFactoryBean与CronTriggerFactoryBean相互依赖,注意bean的名称
     *
     * @return
     */
    @Bean
    public JobDetailFactoryBean jobDetailFactoryBean() {
        //集群模式下必须使用JobDetailFactoryBean，MethodInvokingJobDetailFactoryBean 类中的 methodInvoking 方法，是不支持序列化的
        JobDetailFactoryBean jobDetail = new JobDetailFactoryBean();
        jobDetail.setDurability(true);
        jobDetail.setRequestsRecovery(true);
        jobDetail.setJobClass(RefreshCardDataCacheTimedTask.class);
        return jobDetail;
    }

    /**
     * 配置具体执行规则
     * @param jobDetail
     * @return
     */
    @Bean
    public CronTriggerFactoryBean cronJobTrigger(JobDetail jobDetail) {
        CronTriggerFactoryBean trigger = new CronTriggerFactoryBean();
        trigger.setJobDetail(jobDetail);
        //延迟启动
        trigger.setStartDelay(2000);
        //从application.yml文件读取
        trigger.setCronExpression(cronExpression);
        return trigger;
    }
}
