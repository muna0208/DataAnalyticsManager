package com.daumsoft.dataAnalyticsManager.restFullApi.domain;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@Alias("category")
public class Category {
    private int categoryId;
    private String name;
    private String description;
    private String createDate;
    private String writer;
    private boolean useFlag;
    private boolean deleteFlag;
}