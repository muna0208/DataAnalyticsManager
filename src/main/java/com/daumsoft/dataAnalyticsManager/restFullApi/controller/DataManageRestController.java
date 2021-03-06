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
	 * ํ์ผ์กฐํ
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
	 * ํด๋์์ฑ
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
	 * ํ์ผ/ํด๋ ์ญ์? (๋ด๋ถ๋๋?ํ?๋ฆฌ ๋ฐ ๋ด์ฉ ๋ชจ๋ ์ญ์?)
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
	 * ํ์ผ/ํด๋ ์ด๋
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
	 * ํ์ผ/ํด๋ ์ด๋ฆ ์์?
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
	 * ๋ก์ปฌ ๋ฐ์ดํฐ ์๋ก๋
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
	 * ์ํ๋ฐ์ดํฐ ๊ฐ์?ธ์ค๊ธฐ
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
		 * ๋ก์ปฌ๋๋?ํ?๋ฆฌ(๋ก์ปฌํธ์คํธ ๋๋?ํ?๋ฆฌ ํ์คํธํ๊ฒฝ) ์์ ์์ฒญํ์ฌ๋ ํ์คํธ์๋ ์๋ฒ์ ์๋ ๋๋?ํ?๋ฆฌ์ ๊ฒฝ๋ก๋ฅผ ๋ฐ๋ผ๋ด์ผ ํ๋ค ๋ก์ปฌ์์๋ ์๋ฒ์์ง์
		 * ์์ฒญํ?์ ์๊ธฐ์ ํ์คํธ ํ๊ฒฝ์ ๋ก์ปฌ๊ณผ ์๋ฒ์ ์ผ์น๊ฐ ์์ด์ผ ํ๋ค
		 */

		// root ๊ฒฝ๋ก๋ฅผ ์ญ์?ํด์ฃผ๊ณ? ์์๊ฒฝ๋ก๋ฅผ ๊ตฌํจ root๋ฅผ ์?์ธํ ๊ฒฝ๋ก
		fileFullPath = fileFullPath.replaceAll("\\\\", "/");
		fileFullPath = fileFullPath.replaceFirst(fileTestPath.replaceAll("\\\\", "/"), "");
		fileFullPath = fileFullPath.replaceFirst(filePath.replaceAll("\\\\", "/"), "");

		try {
			mav = dataManageService.getSampleData(fileFullPath, sample, fileName);
		} catch (Exception e) {
			return mav.addObject("error", "DataManageRestController JAVA Exception ๋ฐ์");
		}

		return mav;
	}

	/**
	 * ์๋ฒํ์ผ์ ๋ฐ์ดํฐ ๊ธฐ๋ณธ์?๋ณด๋ฅผ ๊ฐ์?ธ์จ๋ค
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

		// root ๊ฒฝ๋ก๋ฅผ ์ญ์?ํด์ฃผ๊ณ? ์์๊ฒฝ๋ก๋ฅผ ๊ตฌํจ root๋ฅผ ์?์ธํ ๊ฒฝ๋ก
		fileFullPath = fileFullPath.replaceAll("\\\\", "/");
		fileFullPath = fileFullPath.replaceFirst(fileTestPath.replaceAll("\\\\", "/"), "");
		fileFullPath = fileFullPath.replaceFirst(filePath.replaceAll("\\\\", "/"), "");

		String tPath = fpath + fileFullPath;
		List<Object> dataInfoList = dataManageService.getDataInfo(tPath, fileName);
		mav.addObject("dataInfo", dataInfoList);
		return mav;
	}

	/**
	 * ๋๋?ํ?๋ฆฌ๋ฅผ ์กฐํํ์ฌ ์ด๋ฏธ์ง๊ฐ ๋ค์ด์๋์ง ํ์ธํ๋ค.
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
			return mav.addObject("error", "DataManageRestController JAVA Exception ๋ฐ์");
		}

		mav.addObject("dataLableMap", dataLableMap);
		mav.addObject("dataInfoList", dataInfoList);
		return mav;
	}

	/**
	 * ๋จ์ผ ์ด๋ฏธ์ง ํ์ผ์ ๋ํด display ํด์ค๋ค
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
	 * ํ์ผ๊ฒฝ๋ก๋ฅผ ํตํด ์ด๋ฏธ์งํ์ผ์ด๋ฆ์ key ๊ฒฝ๋ก๋ฅผ value ๋ก ๊ฐ์?ธ์จ๋ค
	 * 
	 * @param request
	 * @param session
	 * @return
	 */
	@RequestMapping(value = "/dataManage/getImageMap", method = { RequestMethod.GET, RequestMethod.POST })
	public ModelAndView getImageMap(HttpServletRequest request, HttpSession session) {
		ModelAndView mav = new ModelAndView("jsonView");
		String fileFullPath = request.getParameter("fileFullPath");

		// root ๊ฒฝ๋ก๋ฅผ ์ญ์?ํด์ฃผ๊ณ? ์์๊ฒฝ๋ก๋ฅผ ๊ตฌํจ root๋ฅผ ์?์ธํ ๊ฒฝ๋ก
		fileFullPath = fileFullPath.replaceAll("\\\\", "/");
		fileFullPath = fileFullPath.replaceFirst(filePath, "");

		// ํ์คํธ ๋ฐ ์๋ฒ ๊ฒฝ๋ก๋ฅผ ์ํ์ค๋ค
		String tPath = "true".equalsIgnoreCase(isTest) ? fileTestPath + fileFullPath : filePath + fileFullPath;
		try {
			Map<String, String> imageMap = dataManageService.getImageMap(tPath);
			mav.addObject("imageMap", imageMap);
		} catch (Exception e) {
			JSONObject result = new JSONObject();
			result.put("result", "fail");
			result.put("detail", "์์คํ์ ๋ฌธ์?๊ฐ ๋ฐ์ํ์์ต๋๋ค.");
			result.put("exception", e);
			mav.addObject("result", result);
		}
		return mav;
	}

	/**
	 * user๋ณ ์ฉ๋ ์ฒดํฌ
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
