package com.daumsoft.dataAnalyticsManager.restFullApi.service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.daumsoft.dataAnalyticsManager.common.utils.EncryptionUtil;
import com.daumsoft.dataAnalyticsManager.common.utils.MakeUtil;
import com.daumsoft.dataAnalyticsManager.restFullApi.domain.User;
import com.daumsoft.dataAnalyticsManager.restFullApi.mapper.UserMngMapper;

import net.sf.json.JSONObject;

@Service
@SuppressWarnings("static-access")
public class UserMngService {

	@Autowired
	private SqlSession session;

	@Value("${filePath}")
	private String filePath;

	@Value("${fileTestPath}")
	private String fileTestPath;

	@Value("${isTest}")
	private String isTest;

	@Autowired
	private UserMngMapper userMngMapper;

	public User getUserInfo(User user) throws Exception {
		return userMngMapper.getUserInfo(user);
	}

	/**
	 * 사용자 접속 시간 저장
	 * 
	 * @param userId
	 * @throws Exception
	 */
	public void updateLastAccessDate(String userId) throws Exception {
		userMngMapper.updateLastAccessDate(userId);
	}

	/**
	 * 사용자 목록 조회
	 * 
	 * @return
	 * @throws Exception
	 */
	public JSONObject userAsGet(String currentId, boolean admin, String option, String value, int pageNo)
			throws Exception {
		JSONObject resultJson = new JSONObject();
		Map<String, Object> params = new HashMap<>();
		params.put("currentId", currentId);
		params.put("option", option);
		params.put("value", value);
		params.put("admin", admin);
		params.put("pageNo", pageNo * 10);
		System.out.println(admin == true);

		List<User> userList = session.selectList("userAsGet", params);
		resultJson.put("users", userList);
		int count = userMngMapper.getTotalUserCount(admin, option, value);
		resultJson.put("count", count);
		resultJson.put("result", "success");
		return resultJson;
	}

	/**
	 * 사용자 개별 조회
	 * 
	 * @param userId
	 * @return
	 */
	public JSONObject userAsGetOne(String userId) throws Exception {
		JSONObject resultJson = new JSONObject();
		User user = userMngMapper.userAsGetOne(userId);
		if (MakeUtil.isNotNullAndEmpty(user))
			resultJson.put("user", new JSONObject().fromObject(user));
		resultJson.put("result", "success");
		return resultJson;
	}

	/**
	 * 사용자 등록
	 * 
	 * @param userInfo
	 * @return
	 */
	public JSONObject userAsPost(User userInfo, String currentName) throws Exception {
		JSONObject resultJson = new JSONObject();
		User user = userMngMapper.userAsGetOne(userInfo.getUserId());

		// 사용자 ID 중복체크
		if (MakeUtil.isNotNullAndEmpty(user)) {
			resultJson.put("result", "success");
			resultJson.put("successMessage", "duplication userId");
			return resultJson;
		}

		// 사용자 패스워드 암호화
		if (MakeUtil.isNotNullAndEmpty(userInfo.getUserPw())) {
			userInfo.setUserPw(EncryptionUtil.encrypt(userInfo.getUserPw()));
		} else {
			userInfo.setUserPw("");
		}

		userMngMapper.userAsPost(userInfo, currentName);
		String fpath = "true".equalsIgnoreCase(isTest) ? fileTestPath : filePath;
		File folder = new File(fpath + "/users/" + userInfo.getUserId());
		if (!folder.exists()) {
			folder.mkdir();
		}
		user = userMngMapper.userAsGetOne(userInfo.getUserId());
		if (MakeUtil.isNotNullAndEmpty(user)) {
			resultJson.put("user", new JSONObject().fromObject(user));
		}

		resultJson.put("result", "success");
		return resultJson;
	}

	/**
	 * 사용자 삭제
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public JSONObject userAsDelete(List<Object> list) throws Exception {
		JSONObject resultJson = new JSONObject();
		userMngMapper.userAsDelete(list);
		String fpath = "true".equalsIgnoreCase(isTest) ? fileTestPath : filePath;

		for (Object userId : list) {
			deleteFolder(fpath + "/users/" + (String) userId);
		}
		resultJson.put("result", "success");
		return resultJson;
	}

	/**
	 * 사용자 삭제시 서버의 사용자 ID의 폴더도 삭제
	 * 
	 * @param path
	 */
	public void deleteFolder(String path) {
		File folder = new File(path);
		try {
			if (folder.exists()) {
				if (!folder.isDirectory()) {
					folder.delete();
					return;
				}

				File[] folder_list = folder.listFiles(); // 파일리스트 얻어오기

				for (int i = 0; i < folder_list.length; i++) {
					if (folder_list[i].isFile()) {
						folder_list[i].delete();
					} else {
						deleteFolder(folder_list[i].getPath());
					}
					folder_list[i].delete();
				}
				folder.delete(); // 폴더 삭제
			}
		} catch (Exception e) {
			MakeUtil.printErrorLogger(e, "delete folder");
		}
	}

	/**
	 * 사용자 정보 수정
	 * 
	 * @param userInfo
	 * @param id
	 * @throws Exception
	 */
	public JSONObject userAsPatch(User userInfo, String modifyingId, String currentName) throws Exception {
		JSONObject resultJson = new JSONObject();
		User user = userMngMapper.userAsGetOne(userInfo.getUserId());

		// 사용자 ID 중복체크
		if (MakeUtil.isNotNullAndEmpty(user)) {
			resultJson.put("result", "success");
			resultJson.put("successMessage", "duplication userId");
			return resultJson;
		}

		// 사용자 패스워드 암호화
		if (MakeUtil.isNotNullAndEmpty(userInfo.getUserPw())) {
			userInfo.setUserPw(EncryptionUtil.encrypt(userInfo.getUserPw()));
		} else {
			userInfo.setUserPw("");
		}

		userMngMapper.userAsPatch(userInfo, modifyingId, currentName);
		user = userMngMapper.userAsGetOne(userInfo.getUserId());
		if (MakeUtil.isNotNullAndEmpty(user)) {
			resultJson.put("user", new JSONObject().fromObject(user));
		}

		resultJson.put("result", "success");
		return resultJson;
	}

	/**
	 * 사용 여부 토글
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public JSONObject useFlagAsPatch(String id) throws Exception {
		JSONObject resultJson = new JSONObject();
		userMngMapper.useFlagAsPatch(id);

		User user = userMngMapper.userAsGetOne(id);
		if (MakeUtil.isNotNullAndEmpty(user)) {
			resultJson.put("user", new JSONObject().fromObject(user));
		}

		resultJson.put("result", "success");
		return resultJson;
	}

	/**
	 * 사용자 찾기
	 * 
	 * @param userInfo
	 * @return
	 * @throws Exception
	 */
	public JSONObject findUser(User userInfo) throws Exception {
		JSONObject resultJson = new JSONObject();
		User findedUser = null;

		// ID 찾기
		if ("findUserId".equals(userInfo.getOption())) {
			findedUser = userMngMapper.findUser(userInfo);
			System.out.println(findedUser);
			if (MakeUtil.isNotNullAndEmpty(findedUser)) {
				resultJson.put("result", "success");
				resultJson.put("user", findedUser);
			} else {
				resultJson.put("result", "error");
				resultJson.put("errorMessage", "Not Found UserInfo");
			}
		} else if ("findUserPw".equals(userInfo.getOption())) {
			findedUser = userMngMapper.findUser(userInfo);
			if (MakeUtil.isNotNullAndEmpty(findedUser)) {
				String tempPw = MakeUtil.getRandomString(10);
				findedUser.setUserPw(EncryptionUtil.encrypt(tempPw));
				userMngMapper.updateTempPassword(findedUser);
				resultJson.put("result", "success");
				resultJson.put("tempPw", tempPw);
			} else {
				resultJson.put("result", "error");
				resultJson.put("errorMessage", "Not Found UserInfo");
			}
		}

		return resultJson;
	}

	/**
	 * 사용자 정보 세션에 담기
	 * 
	 * @param request
	 * @param user
	 */
	public void createSession(HttpServletRequest request, User user) {
		HttpSession session = request.getSession();
		session.setAttribute("id", user.getId());
		session.setAttribute("userId", user.getUserId());
		session.setAttribute("userAuth", user.getUserAuth());
		session.setAttribute("userName", user.getUserName());
		session.setAttribute("userEmail", user.getEmail());
	}
}
