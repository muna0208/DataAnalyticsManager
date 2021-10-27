package com.daumsoft.dataAnalyticsManager.restFullApi.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.daumsoft.dataAnalyticsManager.common.utils.FileUtil;
import com.daumsoft.dataAnalyticsManager.restFullApi.service.DeeplearningRestService;

import net.sf.json.JSONObject;

@RestController
public class DeeplearningRestController {

	Logger logger = LoggerFactory.getLogger(DeeplearningRestController.class);

	@Autowired
	private DeeplearningRestService deeplearningRestService;

	@Value("${filePath}")
	private String filePath;

	@Value("${fileTestPath}")
	private String fileTestPath;

	@Value("${isTest}")
	private String isTest;

	@Value("${sampleFilePath}")
	private String sampleFilePath;

	@Value("${sampleFileTestPath}")
	private String sampleFileTestPath;

	/**
	 * 딥러닝 원본데이터 생성
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/deepLearning/createOrgData", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView createOrgData(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		String targetPath = request.getParameter("targetPath");
		String projectId = request.getParameter("projectId");
		String userId = session.getAttribute("userId") + "";
		String userAuth = session.getAttribute("userAuth") + "";
		String path = null;

		// 경로뒤에 슬래쉬추가
		filePath = FileUtil.appendEndsPath(filePath);
		targetPath = FileUtil.appendEndsPath(targetPath);

		// 역슬래쉬는 슬래쉬로 모두 변경
		targetPath = targetPath.replaceAll("\\\\", "/");

		// 모듈과의 연결은 서버에서만 가능하여
		// 경로가 로컬인 경우 서버경로를 입혀준다
		if (targetPath.contains(fileTestPath)) {
			path = targetPath.replaceAll(fileTestPath, "");
			if ("admin".equals(userAuth)) {
				path = filePath + path;
			} else {
				path = filePath + "users/" + userId + path;
			}
		} else if (targetPath.contains(sampleFileTestPath)) {
			path = targetPath.replaceAll(sampleFileTestPath, "");
			path = sampleFilePath + path;
		} else {
			path = targetPath;
		}
		// 중복 슬래쉬 제거
		path = path.replaceAll("//", "/");

		try {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("path", path);
			param.put("id", Integer.parseInt(projectId));
			JSONObject result = deeplearningRestService.createOriginalData(param);
			mav.addObject("result", result);
			mav.addObject("requestPath", path);
		} catch (Exception e) {
			JSONObject result = new JSONObject();
			result.put("result", "fail");
			result.put("detail", "시스템에 문제가 발생하였습니다.");
			mav.addObject("result", result);
		}
		return mav;
	}

	/**
	 * 딥러닝 원본데이터 리스트 조회
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/deepLearning/getOrgDataList", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView getOrgDataList(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		String projectId = request.getParameter("projectId");
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("id", Integer.parseInt(projectId));
			List<Map<String, Object>> list = deeplearningRestService.getOriginalDataList(param);
			JSONObject result = new JSONObject();
			result.put("result", "success");
			result.put("detail", "리스트 조회 성공");
			mav.addObject("result", result);
			mav.addObject("data", list);
		} catch (Exception e) {
			JSONObject result = new JSONObject();
			result.put("result", "fail");
			result.put("detail", "시스템에 문제가 발생하였습니다.");
			mav.addObject("result", result);
		}
		return mav;
	}

	/**
	 * 딥러닝 원본데이터 개별 조회
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/deepLearning/getOrgData", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView getOrgData(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		String orgId = request.getParameter("orgId");
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("id", Integer.parseInt(orgId));
			Map<String, Object> list = deeplearningRestService.getOriginalData(param);
			JSONObject result = new JSONObject();
			result.put("result", "success");
			result.put("detail", "리스트 조회 성공");
			mav.addObject("result", result);
			mav.addObject("data", list);
		} catch (Exception e) {
			JSONObject result = new JSONObject();
			result.put("result", "fail");
			result.put("detail", "시스템에 문제가 발생하였습니다.");
			mav.addObject("result", result);
		}
		return mav;
	}

	/**
	 * 딥러닝 원본데이터 삭제
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/deepLearning/deleteOrgData", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView deleteOrgData(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		mav.addObject("result", HttpStatus.OK);
		String orgId = request.getParameter("orgId");
		try {
			JSONObject result = deeplearningRestService.deleteOriginalData(orgId);
			mav.addObject("result", result);
		} catch (Exception e) {
			JSONObject result = new JSONObject();
			result.put("result", "fail");
			result.put("detail", "시스템에 문제가 발생하였습니다.");
			mav.addObject("result", result);
		}
		return mav;
	}

	/**
	 * 딥러닝 Pre-Trained 모델 목록 가져오기
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/deepLearning/getPreTrainedModelList", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView getPreTrainedModelList(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		try {
			List<Map<String, Object>> resultList = deeplearningRestService.getPreTrainedModelList();
			mav.addObject("result", "succes");
			mav.addObject("resultList", resultList);
		} catch (Exception e) {
			JSONObject result = new JSONObject();
			result.put("result", "fail");
			result.put("detail", "시스템에 문제가 발생하였습니다.");
			result.put("exception", e);
			mav.addObject("result", result);
		}
		return mav;
	}

	/**
	 * 딥러닝 모델 생성
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/deepLearning/createModel", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView createModel(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		String requestParam = request.getParameter("requestParam");
		String userType = request.getParameter("userType");
		requestParam = requestParam.replaceAll("\\\\", "");
		try {
			JSONObject result = deeplearningRestService.createModel(requestParam, userType);
			mav.addObject("result", result);
			mav.addObject("requestParam", result);
		} catch (Exception e) {
			JSONObject result = new JSONObject();
			result.put("result", "fail");
			result.put("detail", "시스템에 문제가 발생하였습니다.");
			mav.addObject("result", result);
		}
		return mav;
	}

	/**
	 * 딥러닝 모델 체크
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/deepLearning/checkModelStatus", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView checkModelStatus(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		String modelId = request.getParameter("modelId");
		Map<String, Object> dbParam = new HashMap<String, Object>();
		dbParam.put("modelId", Integer.parseInt(modelId));
		try {
			Map<String, Object> status = deeplearningRestService.checkModelStatus(dbParam);
			mav.addObject("status", status);
		} catch (Exception e) {
			JSONObject result = new JSONObject();
			result.put("result", "fail");
			result.put("detail", "시스템에 문제가 발생하였습니다.");
			mav.addObject("result", result);
		}
		return mav;
	}

	/**
	 * 딥러닝 Pre-Trained Network 분석 팝업의 입력값(파라미터) 가져오기
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/deepLearning/getNeuralLayerParameterList", method = { RequestMethod.GET,
			RequestMethod.POST })
	public ModelAndView getNeuralLayerParameterList(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		try {
			List<Map<String, Object>> resultList = deeplearningRestService.getNeuralLayerParameterList();
			mav.addObject("result", "succes");
			mav.addObject("resultList", resultList);
		} catch (Exception e) {
			JSONObject result = new JSONObject();
			result.put("result", "fail");
			result.put("detail", "시스템에 문제가 발생하였습니다.");
			result.put("exception", e);
			mav.addObject("result", result);
		}
		return mav;
	}

	/**
	 * 딥러닝 파리미터 설정 팝업의 optimizer 가져오기
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/deepLearning/getNeuralLayerOptimizers", method = { RequestMethod.GET,
			RequestMethod.POST })
	public ModelAndView getNeuralLayerOptimizers(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		try {
			List<Map<String, Object>> resultList = deeplearningRestService.getNeuralLayerOptimizers();
			mav.addObject("result", "succes");
			mav.addObject("resultList", resultList);
		} catch (Exception e) {
			JSONObject result = new JSONObject();
			result.put("result", "fail");
			result.put("detail", "시스템에 문제가 발생하였습니다.");
			result.put("exception", e);
			mav.addObject("result", result);
		}
		return mav;
	}

	/**
	 * 딥러닝 모델 리스트 조회
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/deepLearning/getModelList", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView getModelList(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		String projectId = request.getParameter("projectId");
		String orgId = request.getParameter("orgId");
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("projectId", Integer.parseInt(projectId));
			param.put("orgId", Integer.parseInt(orgId));

			List<Map<String, Object>> list = deeplearningRestService.getModelList(param);
			JSONObject result = new JSONObject();
			result.put("result", "success");
			result.put("detail", "리스트 조회 성공");

			mav.addObject("result", result);
			mav.addObject("data", list);
		} catch (Exception e) {
			JSONObject result = new JSONObject();
			result.put("result", "fail");
			result.put("detail", "시스템에 문제가 발생하였습니다.");
			mav.addObject("result", result);
		}
		return mav;
	}

	/**
	 * 딥러닝 모델 개별 조회
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/deepLearning/getModel", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView getModel(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		String modelId = request.getParameter("modelId");
		try {
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("id", Integer.parseInt(modelId));

			Map<String, Object> list = deeplearningRestService.getModelData(param);
			JSONObject result = new JSONObject();
			result.put("result", "success");
			result.put("detail", "리스트 조회 성공");

			mav.addObject("result", result);
			mav.addObject("data", list);
		} catch (Exception e) {
			JSONObject result = new JSONObject();
			result.put("result", "fail");
			result.put("detail", "시스템에 문제가 발생하였습니다.");
			mav.addObject("result", result);
		}
		return mav;
	}

	/**
	 * 딥러닝 모델 삭제
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/deepLearning/deleteModel", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView deleteModel(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		mav.addObject("result", HttpStatus.OK);
		String modelId = request.getParameter("modelId");
		try {
			JSONObject result = deeplearningRestService.deleteModelData(modelId);
			mav.addObject("result", result);
		} catch (Exception e) {
			JSONObject result = new JSONObject();
			result.put("result", "fail");
			result.put("detail", "시스템에 문제가 발생하였습니다.");
			mav.addObject("result", result);
		}
		return mav;
	}

	/**
	 * 딥러닝 모델 수정 학습중지,모델재생성 등 기능
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/deepLearning/updateModel", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView updateModel(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		mav.addObject("result", HttpStatus.OK);
		return mav;
	}

	/**
	 * 딥러닝 로컬파일 조회
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/deepLearning/getLocalFiles", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView getLocalFiles(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		mav.addObject("result", HttpStatus.OK);
		return mav;
	}

	/**
	 * 딥러닝 모델 테스트
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/deepLearning/testModel", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView testModel(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		mav.addObject("result", HttpStatus.OK);
		String targetPath = request.getParameter("targetPath");
		String modelId = request.getParameter("modelId");

		/**
		 * 로컬디렉토리(로컬호스트 디렉토리 테스트환경) 에서 요청하여도 테스트에는 서버에 있는 디렉토리의 경로를 바라봐야 한다 로컬에서는 서버엔진에
		 * 요청할수 없기에 테스트 환경은 로컬과 서버의 일치가 있어야 한다
		 */

		// root 경로를 삭제해주고 순수경로를 구함 root를 제외한 경로
		targetPath = targetPath.replaceAll("\\\\", "/");
		targetPath = targetPath.replaceFirst(fileTestPath.replaceAll("\\\\", "/"), "");
		targetPath = targetPath.replaceFirst(filePath.replaceAll("\\\\", "/"), "");

		// 서버경로를 입혀준다
		targetPath = filePath + targetPath;

		try {
			JSONObject result = deeplearningRestService.testModelData(modelId, targetPath);
			mav.addObject("result", result);
		} catch (Exception e) {
			JSONObject result = new JSONObject();
			result.put("result", "fail");
			result.put("detail", "시스템에 문제가 발생하였습니다.");
			mav.addObject("result", result);
		}
		return mav;
	}

	/**
	 * 딥러닝 샘플 데이터 가져오기
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/deepLearning/getSampleFilePath", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView getSampleFilePath(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		String fpath = isTest.equals("true") ? sampleFileTestPath + "/dl" : sampleFilePath + "/dl";
		FileUtil.makeFolder(fpath);
		List<Map<String, Object>> fList = null;
		try {
			fList = FileUtil.getDirFileListObject(fpath);
			mav.addObject("result", "success");
		} catch (Exception e) {
			mav.addObject("result", "fail");
		}
		mav.addObject("fList", fList);
		return mav;
	}

}
