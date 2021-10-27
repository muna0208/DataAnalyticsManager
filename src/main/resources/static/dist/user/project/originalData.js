"use strict";

var SELECTED_INSTANCE_PK, SELECTED_ORIGINAL_DATA_PK;
var PREPROCESS_TEST_FLAG = false;
var PREPROCESS_REQUEST_TEST;
var ORG_DATACHART_ARR = new Array();
var ORIGIN_STATISTICS;
var PAGE = 1;

$("#loading").show();
$(function () {
  fnInit();

  // 원본리스트 클릭시
  $(document).on("click", ".originalData", function () {
    /*샌드박스 파일브라우저 샘플 미리보기*/
    $(".originalData").removeClass("active");
    $(this).addClass("active");
    SELECTED_ORIGINAL_DATA_PK = $(this).attr("id");
    fnGetOriginalData();
  });

  // 전처리 처리방식 변경시
  $(document).on("change", "#preprocessFunction", function () {
    fnGetPreprocessFunctionParameters($(this).val());
  });

  // 파라미터 변경시
  $(document).on("change", "#parameters", function () {
    fnSetParamType($(this).val(), $(this).children("option:selected").attr("data-enumerate"), $(this).children("option:selected").attr("data-default"));
  });

  // 전처리 모달의 등록된 전처리 리스트 삭제버튼
  $(document).on("click", ".deletePreprocessBtn", function () {
    $(this).parent().parent().remove();
    $("#preprocessTest").text("");
    PREPROCESS_TEST_FLAG = false;
  });

  // inputValue
  $("#inputValue").bind("keypress", function (e) {
    if (e.which == 13) fnAddParams();
  });

  /*원본데이터 차트 변경시*/
  $(document).on("change", "#originalDataChartName", function () {
    fnOriginalChart(JSON.parse($(this).val()));
  });
});

var fnInit = function () {
  PROJECT_SEQUENCE_PK = $("#projectSequencePk").val(); // 프로젝트 정보를 가져온다.

  fnGetProjectInfo(PROJECT_SEQUENCE_PK); // 원본 리스트 가져오기

  fnGetOriginalDataList(PROJECT_SEQUENCE_PK, function () {
    pageInit();
  });

  $("#preprocessTab li").on("click", function () {
    $("#preprocessTest").html("");
  })
};

/*프로젝트 정보 가져오기*/
var fnGetProjectInfo = function (projectId) {
  var project = fnGetProjectByAjax(projectId);
  $("#name").text(project.name);
  $("#description").text(project.description);
  $("#createDataTime").text("생성일: " + project.create_date);
};

/*원본 리스트 가져오기*/
var fnGetOriginalDataList = function (projectId) {
  var originalDataList = fnGetOriginalDataListByAjax(projectId);
  var html = "";
  for (var i in originalDataList) {
    var oData = originalDataList[i];
    var name = oData.name.length > 40 ? oData.name.substring(0, 40) + "..." : oData.name;

    if (i == 0) {
      html += "<li class='pdTB_5 originalData active' role='button'" + " id=" + oData.id + " title=\"" + oData.name + "\">" + name + "</li>";
      SELECTED_ORIGINAL_DATA_PK = oData.id;
      fnGetOriginalData(oData);
    } else {
      html += "<li class='pdTB_5 originalData' role='button' " + "id=" + oData.id + " title=\"" + oData.name + "\">" + name + "</li>";
    }
  }

  if (html == "") {
    html += "<li>원본데이터가 없습니다.</li>";
    $("#originalDataDiv").hide();

    // 전처리 리스트 가져오기
    fnGetPreprocessedDataList();
  } else {
    $("#deleteOriginalDataBtn").show();
    if (2 > PAGE) {
      PAGE = 2;
    }
    pageInit();
  }

  if (originalDataList.length > 0) {
    $("#step-1").hide();
    $("#step1").removeClass("selected");
    $("#step1").addClass("done");
  } else {
    $("#step-1").show();
    $("#step1").removeClass("done");
    $("#step1").addClass("selected");
    $("#step-2").hide();
    $("#step2").removeClass("selected");
    $("#step2").addClass("disabled");
  }

  $("#originalDataList").html(html);
  $("#loading").hide();
};

/* 원본데이터 가져오기 */
var fnGetOriginalData = function (oData) {
  if (oData == null) {
    oData = fnGetOriginalDataByAjax(PROJECT_SEQUENCE_PK, SELECTED_ORIGINAL_DATA_PK);
  }

  var columns = oData.sample_data.value;
  var sampleDataTemplate = getSampleDataTemplate(columns);
  $("#oDataTable").html(sampleDataTemplate);
  $(".originalDataName").text(oData.name);

  var statistics = oData.statistic_info.value; //	var html = "";
  var statisticsTemplate = getStatisticTemplate(statistics);
  $("#dataShow").html(statisticsTemplate); //오리진차트 전역변수

  ORIGIN_STATISTICS = statistics;
  $("#originalDataDiv").fadeIn(); // 전처리 리스트 가져오기

  fnGetPreprocessedDataList();
};

/**
 * 원본 데이터 샘플 화면 구성요소
 * 
 * @param {JSONObject} columns 
 */
function getSampleDataTemplate(columns) {
  var html = "";

  html += "<thead>";
  html += "<tr>";

  for (var column in columns) {
    html += "<th>" + column + "</th>";
  }
  html += "</tr>";
  html += "</thead>";
  html += "<tbody>";

  for (var i = 0; i < 5; i++) {
    html += "<tr>";
    for (var column in columns) {
      html += "<td>" + columns[column][i] + "</td>";
    }
    html += "</tr>";
  }

  html += "</tbody>";

  return html;
}

/**
 * 통계 정보 화면 구성요소
 * 
 * @param {JSONObject} statistics 
 */
function getStatisticTemplate(statistics) {
  var html = "";

  for (var i in statistics) {
    if ("datetime" == statistics[i].name) {
      continue;
    }

    html += "<div class=\"overY_scrollauto\">";
    html += "<div class=\"col-md-8 col-sm-4 col-xs-12\">";
    html += "<div class=\"x_panel height_320\">";
    html += "	<div class=\"con_title\">";
    html += "	<p class=\"col-md-9\">" + statistics[i].name + "</p>";
    html += "	<button class=\"btn btn-default btn-xs col-md-3\">" + statistics[i].type + "</button>";
    html += "</div>";
    html += "	<div class=\"con_content height_270\" id=\"originalDataChartDiv" + i + "\">";
    html += "	</div>";
    html += "</div>";
    html += "</div>";
    html += "<div class=\"col-md-4 col-sm-4 col-xs-12\">";
    html += "<table class=\"table table-striped table-bordered\">";
    html += "<tbody id=\"tbodyHtml\">";

    for (var j in statistics[i].compact_data.additional_info) {
      if (j == "quantiles") {
        for (var k in statistics[i].compact_data.additional_info[j]) {
          html += "<tr>";
          html += "<td>" + k + "</td>";
          html += "<td>" + statistics[i].compact_data.additional_info[j][k] + "</td>";
          html += "</tr>";
        }
      } else {
        html += "<tr>";
        html += "<td>" + j + "</td>";
        html += "<td>" + statistics[i].compact_data.additional_info[j] + "</td>";
        html += "</tr>";
      }
    }

    html += "";
    html += "</tbody>";
    html += "</table>";
    html += "</div>";
    html += "</div>";
    html += "</div>";
  }

  return html;
}

/*데이터셋 데이터 보기 팝업 클릭시 차트 생성*/
function fnDrawOriginChart() {
  if (ORG_DATACHART_ARR.length != 0) {
    return;
  }

  for (var i in ORIGIN_STATISTICS) {
    (function (ii) {
      setTimeout(function () {
        if ("datetime" != ORIGIN_STATISTICS[ii].name) {
          fnOriginalChart(ORIGIN_STATISTICS[ii], ii);
        }
      }, ii * 400);
    })(i);
  }
}

/* 원본데이터 삭제*/
var fnDeleteOriginalData = function () {
  if (confirm("원본 데이터를 삭제하면 생성된 전처리, 모델이 같이 삭제됩니다. \n삭제하시겠습니까?")) {
    var response = fnDeleteOriginalDataByAjax(PROJECT_SEQUENCE_PK, SELECTED_ORIGINAL_DATA_PK);

    if (response.result == "success") {
      fnComNotify("success", "원본데이터를 삭제 하였습니다.");
      fnGetOriginalDataList(PROJECT_SEQUENCE_PK);
      PAGE = PAGE - 1;
      pageInit();
    } else {
      fnComErrorMessage("원본데이터를 삭제 에러 \n" + response.detail.title, response.detail);
    }
  }
};

/*원본데이터 생성*/
var fnCreateOriginalData = function () {
  var localFile = TARGET.text;
  var data = {};

  if (TARGET == null) {
    fnComNotify("warning", "파일을 선택해주세요.");
    return;
  }

  if (TARGET.icon != "jstree-file") {
    fnComNotify("warning", "선택된" + localFile + "은 파일이 아닙니다.");
    return;
  }

  if (fnNotNullAndEmpty(localFile)) {
    if (confirm("생성하시겠습니까?")) {
      // TARGET_PATH는 dataManage.js 전역변수에 정의
      data["filepath"] = TARGET_PATH;
      data["filename"] = localFile;
      data["projectSequenceFk1"] = PROJECT_SEQUENCE_PK;
      data["instanceSequenceFk2"] = SELECTED_INSTANCE_PK;
      var response = fnCreateOriginalDataByAjax(PROJECT_SEQUENCE_PK, data);

      if (response.result == "success") {
        fnComNotify("success", "학습용 원본 데이터를 생성하였습니다.");
        $(".originModal").modal("hide"); // 원본리스트 가져오기

        fnGetOriginalDataList(PROJECT_SEQUENCE_PK);
      } else if (response.detail == "duplicateName") {
        fnComNotify("warning", localFile + "은 이미 존재합니다.");
      } else {
        fnComErrorMessage("원본데이터 생성 에러 \n" + response.detail.title, response.detail);
      }
    }
  } else {
    fnComNotify("warning", "샌드박스 로컬 파일을 선택해주세요.");
  }
};

/*전처리 모달 생성*/
var fnPreprocessingModal = function () {
  if (fnNotNullAndEmpty(SELECTED_ORIGINAL_DATA_PK)) {
    PREPROCESS_TEST_FLAG = false;

    // 초기화
    $("#preprocessListTbody").children().remove();
    $("#preprocessTest").text("");

    // 필드명 가져오기 (초보자용)
    fnGetColumnsForBeginner();

    // 필드명 가져오기 (전문가용)
    fnGetColumnsForExpert();

    // 처리방식 가져오기
    getPossiblePreProcess();
  } else {
    fnComNotify("warning", "원본데이터를 생성해주세요.");
  }
};

/**
 * 필드명 가져오기 (초보자용)
 */
var fnGetColumnsForBeginner = function () {
  var originalData = fnGetOriginalDataByAjax(PROJECT_SEQUENCE_PK, SELECTED_ORIGINAL_DATA_PK);
  var columns = originalData.statistic_info.value;
  columns.sort(customSort);

  var columnsTemplate = getColumnsTemplate(columns);
  $("#fieldsForBeginner").html(columnsTemplate);
}

/**
 * 필드명 화면 구성요소
 */
function getColumnsTemplate(columns) {
  var html = "";

  for (var i in columns) {
    html += "<tr class='column'>";
    html += "<th class='columnName'>" + columns[i].name + "</th>";
    html += "<th>";
    html += "<select class='form-control columnType'>";

    if (columns[i].type == "numerical") {
      html += "<option selected>numerical</option>";
      html += "<option>categorical</option>";
    } else {
      html += "<option>numerical</option>";
      html += "<option selected>categorical</option>";
    }

    html += "<option>drop</option>";
    html += "<option>사용 안 함</option>";
    html += "</select>";
    html += "</th>";
    html += "</tr>";
  }

  return html;
}

/**
 * JSONArray 맞춤 정렬
 * 
 * @param {JSONObject} a 
 * @param {JSONObject} b 
 */
function customSort(a, b) {
  if (a.name == b.name) {
    return 0;
  }

  return a.name > b.name ? 1 : -1;
}

/*필드명 가져오기 (전문가용)*/
var fnGetColumnsForExpert = function () {
  var originalData = fnGetOriginalDataByAjax(PROJECT_SEQUENCE_PK, SELECTED_ORIGINAL_DATA_PK);
  var columns = originalData.column_info.value;
  columns.sort();

  var html = "";
  for (var i in columns) {
    html += "<option>" + columns[i] + "</option>";
  }
  $("#columns").html(html);

  var sampleData = originalData.sample_data;
  var rowMaxLength = getRowMaxLength(sampleData.value);
  var sampleDataHtml = getColumnNames(sampleData.value);
  sampleDataHtml += getRows(rowMaxLength, sampleData.value);
  $("#sampleData").html(sampleDataHtml);

  $("#inputValue").val("");
};

/**
 * 최대 행 길이
 * 
 * @param {JSONObject} sampleData 
 */
var getRowMaxLength = function (sampleData) {
  for (var key in sampleData) {
    var size = 0;
    for (var value in sampleData[key]) {
      size++;
    }
    return size;
  }
}

/**
 * 컬럼 목록 구하기
 * 
 * @param {JSONObject} sampleData 
 */
var getColumnNames = function (sampleData) {
  var html = "";
  html += "<thead>";
  html += "<tr>"

  for (var key in sampleData) {
    html += "<th>" + key + "</th>";
  }

  html += "</tr>";
  html += "</thead>";
  return html;
}

/**
 * 행 요소 구하기
 * 
 * @param {Number} columnMaxLength 
 * @param {JSONObject} sampleData 
 */
var getRows = function (columnMaxLength, sampleData) {
  var html = "";
  html += "<tbody>";
  for (var i = 0; i < columnMaxLength; i++) {
    html += "<tr>";
    for (var key in sampleData) {
      html += "<td>" + sampleData[key][i] + "</td>";
    }
    html += "</tr>";
  }
  html += "</tbody>";
  return html;
}

/**
 * 사용가능한 전처리 가져오기
 * 
 * @returns
 */
function getPossiblePreProcess() {
  $("#preprocessFunction").html("");
  $("#inputValue").val("");
  var fieldName = $("#columns option:selected").val();
  $.ajax({
    type: "post",
    data: {
      "projectSequencePk": PROJECT_SEQUENCE_PK,
      "originalDataSequencePk": SELECTED_ORIGINAL_DATA_PK,
      "fieldName": fieldName
    },
    url: '/projects/getPossiblePrepro',
    dataType: "json",
    success: function success(data) {
      var libraryFunction;
      var possiblePreProcessTemplate = getPossiblePreProcessTemplate(data);
      $("#preprocessFunction").html(possiblePreProcessTemplate);

      if (data.possibleList.length > 0) {
        libraryFunction = fnGetPreprocessFunctionParameters(data.possibleList[0].id);
      }
      
      var text = "";
      var currentFunc = $("#preprocessFunction option:selected").text();
      text += currentFunc + ": " + libraryFunction;
      $("#preprocessDesc").text(text);
    },
    error: function error(request, _error) {
      console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error);
    },
    complete: function complete() { }
  });
}

/**
 * 사용 가능한 전처리 함수 화면 구성요소
 * 
 * @param {JSONObject} data 
 */
function getPossiblePreProcessTemplate(data) {
  var html = "";

  for (var i in data.possibleList) {
    html += "<option value=" + data.possibleList[i].id + ">" + data.possibleList[i].name + "</option>";
  }

  return html;
}

/*처리방식 가져오기 >> 사용가능한 처리방식으로 변경*/
var fnGetPreprocessFunction = function () {
  var preprocessFunctionList = fnGetPreprocessFunctionByAjax();
  var html = "";

  for (var i in preprocessFunctionList) {
    if (i == 0) {
      fnGetPreprocessFunctionParameters(preprocessFunctionList[i].id);
    }
    html += "<option value=" + preprocessFunctionList[i].id + "'>" + preprocessFunctionList[i].name + "</option>";
  }
};

/*파라미터 가져오기*/
var fnGetPreprocessFunctionParameters = function (preprocessFunctionSequencePk) {
  var preprocessFunctions = fnGetPreprocessFunctionParametersByAjax(preprocessFunctionSequencePk);
  var parameters = preprocessFunctions.parameter;
  var libraryFunction = preprocessFunctions.library_function_description;
  var html = "";
  var functionName = $("#preprocessFunction option:selected").text();
  var text = "";
  if (functionName != null) {
    var currentFunc = $("#preprocessFunction option:selected").text();
    text += currentFunc + ": " + libraryFunction + "\n";
  }

  for (var i in parameters) {
    if (fnNotNullAndEmpty(parameters[i].name)) {
      if (i == 0) fnSetParamType(parameters[i].type, parameters[i].enumerate, parameters[i].default);
      html += "<option value='" + parameters[i].type + "' data-note='" + parameters[i].note + "' data-enumerate='" + parameters[i].enumerate + "' data-default='" + parameters[i].default + "'>" + parameters[i].name + "</option>";
    }
  }

  if (html == "") {
    html = "<option value='none'>없음</option>";
    $("#valueLabel").hide();
    $("#selectType").hide();
    $("#inputType").hide();
  } else {
    text += parameters[0].name + ": " + parameters[0].note;
  }

  $("#parameters").html(html);
  $("#preprocessDesc").text(text);
  $("#inputValue").val("");

  return libraryFunction;
};

/*파라미터 타입별 세팅*/
var fnSetParamType = function (type, enumerate, defaultValue) {
  var html = "";

  if (type == "string") {
    var e = enumerate.split(",");

    for (var i in e) {
      html += "<option>" + e[i] + "</option>";
    }

    $("#selectValue").html(html);
    $("#inputType").hide();
    $("#selectType").fadeIn();
    $("#valueLabel").fadeIn();
  } else if (type == "bool") {
    html += "<option>true</option><option>false</option>";
    $("#selectValue").html(html);
    $("#inputType").hide();
    $("#selectType").fadeIn();
    $("#valueLabel").fadeIn();
  } else if (type == "none") {
    $("#selectType").hide();
    $("#inputType").hide();
    $("#valueLabel").hide();
  } else {
    $("#inputValue").prop("placeholder", fnSetParamTypePlaceholder(type)); // 타입별 예제 적용

    $("#selectType").hide();
    $("#inputType").fadeIn();
    $("#valueLabel").fadeIn();
  }

  var text = $("#preprocessDesc").text();
  text = text.split("\n");
  var context = "";
  context += text[0] + "\n";
  context += $("#parameters option:selected").text() + ": " + $("#parameters option:selected").data("note");
  $("#preprocessDesc").text(context);
};

/*타입별 예제 적용*/
var fnSetParamTypePlaceholder = function (type) {
  var placeholder = "";

  switch (type) {
    case "int":
      placeholder = '예) 5';
      break;

    case "float":
      placeholder = '예) 5.5';
      break;

    case "numerical":
      placeholder = '예) 5.5';
      break;

    case "string, numerical":
      placeholder = '예) String or 8';
      break;

    case "numerical, string, np.nan":
      placeholder = '예) String or 8.8 ';
      break;

    case "string, int, array":
      placeholder = '예) String or 1 or [String,String] or [3,5]';
      break;

    case "string, array":
      placeholder = '예) String or [String,String]';
      break;

    case "string, list of ints, array":
      placeholder = '예) String or [3,5] or [String,String] or [3,5]';
      break;

    case "string, list, array":
      placeholder = '예) String or [String,String]';
      break;

    case "string, list of lists, array":
      placeholder = '예) String or [String,String] or [[String,String],[String,String]]';
      break;

    case "array-like":
      placeholder = '예) [String,String] or [[String,String],[String,String]]';
      break;

    case "int, array-like":
      placeholder = '예) 2 or [3,5]';
      break;

    case "string, sparse matrix":
      placeholder = '예) [[String,String],[String,String]]';
      break;

    case "array":
      placeholder = '예) [3]';
      break;

    case "tuple":
      placeholder = '예) (0,1)';
      break;

    case "int, string, list":
      placeholder = '예) 2 or String or [3,5] or [String,String]';
      break;

    case "int, list":
      placeholder = '예) 2 or [3,5]';
      break;

    case "string, list":
      placeholder = '예) String or [String,String]';
      break;

    case "int, string":
      placeholder = '예) 2 or String';
      break;

    default:
      "";
      break;
  }

  return placeholder;
};

/*선택된 전처리 리스트에 담기*/
var fnAddParams = function () {
  var type = $("#parameters option:selected").val();
  var value = "";
  
  if (type == "string" || type == "bool") {
    value = $("#selectValue option:selected").val();
  } else {
    value = $("#inputValue").val();
  }
  
  if (fnCheckAddParam()) {
    if (type == "none" || value != "") {
      // 타입에 맞는지 체크
      if (fnCheckTypeValue(type, value)) {
        var html = getSelectedPreprocesses(value, type);
        $("#preprocessListTbody").append(html);

        $("#preprocessTest").text("");
        PREPROCESS_TEST_FLAG = false;
      } else {
        fnComNotify("warning", "Value의 형식이 다릅니다. 예를 참고해서 작성해주세요.");

        $("#inputValue").focus();
      }
    } else {
      fnComNotify("warning", "Value에 값을 입력해주세요.");
      $("#inputValue").focus();
    }
  } else {
    fnComNotify("warning", "이미 등록되어 있습니다.");
  }
};

/**
 * 선택된 전처리 함수 화면 구성요소
 */
function getSelectedPreprocesses(value, type) {
  var html = '<tr>';

  html += '<td>' + $("#columns option:selected").val() + '</td>';
  html += '<td data-preprocessPk="' + $("#preprocessFunction option:selected").val() + '">' + $("#preprocessFunction option:selected").text() + '</td>';
  html += '<td data-type="' + type + '">' + $("#parameters option:selected").text() + '</td>';
  html += '<td>' + value + '</td>';
  html += '<td><button type="button" class="btn btn-info btn-xs va_2 deletePreprocessBtn"><i class="fa fa-trash-o"> 삭제</i></button></td>';

  return html;
}

/*전처리 리스트에 담기 체크*/
var fnCheckAddParam = function () {
  var result = true;
  var fildName = $("#columns option:selected").val();
  var functionName = $("#preprocessFunction option:selected").text();
  var functionParam = $("#parameters option:selected").text();
  $("#preprocessListTbody").find("tr").each(function () {
    if (fildName == $(this).children().eq(0).text() && functionName == $(this).children().eq(1).text() && functionParam == $(this).children().eq(2).text()) {
      result = false;
    }
  });
  return result;
};

/*전처리 테스트*/
var fnPreprocessTest = function () {
  var paramData = {};
  PREPROCESS_REQUEST_TEST = [];

  if ($("#preprocess_beginner").hasClass("active")) {
    paramData["request_test"] = getRequestDataForBeginner();
    paramData["beginner"] = true;
    requestTestData(paramData);
    return;
  }

  paramData = {};
  $("#preprocessListTbody").find("tr").each(function () {
    var data = {};
    var condition = {};
    var conditionKey = "";
    var conditionValue = "";

    data["field_name"] = $(this).children().eq(0).text();
    data["preprocess_function_id"] = $(this).children().eq(1).attr("data-preprocessPk");
    if ("없음" != $(this).children().eq(2).text()) conditionKey = $(this).children().eq(2).text();

    if ("" != conditionKey) {
      conditionValue = $(this).children().eq(3).text();
      condition[conditionKey] = conditionValue;
      data["condition"] = condition;
    }

    /*같은 파라미터 병합*/
    for (var i in PREPROCESS_REQUEST_TEST) {
      var tempData = PREPROCESS_REQUEST_TEST[i];

      if (tempData.preprocess_function_id == data.preprocess_function_id && tempData.field_name == data.field_name) {
        var tempCondition = tempData.condition;

        if ("" != conditionKey) {
          tempCondition[conditionKey] = conditionValue;
          tempData["condition"] = tempCondition;
          PREPROCESS_REQUEST_TEST[i] = tempData;
          data = null;
        }
      }
    }

    if (data != null) PREPROCESS_REQUEST_TEST.push(data);
  });
  paramData["request_test"] = PREPROCESS_REQUEST_TEST;

  if (PREPROCESS_REQUEST_TEST.length > 0) {
    requestTestData(paramData);
  } else {
    fnComNotify("warning", "전처리를 추가해주세요.");
  }
};

/**
 * 테스트 요청
 * 
 * @param {JSONObject} paramData 
 */
var requestTestData = function (paramData) {
  var response = fnPreprocessTestByAjax(PROJECT_SEQUENCE_PK, SELECTED_ORIGINAL_DATA_PK, paramData);
  if (response.result == "success") {
    var rowMaxLength = getRowMaxLength(response.data);
    var sampleDataHtml = getColumnNames(response.data);
    sampleDataHtml += getRows(rowMaxLength, response.data);

    $("#preprocessTest").html(sampleDataHtml);
    fnComNotify("success", "테스트를 완료하였습니다.");
    PREPROCESS_TEST_FLAG = true;
  } else {
    $("#preprocessTest").text("");
    PREPROCESS_TEST_FLAG = false;
    fnComErrorMessage("전처리 테스트 에러 \n" + response.detail.title, response.detail);
  }
}

/*전처리 생성 (전문가용) */
var fnCreatePreprocess = function () {
  if (!PREPROCESS_TEST_FLAG) {
    fnComNotify("warning", "전처리 테스트 후 생성 가능합니다.");
    return;
  }

  var paramData;

  if ($("#preprocess_beginner").hasClass("active")) {
    paramData = {
      "beginner": true,
      "project_id": PROJECT_SEQUENCE_PK,
      "original_data_id": SELECTED_ORIGINAL_DATA_PK,
      "request_data": getRequestDataForBeginner()
    };
  } else {
    paramData = {
      "beginner": false,
      "project_id": PROJECT_SEQUENCE_PK,
      "original_data_id": SELECTED_ORIGINAL_DATA_PK,
      "request_data": PREPROCESS_REQUEST_TEST
    };
  }
  fnPreprocess(paramData);
};

/**
 * 전처리용 파라미터 가져오기 (초보자용)
 */
var getRequestDataForBeginner = function () {
  var datas = [];
  var columns = document.querySelectorAll("#fieldsForBeginner .column");

  $(columns).each(function (i) {
    var data = {};
    data.field_name = columns[i].querySelector(".columnName").innerText;

    var type = columns[i].querySelector(".columnType");
    data.preprocess_function_type = type.options[type.selectedIndex].value;

    if (data.preprocess_function_type == "사용 안 함") {
      return true;
    }
    datas.push(data);
  })

  return datas;
}

/**
 * 전처리 로직 이후 전처리 모달 처리
 * 
 * @param {JSONObject} paramData 
 */
var fnPreprocess = function (paramData) {
  if (!confirm("전처리를 시작하시겠습니까?")) {
    return;
  }

  var response = fnCreatePreprocessByAjax(PROJECT_SEQUENCE_PK, paramData);
  if (response.result == "success") {
    // 전처리 리스트 조회
    fnGetPreprocessedDataList();

    // 모델 리스트 조회
    fnGetModelList();
    $("#preprocessingModal").modal("hide");
  } else {
    fnComErrorMessage("전처리 생성 에러 \n" + response.detail.title, response.detail);
  }
}

/*원본데이터 차트*/
var fnOriginalChart = function (param, idx) {
  am4core.ready(function () {
    am4core.options.commercialLicense = true;
    var originalDataChart;

    if (param.graph_type == "histogram" || param.graph_type == "bar") {
      /* ----- 원본데이터 분석 그래프 Start -----------------------------------------------------------*/
      // Themes begin
      am4core.useTheme(am4themes_animated); // Themes end
      // Create chart instance

      originalDataChart = am4core.create("originalDataChartDiv" + idx, am4charts.XYChart); //originalDataChart.scrollbarX = new am4core.Scrollbar();

      var binsMeans;
      if ("bar" == param.graph_type) binsMeans = param.compact_data.elements; else binsMeans = param.compact_data.bins_means;
      var frequency = param.compact_data.frequency;
      var list = [];

      for (var i in binsMeans) {
        var data = {
          "범주값": binsMeans[i],
          "frequency": frequency[i]
        };
        list.push(data);
      }

      originalDataChart.data = list; // Create axes

      var categoryAxis = originalDataChart.xAxes.push(new am4charts.CategoryAxis());
      categoryAxis.dataFields.category = "범주값";
      categoryAxis.title.text = "범주값";
      categoryAxis.renderer.grid.template.location = 0;
      categoryAxis.renderer.minGridDistance = 80;
      categoryAxis.renderer.labels.template.horizontalCenter = "right";
      categoryAxis.renderer.labels.template.verticalCenter = "middle";
      categoryAxis.renderer.labels.template.rotation = 0;
      categoryAxis.tooltip.disabled = true;
      categoryAxis.renderer.minHeight = 50;
      var valueAxis = originalDataChart.yAxes.push(new am4charts.ValueAxis());
      valueAxis.renderer.minWidth = 50;
      valueAxis.title.text = "frequency"; // Create series

      var series = originalDataChart.series.push(new am4charts.ColumnSeries());
      series.sequencedInterpolation = true;
      series.dataFields.valueY = "frequency";
      series.dataFields.categoryX = "범주값";
      series.tooltipText = "[{categoryX}: bold]{valueY}[/]";
      series.columns.template.strokeWidth = 0;
      series.tooltip.pointerOrientation = "vertical";
      series.columns.template.column.cornerRadiusTopLeft = 10;
      series.columns.template.column.cornerRadiusTopRight = 10;
      series.columns.template.column.fillOpacity = 0.8; // on hover, make corner radiuses bigger

      var hoverState = series.columns.template.column.states.create("hover");
      hoverState.properties.cornerRadiusTopLeft = 0;
      hoverState.properties.cornerRadiusTopRight = 0;
      hoverState.properties.fillOpacity = 1;
      series.columns.template.adapter.add("fill", function (fill, target) {
        return originalDataChart.colors.getIndex(target.dataItem.index);
      }); // Cursor

      originalDataChart.cursor = new am4charts.XYCursor();
      /* ----- 원본데이터 분석 그래프 End -----------------------------------------------------------*/
    } else if (param.graph_type == "pie") {
      am4core.useTheme(am4themes_animated);
      originalDataChart = am4core.create("originalDataChartDiv" + idx, am4charts.PieChart);
      originalDataChart.scrollbarX = new am4core.Scrollbar();

      var binsMeans = param.compact_data.elements;
      var frequency = param.compact_data.frequency;
      var list = [];

      for (var i in binsMeans) {
        var data = {
          "범주값": binsMeans[i],
          "frequency": frequency[i]
        };
        list.push(data);
      }

      originalDataChart.data = list;

      var pieSeries = originalDataChart.series.push(new am4charts.PieSeries());
      pieSeries.dataFields.value = "frequency";
      pieSeries.dataFields.category = "범주값";
      pieSeries.slices.template.stroke = am4core.color("#fff");
      pieSeries.slices.template.strokeWidth = 2;
      pieSeries.slices.template.strokeOpacity = 1;

      pieSeries.hiddenState.properties.opacity = 1;
      pieSeries.hiddenState.properties.endAngle = -90;
      pieSeries.hiddenState.properties.startAngle = -90;
    }

    ORG_DATACHART_ARR.push(originalDataChart);
  });
};

/**
 * page 초기 설정
 */
var pageInit = function () {
  if (PAGE != 1) {
    $(".buttonPrevious").removeClass("buttonDisabled");
  } else {
    $(".buttonPrevious").addClass("buttonDisabled");
  }

  if (PAGE == 4) {
    $(".buttonNext").addClass("buttonDisabled");
  } else {
    $(".buttonNext").removeClass("buttonDisabled");

  }
}

/**
 * previous 버튼 눌렀을 시 효과
 */
var movePrevious = function () {
  if (PAGE == 4) {
    $("#modelDiv").hide();
    $("#step-3").show();
    $(".buttonNext").removeClass("buttonDisabled");
    PAGE = 3;
  } else if (PAGE == 3) {
    $("#step-3").hide();
    $("#step-2").show();
    PAGE = 2;
  } else if (PAGE == 2) {
    $("#step-2").hide();
    $("#step-1").show();
    $(".buttonPrevious").addClass("buttonDisabled");
    PAGE = 1;
  }
}

/**
 * Next 버튼 눌렀을 시 효과
 */
var moveNext = function () {
  if (PAGE == 1) {
    $("#step-1").hide();
    $("#step-2").show();
    $(".buttonPrevious").removeClass("buttonDisabled");
    PAGE = 2;
  } else if (PAGE == 2) {
    $("#step-2").hide();
    $("#step-3").show();
    PAGE = 3;
  } else if (PAGE == 3) {
    $("#step-3").hide();
    if (SELECTED_MODEL_PK != null) {
      $("#modelDiv").show();
    }
    PAGE = 4;
    $(".buttonNext").addClass("buttonDisabled");
  }
}
