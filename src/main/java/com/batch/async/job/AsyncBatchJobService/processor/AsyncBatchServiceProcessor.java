package com.batch.async.job.AsyncBatchJobService.processor;

import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.item.ItemProcessor;

import com.batch.async.job.AsyncBatchJobService.Vo.ExcelVo;

public class AsyncBatchServiceProcessor implements ItemProcessor<ExcelVo, ExcelVo>, ItemProcessListener<ExcelVo, ExcelVo> {

	@Override
	public void beforeProcess(ExcelVo item) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterProcess(ExcelVo item, ExcelVo result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProcessError(ExcelVo item, Exception e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ExcelVo process(ExcelVo item) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
