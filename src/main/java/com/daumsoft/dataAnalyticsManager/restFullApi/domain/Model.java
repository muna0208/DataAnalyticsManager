package com.daumsoft.dataAnalyticsManager.restFullApi.domain;

import org.apache.ibatis.type.Alias;

import lombok.Data;

@Data
@Alias("model")
public class Model {
	private Integer modelSequencePk;
	private String command;
	private String name;
	private String filepath;
	private String filename;
	private Integer filesize;
	private String trainSummary;
	private String validationSummary;
	private String createDatetime;
	private String progressState;
	private String progressStartDatetime;
	private String progressEndDatetime;
	private String loadState;
	private String loadProcessId;
	private boolean deleteFlag;
	private Integer originalDataSequenceFk1;
	private Integer preprocessedDataSequenceFk2;
	private Integer instanceSequenceFk3;
	private Integer projectSequenceFk4;
}
