package com.daumsoft.dataAnalyticsManager.restFullApi.domain;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@Alias("user")
public class User {
	private int id;
	private String userId;
	private String userName;
	private String email;
	private String userPw;
	private String lastAccessDate;
	private boolean useFlag;
	private String userAuth;
	private String registerDate;
	private String registerer;
	private String updateDate;
	private String updater;
	private String option;
	private String phoneNumber;

	public User() {
		
	}

	public User(String userId, String userName, String email) {
		this.userId = userId;
		this.userName = userName;
		this.email = email;
	}
}
