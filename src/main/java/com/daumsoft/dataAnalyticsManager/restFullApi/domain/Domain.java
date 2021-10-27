package com.daumsoft.dataAnalyticsManager.restFullApi.domain;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@Alias("domain")
public class Domain {
    private int id;
    private String categoryName;
    private String userId;
    private int projectId;
    private int modelId;
    private String title;
    private String description;
    private String registerer;
    private String createDate;
    private String updateDate;
    private boolean deleteFlag;
    private String type;
}