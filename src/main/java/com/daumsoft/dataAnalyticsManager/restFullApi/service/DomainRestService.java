package com.daumsoft.dataAnalyticsManager.restFullApi.service;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.daumsoft.dataAnalyticsManager.restFullApi.domain.Domain;
import com.daumsoft.dataAnalyticsManager.restFullApi.mapper.DomainRestMapper;
import com.daumsoft.dataAnalyticsManager.restFullApi.mapper.ProjectRestMapper;

import org.apache.ibatis.session.SqlSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;

@Service
public class DomainRestService {

    @Autowired
    private SqlSession session;

    @Autowired
    private DomainRestMapper domainRestMapper;

    @Autowired
    private ProjectRestMapper projectRestMapper;

    @Value("${filePath}")
    private String filePath;

    @Value("${fileTestPath}")
    private String fileTestPath;

    @Value("${isTest}")
    private String isTest;

    /**
     * 도메인 목록 조회
     * 
     * @param pageNo
     * @param pageRow
     * @return
     * @throws Exception
     */
    public JSONObject domainAsGet(int pageNo, String type, boolean mine, String option, String value, Date startDate,
            Date endDate, String currentId) throws Exception {
        JSONObject resultJson = new JSONObject();
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", pageNo * 10);
        params.put("option", option);
        params.put("value", value);
        params.put("startDate", startDate);
        params.put("endDate", endDate);
        params.put("currentId", currentId);
        params.put("type", type);
        params.put("mine", mine);
        System.out.println(type);

        List<Domain> domainList = session.selectList("domainAsGet", params);
        System.out.println(domainList);
        resultJson.put("domains", domainList);
        int count = domainRestMapper.getDomainCount(option, type, mine, value, startDate, endDate, currentId);
        System.out.println(count);
        resultJson.put("count", count);
        resultJson.put("result", "success");
        return resultJson;
    }

    /**
     * 도메인 개별 조회
     * 
     * @param domainId
     * @return
     * @throws Exception
     */
    public JSONObject domainAsGetOne(int domainId) throws Exception {
        JSONObject resultJson = new JSONObject();
        Domain domain = domainRestMapper.domainAsGetOne(domainId);
        resultJson.put("domain", domain);
        resultJson.put("result", "success");
        return resultJson;
    }

    /**
     * 도메인 등록
     * 
     * @param domain
     * @return
     * @throws Exception
     */
    public JSONObject domainAsPost(Domain domain) throws Exception {
        JSONObject resultJson = new JSONObject();
        domainRestMapper.domainAsPost(domain);
        resultJson.put("result", "success");
        return resultJson;
    }

    /**
     * 도메인 수정
     * 
     * @param domain
     * @return
     * @throws Exception
     */
    public JSONObject domainAsPatch(Domain domain) throws Exception { 
        JSONObject resultJson = new JSONObject();
        domainRestMapper.domainAsPatch(domain);
        resultJson.put("result", "success");
        return resultJson;
    }

    /**
     * 도메인 삭제
     * 
     * @param domainIdList
     * @throws Exception
     */
    public JSONObject domainAsDelete(List<Object> domainIdList) throws Exception {
        JSONObject resultJson = new JSONObject();
        domainRestMapper.domainAsDelete(domainIdList);
        resultJson.put("result", "success");
        return resultJson;
    }

    /**
     * 대쉬보드를 위한 데이터 가져오기
     * 
     * @return
     * @throws Exception
     */
    public JSONObject dataForDashboard(String userId, String userAuth) throws Exception {
        final long FORMAT = 1024;
        JSONObject resultJson = new JSONObject();

        int domainCount = domainRestMapper.getDomainCount(null, "all", false, null, null, null, null);
        resultJson.put("domainCount", domainCount);

        int machineModelCount = projectRestMapper.getModelCount("machine");
        resultJson.put("machineModelCount", machineModelCount);

        int deepModelCount = projectRestMapper.getModelCount("deep");
        resultJson.put("deepModelCount", deepModelCount);

        int machineProjectCount = projectRestMapper.getProjectCount(null, null, null, "M");
        resultJson.put("machineProjectCount", machineProjectCount);

        int deepProjectCount = projectRestMapper.getProjectCount(null, null, null, "D");
        resultJson.put("deepProjectCount", deepProjectCount);

        List<Domain> domainList = session.selectList("getRecentDomains");
        resultJson.put("domainList", domainList);

        String fpath = "true".equalsIgnoreCase(isTest) ? fileTestPath : filePath;
        StringBuilder path = new StringBuilder(fpath);
        if ("user".equals(userAuth)) {
            path.append(File.separator + "users" + File.separator + userId);
        }

        File file = new File(path.toString());
        if (file.exists()) {
            double size = (double) getSizeUnder(file) / (FORMAT * FORMAT * FORMAT);
            resultJson.put("size", size);
        }

        resultJson.put("result", "success");
        return resultJson;
    }

    /**
     * 하위 디렉토리 용량 계산하여 합치기
     * 
     * @param folder
     * @return
     */
    public static long getSizeUnder(File folder) {
        long length = 0;
        File[] files = folder.listFiles();
        int count = files.length;
        for (int i = 0; i < count; i++) {
            if (files[i].isFile()) {
                length += files[i].length();
            } else {
                length += getSizeUnder(files[i]);
            }
        }
        return length;
    }

    /**
     * 모델 or 프로젝트 삭제시 도메인 삭제 연동
     * 
     * @param projectSequencePk
     * @param modelSequencePk
     * @return
     * @throws Exception
     */
    public JSONObject domainAsDeleteWithProjectAndModel(int projectSequencePk, Integer modelSequencePk)
            throws Exception {
        JSONObject resultJson = new JSONObject();
        domainRestMapper.domainAsDeleteWithProjectAndModel(projectSequencePk, modelSequencePk);
        resultJson.put("result", "success");
        return resultJson;
    }

    /**
     * 다운로드를 위한 id 조회
     * 
     * @param domainId
     * @param learningType
     * @param dataType
     * @return
     * @throws Exception
     */
    public int getDownloadId(int domainId, char learningType, String dataType) throws Exception {
        int id = domainRestMapper.getDownloadId(domainId, learningType, dataType);
        return id;
    }
}