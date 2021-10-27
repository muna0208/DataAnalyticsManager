package com.daumsoft.dataAnalyticsManager.restFullApi.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.daumsoft.dataAnalyticsManager.common.utils.FileUtil;
import com.daumsoft.dataAnalyticsManager.common.utils.MakeUtil;
import com.daumsoft.dataAnalyticsManager.common.utils.StringUtil;
import com.daumsoft.dataAnalyticsManager.restFullApi.service.DataManageService;
 
import net.sf.json.JSONObject;

@RestController
public class DataManageRestController {
	Logger logger = LoggerFactory.getLogger(DataManageRestController.class);

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

	@Value("${userMaxSpace}")
	private String userMaxSpace;

	@Autowired
	private DataManageService dataManageService;

	/**
	 * 파일조회
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/dataManage/getFileList", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView getFileList(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		String userId = session.getAttribute("userId") + "";
		String userAuth = session.getAttribute("userAuth") + "";

		try {
			Map<String, Object> rootMap = dataManageService.makeFileList(userId, userAuth);
			mav.addObject("fileList", rootMap);
		} catch (Exception e) {
			MakeUtil.printErrorLogger(e, "FAILED TO GET FILES!!!");
		}

		return mav;
	}

	/**
	 * 폴더생성
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/dataManage/createDirectory", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView createDirectory(HttpServletRequest request, HttpSession session) {
		String targetFullPath = request.getParameter("targetFullPath");
		String fileName = request.getParameter("fileName");
		targetFullPath = FileUtil.appendEndsPath(targetFullPath) + fileName;

		ModelAndView mav = dataManageService.createDirectory(targetFullPath, fileName);
		return mav;
	}

	/**
	 * 파일/폴더 삭제 (내부디렉토리 및 내용 모두 삭제)
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/dataManage/deleteDirectory", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView deleteFileDirectory(HttpServletRequest request, HttpSession session) {
		String multiSelectPath = request.getParameter("multiSelectPath");
		ModelAndView mav = dataManageService.deleteFileDirectory(multiSelectPath);
		return mav;
	}

	/**
	 * 파일/폴더 이동
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/dataManage/moveDirectory", method = RequestMethod.POST)
	public ModelAndView moveFileDirectory(HttpServletRequest request, HttpSession session) {
		String oldPath = request.getParameter("oldPath");
		String newPath = request.getParameter("newPath");
		String userId = session.getAttribute("userId") + "";
		String userAuth = session.getAttribute("userAuth") + "";
		ModelAndView mav = dataManageService.moveFileDirectory(oldPath, newPath, userId, userAuth);
		return mav;
	}

	/**
	 * 파일/폴더 이름 수정
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/dataManage/renameFileFolder", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView renameFileFolder(HttpServletRequest request, HttpSession session) {
		String targetPath = request.getParameter("targetPath");
		String targetParentPath = request.getParameter("targetParentPath");
		String targetType = request.getParameter("targetType");
		String oldName = request.getParameter("oldName");
		String newName = request.getParameter("newName");

		targetParentPath = FileUtil.appendEndsPath(targetParentPath);
		targetPath = FileUtil.appendEndsPath(targetPath);

		ModelAndView mav = dataManageService.renameFileDirectory(targetPath, targetParentPath, targetType, oldName,
				newName);
		return mav;
	}

	/**
	 * 로컬 데이터 업로드
	 * 
	 * @param multipartFile
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/dataManage/upload", method = RequestMethod.POST)
	public String localDataUpload(@RequestParam("file") MultipartFile multipartFile, HttpServletRequest request) {
		String uploadFilePath = request.getParameter("uploadFilePath");
		String fpath = "true".equalsIgnoreCase(isTest) ? fileTestPath : filePath;
		String upPath = StringUtil.isNull(uploadFilePath) ? fpath : uploadFilePath;
		File targetFile = new File(FileUtil.appendEndsPath(upPath) + multipartFile.getOriginalFilename());

		try {
			dataManageService.localDataUpload(multipartFile, targetFile);
		} catch (IOException e) {
			FileUtils.deleteQuietly(targetFile);
			logger.error("FAILED TO UPLOAD FILE/FOLDER!!!");
			e.printStackTrace();
		}

		return "redirect:/form";
	}

	/**
	 * 샘플데이터 가져오기
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/dataManage/getSampleData", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView getSampleData(HttpServletRequest request, HttpSession session) {
		String fileName = request.getParameter("fileName");
		String fileFullPath = request.getParameter("fileFullPath");
		String sample = request.getParameter("sample");
		ModelAndView mav = new ModelAndView("jsonView");

		fileName = fileName.replaceAll("&amp;", "&");
		fileFullPath = fileFullPath.replaceAll("&amp;", "&");

		/**
		 * 로컬디렉토리(로컬호스트 디렉토리 테스트환경) 에서 요청하여도 테스트에는 서버에 있는 디렉토리의 경로를 바라봐야 한다 로컬에서는 서버엔진에
		 * 요청할수 없기에 테스트 환경은 로컬과 서버의 일치가 있어야 한다
		 */

		// root 경로를 삭제해주고 순수경로를 구함 root를 제외한 경로
		fileFullPath = fileFullPath.replaceAll("\\\\", "/");
		fileFullPath = fileFullPath.replaceFirst(fileTestPath.replaceAll("\\\\", "/"), "");
		fileFullPath = fileFullPath.replaceFirst(filePath.replaceAll("\\\\", "/"), "");

		try {
			mav = dataManageService.getSampleData(fileFullPath, sample, fileName);
		} catch (Exception e) {
			return mav.addObject("error", "DataManageRestController JAVA Exception 발생");
		}

		return mav;
	}

	/**
	 * 서버파일의 데이터 기본정보를 가져온다
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/dataManage/getDataInfo", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView getDataInfo(HttpServletRequest request, HttpSession session) {
		String fpath = "true".equalsIgnoreCase(isTest) ? fileTestPath : filePath;
		String fileName = request.getParameter("fileName");
		String fileFullPath = request.getParameter("fileFullPath");
		ModelAndView mav = new ModelAndView("jsonView");

		fileName = fileName.replaceAll("&amp;", "&");
		fileFullPath = fileFullPath.replaceAll("&amp;", "&");

		// root 경로를 삭제해주고 순수경로를 구함 root를 제외한 경로
		fileFullPath = fileFullPath.replaceAll("\\\\", "/");
		fileFullPath = fileFullPath.replaceFirst(fileTestPath.replaceAll("\\\\", "/"), "");
		fileFullPath = fileFullPath.replaceFirst(filePath.replaceAll("\\\\", "/"), "");

		String tPath = fpath + fileFullPath;
		List<Object> dataInfoList = dataManageService.getDataInfo(tPath, fileName);
		mav.addObject("dataInfo", dataInfoList);
		return mav;
	}

	/**
	 * 디렉토리를 조회하여 이미지가 들어있는지 확인한다.
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/dataManage/checkImageDirectory", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView checkImageDirectory(HttpServletRequest request, HttpSession session) {
		String directoryPath = request.getParameter("directoryPath");
		directoryPath = dataManageService.getDirectoryPath(directoryPath);

		ModelAndView mav = new ModelAndView("jsonView");
		Map<String, Integer> dataLableMap = new HashMap<>();
		List<Map<String, Object>> dataInfoList = new ArrayList<>();

		try {
			dataLableMap = dataManageService.getDataLableMap(directoryPath);
			dataInfoList = dataManageService.getDataInfoList(directoryPath);
		} catch (Exception e) {
			return mav.addObject("error", "DataManageRestController JAVA Exception 발생");
		}

		mav.addObject("dataLableMap", dataLableMap);
		mav.addObject("dataInfoList", dataInfoList);
		return mav;
	}

	/**
	 * 단일 이미지 파일에 대해 display 해준다
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/dataManage/display", method = { RequestMethod.GET, RequestMethod.POST })
	public ResponseEntity<byte[]> displayFile(HttpServletRequest request) {
		String fileFullPath = request.getParameter("fileFullPath");
		fileFullPath = fileFullPath.replaceAll("\\\\", "/");
		HttpHeaders headers = new HttpHeaders();
		byte[] entity = null;
		headers.setContentType(MediaType.IMAGE_JPEG);

		try {
			entity = dataManageService.displayFile(fileFullPath);
		} catch (Exception e) {
			return new ResponseEntity<byte[]>(HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<byte[]>(entity, headers, HttpStatus.CREATED);
	}

	/**
	 * 파일경로를 통해 이미지파일이름을 key 경로를 value 로 가져온다
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/dataManage/getImageMap", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView getImageMap(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		String fileFullPath = request.getParameter("fileFullPath");

		// root 경로를 삭제해주고 순수경로를 구함 root를 제외한 경로
		fileFullPath = fileFullPath.replaceAll("\\\\", "/");
		fileFullPath = fileFullPath.replaceFirst(filePath, "");

		// 테스트 및 서버 경로를 입혀준다
		String tPath = "true".equalsIgnoreCase(isTest) ? fileTestPath + fileFullPath : filePath + fileFullPath;
		try {
			Map<String, String> imageMap = dataManageService.getImageMap(tPath);
			mav.addObject("imageMap", imageMap);
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
	 * user별 용량 체크
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/dataManage/checkFullUserSpace", method = RequestMethod.POST)
	public ModelAndView checkFullUserSpace(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		String fpath = "true".equalsIgnoreCase(isTest) ? fileTestPath : filePath;

		String userId = session.getAttribute("userId") + "";
		String userAuth = session.getAttribute("userAuth") + "";

		try {
			mav = dataManageService.checkFullUserSpace(fpath, userId, userAuth);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mav;
	}
}
