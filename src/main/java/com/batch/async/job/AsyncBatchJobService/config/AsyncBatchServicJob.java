package com.batch.async.job.AsyncBatchJobService.config;

import java.util.List;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.ItemProcessListener;
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
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.batch.async.job.AsyncBatchJobService.Vo.ExcelVo;

@Configuration
@Import({ AsyncBatchServiceConfig.class })
@EnableScheduling
public class AsyncBatchServicJob {

	private static final Logger logger = LogManager.getLogger(AsyncBatchServicJob.class);

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

	@Scheduled(cron = "0 * * * * *")
	public void perform() throws Exception {
		JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
		jobParametersBuilder.addString("JobName", "AsyncJob" + System.currentTimeMillis());
		jobParametersBuilder.addString("fileDir", fileDirectory);
		jobLauncher.run(asyncJob(), jobParametersBuilder.toJobParameters());
	}

	@Bean(name = "asyncJob")
	public Job asyncJob() throws Exception {
		return jobBuilderFactory.get("asyncJob").start(asyncStep()).listener(new JobExecutionListener() {

			@Override
			public void beforeJob(JobExecution jobExecution) {
				logger.info("Job Started: {}", jobExecution.getStatus());
			}

			@Override
			public void afterJob(JobExecution jobExecution) {
				logger.info("Job Ended: {}", jobExecution.getStatus());
			}
		}).build();
	}

	@Bean(name = "asyncStep")
	public Step asyncStep() throws Exception {
		return stepBuilderFactory.get("asyncStep").<ExcelVo, Future<ExcelVo>>chunk(100).reader(readFileData(null))
				.processor((ItemProcessor<? super ExcelVo, ? extends Future<ExcelVo>>) asyncItemProcessor())
				.listener(new ItemProcessListener<ExcelVo, ExcelVo>() {

					@Override
					public void beforeProcess(ExcelVo item) {
						logger.info("Item Processor started for record: {}", item.getId());
					}

					@Override
					public void afterProcess(ExcelVo item, ExcelVo result) {
						logger.info("Item Processor ended for record: {}", item.getId());
					}

					@Override
					public void onProcessError(ExcelVo item, Exception e) {
						logger.error("Item Processor error for record: {}", item.getId());
						logger.error("Item Processot error: {}", e.getMessage());
					}
				}).writer(asyncItemWriter()).listener(new StepExecutionListener() {

					@Override
					public void beforeStep(StepExecution stepExecution) {
						logger.info("Step Started: {}", stepExecution.getStatus());
					}

					@Override
					public ExitStatus afterStep(StepExecution stepExecution) {
						logger.info("Step Ended: {}", stepExecution.getStatus());
						return ExitStatus.COMPLETED;
					}
				}).taskExecutor(stepThreadPoolTaskExecutor).build();
	}

	@Bean
	@StepScope
	public FlatFileItemReader<ExcelVo> readFileData(@Value("#{jobParameters['fileDir']}") String fileName) {
		FlatFileItemReader<ExcelVo> itemReader = new FlatFileItemReader<ExcelVo>();
		itemReader.setLinesToSkip(1);
		itemReader.setResource(new FileSystemResource(fileName));
		itemReader.setLineMapper(new DefaultLineMapper<ExcelVo>() {
			{
				setLineTokenizer(new DelimitedLineTokenizer("|") {
					{
						setNames(new String[] { "ID", "FULLNAME", "FIRSTNAME", "LASTNAME", "EMAILADDRESS",
								"PHONENUMBER" });
					}
				});
				setFieldSetMapper(new BeanWrapperFieldSetMapper<ExcelVo>() {
					{
						setTargetType(ExcelVo.class);
					}
				});
			}
		});
		return itemReader;
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
		ItemProcessor<ExcelVo, ExcelVo> itemProcessor = new ItemProcessor<ExcelVo, ExcelVo>() {

			@Override
			public ExcelVo process(ExcelVo item) throws Exception {
				return item;
			}
		};
		return itemProcessor;
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
		ItemWriter<ExcelVo> itemWriter = new ItemWriter<ExcelVo>() {

			@Override
			public void write(List<? extends ExcelVo> items) throws Exception {

			}
		};
		return itemWriter;
	}

}
