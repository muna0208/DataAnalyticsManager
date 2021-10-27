package com.daumsoft.dataAnalyticsManager.restFullApi.controller;

import java.io.IOException;
import java.util.Date;

import javax.servlet.http.HttpSession;

import com.daumsoft.dataAnalyticsManager.common.utils.MakeUtil;
import com.daumsoft.dataAnalyticsManager.restFullApi.domain.Domain;
import com.daumsoft.dataAnalyticsManager.restFullApi.domain.wrapper.DeleteWrapper;
import com.daumsoft.dataAnalyticsManager.restFullApi.service.DomainRestService;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import net.sf.json.JSONObject;

@RestController
@RequestMapping("/domains")
public class DomainRestController {

    Logger logger = LoggerFactory.getLogger(DomainRestController.class);

    @Autowired
    private DomainRestService domainRestService;

    @Value("${module.ip}")
    private String moduleIp;

    @Value("${module.port}")
    private String modulePort;

    @Value("${module.method}")
    private String moduleMethod;

    /**
     * 도메인 목록 조회
     * 
     * @return
     */
    @GetMapping
    public ResponseEntity<JSONObject> domains(HttpSession session, @RequestParam int page,
            @RequestParam(required = false) String option, @RequestParam(required = false) String value,
            @RequestParam(required = false) Date startDate, @RequestParam(required = false) Date endDate,
            @RequestParam(required = false) String type, @RequestParam(required = false) boolean mine) {
        JSONObject result = new JSONObject();

        String userId = session.getAttribute("userId") + "";

        try {
            result = domainRestService.domainAsGet(page, type, mine, option, value, startDate, endDate, userId);
            return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
        } catch (Exception e) {
            MakeUtil.printErrorLogger(e, "domains");
            return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
        }
    }


    /**
     * 도메인 개별 조회
     * 
     * @param domainId
     * @return
     */
    @GetMapping("/{domainId}")
    public ResponseEntity<JSONObject> domain(@PathVariable int domainId) {
        JSONObject result = new JSONObject();

        try {
            result = domainRestService.domainAsGetOne(domainId);
            return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
        } catch (Exception e) {
            MakeUtil.printErrorLogger(e, "domain");
            return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
        }
    }

    /**
     * 도메인 등록
     * 
     * @param domain
     * @return
     */
    @PostMapping  
    public ResponseEntity<JSONObject> domainAsPost(HttpSession session, @RequestBody Domain domain) {
        JSONObject result = new JSONObject();

        try {
            if (MakeUtil.isNotNullAndEmpty(domain)) {
                String userId = session.getAttribute("userId") + "";
                String userName = session.getAttribute("userName") + "";

                domain.setUserId(userId);
                domain.setRegisterer(userName);
                result = domainRestService.domainAsPost(domain);
                return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
            } else {
                logger.error("BAD REQUEST TO REGISTER DOMAIN!!!");
                return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            MakeUtil.printErrorLogger(e, "userAsPost");
            return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
        }
    }

    /**
     * 도메인 수정
     * 
     * @param session
     * @param domain
     * @return
     */
    @PatchMapping
    public ResponseEntity<JSONObject> domainAsPatch(@RequestBody Domain domain) {
        JSONObject result = new JSONObject();

        try {
            if (MakeUtil.isNotNullAndEmpty(domain)) {
                result = domainRestService.domainAsPatch(domain);
                return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
            } else {
                logger.error("BAD REQUEST TO MODIFY DOMAIN!!!");
                return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            MakeUtil.printErrorLogger(e, "userAsPatch");
            return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
        }
    }

    /**
     * 도메인 삭제
     * 
     * @param domainIds
     * @return
     */
    @DeleteMapping
    public ResponseEntity<JSONObject> domainAsDelete(@RequestBody DeleteWrapper domainIds) {
        JSONObject result = new JSONObject();

        try {
            if (MakeUtil.isNotNullAndEmpty(domainIds.getList())) {
                result = domainRestService.domainAsDelete(domainIds.getList());
                return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
            } else {
                logger.error("BAD REQUEST TO DELETE DOMAINS!!!");
                return new ResponseEntity<JSONObject>(result, HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            MakeUtil.printErrorLogger(e, "domainAsDelete");
            return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
        }
    }

    /**
     * 도메인 데이터 다운로드
     * 
     * @param domainId
     * @param type
     * @return
     * @throws IOException
     */
    @GetMapping("/{domainId}/{learningType}/{dataType}/download")
    public View download(@PathVariable int domainId, @PathVariable char learningType, @PathVariable String dataType) {
        int id;
        StringBuilder listUrl = new StringBuilder();

        try {
            id = domainRestService.getDownloadId(domainId, learningType, dataType);
            listUrl.append(moduleIp + ":" + modulePort + moduleMethod);
            if ('D' == learningType) {
                listUrl.append("/dl/");
            } else if ('M' == learningType) {
                listUrl.append("/ml/");
            }
            listUrl.append(dataType + "/" + id + "/download");

            if ("preprocessedData".equals(dataType)) {
                listUrl.append("?type=data");
            }

        } catch (Exception e) {
            MakeUtil.printErrorLogger(e, "domainAsDownload");
        }

        return new RedirectView(listUrl.toString());
    }

    /**
     * 대쉬보드를 위한 데이터 가져오기
     * 
     * @return
     * @throws Exception
     */
    @GetMapping("/dashboard")
    public ResponseEntity<JSONObject> dashboard(HttpSession session) {
        JSONObject result = new JSONObject();
        String userId = session.getAttribute("userId") + "";
        String userAuth = session.getAttribute("userAuth") + "";

        try {
            result = domainRestService.dataForDashboard(userId, userAuth);
            return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
        } catch (Exception e) {
            MakeUtil.printErrorLogger(e, "domainForDashboard");
            return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
        }
    }
}