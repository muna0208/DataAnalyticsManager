"use strict";

var url, errorMessage;

// TODO 비동기 함수들 관련 페이지로 이동시켜야

/************************************************** 알고리즘 **************************************************/

/*알고리즘 조회*/
var fnAlgorithmListByAjax = function (paging) {
  var result;
  url = "/algorithms?page=" + paging.getCurrentPage();

  var type = $('#type option:selected').val();
  if (type !== 'all') {
    url += "&type=" + type;
  }

  var option = paging.getOption();
  if (option !== undefined) {
    url += "&option=" + option;
  }

  var value = paging.getValue();
  if (value !== undefined) {
    url += "&value=" + value;
  }

  errorMessage = "알고리즘 목록 조회 에러";
  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.algorithms;
    paging.setTotalPage(Math.floor((response.count - 1) / 10));
  });
  return result;
};

/*알고리즘 상세조회*/
var fnAlgorithmByAjax = function (algorithmPk) {
  var result;
  url = "/algorithms/" + algorithmPk;
  errorMessage = "알고리즘 상세조회 에러";
  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.algorithm;
  });
  return result;
};

/************************************************** 프로젝트 **************************************************/


/*프로젝트 목록 조회*/
var fnGetProjectListByAjax = function (paging) {
  var result;
  var option = paging.getOption();
  var value = paging.getValue();

  url = "/projects?page=" + paging.getCurrentPage();

  var type = $("#projectType").val();
  url += "&type=" + type;

  if (option !== undefined) {
    url += "&option=" + option;
  }

  if (value !== undefined) {
    url += "&value=" + value;
  }

  errorMessage = "프로젝트 목록 조회 에러";
  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.projects;
    paging.setTotalPage(Math.floor((response.count - 1) / 10));
  });
  return result;
};

/*프로젝트 상세정보 가져오기*/
var fnGetProjectByAjax = function (projectSequencePk) {
  var result;
  url = "/projects/" + projectSequencePk;
  errorMessage = "프로젝트 상세조회 에러";
  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.project;
  });
  return result;
};

/*프로젝트 등록/수정*/
var fnSaveProjectByAjax = function (url, method, data) {
  errorMessage = "프로젝트 등록/수정 에러";
  fnAjaxDataSync(url, method, JSON.stringify(data), errorMessage, function (response) {
    result = response;
  });
  return result;
};

/*프로젝트 삭제*/
var fnDeleteProjcetByAjax = function (checkId) {
  var result; // 체크된 항목 가져오기

  url = "/projects/" + checkId;
  errorMessage = "프로젝트 삭제 에러";
  fnAjaxDeleteDataSync(url, errorMessage, function (response) {
    result = response;
  });
  return result;
};

/*원본 리스트 가져오기*/
var fnGetOriginalDataListByAjax = function (projectSequencePk) {
  var result;
  url = "/projects/" + projectSequencePk + "/originalData";
  errorMessage = "원본 리스크 조회 에러";
  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.originalDataList;
  });
  return result;
};

/* 원본데이터 가져오기 */
var fnGetOriginalDataByAjax = function (projectSequencePk, selectedOriginalDataPk) {
  var result;
  url = "/projects/" + projectSequencePk + "/originalData/" + selectedOriginalDataPk;
  errorMessage = "원본데이터 조회 에러";
  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.originalData;
  });
  return result;
};

/* 원본데이터 삭제*/
var fnDeleteOriginalDataByAjax = function (projectSequencePk, selectedOriginalDataPk) {
  var result;
  url = "/projects/" + projectSequencePk + "/originalData/" + selectedOriginalDataPk;
  errorMessage = "원본데이터 삭제 에러";
  fnAjaxDeleteDataSync(url, errorMessage, function (response) {
    result = response;
  });
  return result;
};

/*파일브라우저 가져오기*/
var fnGetFileBrowserByAjax = function () {
  var result;
  url = "/projects/localFiles";
  errorMessage = "로컬파일 조회 에러";
  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.localFiles;
  });
  return result;
};

/*파일브라우저 샘플 미리보기*/
var fnGetLocalFileSampleByAjax = function (localFile) {
  var result;
  url = "/projects/localFiles/" + localFile;
  errorMessage = "로컬파일 샘플조회 에러";
  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response;
  });
  return result;
};

/*원본데이터 생성*/
var fnCreateOriginalDataByAjax = function (projectSequencePk, data) {
  var result;
  url = "/projects/" + projectSequencePk + "/originalData/";
  errorMessage = "원본데이서 생성 에러";
  fnAjaxDataSync(url, "POST", JSON.stringify(data), errorMessage, function (response) {
    result = response;
  });
  return result;
};

/*처리방식 가져오기*/
var fnGetPreprocessFunctionByAjax = function () {
  var result;
  url = "/preprocessFunctions";
  errorMessage = "처리방식 조회 에러";
  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.preprocessFunctionList;
  });
  return result;
};

/*파라미터 가져오기*/
var fnGetPreprocessFunctionParametersByAjax = function (preprocessFunctionSequencePk) {
  var result;
  url = "/preprocessFunctions/" + preprocessFunctionSequencePk;
  errorMessage = "처리방식 파라미터 조회 에러";
  fnAjaxGetDataSync(url, errorMessage, function (response) {
    console.log(response);
    result = response.preprocessFunction;
  });
  return result;
};

/*전처리 테스트*/
var fnPreprocessTestByAjax = function (projectSequencePk, selectedOriginalDataPk, data, beginner) {
  var result;
  url = "/projects/" + projectSequencePk + "/originalData/" + selectedOriginalDataPk;
  if (beginner) {
    url += "?user_type=beginner";
  }
  errorMessage = "전처리 테스트 에러";
  fnAjaxDataSync(url, "PATCH", JSON.stringify(data), errorMessage, function (response) {
    result = response;
  });
  return result;
};

/*전처리 생성*/
var fnCreatePreprocessByAjax = function (projectSequencePk, data) {
  var result;
  url = "/projects/" + projectSequencePk + "/preprocessedData";
  errorMessage = "전처리 생성 에러";
  fnAjaxDataSync(url, "POST", JSON.stringify(data), errorMessage, function (response) {
    result = response;
  });
  return result;
};

/*전처리 목록 가져오기*/
var fnGetPreprocessedDataListByAjax = function (selectedOriginalData) {
  var result;
  url = "/originalData/" + selectedOriginalData + "/preprocessedData";
  errorMessage = "전처리 리스트 조회 에러";
  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.preprocessedDataList;
  });
  return result;
};

/* 전처리 데이터 가져오기*/
var fnGetPreprocessedDataByAjax = function (selectedOriginalData, selectedPreprocessedData) {
  var result;
  url = "/originalData/" + selectedOriginalData + "/preprocessedData/" + selectedPreprocessedData;
  errorMessage = "전처리데이터 조회 에러";
  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.preprocessedData;
  });
  return result;
};

/*전처리 삭제*/
var fnDeletePreprocessedDataByAjax = function (projectSequencePk, selectedPreprocessedData) {
  var result;
  url = "/projects/" + projectSequencePk + "/preprocessedData/" + selectedPreprocessedData;
  errorMessage = "전처리 데이터 삭제 에러";
  fnAjaxDeleteDataSync(url, errorMessage, function (response) {
    result = response;
  });
  return result;
};

/*알고리즘 검색 조회 */
var fnSearchAlgorithmByAjax = function (searchValue, searchType) {
  var result;
  url = "/searchAlgorithms";
  errorMessage = "알고리즘 검색 조회 에러";
  var data = {
    "searchValue": searchValue
    , "projectType": searchType
  };
  fnAjaxDataSync(url, "POST", JSON.stringify(data), errorMessage, function (response) {
    result = response.algorithms;
  });
  return result;
};

/*학습모델 생성 */
var fnModelsByAjax = function (projectSequencePk, data) {
  var result;
  url = "/projects/" + projectSequencePk + "/models";
  errorMessage = "학습모델 생성 에러";
  fnAjaxDataSync(url, "POST", JSON.stringify(data), errorMessage, function (response) {
    result = response;
  });
  return result;
};

/*모델 목록 조회*/
var fnGetModelsByAjax = function (projectSequencePk, preprocessedDataSequencePk) {
  var result;
  url = "/projects/" + projectSequencePk + "/models?preprocessedDataSequencePk=" + preprocessedDataSequencePk;
  errorMessage = "모델 목록 조회 에러";
  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.modelsList;
  });
  return result;
};

/*모델 조회*/
var fnGetModeslByAjax = function (projectSequencePk, modelSequencePk) {
  var result;
  url = "/projects/" + projectSequencePk + "/models/" + modelSequencePk;
  errorMessage = "모델 조회 에러";
  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.model;
  });
  return result;
};

/*모델 삭제*/
var fnDeleteModelByAjax = function (projectSequencePk, modelSequencePk) {
  var result;
  url = "/projects/" + projectSequencePk + "/models/" + modelSequencePk;
  errorMessage = "모델 삭제 에러";
  fnAjaxDeleteDataSync(url, errorMessage, function (response) {
    result = response;
  });
  return result;
};

/*모델 학습 중지*/
var fnStopAndRestartModelByAjax = function (projectSequencePk, modelSequencePk, data) {
  var result;
  url = "/projects/" + projectSequencePk + "/models/" + modelSequencePk;
  errorMessage = "모델 학습 에러";
  fnAjaxDataSync(url, "PATCH", JSON.stringify(data), errorMessage, function (response) {
    result = response;
  });
  return result;
};

/*모델 테스트*/
var fnModelTestByAjax = function (projectSequencePk, modelSequencePk, data) {
  var result;
  url = "/projects/" + projectSequencePk + "/modelsTest/" + modelSequencePk;
  errorMessage = "모델 테스트 에러";
  fnAjaxDataSync(url, "PATCH", JSON.stringify(data), errorMessage, function (response) {
    result = response;
  });
  return result;
};

/* 인스턴스별 모델 목록 조회*/
var fnGetModelsOfInstancePkByAjax = function (instanceSequencePk) {
  var result;
  url = "/modelsOfInstancePk/" + instanceSequencePk;
  errorMessage = "모델 목록 조회 에러";
  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.modelsList;
  });
  return result;
};
