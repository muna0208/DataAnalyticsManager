package com.daumsoft.dataAnalyticsManager.restFullApi.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daumsoft.dataAnalyticsManager.common.service.HttpService;
import com.daumsoft.dataAnalyticsManager.restFullApi.mapper.DeeplearningRestMapper;

import net.sf.json.JSONObject;

@Service
public class DeeplearningRestService {

	@Autowired
	private DeeplearningRestMapper deeplearningRestMapper;

	@Autowired
	private HttpService httpService;

	@Value("${module.ip}")
	private String moduleIp;

	@Value("${module.port}")
	private String modulePort;

	@Value("${module.method}")
	private String moduleMethod;

	/**
	 * 딥러닝 원본 데이터 등록
	 * 
	 * @param originalData
	 * @return
	 */
	public JSONObject createOriginalData(Map<String, Object> param) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();
		JSONObject requestParam = new JSONObject();

		// 원본리스트 중복체크
		if (deeplearningRestMapper.checkDuplicateOriginalData(param) > 0) {
			resultJson.put("result", "warning");
			resultJson.put("detail", "중복된 원본 데이터가 있습니다.");
			return resultJson;
		}

		String listUrl = moduleIp + ":" + modulePort + moduleMethod + "/dl/originalData";
		// 파일 경로 가져오기
		requestParam.put("project_id", param.get("id"));
		requestParam.put("data_path", param.get("path"));
		httpJson = httpService.httpServicePOST(listUrl, requestParam.toString());
		if ("202".equals(httpJson.get("type"))) {
			resultJson.put("result", "success");
			resultJson.put("detail", "생성에 성공하였습니다.");
		} else {
			resultJson.put("result", "fail");
			resultJson.put("detail", httpJson);
		}
		return resultJson;
	}

	/**
	 * 딥러닝 원본 데이터 삭제
	 * 
	 * @param originalData
	 * @return
	 */
	public JSONObject deleteOriginalData(String orgId) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();
		String listUrl = moduleIp + ":" + modulePort + moduleMethod + "/dl/originalData/" + orgId;
		httpJson = httpService.httpServiceDELETE(listUrl);
		if ("200".equals(httpJson.get("type"))) {
			resultJson.put("result", "success");
			resultJson.put("detail", "삭제에 성공하였습니다.");
		} else {
			resultJson.put("result", "fail");
			resultJson.put("detail", "삭제에 실패하였습니다.");
		}
		return resultJson;
	}

	/**
	 * 딥러닝 원본 데이터 리스트 가져오기
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getOriginalDataList(Map<String, Object> param) throws Exception {
		return deeplearningRestMapper.getOriginalDataList(param);
	}

	/**
	 * 딥러닝 원본 데이터 가져오기
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getOriginalData(Map<String, Object> param) throws Exception {
		return deeplearningRestMapper.getOriginalData(param);
	}

	/**
	 * 딥러닝 Pre-Trained 모델 목록 가져오기
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getPreTrainedModelList() throws Exception {
		return deeplearningRestMapper.getPreTrainedModelList();
	}

	/**
	 * 딥러닝 Pre-Trained Network 분석 팝업의 입력값(파라미터) 가져오기
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getNeuralLayerParameterList() throws Exception {
		return deeplearningRestMapper.getNeuralLayerParameterList();
	}

	/**
	 * 딥러닝 파리미터 설정 팝업의 optimizer 가져오기
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getNeuralLayerOptimizers() throws Exception {
		return deeplearningRestMapper.getNeuralLayerOptimizers();
	}

	/**
	 * 딥러닝 모델 등록
	 * 
	 * @param originalData
	 * @return
	 */
	public JSONObject createModel(String param, String userType) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();
		String listUrl = moduleIp + ":" + modulePort + moduleMethod + "/dl/model";
		// 사용자 타입 요청 파라미터 추가
		listUrl += ("beginner".equals(userType)) ? "?user_type=beginner" : "?user_type=advanced";
		// 파일 경로 가져오기
		httpJson = httpService.httpServicePOST(listUrl, param);
		if ("202".equals(httpJson.get("type"))) {
			resultJson.put("result", "success");
			resultJson.put("detail", "생성에 성공하였습니다.");
			resultJson.put("data", httpJson.get("data"));
		} else {
			resultJson.put("result", "fail");
			resultJson.put("detail", httpJson);
		}
		return resultJson;
	}

	/**
	 * 딥러닝 모델의 상태 확인
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> checkModelStatus(Map<String, Object> param) throws Exception {
		return deeplearningRestMapper.checkModelStatus(param);
	}

	/**
	 * 딥러닝 모델 리스트 가져오기
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getModelList(Map<String, Object> param) throws Exception {
		return deeplearningRestMapper.getModelList(param);
	}

	/**
	 * 딥러닝 모델 데이터 가져오기
	 * 
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getModelData(Map<String, Object> param) throws Exception {
		return deeplearningRestMapper.getModelData(param);
	}

	/**
	 * 딥러닝 원본 데이터 삭제
	 * 
	 * @param originalData
	 * @return
	 */
	public JSONObject deleteModelData(String modelId) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();
		String listUrl = moduleIp + ":" + modulePort + moduleMethod + "/dl/model/" + modelId;
		httpJson = httpService.httpServiceDELETE(listUrl);
		if ("200".equals(httpJson.get("type"))) {
			resultJson.put("result", "success");
			resultJson.put("detail", "삭제에 성공하였습니다.");
		} else {
			resultJson.put("result", "fail");
			resultJson.put("detail", "삭제에 실패하였습니다.");
		}
		return resultJson;
	}

	/**
	 * 딥러닝 모델 테스트
	 * 
	 * @param originalData
	 * @return
	 */
	public JSONObject testModelData(String modelId, String testDataPath) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();
		JSONObject requestParam = new JSONObject();

		// 파일 경로 가져오기
		requestParam.put("mode", "TEST");
		requestParam.put("test_data_path", testDataPath);

		String listUrl = moduleIp + ":" + modulePort + moduleMethod + "/dl/model/" + modelId;
		httpJson = httpService.httpServicePATCH(listUrl, requestParam.toString());
		resultJson.put("data", httpJson);
		if ("200".equals(httpJson.get("type"))) {
			resultJson.put("result", "success");
			resultJson.put("detail", "테스트에 성공하였습니다.");
		} else {
			resultJson.put("result", "fail");
			resultJson.put("detail", "테스트에 실패하였습니다.");
		}
		return resultJson;
	}
}
