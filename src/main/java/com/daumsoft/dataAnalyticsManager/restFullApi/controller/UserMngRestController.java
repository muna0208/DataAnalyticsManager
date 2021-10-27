package com.daumsoft.dataAnalyticsManager.restFullApi.controller;

import javax.servlet.http.HttpSession;

import com.daumsoft.dataAnalyticsManager.common.utils.MakeUtil;
import com.daumsoft.dataAnalyticsManager.restFullApi.domain.User;
import com.daumsoft.dataAnalyticsManager.restFullApi.domain.wrapper.DeleteWrapper;
import com.daumsoft.dataAnalyticsManager.restFullApi.service.UserMngService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.sf.json.JSONObject;

@RestController
@RequestMapping("/users")
public class UserMngRestController {

    private Logger logger = LoggerFactory.getLogger(UserMngRestController.class);

    @Autowired
    private UserMngService userMngService;

    /**
     * 사용자 리스트 조회
     * 
     * @param session
     * @return
     */
    @GetMapping
    public ResponseEntity<JSONObject> users(HttpSession session, @RequestParam int page,
            @RequestParam(required = false) String option, @RequestParam(required = false) String value,
            @RequestParam(required = false) boolean admin) {
        JSONObject result = new JSONObject();

        try {
            String userId = session.getAttribute("userId") + "";
            result = userMngService.userAsGet(userId, admin, option, value, page);
            return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
        } catch (Exception e) {
            MakeUtil.printErrorLogger(e, "users");
            return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
        }
    }

    /**
     * 사용자 개별 조회
     * 
     * @param id
     * @return
     */
    @GetMapping("/{userId}")
    public ResponseEntity<JSONObject> user(HttpSession session, @PathVariable String userId) {
        JSONObject result = new JSONObject();

        try {
            if ("myself".equals(userId)) {
                String id = session.getAttribute("userId") + "";
                String userName = session.getAttribute("userName") + "";
                String email = session.getAttribute("userEmail") + "";
                User user = new User(id, userName, email);

                result.put("user", user);
                result.put("result", "success");
                return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
            } else if (MakeUtil.isNotNullAndEmpty(userId)) {
                result = userMngService.userAsGetOne(userId);
                return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
            } else {
                logger.error("BAD REQUEST TO LOOK UP ONE USER!!!");
                return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            MakeUtil.printErrorLogger(e, "user");
            return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
        }
    }

    /**
     * 사용자 등록
     * 
     * @param userInfo
     * @return
     */
    @PostMapping
    public ResponseEntity<JSONObject> userAsPost(HttpSession session, @RequestBody User userInfo) {
        JSONObject result = new JSONObject();
        try {
            if (MakeUtil.isNotNullAndEmpty(userInfo)) {
                String name = session.getAttribute("userName") + "";
                result = userMngService.userAsPost(userInfo, name);
                return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
            } else {
                logger.error("BAD REQUEST TO REGISTER " + userInfo.getUserId() + "!!!");
                return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            MakeUtil.printErrorLogger(e, "usersAsPost");
            return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
        }
    }

    /**
     * 사용자 삭제
     * 
     * @param userId
     * @return
     */
    @DeleteMapping
    public ResponseEntity<JSONObject> userAsDelete(@RequestBody DeleteWrapper userIds) {
        JSONObject result = new JSONObject();

        try {
            if (MakeUtil.isNotNullAndEmpty((userIds.getList()))) {
                result = userMngService.userAsDelete(userIds.getList());
                return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
            } else {
                logger.error("BAD REQUEST TO DELETE USERS!!!");
                return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            MakeUtil.printErrorLogger(e, "userAsDelete");
            return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
        }
    }

    /**
     * 사용자 정보 수정
     * 
     * @param userInfo
     * @return
     */
    @PatchMapping("/{modifyingId}")
    public ResponseEntity<JSONObject> userAsPatch(@RequestBody User userInfo, @PathVariable String modifyingId,
            HttpSession session) {
        JSONObject result = new JSONObject();

        try {
            if (MakeUtil.isNotNullAndEmpty(userInfo) && MakeUtil.isNotNullAndEmpty(modifyingId)) {
                String userName = session.getAttribute("userName") + "";
                result = userMngService.userAsPatch(userInfo, modifyingId, userName);
                return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
            } else {
                logger.error("BAD REQUEST TO MODIFY USER!!!");
                return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            MakeUtil.printErrorLogger(e, "userAsPatch");
            return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
        }
    }

    /**
     * 사용 여부 토글
     * 
     * @param id
     * @return
     */
    @PatchMapping("/{id}/useFlag")
    public ResponseEntity<JSONObject> useFlagAsPatch(@PathVariable String id) {
        JSONObject result = new JSONObject();

        try {
            if (MakeUtil.isNotNullAndEmpty(id)) {
                result = userMngService.useFlagAsPatch(id);
                return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
            } else {
                logger.error("BAD REQUEST TO TOGGLE USEFLAG OF " + id + "!!!");
                return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            MakeUtil.printErrorLogger(e, "useFlagAsPatch");
            return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
        }
    }

    /**
     * 아이디, 비밀번호 찾기
     * 
     * @param userInfo
     * @return
     */
    @PostMapping("/find")
    public ResponseEntity<JSONObject> findUser(@RequestBody User userInfo) {
        JSONObject result = new JSONObject();
        try {
            result = userMngService.findUser(userInfo);
            return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
        } catch (Exception e) {
            result.put("result", "error");
            result.put("errorMessage", MakeUtil.printErrorLogger(e, "findUser"));
            return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
        }
    }

    /**
     * 회원가입
     * 
     * @param userInfo
     * @return
     * @throws Exception
     */
    @PostMapping("/signUp")
    private ResponseEntity<JSONObject> signUp(@RequestBody User userInfo) throws Exception {
        JSONObject result = new JSONObject();
        try {
            if (MakeUtil.isNotNullAndEmpty(userInfo)) {
                result = userMngService.userAsPost(userInfo, null);
                logger.info("SIGN UP " + userInfo.getUserId() + "!!!");
                return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
            } else {
                logger.error("BAD REQUEST TO SIGN UP " + userInfo.getUserId() + "!!!");
                return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            MakeUtil.printErrorLogger(e, "SIGN UP");
            return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
        }
    }
}
