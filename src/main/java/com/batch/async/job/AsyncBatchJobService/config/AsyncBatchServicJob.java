package com.batch.async.job.AsyncBatchJobService.config;

import java.util.concurrent.Future;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.batch.async.job.AsyncBatchJobService.Vo.ExcelVo;
import com.batch.async.job.AsyncBatchJobService.processor.AsyncBatchServiceProcessor;
import com.batch.async.job.AsyncBatchJobService.reader.AsyncBatchServiceReader;
import com.batch.async.job.AsyncBatchJobService.writer.AsyncBatchServiceWriter;

@Configuration
@Import({ AsyncBatchServiceConfig.class })
public class AsyncBatchServicJob {

	@Autowired
	JobLauncher jobLauncher;

	@Autowired
	JobBuilderFactory jobBuilderFactory;

	@Autowired
	StepBuilderFactory stepBuilderFactory;

	@Qualifier(value = "stepThreadPoolTaskExecutor")
	@Autowired
	ThreadPoolTaskExecutor stepThreadPoolTaskExecutor;
	
	@Qualifier(value = "asyncThreadPoolTaskProcessor")
	@Autowired
	ThreadPoolTaskExecutor asyncThreadPoolTaskProcessor;
	
	@Value("${file.dir}")
	private String fileDirectory;

	public void perform() throws Exception {
		JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
		jobParametersBuilder.addString("JobName", "AsyncJob");
		jobParametersBuilder.addString("fileDir", fileDirectory);
		jobLauncher.run(asyncJob(), jobParametersBuilder.toJobParameters());
	}

	@Bean(name = "asyncJob")
	public Job asyncJob() throws Exception {
		return jobBuilderFactory.get("asyncJob").start(asyncStep()).listener(new JobExecutionListener() {

			@Override
			public void beforeJob(JobExecution jobExecution) {
			}

			@Override
			public void afterJob(JobExecution jobExecution) {
			}
		}).build();
	}

	@Bean(name = "asyncStep")
	public Step asyncStep() throws Exception {
		return stepBuilderFactory.get("asyncStep").<ExcelVo, Future<ExcelVo>>chunk(100).reader(readFileData(null))
				.processor((ItemProcessor<? super ExcelVo, ? extends Future<ExcelVo>>) asyncItemProcessor())
				.writer(asyncItemWriter()).listener(new StepExecutionListener() {

					@Override
					public void beforeStep(StepExecution stepExecution) {
					}

					@Override
					public ExitStatus afterStep(StepExecution stepExecution) {
						return ExitStatus.COMPLETED;
					}
				}).taskExecutor(stepThreadPoolTaskExecutor).build();
	}

	@Bean
	@StepScope
	public ItemReader<ExcelVo> readFileData(@Value("#{jobParameters['fileDir']}") String fileName) {
		return new AsyncBatchServiceReader();
	}

	@Bean
	public AsyncItemProcessor<ExcelVo, ExcelVo> asyncItemProcessor() throws Exception {
		AsyncItemProcessor<ExcelVo, ExcelVo> asyncItemProcessor = new AsyncItemProcessor<>();
		asyncItemProcessor.setTaskExecutor(asyncThreadPoolTaskProcessor);
		asyncItemProcessor.setDelegate(processFileData());
		asyncItemProcessor.afterPropertiesSet();
		return asyncItemProcessor;
	}

	@Bean
	public ItemProcessor<ExcelVo, ExcelVo> processFileData() {
		return new AsyncBatchServiceProcessor();
	}

	@Bean
	public AsyncItemWriter<ExcelVo> asyncItemWriter() throws Exception {
		AsyncItemWriter<ExcelVo> asyncItemWriter = new AsyncItemWriter<>();
		asyncItemWriter.setDelegate(writeFileData());
		asyncItemWriter.afterPropertiesSet();
		return asyncItemWriter;
	}

	@Bean
	public ItemWriter<ExcelVo> writeFileData() {
		return new AsyncBatchServiceWriter();
	}

}
