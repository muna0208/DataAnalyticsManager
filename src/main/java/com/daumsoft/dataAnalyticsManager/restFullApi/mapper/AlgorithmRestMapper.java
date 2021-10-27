package com.daumsoft.dataAnalyticsManager.restFullApi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AlgorithmRestMapper {

	List<Map<String, Object>> algorithms(int pageNo, String option, String value, String type);

	Map<String, Object> algorithm(Integer id);

	List<Map<String, Object>> searchAlgorithms(String searchValue, String searchType);

	int getTotalAlgorithmCount(String option, String value, String type) throws Exception;
}