package com.daumsoft.dataAnalyticsManager.restFullApi.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daumsoft.dataAnalyticsManager.common.utils.MakeUtil;
import com.daumsoft.dataAnalyticsManager.restFullApi.mapper.AlgorithmRestMapper;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
@SuppressWarnings("static-access")
public class AlgorithmRestService {

	@Autowired
	private AlgorithmRestMapper algorithmRestMapper;

	/**
	 * 알고리즘 조회
	 * 
	 * @return
	 * @throws Exception
	 */

	public JSONObject algorithms(int page, String option, String value, String type) throws Exception {
		JSONObject result = new JSONObject();
		JSONArray jsonArr = new JSONArray();

		List<Map<String, Object>> list = algorithmRestMapper.algorithms(page * 10, option, value, type);

		for (Map<String, Object> map : list) {
			if (MakeUtil.isNotNullAndEmpty(map))
				jsonArr.add(MakeUtil.nvlJson(new JSONObject().fromObject(map)));
		}
		result.put("result", "success");
		result.put("type", "2000");
		result.put("algorithms", jsonArr);

		int count = algorithmRestMapper.getTotalAlgorithmCount(option, value, type);
		result.put("count", count);
		return result;
	}

	/**
	 * 알고림즘 상세조회
	 * 
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public JSONObject algorithm(Integer id) throws Exception {
		JSONObject result = new JSONObject();

		Map<String, Object> detail = algorithmRestMapper.algorithm(id);
		if (MakeUtil.isNotNullAndEmpty(detail))
			result.put("algorithm", MakeUtil.nvlJson(new JSONObject().fromObject(detail)));

		result.put("result", "success");
		result.put("type", "2000");
		return result;
	}

	/**
	 * 알고리즘 검색 조회
	 * 
	 * @param value
	 * @return
	 */
	public JSONObject searchAlgorithms(String searchValue, String searchType) {
		JSONObject result = new JSONObject();
		JSONArray jsonArr = new JSONArray();

		List<Map<String, Object>> list = algorithmRestMapper.searchAlgorithms(searchValue, searchType);

		for (Map<String, Object> map : list) {
			if (MakeUtil.isNotNullAndEmpty(map)) {
				jsonArr.add(MakeUtil.nvlJson(new JSONObject().fromObject(map)));
			}
		}
		result.put("result", "success");
		result.put("type", "2000");
		result.put("algorithms", jsonArr);
		return result;
	}
}
