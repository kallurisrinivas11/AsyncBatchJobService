package com.batch.async.job.AsyncBatchJobService.writer;

import java.util.List;

import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.ItemWriter;

import com.batch.async.job.AsyncBatchJobService.Vo.ExcelVo;

public class AsyncBatchServiceWriter implements ItemWriter<ExcelVo>, ItemWriteListener<ExcelVo> {

	@Override
	public void beforeWrite(List<? extends ExcelVo> items) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterWrite(List<? extends ExcelVo> items) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onWriteError(Exception exception, List<? extends ExcelVo> items) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void write(List<? extends ExcelVo> items) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
