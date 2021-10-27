package com.daumsoft.dataAnalyticsManager.common.service;

import java.net.ConnectException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

// import com.daumsoft.dataAnalyticsManager.restFullApi.mapper.ProjectRestMapper;

import net.sf.json.JSONObject;

@Service
@Async("threadPoolTaskExecutor")
public class AsyncService {
	private Logger logger = LoggerFactory.getLogger(HttpService.class);
	
	@Autowired
	private HttpService httpService;
	
	// @Autowired
	// private ProjectRestMapper projectRestMapper;
	
	@Value("${module.asyncSecond}")
	private Integer asyncSecond;
	
	@Value("${module.asyncPeriod}")
	private Integer asyncPeriod;
	
	private static int limitFailCnt = 5;
	private int connectionFailCnt;

	/**
	 * 머신러닝 전처리 생성
	 * @param url
	 * @param preprocessedDataSequencePk
	 * @return
	 */
	@SuppressWarnings("static-access")
	public Future<String> preprocessedData(String url, Integer preprocessedDataSequencePk){
		logger.info("AsyncService-preprocessedData : Start!!! ");
		JSONObject httpJson = null;
		JSONObject preprocessedDataJson = null;
		boolean result = false;
		connectionFailCnt = 0;
		try {
			for (int i = 0; i < asyncPeriod; i++) {
				try {
					httpJson = httpService.httpServiceGET(url);
					preprocessedDataJson = new JSONObject().fromObject(httpJson.get("data"));	
				} catch (ConnectException e) {
					connectionFailCnt++;
					logger.info("AsyncService-preprocessedData connectionFailCnt: "+connectionFailCnt+", Error: "+e);
					if( connectionFailCnt > limitFailCnt)	break;
				}
				
				if( !"ongoing".equals(preprocessedDataJson.get("progress_state")) ) {
//					PreprocessedData pData = new PreprocessedData();
//					pData.setPreprocessedDataSequencePk(preprocessedDataSequencePk);
//					pData.setFilepath(""+preprocessedDataJson.get("filepath"));
//					pData.setFilename(""+preprocessedDataJson.get("filename"));
//					pData.setSummary(""+preprocessedDataJson.get("summary"));
//					pData.setProgressState(""+preprocessedDataJson.get("progress_state"));
//					pData.setProgressEndDatetime(""+preprocessedDataJson.get("progress_end_date"));
//					pData.setColumns(""+preprocessedDataJson.get("column_info"));
//					pData.setStatistics(""+preprocessedDataJson.get("statistic_info"));
//					pData.setSampleData(""+preprocessedDataJson.get("sample_data"));
//					pData.setAmount(Integer.parseInt(""+preprocessedDataJson.get("amount")));
//					projectRestMapper.updatePreprocessedData(pData);
					
					logger.info("AsyncService-preprocessedData : Complete preprecessedData");
					result = true;
					break;
				}
				logger.info("AsyncService-preprocessedData : i => "+i);
				Thread.sleep(asyncSecond);
			}
			if( !result ) {
//				PreprocessedData pData = new PreprocessedData();
//				pData.setPreprocessedDataSequencePk(preprocessedDataSequencePk);
//				pData.setProgressState("fail");
//				projectRestMapper.updatePreprocessedData(pData);
				logger.info("AsyncService-preprocessedData : overTime");
			}
		} catch (Exception e) {
			logger.error("Error AsyncService-preprocessedData",e);
			
			try {
//				PreprocessedData pData = new PreprocessedData();
//				pData.setPreprocessedDataSequencePk(preprocessedDataSequencePk);
//				pData.setProgressState("fail");
//				projectRestMapper.updatePreprocessedData(pData);
				logger.error("Error AsyncService-preprocessedData update",e);
				
			} catch (Exception e1) {
				logger.error("Error AsyncService-preprocessedData update",e);
			}
			
		}
		logger.info("AsyncService-preprocessedData : End!!! ");
		if( result )return new AsyncResult<String>("success");
		else return new AsyncResult<String>("false");
	}

	/**
	 * 머신러닝 모델 생성
	 * @param url
	 * @param modelSequencePk
	 */
	@SuppressWarnings("static-access")
	public Future<String> models(String url, Integer modelSequencePk) {
		logger.info("AsyncService-models : Start!!! ");
		JSONObject httpJson = null;
		JSONObject modelsJson = null;
		boolean result = false;
		connectionFailCnt = 0;
		try {
			for (int i = 0; i < asyncPeriod; i++) {
				try {
					httpJson = httpService.httpServiceGET(url);
					modelsJson = new JSONObject().fromObject(httpJson.get("data"));
					
				} catch (ConnectException e) {
					logger.info("AsyncService-models : "+e);
					connectionFailCnt++;
					logger.info("AsyncService-models connectionFailCnt: "+connectionFailCnt+", Error: "+e);
					if( connectionFailCnt > limitFailCnt)	break;
				}
				
				if( !"ongoing".equals(modelsJson.get("progress_state")) ) {
//					Model model = new Model();
//					model.setModelSequencePk(modelSequencePk);
//					model.setFilepath(""+modelsJson.get("filepath"));
//					model.setFilename(""+modelsJson.get("filename"));
//					model.setTrainSummary(""+modelsJson.get("train_summary"));
//					model.setValidationSummary(""+modelsJson.get("validation_summary"));
//					model.setProgressState(""+modelsJson.get("progress_state"));
//					model.setProgressEndDatetime(""+modelsJson.get("progress_end_date"));
//					model.setLoadState(""+modelsJson.get("LOAD_STATE"));
//					projectRestMapper.updateModels(model);
					//관리도구가 아닌 모듈에서 수행
					logger.info("AsyncService-models : Complete preprecessedData");
					result = true;
					break;
				}
				Thread.sleep(asyncSecond);
			}
			if( !result ) {
//				Model model = new Model();
//				model.setModelSequencePk(modelSequencePk);
//				model.setProgressState("standby");
//				projectRestMapper.updateModels(model);
				//관리도구가 아닌 모듈에서 수행
				logger.info("AsyncService-models : overTime");
			}
		} catch (Exception e) {
			try {
//				Model model = new Model();
//				model.setModelSequencePk(modelSequencePk);
//				model.setProgressState("fail");
//				projectRestMapper.updateModels(model);
				//관리도구가 아닌 모듈에서 수행
			} catch (Exception e1) {
				logger.error("Error AsyncService-models update",e);
			}
			logger.error("Error AsyncService-models",e);
		}
		logger.info("AsyncService-models : End!!! ");
		if( result )return new AsyncResult<String>("success");
		else return new AsyncResult<String>("false");
		
	}
}
