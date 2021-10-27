package com.daumsoft.dataAnalyticsManager.restFullApi.domain;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@Alias("preprocessedData")
public class PreprocessedData {
	private Integer preprocessedDataSequencePk;
	private String command;
	private String name;
	private String filepath;
	private String filename;
	private String summary;
	private String createDatetime;
	private String progressState;
	private String progressStartDatetime;
	private String progressEndDatetime;
	private boolean deleteFlag;
	private Integer projectSequenceFk1;
	private Integer originalDataSequenceFk2;
	private String columns;
	private String statistics;
	private String sampleData;
	private Integer amount;
}
