package com.daumsoft.dataAnalyticsManager.restFullApi.controller;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.daumsoft.dataAnalyticsManager.common.utils.MakeUtil;
import com.daumsoft.dataAnalyticsManager.restFullApi.domain.Project;
import com.daumsoft.dataAnalyticsManager.restFullApi.service.ProjectRestService;

import net.sf.json.JSONObject;

/**
 * 웹서비스에서는 머신러닝 관리 매뉴에서 주관 프로젝트 관리 >> 머신러닝 관리 로 이름 변경됨
 * 
 * @author Daumsoft
 */
@RestController
public class ProjectRestController {

	Logger logger = LoggerFactory.getLogger(ProjectRestController.class);

	@Autowired
	private ProjectRestService projectRestService;

	@Value("${filePath}")
	private String filePath;

	@Value("${fileTestPath}")
	private String fileTestPath;

	@Value("${isTest}")
	private String isTest;

	/**
	 * 프로젝트 리스트 조회 머신러닝 관리, 딥러닝 관리 에서 같이사용 type-M 머신러닝, D 딥러닝
	 * 
	 * @return
	 */
	@GetMapping(value = "/projects")
	public ResponseEntity<JSONObject> projects(HttpSession session, @RequestParam int page,
			@RequestParam(required = false) String option, @RequestParam(required = false) String value,
			@RequestParam(required = false) String type) {
		JSONObject result = new JSONObject();
		try {
			String userAuth = session.getAttribute("userAuth") + "";
			String currentId = session.getAttribute("userId") + "";
			if ("admin".equals(userAuth))
				currentId = "";

			result = projectRestService.projects(currentId, page, option, value, type);
			return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "projects");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 프로젝트 이름 조회 머신러닝 관리, 딥러닝 관리 에서 같이사용
	 * 
	 * @param session
	 * @return
	 */
	@GetMapping("/projectsName")
	public ResponseEntity<JSONObject> getProjects(HttpSession session) {
		JSONObject result = new JSONObject();
		try {
			String userAuth = session.getAttribute("userAuth") + "";
			String currentId = session.getAttribute("userId") + "";
			result = projectRestService.getProjects(currentId, userAuth);
			return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
		} catch (Exception e) {
			result.put("type", "5000");
			MakeUtil.printErrorLogger(e, "getProjects");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 프로젝트 개별 조회 머신러닝 관리, 딥러닝 관리 에서 같이사용
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/projects/{projectSequencePk}")
	public ResponseEntity<JSONObject> project(@PathVariable Integer projectSequencePk) {
		JSONObject result = new JSONObject();
		try {
			if (MakeUtil.isNotNullAndEmpty(projectSequencePk)) {
				result = projectRestService.project(projectSequencePk);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
			} else {
				logger.error("BAD REQUEST TO GET ONE PROJECT!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "project");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}

	/**
	 * 프로젝트 등록 머신러닝 관리, 딥러닝 관리 에서 같이사용 type-M 머신러닝, D 딥러닝
	 * 
	 * @param project
	 * @return
	 */
	@RequestMapping(value = "/projects/insert", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView insertProjects(HttpServletRequest request, HttpSession session) {
		String name = request.getParameter("name");
		String description = request.getParameter("description");
		String type = request.getParameter("type");

		ModelAndView mav = new ModelAndView("jsonView");
		JSONObject result = new JSONObject();
		try {
			Project projectVo = new Project();
			projectVo.setName(name);
			projectVo.setDescription(description);
			projectVo.setType(type);

			if (MakeUtil.isNotNullAndEmpty(projectVo)) {
				String userId = session.getAttribute("userId") + "";
				projectVo.setUserId(userId);
				result = projectRestService.projectsAsPost(projectVo);
			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO REGISTER PROJECT!!!");
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "projectsAsPost");
		}
		mav.addObject("result", result);
		return mav;
	}

	/**
	 * 프로젝트 수정 머신러닝 관리, 딥러닝 관리 에서 같이사용
	 * 
	 * @param project
	 * @return
	 */
	@RequestMapping(value = "/projects/update", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView updateProjects(HttpServletRequest request, HttpSession session) {
		String name = request.getParameter("name");
		String description = request.getParameter("description");
		String type = request.getParameter("type");
		String projectSequencePk = request.getParameter("id");
		ModelAndView mav = new ModelAndView("jsonView");
		JSONObject result = new JSONObject();
		try {
			Project projectVo = new Project();
			projectVo.setName(name);
			projectVo.setDescription(description);
			projectVo.setType(type);
			projectVo.setId(Integer.parseInt(projectSequencePk));

			if (MakeUtil.isNotNullAndEmpty(projectSequencePk)) {
				String userId = session.getAttribute("userId") + "";
				projectVo.setUserId(userId);
				result = projectRestService.projectsAsPatch(projectVo);
			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO MODIFY PROJECT!!!");
			}
		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "projectsAsPatch");
		}
		mav.addObject("result", result);
		return mav;
	}

	/**
	 * 프로젝트 삭제 머신러닝 관리, 딥러닝 관리 에서 같이사용
	 * 
	 * @param projectSequencePk
	 * @return
	 */
	@DeleteMapping(value = "/projects/{projectSequencePk}")
	public ResponseEntity<JSONObject> projectAsDelete(@PathVariable Integer projectSequencePk) {
		JSONObject result = new JSONObject();
		try {
			if (MakeUtil.isNotNullAndEmpty(projectSequencePk)) {
				result = projectRestService.projectAsDelete(projectSequencePk, null);
				return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
			} else {
				result.put("type", "4101");
				result.put("detail", "MANDATORY PARAMETER MISSING");
				logger.error("BAD REQUEST TO DELETE PROJECTS!!!");
				return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
			}

		} catch (Exception e) {
			result.put("type", "5000");
			result.put("detail", e.toString());
			MakeUtil.printErrorLogger(e, "projectAsDelete");
			return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
		}
	}
}
