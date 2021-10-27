package com.daumsoft.dataAnalyticsManager.restFullApi.domain;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@Alias("project")
public class Project {
	private Integer id;
	private String name;
	private String description;
	private String createDate;
	private String userId;
	private String type;
	private String typeDetail;
	private Integer selectedInstance;
	private boolean deleteFlag;
}
