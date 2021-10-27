package com.daumsoft.dataAnalyticsManager.restFullApi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.daumsoft.dataAnalyticsManager.restFullApi.domain.Model;
import com.daumsoft.dataAnalyticsManager.restFullApi.domain.OriginalData;
import com.daumsoft.dataAnalyticsManager.restFullApi.domain.PreprocessedData;
import com.daumsoft.dataAnalyticsManager.restFullApi.domain.Project;

@Mapper
public interface ProjectRestMapper {

	List<Map<String, Object>> projects(String currentId, int pageNo, String option, String value, String type)
			throws Exception;

	Map<String, Object> project(Integer projectSequencePk) throws Exception;

	int checkProjectName(Project project) throws Exception;

	void insertProject(Project project) throws Exception;

	void updateProject(Project project) throws Exception;

	List<Map<String, Object>> originalDataList(Integer projectSequencePk) throws Exception;

	Map<String, Object> originalData(Integer projectSequencePk, Integer originalDataSequencePk) throws Exception;

	void insertOriginalData(OriginalData origianlData) throws Exception;

	void deleteOriginalData(OriginalData origianlData) throws Exception;

	List<Map<String, Object>> preprocessFunctionList() throws Exception;

	Map<String, Object> preprocessFunction(Integer preprocessFunctionSequencePk) throws Exception;

	int checkDuplicateOriginalData(OriginalData originalData) throws Exception;

	void insertPreprocessedData(PreprocessedData preprocessedData) throws Exception;

	List<Map<String, Object>> preprocessedDataList(Integer originalDataSequencePk) throws Exception;

	Map<String, Object> preprocessedData(Integer preprocessedDataSequencePk) throws Exception;

	void updatePreprocessedData(PreprocessedData pData) throws Exception;

	void insertModel(@Param("model") Model model, @Param("currentId") String currentId) throws Exception;

	void updateModels(Model model) throws Exception;

	List<Map<String, Object>> modelsList(@Param("model") Model model, @Param("userAuth") String userAuth,
			@Param("currentId") String currentId, @Param("type") Character type) throws Exception;

	Map<String, Object> model(Integer projectSequencePk, Integer modelSequencePk) throws Exception;

	Map<String, Object> getValidationSummary(int modelSequencePk) throws Exception;

	List<Map<String, Object>> getPreprocesseFunctionList() throws Exception;

	int getProjectCount(String currentId, String option, String value, String type) throws Exception;

	int getModelCount(String type) throws Exception;
}
