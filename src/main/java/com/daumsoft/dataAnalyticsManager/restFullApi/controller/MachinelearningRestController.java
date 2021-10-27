package com.daumsoft.dataAnalyticsManager.restFullApi.controller;

import java.util.ArrayList;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.daumsoft.dataAnalyticsManager.common.utils.FileUtil;
import com.daumsoft.dataAnalyticsManager.common.utils.MakeUtil;
import com.daumsoft.dataAnalyticsManager.restFullApi.domain.Model;
import com.daumsoft.dataAnalyticsManager.restFullApi.domain.OriginalData;
import com.daumsoft.dataAnalyticsManager.restFullApi.mapper.ProjectRestMapper;
import com.daumsoft.dataAnalyticsManager.restFullApi.service.ProjectRestService;

import net.sf.json.JSONObject;

/**
 * 웹서비스에서는 머신러닝 관리 매뉴에서 주관
 * 프로젝트 관리 >> 머신러닝 관리 로 이름 변경됨
 * 
 * @author Daumsoft
 */
@RestController
public class MachinelearningRestController {

	Logger logger = LoggerFactory.getLogger(MachinelearningRestController.class);

	@Autowired
	private ProjectRestService projectRestService;

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

	@Autowired
	private ProjectRestMapper projectRestMapper;

	/**
	 * 머신러닝 원본 데이터 조회
	 * 
	 * @param projectSequencePk
	 * @return
	 */
	@GetMapping(value = "/projects/{projectSequencePk}/originalData")
	public ResponseEntity<JSONObject> originalDataList(@PathVariable Integer projectSequencePk) {
		JSONObject result = new JSONObject();
		try {
			if (MakeUtil.isNotNullAndEmpty(projectSequencePk)) {
				result = projectRestService.originalDataList(projectSequencePk);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);

			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO GET ORIGINAL DATAS!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "originalDataList");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 원본 데이터 개별 조회
	 * 
	 * @param projectSequencePk
	 * @param originalDataSequencePk
	 * @return
	 */
	@GetMapping(value = "/projects/{projectSequencePk}/originalData/{originalDataSequencePk}")
	public ResponseEntity<JSONObject> originalData(@PathVariable Integer projectSequencePk,
			@PathVariable Integer originalDataSequencePk) {
		JSONObject result = new JSONObject();
		try {
			if (MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(originalDataSequencePk)) {

				result = projectRestService.originalData(projectSequencePk, originalDataSequencePk);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO GET ONE ORIGINAL DATA!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "originalData");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 원본 데이터 생성
	 * 
	 * @param projectSequencePk
	 * @param originalData
	 * @return
	 */
	@PostMapping(value = "/projects/{projectSequencePk}/originalData")
	public ResponseEntity<JSONObject> originalDataAsPost(@PathVariable Integer projectSequencePk,
			@RequestBody OriginalData originalData) {
		JSONObject result = new JSONObject();

		try {
			if (MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(originalData)) {

				String fileName = originalData.getFilename();

				String fPath = originalData.getFilepath();
				// root 경로를 삭제해주고 순수경로를 구함 root를 제외한 경로
				fPath = fPath.replaceFirst(filePath, "");
				fPath = fPath.replaceAll("\\\\", "/");
				fPath = fPath.replaceFirst(fileTestPath, "");

				// 서버경로를 입혀준다
				fPath = fPath.contains(filePath) ? fPath : filePath + "/" + fPath;
				if (fPath.contains("/daSample/")) {
					String[] temp = fPath.split("/daSample/");
					fPath = sampleFilePath + temp[1];
				}

				originalData.setFilepath(fPath);

				if (fileName.contains(".")) {
					String[] toks = fileName.split("\\.");
					if (toks.length > 0)
						originalData.setName(toks[0]);
				}
				originalData.setExtension(FileUtil.getFileExtLowerCase(fileName));

				result = projectRestService.originalDataAsPost(originalData);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO CREATE ORIGINAL DATA!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "originalDataAsPost");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 원본 데이터 전처리 테스트
	 * 
	 * @param projectSequencePk
	 * @param originalData
	 * @return
	 */
	@PatchMapping(value = "/projects/{projectSequencePk}/originalData/{originalDataSequencePk}")
	public ResponseEntity<JSONObject> originalDataAsPatch(@PathVariable Integer projectSequencePk,
			@PathVariable Integer originalDataSequencePk, @RequestBody Map<String, Object> requestTtest) {
		JSONObject result = new JSONObject();

		try {
			if (MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(originalDataSequencePk)
					&& MakeUtil.isNotNullAndEmpty(requestTtest)) {
				result = projectRestService.originalDataAsPatch(projectSequencePk, originalDataSequencePk,
						requestTtest);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO TEST PREPROCESSING OF ORIGINAL DATA!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "originalDataAsPatch");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 적용 가능한 전처리 필드 가져오기
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/projects/getPossiblePrepro", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView getPossiblePrepro(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		String projectSequencePk = request.getParameter("projectSequencePk");
		String originalDataSequencePk = request.getParameter("originalDataSequencePk");
		String fieldName = request.getParameter("fieldName");

		List<Map<String, String>> possibleList = new ArrayList<Map<String, String>>();
		List<Map<String, Object>> ppList = null;
		try {
			ppList = projectRestMapper.getPreprocesseFunctionList();
		} catch (Exception e1) {
			logger.error("FAILED TO GET PREPROCESS FUNCTION LIST!!!");
			mav.addObject("error", "ProjectRestController JAVA Exception 발생");
		}
		for (Map<String, Object> map : ppList) {
			String tParam = String.valueOf(map.get("valid_check"));
			String id = String.valueOf(map.get("id"));
			String name = String.valueOf(map.get("name"));
			String desc = String.valueOf(map.get("library_function_description"));
			tParam = tParam.replaceFirst("_changeMe", fieldName);
			JSONObject result = null;
			try {
				result = projectRestService.originalDataAsPatch(Integer.parseInt(projectSequencePk),
						Integer.parseInt(originalDataSequencePk), tParam);
				logger.info("Success To Add Preprocess Field List!!!");
			} catch (Exception e) {
				// 예외 났을경우 List에 추가 하지 않음
				logger.error("FAILED TO ADD PREPROCESS FIELD LIST!!!");
			}
			if (result != null && !"5000".equals(result.get("type")) && "success".equals(result.get("result"))) {
				Map<String, String> possibleMap = new HashMap<String, String>();
				possibleMap.put("id", id);
				possibleMap.put("name", name);
				possibleMap.put("desc", desc);
				possibleList.add(possibleMap);
			}
		}
		mav.addObject("possibleList", possibleList);
		return mav;
	}

	/**
	 * 머신러닝 원본 데이터 삭제
	 * 
	 * @param projectSequencePk
	 * @param originalDataSequencePk
	 * @return
	 */
	@DeleteMapping(value = "/projects/{projectSequencePk}/originalData/{originalDataSequencePk}")
	public ResponseEntity<JSONObject> originalDataAsDelete(@PathVariable Integer projectSequencePk,
			@PathVariable Integer originalDataSequencePk) {
		JSONObject result = new JSONObject();
		try {
			if (MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(originalDataSequencePk)) {
				result = projectRestService.originalDataAsDelete(projectSequencePk, originalDataSequencePk, null);
				logger.info("Success To Delete Original Data!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO DELETE ORIGINAL DATA!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "originalDataAsDelete");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 전처리 처리방식 목록 가져오기
	 * 
	 * @return
	 */
	@GetMapping(value = "/preprocessFunctions")
	public ResponseEntity<JSONObject> preprocessFunctionList() {
		JSONObject result = new JSONObject();
		try {
			result = projectRestService.preprocessFunctionList();
			return new ResponseEntity<JSONObject>(result, HttpStatus.OK);

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "preprocessFunctionList");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 전처리 처리방식 가져오기
	 * 
	 * @param preprocessFunctionSequencePk
	 * @return
	 */
	@GetMapping(value = "/preprocessFunctions/{preprocessFunctionSequencePk}")
	public ResponseEntity<JSONObject> preprocessFunction(@PathVariable Integer preprocessFunctionSequencePk) {
		JSONObject result = new JSONObject();
		try {
			if (MakeUtil.isNotNullAndEmpty(preprocessFunctionSequencePk)) {
				result = projectRestService.preprocessFunction(preprocessFunctionSequencePk);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);

			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO GET ONE PREPROCESS FUNCTION!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "preprocessFunction");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 전처리 데이터 생성
	 * 
	 * @param projectSequencePk
	 * @param originalDataSequencePk
	 * @param requestTtest
	 * @return
	 */
	@PostMapping(value = "/projects/{projectSequencePk}/preprocessedData")
	public ResponseEntity<JSONObject> preprocessedDataAsPost(@PathVariable Integer projectSequencePk,
			@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();

		try {
			if (MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(params)) {
				result = projectRestService.preprocessedDataAsPost(projectSequencePk, params);
				logger.info("Success To Create Propressed Data!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO CREATE PREPROCESSED DATA!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "preprocessedDataAsPost");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 전처리 데이터 목록 가져오기
	 * 
	 * @param projectSequencePk
	 * @return
	 */
	@GetMapping(value = "/originalData/{originalDataSequencePk}/preprocessedData")
	public ResponseEntity<JSONObject> preprocessedDataList(@PathVariable Integer originalDataSequencePk) {
		JSONObject result = new JSONObject();
		try {
			if (MakeUtil.isNotNullAndEmpty(originalDataSequencePk)) {
				result = projectRestService.preprocessedDataList(originalDataSequencePk);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);

			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO GET PREPROCESSED DATA LIST!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "preprocessedDataList");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 전처리 데이터 개별 조회
	 * 
	 * @param projectSequencePk
	 * @param originalDataSequencePk
	 * @return
	 */
	@GetMapping(value = "/originalData/{originalDataSequencePk}/preprocessedData/{preprocessedDataSequencePk}")
	public ResponseEntity<JSONObject> preprocessedData(@PathVariable Integer originalDataSequencePk,
			@PathVariable Integer preprocessedDataSequencePk) {
		JSONObject result = new JSONObject();
		try {
			if (MakeUtil.isNotNullAndEmpty(preprocessedDataSequencePk)
					&& MakeUtil.isNotNullAndEmpty(preprocessedDataSequencePk)) {

				result = projectRestService.preprocessedData(originalDataSequencePk, preprocessedDataSequencePk);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO GET ONE PREPROCESSED DATA!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "preprocessedData");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 전처리 데이터 삭제
	 * 
	 * @param projectSequencePk
	 * @param preprocessedDataSequencePk
	 * @return
	 */
	@DeleteMapping(value = "/projects/{projectSequencePk}/preprocessedData/{preprocessedDataSequencePk}")
	public ResponseEntity<JSONObject> preprocessedDataAsDelete(@PathVariable Integer projectSequencePk,
			@PathVariable Integer preprocessedDataSequencePk) {
		JSONObject result = new JSONObject();
		try {
			if (MakeUtil.isNotNullAndEmpty(projectSequencePk)
					&& MakeUtil.isNotNullAndEmpty(preprocessedDataSequencePk)) {
				result = projectRestService.preprocessedDataAsDelete(projectSequencePk, preprocessedDataSequencePk, null);
				logger.info("Success To Delete Preprocessed Data!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO DELETE PREPROCESSED DATA!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "originalDataAsDelete");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 모델 생성
	 * 
	 * @param projectSequencePk
	 * @param params
	 * @return
	 */
	@PostMapping(value = "/projects/{projectSequencePk}/models")
	public ResponseEntity<JSONObject> modelsAsPost(HttpSession session, @PathVariable Integer projectSequencePk,
			@RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();

		String userId = session.getAttribute("userId") + "";

		try {
			if (MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(params)) {
				result = projectRestService.modelsAsPost(projectSequencePk, params, userId);
				logger.info("Success To Create Model!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO CREATE MODEL!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "modelsAsPost");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 모델 목록 조회
	 * 
	 * @param projectSequencePk
	 * @return
	 */
	@GetMapping(value = "/projects/{projectSequencePk}/models")
	public ResponseEntity<JSONObject> modelsList(HttpSession session, @PathVariable Integer projectSequencePk,
			@RequestParam(value = "preprocessedDataSequencePk", required = false) Integer preprocessedDataSequencePk,
			@RequestParam(required = false) Character projectType) {
		JSONObject result = new JSONObject();
		try {
			if (MakeUtil.isNotNullAndEmpty(projectSequencePk)) {
				Model model = new Model();
				model.setProjectSequenceFk4(projectSequencePk);
				if (MakeUtil.isNotNullAndEmpty(preprocessedDataSequencePk))
					model.setPreprocessedDataSequenceFk2(preprocessedDataSequencePk);

				String userAuth = session.getAttribute("userAuth") + "";
				String currentId = session.getAttribute("userId") + "";
				result = projectRestService.modelsList(model, userAuth, currentId, projectType);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);

			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO GET MODEL LIST!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "modelsList");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 모델 개별 조회
	 * 
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @return
	 */
	@GetMapping(value = "/projects/{projectSequencePk}/models/{modelSequencePk}")
	public ResponseEntity<JSONObject> model(@PathVariable Integer projectSequencePk,
			@PathVariable Integer modelSequencePk) {
		JSONObject result = new JSONObject();
		try {
			if (MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(modelSequencePk)) {

				result = projectRestService.model(projectSequencePk, modelSequencePk);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO GET ONE MODEL!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "preprocessedData");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 모델 테스트를 위한 스코어 가져오기
	 * 
	 * @param modelSequencePk
	 * @return
	 */
	@GetMapping("/models/{modelSequencePk}")
	public ResponseEntity<JSONObject> getValidationSummary(@PathVariable int modelSequencePk) {
		JSONObject result = new JSONObject();
		try {
			if (MakeUtil.isNotNullAndEmpty(modelSequencePk)) {

				result = projectRestService.getValidationSummary(modelSequencePk);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO GET SCORE FOR MODEL TEST!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "FAILED TO GET VALIDATION SUMMARY");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 모델 삭제
	 * 
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @return
	 */
	@DeleteMapping(value = "/projects/{projectSequencePk}/models/{modelSequencePk}")
	public ResponseEntity<JSONObject> modelsAsDelete(@PathVariable Integer projectSequencePk,
			@PathVariable Integer modelSequencePk) {
		JSONObject result = new JSONObject();
		try {
			if (MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(modelSequencePk)) {
				result = projectRestService.modelsAsDelete(projectSequencePk, modelSequencePk, null);
				logger.info("Success To Delete Model!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO DELETE MODEL!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "modelsAsDelete");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 학습 중지, 모델재생성 을 포함한 모델 수정
	 * 
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @return
	 */
	@PatchMapping(value = "/projects/{projectSequencePk}/models/{modelSequencePk}")
	public ResponseEntity<JSONObject> modelsAsPatch(@PathVariable Integer projectSequencePk,
			@PathVariable Integer modelSequencePk, @RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		try {
			if (MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(modelSequencePk)) {
				result = projectRestService.modelsAsPatch(projectSequencePk, modelSequencePk, params);
				logger.info("Success To Modify Model Including Learning Stop and Model Recreation!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO MODIFY MODEL INCLUDING LEARNING STOP AND MODEL RECREATION!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "modelsAsPatch");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 모델 테스트
	 * 
	 * @param projectSequencePk
	 * @param modelSequencePk
	 * @param params
	 * @return
	 */
	@PatchMapping(value = "/projects/{projectSequencePk}/modelsTest/{modelSequencePk}")
	public ResponseEntity<JSONObject> modelsTestAsPatch(@PathVariable Integer projectSequencePk,
			@PathVariable Integer modelSequencePk, @RequestBody Map<String, Object> params) {
		JSONObject result = new JSONObject();
		try {
			if (MakeUtil.isNotNullAndEmpty(projectSequencePk) && MakeUtil.isNotNullAndEmpty(modelSequencePk)) {
				result = projectRestService.modelsTestAsPatch(projectSequencePk, modelSequencePk, params);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO TEST MODEL!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "modelsTestAsPatch");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 로컬파일 조회
	 * 
	 * @return
	 */
	@GetMapping(value = "/projects/localFiles")
	public ResponseEntity<JSONObject> instancesLocalFiles() {
		JSONObject result = new JSONObject();
		try {
			result = projectRestService.instancesLocalFiles();
			return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "instances");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 머신러닝 샘플 데이터 가져오기
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/machineLearning/getSampleFilePath", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView getSampleFilePath(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		String fpath = isTest.equals("true") ? sampleFileTestPath + "/ml" : sampleFilePath + "/ml";
		logger.info("fpath: " + fpath);
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
