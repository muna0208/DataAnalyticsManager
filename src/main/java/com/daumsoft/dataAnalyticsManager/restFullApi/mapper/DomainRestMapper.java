package com.daumsoft.dataAnalyticsManager.restFullApi.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.Date;
import java.util.List;

import com.daumsoft.dataAnalyticsManager.restFullApi.domain.Domain;

@Mapper
public interface DomainRestMapper {

    Domain domainAsGetOne(int domainId) throws Exception;

    void domainAsPost(Domain domain) throws Exception;

    void domainAsDelete(List<Object> domainIdList) throws Exception;

    int getDomainCount(String option, String type, boolean mine, String value, Date startDate, Date endDate,
            String currentId) throws Exception;

    void domainAsPatch(Domain domain) throws Exception;

    void domainAsDeleteWithProjectAndModel(int projectSequencePk, Integer modelSequencePk) throws Exception;

    int getDownloadId(int domainId, char learningType, String dataType) throws Exception;
}