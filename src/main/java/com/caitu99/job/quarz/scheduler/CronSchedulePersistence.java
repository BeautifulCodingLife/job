package com.caitu99.job.quarz.scheduler;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.caitu99.job.quarz.bean.CronJobBean;
import com.caitu99.job.quarz.util.QuarzKeyUtil;

@Component
public class CronSchedulePersistence {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private Scheduler persistenceScheduler;
	
	/**
	 * 启动
	 * @Title: run 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param jobBean
	 * @throws SchedulerException
	 * @date 2014年8月21日 下午3:43:54  
	 * @author dzq
	 */
	public void scheduleJob(CronJobBean bean) throws SchedulerException {
		try {
			/** 获取组名称 */
			String schedId = persistenceScheduler.getSchedulerInstanceId();
			logger.info("-------  Cron Scheduling Job  -------------------");
			String jobName = null;
			if (null == bean.getJobName() || "".equals(bean.getJobName())) {
				jobName = QuarzKeyUtil.getJobName("CRON_JOB_NAME",null);
			}else {
				jobName = bean.getJobName();
			}
			JobDetail job = newJob(bean.getJobClazz())
					.withIdentity(new JobKey(jobName, bean.getJobGroupName()))
					.withDescription(bean.getCronExpression())
					.requestRecovery().build();
			if (null != bean.getParams() && !bean.getParams().isEmpty()) {
				for (String key : bean.getParams().keySet()) {
					job.getJobDataMap().put(key,bean.getParams().get(key));
				}
			}
			TriggerKey triggerKey = new TriggerKey(QuarzKeyUtil.getCronTrigerName(jobName, 
					 bean.getJobGroupName()), schedId);  
			Trigger trigger = newTrigger()
					    .withIdentity(triggerKey)  
		                .withSchedule(cronSchedule(bean.getCronExpression())
		                .withMisfireHandlingInstructionFireAndProceed())
		                .forJob(job).build(); 
			if (persistenceScheduler.checkExists(triggerKey)) {
				persistenceScheduler.rescheduleJob(triggerKey, trigger);
			}else{
				persistenceScheduler.scheduleJob(job, trigger);
			}
			logger.info("{} will run as: {} ",job.getKey(), bean.getCronExpression());
		} catch (Exception e) {
			throw new SchedulerException(e);
		}
	}
	
	/**
	 * 删除任务
	 * @Title: deleteJob 
	 * @Description: (这里用一句话描述这个方法的作用) 
	 * @param jobName
	 * @param JobGroupName
	 * @throws SchedulerException
	 * @date 2014年8月21日 下午4:48:45  
	 * @author dzq
	 */
	public void deleteJob(String jobName,String jobGroupName) throws SchedulerException{
		try {
			JobKey jobKey = new JobKey(jobName,jobGroupName);
			persistenceScheduler.deleteJob(jobKey);
			logger.info("{} will stoped ",jobKey);
		} catch (Exception e) {
			throw new SchedulerException(e);
		}
	}



}
