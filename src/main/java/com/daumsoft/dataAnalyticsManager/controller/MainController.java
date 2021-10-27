package com.daumsoft.dataAnalyticsManager.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import com.daumsoft.dataAnalyticsManager.common.utils.MakeUtil;

@Controller
public class MainController {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 로그인 페이지
	 * 
	 * @param model
	 * @param error
	 * @return
	 */
	@GetMapping("/login")
	public String login(Model model, @RequestParam(value = "error", required = false) String error,
			HttpServletRequest request, HttpSession session) {
		session = request.getSession();
		session.invalidate();
		if (MakeUtil.isNotNullAndEmpty(error))
			model.addAttribute("error", error);
		return "login";
	}

	/**
	 * 통합모듈에서 로그인 후 code, state값 받음(interceptor에서 처리) 로그인 후 첫화면
	 * 
	 * @param code
	 * @param state
	 * @param response
	 * @param request
	 */
	@RequestMapping("/")
	public RedirectView rootPath(HttpServletRequest request, HttpSession session) {
		logger.info("스프링 버전: " + org.springframework.core.SpringVersion.getVersion());
		logger.info("userId = {}", session.getAttribute("userId"));
		return new RedirectView("dashBoard");
	}

	/**
	 * 로그아웃
	 * 
	 * @param request
	 * @param response
	 * @param session
	 * @return
	 */
	@GetMapping("/logout")
	public RedirectView logout(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		String userId = session.getAttribute("userId") + "";
		logger.info(userId + ": Logout!!!");
		return new RedirectView("login");
	}

	/**
	 * 알고리즘 조회
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping("/dashBoard")
	public String dashBoard() {
		return "user/dashBoard";
	}

	/**
	 * 알고리즘 조회
	 * 
	 * @param request
	 * @return
	 */
	@GetMapping("/algorithmManage")
	public String algorithm() {
		return "user/algorithmManage";
	}

	/**
	 * 데이터 관리
	 * 
	 * @return
	 */
	@GetMapping("/dataManage")
	public String data() {
		return "user/dataManage";
	}

	/**
	 * 프로젝트 관리 , 머신러닝 관리
	 * 
	 * @return
	 */
	@GetMapping("/projectManage")
	public String project() {
		return "user/projectManage";
	}

	/**
	 * 딥러닝 관리
	 * 
	 * @return
	 */
	@GetMapping("/deepLearningManage")
	public String deepLearning() {
		return "user/deepLearningManage";
	}

	/**
	 * 머신러닝 프로젝트 상세화면
	 * 
	 * @param projectSequencePk
	 * @return
	 */
	@PostMapping("/projectDetail")
	public String projectDetail(@RequestParam(value = "projectSequencePk") String projectSequencePk, Model model) {
		model.addAttribute("projectSequencePk", projectSequencePk);
		return "user/projectDetail";
	}

	/**
	 * 딥러닝 프로젝트 상세화면
	 * 
	 * @param projectSequencePk
	 * @return
	 */
	@PostMapping("/deepLearningDetail")
	public String deepLearningDetail(@RequestParam(value = "projectSequencePk") String projectSequencePk, Model model) {
		model.addAttribute("projectSequencePk", projectSequencePk);
		return "user/deepLearningDetail";
	}

	/**
	 * 서비스 관리
	 * 
	 * @return
	 */
	@GetMapping("/serviceManage")
	public String service() {
		return "user/serviceManage";
	}

	/**
	 * 사용자 가이드
	 * 
	 * @return
	 */
	@GetMapping("/userGuide")
	public String userGuide() {
		return "user/userGuide";
	}

	/**
	 * 사용자 관리
	 * 
	 * @return
	 */
	@GetMapping("/userManage")
	public String user() {
		return "user/userManage";
	}

	/**
	 * 도메인 관리
	 * 
	 * @return
	 */
	@GetMapping("/domainManage")
	public String domain() {
		return "user/domainManage";
	}
}
