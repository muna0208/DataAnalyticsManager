package com.daumsoft.dataAnalyticsManager.restFullApi.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.daumsoft.dataAnalyticsManager.restFullApi.domain.User;

@Mapper
public interface UserMngMapper {

	User getUserInfo(User user) throws Exception;

	int getTotalUserCount(boolean admin, String option, String value) throws Exception;

	void updateLastAccessDate(String userId) throws Exception;

	User userAsGetOne(String userId) throws Exception;

	void userAsPost(@Param("user") User userInfo, @Param("currentName") String currentName) throws Exception;

	void userAsDelete(List<Object> userIdList) throws Exception;

	void userAsPatch(@Param("user") User userInfo, @Param("modifyingId") String modifyingId,
			@Param("currentName") String currentName) throws Exception;

	void useFlagAsPatch(String id) throws Exception;

	User findUser(User userInfo) throws Exception;

	void updateTempPassword(User userInfo) throws Exception;

	// List<User> userAsGet() throws Exception;
}
