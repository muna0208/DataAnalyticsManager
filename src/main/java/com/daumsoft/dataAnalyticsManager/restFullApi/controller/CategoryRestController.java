package com.daumsoft.dataAnalyticsManager.restFullApi.controller;

import com.daumsoft.dataAnalyticsManager.common.utils.MakeUtil;
import com.daumsoft.dataAnalyticsManager.restFullApi.service.CategoryRestService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import net.sf.json.JSONObject;

@RestController
@RequestMapping("/categories")
public class CategoryRestController {

    Logger logger = LoggerFactory.getLogger(DomainRestController.class);

    @Autowired
    private CategoryRestService categoryRestService;

    /**
     * 카테고리 목록 가져오기
     * 
     * @return
     */
    @GetMapping
    public ResponseEntity<JSONObject> getCategories() {
        JSONObject result = new JSONObject();

        try {
            result = categoryRestService.getCategories();
            return new ResponseEntity<JSONObject>(result, HttpStatus.OK);
        } catch (Exception e) {
            MakeUtil.printErrorLogger(e, "categories");
            return new ResponseEntity<JSONObject>(result, HttpStatus.EXPECTATION_FAILED);
        }
    }
}