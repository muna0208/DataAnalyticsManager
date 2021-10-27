"use strict";

var PREPROCESSED_INTERVAL_ID = 0;
var SELECTED_PREPROCESSED_NAME, SELECTED_PREPROCESSED_DATA_PK;
var PREPRO_STATISTICS;
var MODLE_PARAMS, TRAIN_PARAMS;
var PP_DATACHART_ARR = new Array();

$(function () {
  // 전처리 리스트 클릭시
  $(document).on("click", ".preprocessedData", function () {
    /*샌드박스 파일브라우저 샘플 미리보기*/
    $(".preprocessedData").removeClass("active");
    $(this).addClass("active");
    SELECTED_PREPROCESSED_DATA_PK = $(this).attr("id");
    SELECTED_PREPROCESSED_NAME = $(this)[0].innerText;
    fnGetPreprocessedData();
  });

  /*학습 모달 클릭시 */
  $(document).on("click", "#learningModalBtn", function () {
    $('a[href="#tab_algorithm"]').click();
    fnSearchAlgorithm();
    setTrainParams();
  });

  /*학습 모달 알고리즘 조회*/
  $(document).on("click", "#searchAlgorithm", function () {
    fnSearchAlgorithm();
  });

  /**
   * 검색
   */
  $("#searchAlgorithmValue").bind("keypress", function (e) {
    if (e.which == 13) fnSearchAlgorithm();
  });

  /*학습 모달 알고리즘 선택시*/
  $("#searchAlgorithmList").on("change", function () {

    /*파라미터 설정 (초보자용)*/
    fnSetAlgorithmParamsForBeginner(JSON.parse($("#searchAlgorithmList option:selected").attr("data-algorithm")));
  });

  /*학습 모달 알고리즘 선택시*/
  $("#searchAlgorithmList2").on("change", function () {

    /*파라미터 설정 (초보자용)*/
    fnSetAlgorithmParamsForExpert(JSON.parse($("#searchAlgorithmList2 option:selected").attr("data-algorithm")));
  });
});

/*전처리 목록 가져오기*/
var fnGetPreprocessedDataList = function () {
  var html = "";

  if (fnNotNullAndEmpty(SELECTED_ORIGINAL_DATA_PK)) {
    var preprocessedDataList = fnGetPreprocessedDataListByAjax(SELECTED_ORIGINAL_DATA_PK);
    /*전처리 리스트 가져오기*/

    for (var i in preprocessedDataList) {
      var pData = preprocessedDataList[i];

      if (i == 0) {
        html += "<li class='pdTB_5 preprocessedData active' role='button'" + " id=" + pData.id + ">" + pData.name + "</li>";
        SELECTED_PREPROCESSED_NAME = pData.name;
        SELECTED_PREPROCESSED_DATA_PK = pData.id;
        fnGetPreprocessedData(pData);
        /* 전처리데이터 가져오기*/
      } else {
        html += "<li class='pdTB_5 preprocessedData' role='button' " + "id=" + pData.id + ">" + pData.name + "</li>";
      }
    }

    if (preprocessedDataList.length > 0) {
      $("#step-2").hide();
      $("#step2").removeClass("selected");
      $("#step2").addClass("done");
    } else {
      $("#step-2").show();
      $("#step2").removeClass("done");
      $("#step2").addClass("selected");
      $("#step-3").hide();
      $("#step3").removeClass("selected");
      $("#step3").removeClass("done");
      $("#step3").addClass("disabled");
    }
    $("#step2").removeClass("disabled");
  }

  if (html == "") {
    html += "<li>전처리 데이터가 없습니다.</li>";
    SELECTED_PREPROCESSED_DATA_PK = "";
    $("#preprocessedDiv").hide(); // 전처리 숨김
    $("#preprocessedDataDiv").hide();

    // 모델 리스트 가져오기
    fnGetModelList();

    // 종료
    if (PREPROCESSED_INTERVAL_ID != 0) clearInterval(PREPROCESSED_INTERVAL_ID);
  } else {
    $("#deletePreprocessedDataBtn").show();
    $("#preprocessedDiv").fadeIn(); // 전처리 숨김
    if (3 > PAGE) {
      PAGE = 3;
    }
    pageInit();
  }


  $("#preprocessedDataList").html(html);
  $("#loading").hide();
};

/* 전처리데이터 가져오기*/
var fnGetPreprocessedData = function (pData) {
  var requestData;
  var statisticsInfo;

  if (pData == null) {
    pData = fnGetPreprocessedDataByAjax(SELECTED_ORIGINAL_DATA_PK, SELECTED_PREPROCESSED_DATA_PK);
    requestData = JSON.parse(pData.command.value).request_data;
    statisticsInfo = JSON.parse(pData.statistic_info.value);
  } else if (pData.sample_Data == null) {
    requestData = pData.command.value.request_data;
    if (pData.statistic_info != null) {
      statisticsInfo = pData.statistic_info.value;
    }
  }

  $(".pDataName").text(pData.name);
  $("#preprocessedDetailDiv").hide();
  $("#preprocessedChartDiv").hide();
  $("#learningModalBtn").hide();
  $("#preprocessedDataShow").hide();
  if (PREPROCESSED_INTERVAL_ID != 0) clearInterval(PREPROCESSED_INTERVAL_ID);

  /* 전처리 상태값 체크 후 변경 */
  if ("ongoing" == pData.progress_state) {
    var html = "";
    html += "	<div class='progress mgBottom_0'>" ;
    html += "				<div class='progress-bar progress-bar-striped width_100p active' role='progressbar'>생성중</div></div>";
    $("#pDataFilename").html(html);
    $("#pDataFilepath").html(html);
    $("#pDataAmount").html(html);
    $("#pDataCreateDatetime").text(pData.create_date);
    PREPROCESSED_INTERVAL_ID = setInterval(fnChangePreprocessedState, 5000);
  } else if ("fail" == pData.progress_state) {
    $("#pDataFilename").html("실패");
    $("#pDataFilepath").html("실패");
    $("#pDataAmount").html("실패");
    $("#pDataCreateDatetime").text("실패");
  } else {
    // 성공
    pData = fnGetPreprocessedDataByAjax(SELECTED_ORIGINAL_DATA_PK, SELECTED_PREPROCESSED_DATA_PK);

    var pSampleDataHtml = makePDataTable(JSON.parse(pData.sample_data.value));
    $("#pDataTable").html(pSampleDataHtml);
    $("#preprocessedDataDiv").fadeIn();
    $("#preprocessSampleData").html(pSampleDataHtml);
    $("#pDataFilename").text(pData.filename);
    $("#pDataFilepath").text(pData.filepath);
    $("#pDataAmount").text(numberWithCommas(pData.amount));
    $("#pDataCreateDatetime").text(pData.create_date);
    $("#preprocessedDetailDiv").fadeIn();
    $("#preprocessedChartDiv").fadeIn();
    $("#learningModalBtn").show();
    $("#preprocessedDataShow").show();
    $("#preprocessedDiv").fadeIn(); // 전처리 프로세스 생성

    fnSetRequestData(requestData); // 차트 설정
    fnSetStatistics(statisticsInfo); // 모델 리스트 가져오기
    fnGetModelList();
  }
};

/**
 * 전처리 데이터 미리보기 테이블
 * 
 * @param {JSONObject} columns 
 */
var makePDataTable = function (columns) {
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

/*차트 설정*/
var fnSetStatistics = function (statistics) {
  var html = "";

  for (var i in statistics) {
    if ("datetime" == statistics[i].name) {
      continue;
    }

    html += "<div class=\"overY_scrollauto\">";
    html += "<div class=\"col-md-8 col-sm-4 col-xs-12\">";
    html += "	<div class=\"x_panel height_320\">";
    html += "		<div class=\"con_title\">";
    html += "			<p class=\"col-md-9\">" + statistics[i].name + "</p>";
    html += "		<button class=\"btn btn-default btn-xs col-md-3\">" + statistics[i].type + "</button>";
    html += "	</div>";
    html += "		<div class=\"con_content height_270\" id=\"preprocessedDataChartDiv" + i + "\">";
    html += "		</div>";
    html += "</div>";
    html += "</div>";
    html += "<div class=\"col-md-4 col-sm-4 col-xs-12\">";
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

  $("#preDataShow").html(html);
  $("#preName").html(SELECTED_PREPROCESSED_NAME); //전처리차트 전역변수

  PREPRO_STATISTICS = statistics;
};

/*전처리 데이터 보기 팝업 클릭시 차트 생성*/
function fnDrawPreproChart() {
  //	순차적으로 불러오기
  if (PP_DATACHART_ARR.length != 0) return;

  for (var i in PREPRO_STATISTICS) {
    (function (ii) {
      setTimeout(function () {
        if ("datetime" != PREPRO_STATISTICS[ii].name) {
          fnPreprocessedDataChart(PREPRO_STATISTICS[ii], ii);
        }
      }, ii * 400);
    })(i);
  }
}

/*전처리 프로세스 생성*/
var fnSetRequestData = function (requestData) {
  // 전처리 함수 가져오기
  var preprocessFunctionList = fnGetPreprocessFunctionByAjax();
  var functionName = "";
  var html = "";

  html += "<thead>";
  html += "  <tr>";
  html += "    <th>필드</th>";
  html += "    <th>처리방식</th>";

  if (requestData[0].preprocess_function_type == null) {
    html += "    <th>Parameter</th>";
    html += "    <th>Value</th>";
  }

  html += "  </tr>";
  html += "</thead>";
  html += "<tbody>";


  var _loop = function _loop(i) {
    var beginner = false;

    html += "<tr><td>" + requestData[i].field_name + "</td>";

    for (var f in preprocessFunctionList) {
      if (requestData[i].preprocess_function_id == preprocessFunctionList[f].id) {
        functionName = preprocessFunctionList[f].library_function_name;
      } else if (requestData[i].preprocess_function_type != null) {
        functionName = requestData[i].preprocess_function_type;
        beginner = true;
      }
    }

    html += "<td>" + functionName + "</td>";

    if (!beginner) {
      if (fnNotNullAndEmpty(requestData[i].condition)) {
        var condition = requestData[i].condition;
        var cnt = 0;
        $.each(condition, function (index, value) {
          cnt++;

          if (cnt == 1) {
            html += "<td>" + index + "</td>";
            html += "<td>" + value + "</td></tr>";
          } else {
            html += "<tr><td>" + requestData[i].field_name + "</td>";
            html += "<td>" + functionName + "</td>";
            html += "<td>" + index + "</td>";
            html += "<td>" + value + "</td></tr>";
          }
        });
      } else {
        html += "<td>없음</td>";
        html += "<td></td>";
      }
    }
    html += "</tr>";
  };

  for (var i in requestData) {
    _loop(i);
  }
  html += "</tbody>";

  $("#requestDataTable").html(html);
};

/*전처리셋 상태값 변경*/
var fnChangePreprocessedState = function () {
  var pData = fnGetPreprocessedDataByAjax(SELECTED_ORIGINAL_DATA_PK, SELECTED_PREPROCESSED_DATA_PK);

  if ("success" == pData.progress_state) {
    var pSampleDataHtml = makePDataTable(JSON.parse(pData.sample_data.value));
    $("#pDataTable").html(pSampleDataHtml);
    $("#preprocessSampleData").html(pSampleDataHtml);
    $("#pDataFilename").text(pData.filename);
    $("#pDataFilepath").text(pData.filepath);
    $("#pDataAmount").text(numberWithCommas(pData.amount));
    $("#preprocessedDetailDiv").fadeIn();
    $("#preprocessedChartDiv").fadeIn();
    $("#preprocessedDataDiv").fadeIn();
    $("#learningModalBtn").fadeIn(); // 전처리 프로세스 생성
    $("#preprocessedDataShow").fadeIn();

    var requestData = JSON.parse(pData.command.value).request_data;
    fnSetRequestData(requestData); // 차트 설정

    fnSetStatistics(JSON.parse(pData.statistic_info.value)); // 종료
    fnComNotify("success", "전처리 생성을 완료하였습니다.");
    clearInterval(PREPROCESSED_INTERVAL_ID);
  } else if ("fail" == pData.progress_state) {
    $("#pDataFilename").text("실패");
    $("#pDataFilepath").text("실패");
    $("#pDataAmount").text("실패");
    $("#preprocessedDetailDiv").hide();
    $("#preprocessedChartDiv").hide();
    $("#learningModalBtn").hide();
    $("#preprocessedDataShow").hide();
    fnComNotify("error", "전처리 생성을 실패 하였습니다. 관리자에게 문의해주세요.");
    clearInterval(PREPROCESSED_INTERVAL_ID);
  }
};

/*전처리 삭제*/
var fnDeletePreprocessedData = function () {
  if (confirm("전처리 데이터를 삭제하면 생성된 모델이 같이 삭제됩니다. \n삭제하시겠습니까?")) {
    var response = fnDeletePreprocessedDataByAjax(PROJECT_SEQUENCE_PK, SELECTED_PREPROCESSED_DATA_PK);

    if (response.result == "success") {
      fnComNotify("success", "전처리 데이터를 삭제 하였습니다."); // 전처리 리스트 가져오기
      fnGetPreprocessedDataList();
      PAGE = PAGE - 1;
      pageInit();
    } else {
      fnComErrorMessage("전처리 데이터를 삭제 에러 \n" + response.detail.title, response.detail);
    }
  }
};

/*학습 모달 알고리즘 조회*/
var fnSearchAlgorithm = function () {
  var algorithms = fnSearchAlgorithmByAjax($("#searchAlgorithmValue").val(), $("#projectType").val());
  var html = "";

  for (var i in algorithms) {
    if (i == 0) {
      fnSetAlgorithmParamsForExpert(algorithms[i]);
      fnSetAlgorithmParamsForBeginner(algorithms[i]);
    }
    html += "<option class='algorithm' data-algorithm='" + JSON.stringify(algorithms[i]) + "' role='button'>" + "<a>" + algorithms[i].library_function_name + "</a></option>";
  }

  $("#searchAlgorithmList, #searchAlgorithmList2").html(html);
};

/**
 * 학습 파라미터 세팅
 */
var setTrainParams = function () {
  var pData = fnGetPreprocessedDataByAjax(SELECTED_ORIGINAL_DATA_PK, SELECTED_PREPROCESSED_DATA_PK);
  var statistics = JSON.parse(pData.statistic_info.value);
  statistics.sort(customSort);
  var params = getTrainParamsTemplates(statistics);

  $("#xParameters").html(params[0]);
  $("#yParameters").html(params[1]);

  $('.form-check-input, .flat').iCheck({
    checkboxClass: 'icheckbox_flat-green',
    radioClass: 'iradio_flat-green'
  });
  checkAll();
  freeCheckAll();
}

/**
 * 학습 파라미터 화면 구성요소
 * 
 * @param {JSONObject} statistics 
 */
function getTrainParamsTemplates(statistics) {
  var xParams = "<tr class='text-left'>";
  var yParams = "<tr class='text-left'>";

  for (var i in statistics) {
    var key = statistics[i].name;

    xParams += "<td class='ta_center'>";
    xParams += "	<input type='checkbox' class='flat' name='xParams'  id='xParams" + i + "' value='" + key + "'>";
    xParams += "	<label class='form-check-label pdRL_10' for='xParams" + i + "'>" + key + "</label>";
    xParams += "</td>";

    yParams += "<td class='ta_center'>";;
    yParams += "	<input type='radio' class='flat' name='yParams'  id='yParams" + i + "' value='" + key + "'>";
    yParams += "	<label class='form-check-label pdRL_10' for='yParams" + i + "' >" + key + "</label>";
    yParams += "</td>";
  }
  xParams += "</tr>";
  yParams += "</tr>";

  var params = [];
  params.push(xParams);
  params.push(yParams);

  return params;
}

/*파라미터 설정 (전문가용)*/
var fnSetAlgorithmParamsForExpert = function (algorithm) {
  // 알고리즘 선택 결과
  var result = "";
  result += "Algorithm: " + algorithm.name + "\nLibrary: " + algorithm.library_name + "\nUsage: " + algorithm.library_function_name + "\nInfomation: " + algorithm.library_function_description;
  $("#selectedAlgorithmResult2").text(result);

  setModelParams(algorithm);
}

// 모델 파라미터 설정
var setModelParams = function (algorithm) {
  var modelParams = algorithm.model_parameter;
  var mParamHtml = "";

  for (var i in modelParams) {
    var mp = modelParams[i];
    mParamHtml += "<tr>";
    mParamHtml += "	<td>" + mp.name + "</td>";
    mParamHtml += "	<td>" + mp.type + "</td>";
    mParamHtml += "	<td>" + mp.default + "</td>";
    if (mp.type == "int") mParamHtml += "	<td><input type='text' class='form-control' onkeydown='return fnOnlyNumber();' onkeyup='fnRemoveChar(event);' placeholder='" + mp.default + "' ></td>"; else if (mp.type == "float") mParamHtml += "	<td><input type='text' class='form-control' onkeydown='return fnOnlyNumberDot();' onkeyup='fnRemoveChar(event);' placeholder='" + mp.default + "' ></td>"; else mParamHtml += "	<td><input type='text' class='form-control' placeholder='" + mp.default + "' ></td>";
    mParamHtml += "</tr>";
  }

  $("#modelParams").html(mParamHtml);
}

/*파라미터 설정 (초보자용)*/
var fnSetAlgorithmParamsForBeginner = function (algorithm) {

  // 알고리즘 선택 결과
  var result = "";
  result += "Algorithm: " + algorithm.name + "\nLibrary: " + algorithm.library_name + "\nUsage: " + algorithm.library_function_name + "\nInfomation: " + algorithm.library_function_description;
  $("#selectedAlgorithmResult").text(result);

  // 학습 파라미터 설정
  var trainParams = algorithm.train_parameter;
  var pData = fnGetPreprocessedDataByAjax(SELECTED_ORIGINAL_DATA_PK, SELECTED_PREPROCESSED_DATA_PK);
  var statistics = JSON.parse(pData.statistic_info.value);
  
  var tParamYTemplate = getTrainParamYTemplate(statistics);
  var tParamXTemplate = getTrainParamXTemplate(statistics);
  
  var tParamTemplate = getTrainParamTemplate(trainParams, tParamXTemplate, tParamYTemplate);

  setModelParams(algorithm);
  $("#trainParams").html(tParamTemplate);
  $("#libraryName").text(algorithm.library_name);
  $("#algorithmName").text(algorithm.name);
};

/**
 * 학습 파라미터 Y 화면 구성요소
 * 
 * @param {JSONObject} statistics 
 */
function getTrainParamYTemplate(statistics) {
  var tParamYHtml = "<div class=\"form-check\">";

  for (var i in statistics) {
    tParamYHtml += "<span class='floatL'>";
    tParamYHtml += "	<input type=\"radio\" class=\"flat\" name=\"yParamCheckbox\"  id=\"yParamCheckbox" + i + "\" value=\"" + statistics[i].name + "\">";
    tParamYHtml += "	<label class=\"form-check-label pdright_10\" for=\"yParamCheckbox" + i + "\">" + statistics[i].name + "</label>";
    tParamYHtml += "</span>";
  }
  tParamYHtml += "</div>";

  return tParamYHtml;
}

/**
 * 학습 파라미터 X 화면 구성요소
 * 
 * @param {JSONObject} statistics 
 */
function getTrainParamXTemplate(statistics) {
  var tParamXHtml = "	<div class=\"form-check form-check-inline\">";

  tParamXHtml += "<p>";
  tParamXHtml += "	<input type=\"checkbox\" class=\"flat\" name=\"xParamAll\" id='check_all'>";
  tParamXHtml += "	<label class=\"form-check-label pdright_10 pdLeft_5 fontWeight_bold\" for=\"xParamAll\">전체 선택</label>";
  tParamXHtml += "</p>";

  for (var i in statistics) {
    tParamXHtml += "<span class='floatL'>";
    tParamXHtml += "	<input class=\"form-check-input\" type=\"checkbox\" name=\"xParamCheckbox\" id=\"xParamCheckbox" + i + "\" value=\"" + statistics[i].name + "\">";
    tParamXHtml += "	<label class=\"form-check-label pdright_10 pdLeft_5\" for=\"xParamCheckbox" + i + "\">" + statistics[i].name + "</label>";
    tParamXHtml += "</span>";
  }
  tParamXHtml += "</div>";

  return tParamXHtml;
}

/**
 * 학습 파라미터 화면 구성요소
 * 
 * @param {JSONObject} statistics 
 */
function getTrainParamTemplate(trainParams, tParamX, tParamY) {
  var tParamHtml = "";
  for (var _i3 in trainParams) {
    var tp = trainParams[_i3];
    tParamHtml += "<tr>";
    tParamHtml += "	<td>" + tp.name + "</td>";
    tParamHtml += "	<td>" + tp.type + "</td>";
    tParamHtml += "	<td>" + tp.default + "</td>";

    if ("y" == tp.name) {
      tParamHtml += "	<td>" + tParamY + "</td>";
    } else if ("X" == tp.name) {
      tParamHtml += "	<td>" + tParamX + "</td>";
    } else {
      tParamHtml += "	<td><input type='text' class='form-control' onkeyup='fnRemoveChar(event);' placeholder='" + tp.default + "'></td>";
    }
    
    tParamHtml += "</tr>";
  }

  return tParamHtml;
}

/*모델 생성*/
var fnCreateModel = function () {
  if (!preCreateModel()) {
    return;
  }

  var data = {};
  var trainData = {};
  var algorithm;
  var algorithmsSequencePk;

  if ($("#algorithm-tab").closest("li").hasClass("active")) {
    algorithm = JSON.parse($("#searchAlgorithmList option:selected").attr("data-algorithm"));
  } else {
    algorithm = JSON.parse($("#searchAlgorithmList2 option:selected").attr("data-algorithm"));
  }
  algorithmsSequencePk = algorithm.id;

  if (fnNotNullAndEmpty(algorithmsSequencePk)) {
    data["project_id"] = PROJECT_SEQUENCE_PK;
    data["algorithm_id"] = algorithmsSequencePk;
    trainData["preprocessed_data_id"] = SELECTED_PREPROCESSED_DATA_PK;
    data["train_data"] = trainData;
    data["model_parameters"] = MODLE_PARAMS;
    data["train_parameters"] = TRAIN_PARAMS;

    if (confirm("학습을 시작하시겠습니까?")) {
      var response = fnModelsByAjax(PROJECT_SEQUENCE_PK, data);

      if (response.result == "success") {
        // 모델 리스트 조회
        fnGetModelList();
        $(".learningModal").modal("hide");
      } else if (fnNotNullAndEmpty(response.detail)) {
        fnComErrorMessage("모델 생성 에러 \n" + response.detail.title, response.detail);
      }
    }
  } else {
    fnComNotify("warning", "알고리즘을 선택해주세요.");
  }
};

/**
 * 모델 생성 이전 로직
 */
var preCreateModel = function () {
  var xParam = new Array();
  $("input:checkbox[name='xParams']").each(function () {
    if ($(this).is(":checked")) {
      xParam.push($(this).val());
    }
  });

  var yParam;
  $("input:radio[name='yParams']").each(function () {
    if ($(this).is(":checked")) {
      yParam = $(this).val();
    }
  });

  if (xParam.length == 0) {
    fnComNotify("warning", "X 파라미터를 체크해 주세요");

    if (yParam == null) {
      fnComNotify("warning", "y 파라미터를 체크해 주세요");
    }

    return false;
  }

  if (yParam == null) {
    fnComNotify("warning", "y 파라미터를 체크해 주세요");
    return false;
  }

  fnConfirm(xParam, yParam);
  return true;
}

/**
 * 모델, 학습 파라미터 구성
 * 
 * @param {Array} xParam 
 * @param {Array} yParam 
 */
var fnConfirm = function (xParam, yParam) {
  var modelParameters = {};
  var trainParameters = {};

  $("#modelParams tr").each(function (i) {
    var tr = $(this);
    var pName = tr.children().eq(0).text();
    var pDefault = tr.children().eq(2).text();
    var pValue = tr.children().children().val();

    if ($("#algorithm-tab").closest("li").hasClass("active") || !fnNotNullAndEmpty(pValue)) {
      modelParameters[pName] = pDefault;
    } else {
      modelParameters[pName] = pValue;
    }
  });

  trainParameters["X"] = xParam;
  trainParameters["y"] = yParam;

  //각 파라미터 전역설정
  MODLE_PARAMS = modelParameters;
  TRAIN_PARAMS = trainParameters;
};

/*전처리 차트*/
var fnPreprocessedDataChart = function (param, idx) {
  am4core.ready(function () {
    am4core.options.commercialLicense = true;
    var preprocessedDataChart;

    if (param.graph_type == "histogram" || param.graph_type == "bar") {
      am4core.useTheme(am4themes_animated);
      preprocessedDataChart = am4core.create("preprocessedDataChartDiv" + idx, am4charts.XYChart);
      preprocessedDataChart.scrollbarX = new am4core.Scrollbar();
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

      preprocessedDataChart.data = list;

      var categoryAxis = preprocessedDataChart.xAxes.push(new am4charts.CategoryAxis());
      categoryAxis.dataFields.category = "범주값";
      categoryAxis.title.text = "범주값";
      categoryAxis.renderer.grid.template.location = 0;
      categoryAxis.renderer.minGridDistance = 30;
      categoryAxis.renderer.labels.template.horizontalCenter = "right";
      categoryAxis.renderer.labels.template.verticalCenter = "middle";
      categoryAxis.renderer.labels.template.rotation = 0;
      categoryAxis.tooltip.disabled = true;
      categoryAxis.renderer.minHeight = 50;
      var valueAxis = preprocessedDataChart.yAxes.push(new am4charts.ValueAxis());
      valueAxis.renderer.minWidth = 50;
      valueAxis.title.text = "frequency";

      var series = preprocessedDataChart.series.push(new am4charts.ColumnSeries());
      series.sequencedInterpolation = true;
      series.dataFields.valueY = "frequency";
      series.dataFields.categoryX = "범주값";
      series.tooltipText = "[{categoryX}: bold]{valueY}[/]";
      series.columns.template.strokeWidth = 0;
      series.tooltip.pointerOrientation = "vertical";
      series.columns.template.column.cornerRadiusTopLeft = 10;
      series.columns.template.column.cornerRadiusTopRight = 10;
      series.columns.template.column.fillOpacity = 0.8;

      var hoverState = series.columns.template.column.states.create("hover");
      hoverState.properties.cornerRadiusTopLeft = 0;
      hoverState.properties.cornerRadiusTopRight = 0;
      hoverState.properties.fillOpacity = 1;
      series.columns.template.adapter.add("fill", function (fill, target) {
        return preprocessedDataChart.colors.getIndex(target.dataItem.index);
      });

      preprocessedDataChart.cursor = new am4charts.XYCursor();
    } else if (param.graph_type == "pie") {
      am4core.useTheme(am4themes_animated);

      preprocessedDataChart = am4core.create("preprocessedDataChartDiv" + idx, am4charts.PieChart);
      preprocessedDataChart.scrollbarX = new am4core.Scrollbar();
      var _binsMeans = param.compact_data.elements;
      var _frequency = param.compact_data.frequency;
      var _list = [];

      for (var _i4 in _binsMeans) {
        var _data = {
          "범주값": _binsMeans[_i4],
          "frequency": _frequency[_i4]
        };

        _list.push(_data);
      }

      preprocessedDataChart.data = _list;

      var pieSeries = preprocessedDataChart.series.push(new am4charts.PieSeries());
      pieSeries.dataFields.value = "frequency";
      pieSeries.dataFields.category = "범주값";
      pieSeries.slices.template.stroke = am4core.color("#fff");
      pieSeries.slices.template.strokeWidth = 2;
      pieSeries.slices.template.strokeOpacity = 1;

      pieSeries.hiddenState.properties.opacity = 1;
      pieSeries.hiddenState.properties.endAngle = -90;
      pieSeries.hiddenState.properties.startAngle = -90;
    }

    PP_DATACHART_ARR.push(preprocessedDataChart);
  });
};