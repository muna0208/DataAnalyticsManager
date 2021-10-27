package com.daumsoft.dataAnalyticsManager.restFullApi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeeplearningRestMapper {
	int checkDuplicateOriginalData(Map<String, Object> param) throws Exception;

	List<Map<String, Object>> getOriginalDataList(Map<String, Object> param) throws Exception;

	Map<String, Object> getOriginalData(Map<String, Object> param) throws Exception;

	List<Map<String, Object>> getPreTrainedModelList() throws Exception;

	List<Map<String, Object>> getNeuralLayerParameterList() throws Exception;

	List<Map<String, Object>> getNeuralLayerOptimizers() throws Exception;

	List<Map<String, Object>> getModelList(Map<String, Object> param) throws Exception;

	Map<String, Object> checkModelStatus(Map<String, Object> param) throws Exception;

	Map<String, Object> getModelData(Map<String, Object> param) throws Exception;
}
