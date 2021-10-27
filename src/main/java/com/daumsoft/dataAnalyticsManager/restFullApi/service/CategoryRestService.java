package com.daumsoft.dataAnalyticsManager.restFullApi.service;

import java.util.List;

import com.daumsoft.dataAnalyticsManager.restFullApi.domain.Category;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;

@Service
public class CategoryRestService {

    @Autowired
    private SqlSession session;

    /**
     * 카테고리 목록 조회
     * 
     * @return
     */
    public JSONObject getCategories() {
        JSONObject resultJson = new JSONObject();
        List<Category> categories = session.selectList("getCategoryNameList");

        resultJson.put("categories", categories);
        resultJson.put("result", "success");
        return resultJson;
    }
}