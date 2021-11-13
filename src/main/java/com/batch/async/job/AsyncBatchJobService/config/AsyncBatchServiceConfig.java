package com.batch.async.job.AsyncBatchJobService.config;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@EnableBatchProcessing
public class AsyncBatchServiceConfig extends DefaultBatchConfigurer {
	
	@Bean
	public JobLauncher getJobLauncher() {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(getJobRepository());
		jobLauncher.setTaskExecutor(getTaskExecutor());
		try {
			jobLauncher.afterPropertiesSet();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jobLauncher;
	}

	@Primary
	@Bean(name = "threadPoolTaskExecutor")
	public ThreadPoolTaskExecutor getTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(10);
		taskExecutor.setMaxPoolSize(20);
		taskExecutor.setQueueCapacity(50);
		taskExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {

			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				executor.execute(r);
			}
		});
		taskExecutor.setThreadNamePrefix("ThreadPoolTaskExecutor-");
		taskExecutor.afterPropertiesSet();
		return taskExecutor;
	}
	
	@Bean(name = "stepThreadPoolTaskExecutor")
	public ThreadPoolTaskExecutor getStepTaskExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(10);
		taskExecutor.setMaxPoolSize(20);
		taskExecutor.setQueueCapacity(50);
		taskExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {

			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				executor.execute(r);
			}
		});
		taskExecutor.setThreadNamePrefix("stepThreadPoolTaskExecutor-");
		taskExecutor.afterPropertiesSet();
		return taskExecutor;
	}
	
	@Bean(name = "asyncThreadPoolTaskProcessor")
	public ThreadPoolTaskExecutor getAsyncTaskProcessExecutor() {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setCorePoolSize(10);
		taskExecutor.setMaxPoolSize(20);
		taskExecutor.setQueueCapacity(50);
		taskExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {

			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				executor.execute(r);
			}
		});
		taskExecutor.setThreadNamePrefix("AsyncThreadProcessor-");
		taskExecutor.afterPropertiesSet();
		return taskExecutor;
	}

	@Override
	public void setDataSource(DataSource datSource) {
	}
}