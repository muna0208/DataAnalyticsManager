package com.daumsoft.dataAnalyticsManager.restFullApi.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daumsoft.dataAnalyticsManager.common.service.AsyncService;
import com.daumsoft.dataAnalyticsManager.common.service.HttpService;
import com.daumsoft.dataAnalyticsManager.common.utils.MakeUtil;
import com.daumsoft.dataAnalyticsManager.common.utils.StringUtil;
import com.daumsoft.dataAnalyticsManager.restFullApi.domain.Model;
import com.daumsoft.dataAnalyticsManager.restFullApi.domain.OriginalData;
import com.daumsoft.dataAnalyticsManager.restFullApi.domain.PreprocessedData;
import com.daumsoft.dataAnalyticsManager.restFullApi.domain.Project;
import com.daumsoft.dataAnalyticsManager.restFullApi.mapper.ProjectRestMapper;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
@SuppressWarnings("static-access")
public class ProjectRestService {

	@Autowired
	private HttpService httpService;

	@Autowired
	private ProjectRestMapper projectRestMapper;

	@Autowired
	private AsyncService asyncService;

	@Autowired
	private SqlSession session;

	@Value("${module.ip}")
	private String moduleIp;

	@Value("${module.port}")
	private String modulePort;

	@Value("${module.method}")
	private String moduleMethod;

	@Value("${filePath}")
	private String filePath;

	@Value("${fileTestPath}")
	private String fileTestPath;

	@Value("${isTest}")
	private String isTest;

	/**
	 * 프로젝트 리스트 조회
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public JSONObject projects(String currentId, int page, String option, String value, String type) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONArray jsonArr = new JSONArray();

		List<Map<String, Object>> list = projectRestMapper.projects(currentId, page * 10, option, value, type);
		for (Map<String, Object> map : list) {
			if (MakeUtil.isNotNullAndEmpty(map)) {
				StringUtil.setMapDateFormat(map, "create_date", "yyyy-MM-dd HH:mm:ss");
				jsonArr.add(MakeUtil.nvlJson(new JSONObject().fromObject(map)));
			}
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		resultJson.put("projects", jsonArr);

		int count = projectRestMapper.getProjectCount(currentId, option, value, type);
		resultJson.put("count", count);
		return resultJson;
	}

	/**
	 * 프로젝트 이름 가져오기
	 * 
	 * @param currentId
	 * @return
	 * @throws Exception
	 */
	public JSONObject getProjects(String currentId, String userAuth) throws Exception {
		JSONObject resultJson = new JSONObject();
		Map<String, String> params = new HashMap<>();
		params.put("currentId", currentId);
		params.put("userAuth", userAuth);
		List<Project> projects = session.selectList("getProjectNameList", params);

		resultJson.put("projects", projects);
		resultJson.put("result", "success");
		MakeUtil.debug("" + projects);
		return resultJson;
	}

	/**
	 * 프로젝트 개별 조회
	 * 
	 * @param projectSequencePk
	 * @return
	 * @throws Exception
	 */
	public JSONObject project(Integer projectSequencePk) throws Exception {
		JSONObject resultJson = new JSONObject();

		Map<String, Object> detail = projectRestMapper.project(projectSequencePk);
		if (MakeUtil.isNotNullAndEmpty(detail)) {
			StringUtil.setMapDateFormat(detail, "create_date", "yyyy/MM/dd");
			resultJson.put("project", MakeUtil.nvlJson(new JSONObject().fromObject(detail)));
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}

	/**
	 * 프로젝트 등록
	 * 
	 * @param project
	 * @return
	 * @throws Exception
	 */
	public JSONObject projectsAsPost(Project project) throws Exception {
		JSONObject resultJson = new JSONObject();
		// 프로젝트 명 중복 체크
		if (projectRestMapper.checkProjectName(project) > 0) {
			resultJson.put("result", "fail");
			resultJson.put("type", "4100");
			resultJson.put("detail", "duplicateName");
			return resultJson;

		} else {
			// 프로젝트 등록
			if (MakeUtil.isNotNullAndEmpty(project.getId()))
				projectRestMapper.updateProject(project);
			else
				projectRestMapper.insertProject(project);

			Map<String, Object> detail = projectRestMapper.project(project.getId());
			if (MakeUtil.isNotNullAndEmpty(detail))
				resultJson.put("project", MakeUtil.nvlJson(new JSONObject().fromObject(detail)));

			resultJson.put("result", "success");
			resultJson.put("type", "2001");
			return resultJson;
		}
	}

	/**
	 * 프로젝트 수정
	 * 
	 * @param project
	 * @return
	 */
	public JSONObject projectsAsPatch(Project project) throws Exception {
		JSONObject resultJson = new JSONObject();
		// 템플릿 명 중복 체크
		if (projectRestMapper.checkProjectName(project) > 0) {
			resultJson.put("result", "fail");
			resultJson.put("type", "4100");
			resultJson.put("detail", "duplicateName");
			return resultJson;

		} else {
			// 프로젝트 수정
			projectRestMapper.updateProject(project);

			Map<String, Object> detail = projectRestMapper.project(project.getId());
			if (MakeUtil.isNotNullAndEmpty(detail))
				resultJson.put("project", MakeUtil.nvlJson(new JSONObject().fromObject(detail)));

			resultJson.put("result", "success");
			resultJson.put("type", "2001");
			return resultJson;
		}
	}

	/**
	 * 프로젝트 삭제
	 * 
	 * @param projectSequencePk
	 * @return
	 */
	public JSONObject projectAsDelete(Integer projectSequencePk, String option) throws Exception {
		JSONObject resultJson = new JSONObject();

		// 원본데이터 삭제(전처리 삭제,모델 삭제) => 전처리 삭제(update) => 모델 삭제(update)
		List<Map<String, Object>> originalDataList = projectRestMapper.originalDataList(projectSequencePk);
		JSONObject originalDataJson = null;
		for (Map<String, Object> originalData : originalDataList) {
			originalDataJson = originalDataAsDelete(projectSequencePk, Integer.parseInt("" + originalData.get("id")),
					option);
			if (!"success".equals(originalDataJson.get("result"))) {
				resultJson.put("type", originalDataJson.get("type"));
				resultJson.put("title", originalDataJson.get("title"));
				resultJson.put("data", originalDataJson.get("data"));
				throw new RuntimeException(resultJson.toString());
			}
		}

		// 프로젝트 수정
		Project project = new Project();
		project.setId(projectSequencePk);
		project.setDeleteFlag(true);
		projectRestMapper.updateProject(project);

		resultJson.put("result", "success");
		resultJson.put("type", "2001");
		return resultJson;
	}

	/**
	 * 원본 데이터 조회
	 * 
	 * @param projectSequencePk
	 * @return
	 * @throws Exception
	 */
	public JSONObject originalDataList(Integer projectSequencePk) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONArray jsonArr = new JSONArray();

		List<Map<String, Object>> list = projectRestMapper.originalDataList(projectSequencePk);
		for (Map<String, Object> map : list) {
			if (MakeUtil.isNotNullAndEmpty(map)) {
				jsonArr.add(MakeUtil.nvlJson(new JSONObject().fromObject(map)));
			}
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		resultJson.put("originalDataList", jsonArr);
		return resultJson;
	}

	/**
	 * 원본 데이터 개별 조회
	 * 
	 * @param projectSequencePk
	 * @param originalDataSequencePk
	 * @return
	 * @throws Exception
	 */
	public JSONObject originalData(Integer projectSequencePk, Integer originalDataSequencePk) throws Exception {
		JSONObject resultJson = new JSONObject();
		Map<String, Object> detail = projectRestMapper.originalData(projectSequencePk, originalDataSequencePk);

		if (MakeUtil.isNotNullAndEmpty(detail))
			resultJson.put("originalData", MakeUtil.nvlJson(new JSONObject().fromObject(detail)));

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}

	/**
	 * 머신러닝 원본 데이터 등록
	 * 
	 * @param originalData
	 * @return
	 */
	public JSONObject originalDataAsPost(OriginalData originalData) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();
		// JSONObject originalDataJson = new JSONObject();
		JSONObject param = new JSONObject();

		// 원본리스트 중복체크
		if (projectRestMapper.checkDuplicateOriginalData(originalData) > 0) {
			resultJson.put("result", "fail");
			resultJson.put("type", "4100");
			resultJson.put("detail", "duplicateName");
			return resultJson;

		}

		String listUrl = moduleIp + ":" + modulePort + moduleMethod + "/ml/originalData";
		// 파일 경로 가져오기
		param.put("project_id", originalData.getProjectSequenceFk1());
		param.put("data_path", originalData.getFilepath());
		httpJson = httpService.httpServicePOST(listUrl, param.toString());
		if ("201".equals(httpJson.get("type"))) {
			resultJson.put("result", "success");
			resultJson.put("type", "2000");
		} else {
			resultJson.put("type", "5000");
			resultJson.put("detail", httpJson);
		}

		return resultJson;
	}

	/**
	 * 원본 데이터 전처리 테스트
	 * 
	 * @param originalData
	 * @return
	 * @throws Exception
	 */
	public JSONObject originalDataAsPatch(Integer projectSequencePk, Integer originalDataSequencePk,
			Map<String, Object> requestTtest) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();
		JSONObject param = new JSONObject();

		StringBuilder listUrl = new StringBuilder(
				moduleIp + ":" + modulePort + moduleMethod + "/ml/originalData/" + originalDataSequencePk);
		Boolean beginner = (Boolean) requestTtest.get("beginner");
		if (beginner != null && beginner) {
			listUrl.append("?user_type=beginner");
		}

		param.put("request_test", requestTtest.get("request_test"));
		httpJson = httpService.httpServicePATCH(listUrl.toString(), param.toString());
		if ("200".equals(httpJson.get("type"))) {
			// 테스트 성공
			resultJson.put("data", httpJson.get("data"));
			resultJson.put("result", "success");
			resultJson.put("type", "2000");

		} else {
			resultJson.put("type", "5000");
			resultJson.put("detail", httpJson);
		}

		return resultJson;
	}

	/**
	 * 원본 데이터 수정
	 * 
	 * @param projectSequencePk
	 * @param originalDataSequencePk
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public JSONObject originalDataAsPatch(Integer projectSequencePk, Integer originalDataSequencePk, String param)
			throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();

		String listUrl = moduleIp + ":" + modulePort + moduleMethod + "/ml/originalData/" + originalDataSequencePk;
		httpJson = httpService.httpServicePATCH(listUrl, param);
		if ("200".equals(httpJson.get("type"))) {
			// 테스트 성공
			resultJson.put("data", httpJson.get("data"));
			resultJson.put("result", "success");
			resultJson.put("type", "2000");

		} else {
			resultJson.put("type", "5000");
			resultJson.put("detail", httpJson);
		}
		return resultJson;
	}

	/**
	 * 원본 데이터 삭제
	 * 
	 * @param projectSequencePk
	 * @param originalDataSequencePk
	 * @return
	 * @throws Exception
	 */
	public JSONObject originalDataAsDelete(Integer projectSequencePk, Integer originalDataSequencePk, String option)
			throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();

		// 전처리 삭제(모델 삭제) => 모델 삭제(update)
		List<Map<String, Object>> preprocessedList = projectRestMapper.preprocessedDataList(originalDataSequencePk);

		JSONObject preprocessedJson = null;
		for (Map<String, Object> preData : preprocessedList) {
			preprocessedJson = preprocessedDataAsDelete(projectSequencePk, Integer.parseInt("" + preData.get("id")),
					option);
			if (!"success".equals(preprocessedJson.get("result"))) {
				resultJson.put("type", preprocessedJson.get("type"));
				resultJson.put("title", preprocessedJson.get("title"));
				resultJson.put("data", preprocessedJson.get("data"));
				throw new RuntimeException(resultJson.toString());
			}
		}

		String listUrl = moduleIp + ":" + modulePort + moduleMethod + "/ml/originalData/" + originalDataSequencePk;

		// 원본데이터 삭제 API
		if (!"NoAPI".equals(option)) {
			httpJson = httpService.httpServiceDELETE(listUrl);
		} else {
			httpJson.put("type", "200");
		}

		if ("200".equals(httpJson.get("type"))) {
			// 삭제 성공
			OriginalData originalData = new OriginalData();
			originalData.setOriginalDataSequencePk(originalDataSequencePk);
			originalData.setDeleteFlag(true);
			projectRestMapper.deleteOriginalData(originalData);

			resultJson.put("result", "success");
			resultJson.put("type", "2000");

		} else {
			JSONObject json = new JSONObject().fromObject(httpJson.get("data"));
			// 이미 삭제처리되었을 경우
			if ("4004".equals(json.get("type")) && "File Not Found".equals(json.get("title"))) {
				// 삭제 성공
				OriginalData originalData = new OriginalData();
				originalData.setOriginalDataSequencePk(originalDataSequencePk);
				originalData.setDeleteFlag(true);
				projectRestMapper.deleteOriginalData(originalData);

				resultJson.put("result", "success");
				resultJson.put("type", "2000");
			} else {
				resultJson.put("detail", httpJson);
			}
		}
		return resultJson;
	}

	/**
	 * 전처리 처리방식 목록 가져오기
	 * 
	 * @param projectSequencePk
	 * @return
	 * @throws Exception
	 */
	public JSONObject preprocessFunctionList() throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONArray jsonArr = new JSONArray();

		List<Map<String, Object>> list = projectRestMapper.preprocessFunctionList();
		for (Map<String, Object> map : list) {
			if (MakeUtil.isNotNullAndEmpty(map)) {
				// Object createDate = String.valueOf(map.get("CREATE_DATETIME"));
				// DateFormat format = new SimpleDateFormat("yyyy/MM/dd");
				// Date date = format.parse((String) createDate);
				// String datestr = format.format(date);
				// map.put("create_date",datestr);
				jsonArr.add(MakeUtil.nvlJson(new JSONObject().fromObject(map)));
			}
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		resultJson.put("preprocessFunctionList", jsonArr);
		return resultJson;
	}

	/**
	 * 전처리 처리방식 가져오기
	 * 
	 * @param preprocessFunctionSequencePk
	 * @return
	 * @throws Exception
	 */
	public JSONObject preprocessFunction(Integer preprocessFunctionSequencePk) throws Exception {
		JSONObject resultJson = new JSONObject();

		Map<String, Object> detail = projectRestMapper.preprocessFunction(preprocessFunctionSequencePk);
		if (MakeUtil.isNotNullAndEmpty(detail)) {
			resultJson.put("preprocessFunction", MakeUtil.nvlJson(new JSONObject().fromObject(detail)));
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}

	/**
	 * 전처리 생성
	 * 
	 * @param projectSequencePk
	 * @param requestTtest
	 * @return
	 * @throws Exception
	 */
	public JSONObject preprocessedDataAsPost(Integer projectSequencePk, Map<String, Object> params) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = null;
		JSONObject param = null;
		JSONObject preprocessedDataJson = null;

		// 인스턴스 내부IP 가져오기
		StringBuilder listUrl = new StringBuilder(moduleIp + ":" + modulePort + moduleMethod + "/ml/preprocessedData");

		param = new JSONObject().fromObject(params);
		boolean beginner = (boolean) param.get("beginner");
		if (beginner) {
			listUrl.append("?user_type=beginner");
		}

		param.remove("beginner");
		httpJson = httpService.httpServicePOST(listUrl.toString(), param.toString());
		if ("202".equals(httpJson.get("type"))) {
			// 생성 성공

			preprocessedDataJson = new JSONObject().fromObject(httpJson.get("data"));
			PreprocessedData pData = new PreprocessedData();
			pData.setPreprocessedDataSequencePk(Integer.parseInt("" + preprocessedDataJson.get("id")));
			pData.setCommand("" + preprocessedDataJson.get("command"));
			pData.setName("P_" + preprocessedDataJson.get("name"));
			pData.setCreateDatetime("" + preprocessedDataJson.get("create_date"));
			pData.setProgressState("" + preprocessedDataJson.get("progress_state"));
			pData.setProgressStartDatetime("" + preprocessedDataJson.get("progress_start_date"));
			pData.setProjectSequenceFk1(projectSequencePk);
			pData.setOriginalDataSequenceFk2(Integer.parseInt("" + preprocessedDataJson.get("ml_original_data_id")));

			resultJson.put("result", "success");
			resultJson.put("type", "2000");

		} else {
			resultJson.put("detail", httpJson);
		}

		return resultJson;
	}

	/**
	 * 전처리 데이터 목록 가져오기
	 * 
	 * @param originalDataSequencePk
	 * @return
	 * @throws Exception
	 */
	public JSONObject preprocessedDataList(Integer originalDataSequencePk) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONArray jsonArr = new JSONArray();

		List<Map<String, Object>> list = projectRestMapper.preprocessedDataList(originalDataSequencePk);
		for (Map<String, Object> map : list) {
			if (MakeUtil.isNotNullAndEmpty(map)) {
				StringUtil.setMapDateFormat(map, "create_date", "yyyy/MM/dd");
				jsonArr.add(MakeUtil.nvlJson(new JSONObject().fromObject(map)));
			}
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		resultJson.put("preprocessedDataList", jsonArr);
		return resultJson;
	}

	/**
	 * 전처리 데이터 개별 조회
	 * 
	 * @param originalDataSequencePk
	 * @param preprocessedDataSequencePk
	 * @return
	 * @throws Exception
	 */
	public JSONObject preprocessedData(Integer originalDataSequencePk, Integer preprocessedDataSequencePk)
			throws Exception {
		JSONObject resultJson = new JSONObject();

		Map<String, Object> detail = projectRestMapper.preprocessedData(preprocessedDataSequencePk);
		MakeUtil.log("#####" + detail);
		if (MakeUtil.isNotNullAndEmpty(detail)) {
			StringUtil.setMapDateFormat(detail, "create_date", "yyyy/MM/dd");
			resultJson.put("preprocessedData", detail);
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}

	/**
	 * 전처리 삭제
	 * 
	 * @param projectSequencePk
	 * @param preprocessedDataSequencePk
	 * @return
	 * @throws Exception
	 */
	public JSONObject preprocessedDataAsDelete(Integer projectSequencePk, Integer preprocessedDataSequencePk,
			String option) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();

		// 모델 삭제
		Model model = new Model();
		model.setProjectSequenceFk4(projectSequencePk);
		model.setPreprocessedDataSequenceFk2(preprocessedDataSequencePk);
		List<Map<String, Object>> modelList = projectRestMapper.modelsList(model, null, null, 'M');
		JSONObject modelJson = null;
		for (Map<String, Object> m : modelList) {
			modelJson = modelsAsDelete(projectSequencePk, Integer.parseInt("" + m.get("id")), option);
			if (!"success".equals(modelJson.get("result"))) {
				resultJson.put("type", modelJson.get("type"));
				resultJson.put("title", modelJson.get("title"));
				resultJson.put("data", modelJson.get("data"));
				throw new RuntimeException(resultJson.toString());
			}
		}

		// PROGRESS_STATE가 success가 아니고 option이 NoAPI가 아니면 DB에서 삭제
		Map<String, Object> detail = projectRestMapper.preprocessedData(preprocessedDataSequencePk);
		if ("success".equals(detail.get("progress_state")) && !"NoAPI".equals(option)) {
			String listUrl = moduleIp + ":" + modulePort + moduleMethod + "/ml/preprocessedData/"
					+ preprocessedDataSequencePk;

			// 천처리 삭제 API
			httpJson = httpService.httpServiceDELETE(listUrl);

		} else {
			httpJson.put("type", "200");
		}

		if ("200".equals(httpJson.get("type"))) {
			resultJson.put("result", "success");
			resultJson.put("type", "2000");

		} else {
			JSONObject json = new JSONObject().fromObject(httpJson.get("data"));
			// 이미 삭제처리되었을 경우
			if ("4004".equals(json.get("type")) && "File Not Found".equals(json.get("title"))) {
				resultJson.put("result", "success");
				resultJson.put("type", "2000");
			} else {
				resultJson.put("detail", httpJson);
			}
		}

		return resultJson;
	}

	/**
	 * 모델 생성
	 * 
	 * @param projectSequencePk
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public JSONObject modelsAsPost(Integer projectSequencePk, Map<String, Object> params, String currentId)
			throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = null;
		JSONObject param = null;
		JSONObject modelJson = null;

		String listUrl = moduleIp + ":" + modulePort + moduleMethod + "/ml/model";

		param = new JSONObject().fromObject(params);
		httpJson = httpService.httpServicePOST(listUrl, param.toString());
		if ("202".equals(httpJson.get("type"))) {
			// 생성 성공
			modelJson = new JSONObject().fromObject(httpJson.get("data"));
			Model model = new Model();
			model.setModelSequencePk(Integer.parseInt("" + modelJson.get("id")));
			model.setCommand("" + modelJson.get("command"));
			model.setName("M_" + modelJson.get("id"));
			model.setCreateDatetime("" + modelJson.get("create_date"));
			model.setProgressState("" + modelJson.get("progress_state"));
			model.setProgressStartDatetime("" + modelJson.get("progress_start_date"));
			model.setOriginalDataSequenceFk1(Integer.parseInt("" + modelJson.get("ml_original_data_id")));
			model.setPreprocessedDataSequenceFk2(Integer.parseInt("" + modelJson.get("ml_preprocessed_data_id")));
			model.setProjectSequenceFk4(projectSequencePk);

			listUrl = listUrl + "/" + model.getModelSequencePk();
			// 비동기 조회
			asyncService.models(listUrl, model.getModelSequencePk());

			resultJson.put("result", "success");
			resultJson.put("type", "2000");

		} else {
			resultJson.put("detail", httpJson);
		}

		return resultJson;
	}

	/**
	 * 모델 목록 조회
	 * 
	 * @param projectSequencePk
	 * @return
	 * @throws Exception
	 */
	public JSONObject modelsList(Model model, String userAuth, String currentId, Character type) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONArray jsonArr = new JSONArray();

		List<Map<String, Object>> list = projectRestMapper.modelsList(model, userAuth, currentId, type);
		for (Map<String, Object> map : list) {
			if (MakeUtil.isNotNullAndEmpty(map)) {
				if (MakeUtil.isNotNullAndEmpty(map.get("progress_end_date"))) {
					String startDate = "" + map.get("progress_start_date");
					String endDate = "" + map.get("progress_end_date");
					// startDate = startDate.substring(0, 19).replaceAll("T", " ");
					// endDate = endDate.substring(0, 19).replaceAll("T", " ");
					map.put("diffDateTime", MakeUtil.diffOfDateAll(startDate, endDate));
				}
				StringUtil.setMapDateFormat(map, "create_date", "yyyy/MM/dd");
				jsonArr.add(MakeUtil.nvlJson(new JSONObject().fromObject(map)));
			}
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		resultJson.put("modelsList", jsonArr);
		return resultJson;
	}

	/**
	 * 모델 개별 조회
	 * 
	 * @param modelSequencePk
	 * @return
	 * @throws Exception
	 */
	public JSONObject model(Integer projectSequencePk, Integer modelSequencePk) throws Exception {
		JSONObject resultJson = new JSONObject();

		Map<String, Object> detail = projectRestMapper.model(projectSequencePk, modelSequencePk);
		if (MakeUtil.isNotNullAndEmpty(detail.get("progress_end_date"))) {
			String startDate = "" + detail.get("progress_start_date");
			String endDate = "" + detail.get("progress_end_date");
			// startDate = startDate.substring(0, 19).replaceAll("T", " ");
			// endDate = endDate.substring(0, 19).replaceAll("T", " ");
			detail.put("diffDateTime", MakeUtil.diffOfDateAll(startDate, endDate));
		}

		if (MakeUtil.isNotNullAndEmpty(detail)) {
			StringUtil.setMapDateFormat(detail, "create_date", "yyyy/MM/dd");
			resultJson.put("model", MakeUtil.nvlJson(new JSONObject().fromObject(detail)));
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}

	/**
	 * 모델 테스트를 위한 학습 스코어 가져오기
	 * 
	 * @param modelSequencePk
	 * @return
	 * @throws Exception
	 */
	public JSONObject getValidationSummary(int modelSequencePk) throws Exception {
		JSONObject resultJson = new JSONObject();

		Map<String, Object> score = projectRestMapper.getValidationSummary(modelSequencePk);
		if (MakeUtil.isNotNullAndEmpty(score)) {
			resultJson.put("score", score);
		}

		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}

	/**
	 * 모델 삭제
	 * 
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @return
	 * @throws Exception
	 */
	public JSONObject modelsAsDelete(Integer projectSequencePk, Integer modelSequencePk, String option)
			throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();

		// PROGRESS_STATE가 success가 아니면 DB에서 삭제
		Map<String, Object> detail = projectRestMapper.model(projectSequencePk, modelSequencePk);
		if ("success".equals(detail.get("progress_state")) && !"NoAPI".equals(option)) {
			// 인스턴스 내부IP 가져오기
			String listUrl = moduleIp + ":" + modulePort + moduleMethod + "/ml/model/" + modelSequencePk;

			// 모델 삭제 API
			httpJson = httpService.httpServiceDELETE(listUrl);

		} else {
			httpJson.put("type", "200");
		}

		if ("200".equals(httpJson.get("type"))) {
			resultJson.put("result", "success");
			resultJson.put("type", "2000");

		} else {
			JSONObject json = new JSONObject().fromObject(httpJson.get("data"));
			// 이미 삭제처리되었을 경우
			if ("4004".equals(json.get("type")) && "File Not Found".equals(json.get("title"))) {
				resultJson.put("result", "success");
				resultJson.put("type", "2000");
			} else {
				resultJson.put("detail", httpJson);
			}
		}

		return resultJson;
	}

	/**
	 * 모델 로드, 언로드, 학습 중지를 포함한 모델 수정
	 * 
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public JSONObject modelsAsPatch(Integer projectSequencePk, Integer modelSequencePk, Map<String, Object> params)
			throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();
		JSONObject param = null;
		String listUrl = null;

		// PROGRESS_STATE가 success가 아니면 DB에서 삭제
		Map<String, Object> detail = projectRestMapper.model(projectSequencePk, modelSequencePk);
		if ("ongoing".equals(detail.get("progress_state")) || "standby".equals(detail.get("progress_state"))) {
			listUrl = moduleIp + ":" + modulePort + moduleMethod + "/ml/model/" + modelSequencePk;
			param = new JSONObject().fromObject(params);
			httpJson = httpService.httpServicePATCH(listUrl, param.toString());

		} else {
			httpJson.put("type", "200");
		}

		if ("200".equals(httpJson.get("type")) || "202".equals(httpJson.get("type"))) {
			if ("RESTART".equals(params.get("mode"))) {
				// 비동기 조회
				asyncService.models(listUrl, modelSequencePk);
			}

			resultJson.put("result", "success");
			resultJson.put("type", "2000");

		} else {
			resultJson.put("detail", httpJson);
		}

		return resultJson;
	}

	/**
	 * 모델 테스트
	 * 
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @param params
	 * @return
	 * @throws Exception
	 */
	public JSONObject modelsTestAsPatch(Integer projectSequencePk, Integer modelSequencePk, Map<String, Object> params)
			throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject httpJson = new JSONObject();
		JSONObject param = null;
		String listUrl = null;

		// PROGRESS_STATE가 success가 아니면 DB에서 삭제
		listUrl = moduleIp + ":" + modulePort + moduleMethod + "/ml/model/" + modelSequencePk;

		String path = String.valueOf(params.get("test_data_path"));
		// root 경로를 삭제해주고 순수경로를 구함 root를 제외한 경로
		path = path.replaceFirst(filePath, "");
		path = path.replaceAll("\\\\", "/");
		path = path.replaceFirst(fileTestPath, "");

		// 서버경로를 입혀준다
		path = path.contains(filePath) ? path : filePath + "/" + path;

		params.put("test_data_path", path);
		param = new JSONObject().fromObject(params);
		httpJson = httpService.httpServicePATCH(listUrl, param.toString());

		if ("200".equals(httpJson.get("type")) || "202".equals(httpJson.get("type"))) {
			resultJson.put("result", "success");
			resultJson.put("type", "2000");
			resultJson.put("data", httpJson.get("data"));
		} else {
			resultJson.put("detail", httpJson);
		}

		return resultJson;
	}

	/**
	 * 로컬파일 조회
	 * 
	 * @param selectedInstance
	 * @return
	 * @throws Exception
	 */
	public JSONObject instancesLocalFiles() throws Exception {
		JSONObject resultJson = new JSONObject();
		List<String> fileList = new ArrayList<String>();

		// 파일목록 가져오기
		String fpath = "true".equalsIgnoreCase(isTest) ? fileTestPath : filePath;
		File path = new File(fpath);
		File[] list = path.listFiles();
		for (int i = 0; i < list.length; i++) {
			fileList.add(list[i].getName());
		}

		resultJson = new JSONObject();
		resultJson.put("localFiles", fileList);
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}

	/**
	 * 로컬파일 샘플 조회
	 * 
	 * @param selectedInstance
	 * @param localFile
	 * @return
	 * @throws Exception
	 */
	public JSONObject instancesLocalFileSample(String localFile) throws Exception {
		JSONObject resultJson = new JSONObject();
		JSONObject localFileJson = null;

		String listUrl = moduleIp + ":" + modulePort + moduleMethod + "/localFile?path=" + localFile
				+ "&&command=get_sample";
		localFileJson = httpService.httpServiceGET(listUrl);

		resultJson = new JSONObject();
		resultJson.put("localFile", localFileJson.get("data"));
		resultJson.put("result", "success");
		resultJson.put("type", "2000");
		return resultJson;
	}

}
