"use strict";

var MODEL_TRAIN_SCORE, MODEL_TEST_SCORE;
var PROJECT_SEQUENCE_PK, SELECTED_MODEL_PK;
var MODEL_INTERVA_LID = 0;

$(function () {
  // 모델 리스트 클릭시
  $(document).on("click", ".modelData", function () {
    /*샌드박스 파일브라우저 샘플 미리보기*/
    $(".modelData").removeClass("active");
    $(this).addClass("active");
    SELECTED_MODEL_PK = $(this).attr("id");
    fnGetModel();
  });
});

/*모델 목록 가져오기*/
var fnGetModelList = function () {
  var html = "";

  if (fnNotNullAndEmpty(SELECTED_PREPROCESSED_DATA_PK)) {
    var modelList = fnGetModelsByAjax(PROJECT_SEQUENCE_PK, SELECTED_PREPROCESSED_DATA_PK);
    var preprocessedDataList = fnGetPreprocessedDataListByAjax(SELECTED_ORIGINAL_DATA_PK);

    for (var i in modelList) {
      var model = modelList[i];

      if (i == 0) {
        html += "<li class='pdTB_5 modelData active' role='button'" + " id=" + model.id + ">" + model.name + "</li>";
        SELECTED_MODEL_PK = model.id;
        fnGetModel(model);
        /* 전처리데이터 가져오기*/
      } else {
        html += "<li class='pdTB_5 modelData' role='button' " + "id=" + model.id + ">" + model.name + "</li>";
      }
    }
  }

  if (html == "") {
    html += "<li>모델 데이터가 없습니다.</li>";
    SELECTED_MODEL_PK = null;
    $("#modelDiv").hide(); // 모델 숨김

    if (MODEL_INTERVA_LID != 0) clearInterval(MODEL_INTERVA_LID);
  } else {
    $("#deleteModelBtn").show();
    $("#modelDiv").fadeIn();
    $("#step3").removeClass("selected");
    $("#step3").addClass("done");
    if (4 > PAGE) {
      PAGE = 4;
    }
    pageInit();
  }

  if ((preprocessedDataList != null && preprocessedDataList.length > 0) && (modelList == null || modelList.length == 0)) {
    $("#step-3").show();
    $("#step3").removeClass("done");
    $("#step3").addClass("selected");
    $("#step3").removeClass("disabled");
  } else {
    $("#step-3").hide();
  }

  $("#modelList").html(html);
  $("#loading").hide();
};

/*모델 상세내용*/
var fnGetModel = function (model) {
  if (PAGE != 4) {
    PAGE = 4;
    $("#step-1").hide();
    $("#step-2").hide();
    $("#step-3").hide();
    $(".buttonNext").addClass("buttonDisabled");
    $(".buttonPrevious").removeClass("buttonDisabled");
  }

  if (model == null) {
    model = fnGetModeslByAjax(PROJECT_SEQUENCE_PK, SELECTED_MODEL_PK);
  }

  $(".modelName").text(model.name);
  $("#modelDetailDiv").hide();
  $("#modelTestBtn").hide();
  if (MODEL_INTERVA_LID != 0) clearInterval(MODEL_INTERVA_LID);

  /* 전처리 상태값 체크 후 변경 */
  if ("ongoing" == model.progress_state) {
    var html = "";
    html += "	<div class='progress mgBottom_0'>" + "				<div class='progress-bar progress-bar-striped width_100p active' role='progressbar'>생성중</div></div>";
    var stopHtml = "<button class='btn btn-danger btn-xs' onclick='fnStopModel();'>중지</button>";
    $("#modelFilename").html(html);
    $("#modelFilepath").html(html);
    $("#modelCreateDatetime").text(model.create_date);
    $("#progressState").html(stopHtml);
    MODEL_INTERVA_LID = setInterval(fnChangeModelState, 5000);
  } else if ("standby" == model.progress_state) {
    var restartHtml = "<button class='btn btn-danger btn-xs' onclick='fnRestartModel(" + model.id + ");'>재시작</button>";
    $("#modelFilename").text("중지");
    $("#modelFilepath").text("중지");
    $("#modelCreateDatetime").text(model.progress_state);
    $("#progressState").html(restartHtml);
  } else if ("fail" == model.progress_state) {
    $("#modelFilename").text("실패");
    $("#modelFilepath").text("실패");
    $("#modelCreateDatetime").text("실패");
    $("#progressState").text("실패");
  } else {
    // 성공
    $("#modelFilename").text(model.filename);
    $("#modelFilepath").text(model.filepath);
    $("#modelCreateDatetime").text(model.create_date);
    $("#progressState").text("학습완료");
    $("#modelDetailDiv").fadeIn();
    $("#modelTestBtn").show(); // 모델 학습정보

    fnSetModelInfo(model);
  }

  $("#modelDiv").fadeIn();
};

/*모델 학습정보*/
var fnSetModelInfo = function (model) {
  MODEL_TRAIN_SCORE = model.validation_summary.value.holdout_score;
  var command = model.command.value;
  var algorithm = fnAlgorithmByAjax(command.algorithm_id);
  var modelInfoTemplate = getModelInfoTemplate(command, algorithm, model);
  $("#modelInfo").html(modelInfoTemplate);
};

/**
 * 모델 학습정보 화면 구성요소
 * 
 * @param {JSONObject} command 
 * @param {JSONObject} algorithm 
 */
function getModelInfoTemplate(command, algorithm, model) {
  var html = "";

  html += "<tr><th>라이브러리</th><td colspan='2'>" + algorithm.library_name + "</td></tr>";
  html += "<tr><th>알고리즘</th><td colspan='2'>" + algorithm.library_function_usage + "</td></tr>";
  html += "<tr><th>학습 소요 시간</th><td colspan='2'>" + model.diffDateTime + "</td></tr>";
  html += "<tr><th>모델 성능 평가</th><td colspan='2'>" + parseFloat(model.validation_summary.value.holdout_score).toFixed(3) + "</td></tr>"; //트레인 전역변수에 세팅

  var modelParam = command.model_parameters;
  var trainParam = command.train_parameters;
  var cnt = 0;

  $.each(modelParam, function (key, value) {
    if (cnt == 0) html += "<tr><th rowspan='" + Object.keys(modelParam).length + "'>모델 파라미터</th><td>" + key + "</td><td>" + value + "</td></tr>"; else html += "<tr><td>" + key + "</td><td>" + value + "</td></tr>";
    cnt++;
  });
  
  cnt = 0;
  $.each(trainParam, function (key, value) {
    if (cnt == 0) html += "<tr><th rowspan='" + Object.keys(trainParam).length + "'>학습 파라미터</th><td>" + key + "</td><td>" + fnArraySplitBR(value) + "</td></tr>"; else html += "<tr><td>" + key + "</td><td>" + fnArraySplitBR(value) + "</td></tr>";
    cnt++;
  });

  return html;
}

/*모델 삭제*/
var fnDeleteModel = function () {
  if (confirm("모델을 삭제하시면 배치관련 데이터도 같이 삭제됩니다. \n삭제하시겠습니까?")) {
    var response = fnDeleteModelByAjax(PROJECT_SEQUENCE_PK, SELECTED_MODEL_PK);

    if (response.result == "success") {
      fnComNotify("success", "모델을 삭제하였습니다."); // 모델 목록 가져오기
      SELECTED_MODEL_PK = null;
      resetModelTest();
      PAGE = PAGE - 1;
      pageInit();
      fnGetModelList();
    } else {
      fnComErrorMessage("삭제 에러 \n" + response.detail.title, response.detail);
    }
  }
};

/**
 * 모델 정보 리셋
 */
function resetModelTest() {
  $("#modelTestResult").html("");
  $("#modelResultChart").html("");
}

/*모델 중지*/
var fnStopModel = function () {
  if (confirm("학습을 중지 요청을 하시겠습니까?")) {
    var data = {
      "mode": "STOP"
    };
    var response = fnStopAndRestartModelByAjax(PROJECT_SEQUENCE_PK, SELECTED_MODEL_PK, data);

    if (response.result == "success") {
      fnComNotify("success", "모델 생성중지 요청을 하였습니다.");
    } else {
      fnComErrorMessage("모델 생성중지 요청 에러 \n" + response.detail.title, response.detail);
    }
  }
};

/*모델 재시작*/
var fnRestartModel = function () {
  if (confirm("학습을 재시작 요청을 하시겠습니까?")) {
    var data = {
      "mode": "RESTART"
    };
    var response = fnStopAndRestartModelByAjax(PROJECT_SEQUENCE_PK, SELECTED_MODEL_PK, data);

    if (response.result == "success") {
      fnComNotify("success", "모델 재시작 요청을 하였습니다."); // 모델 목록 가져오기

      fnGetModelList();
    } else {
      fnComErrorMessage("모델 재시작 요청 에러 \n" + response.detail.title, response.detail);
    }
  }
};

/*모델 상태변경*/
var fnChangeModelState = function () {
  var model = fnGetModeslByAjax(PROJECT_SEQUENCE_PK, SELECTED_MODEL_PK);

  if ("success" == model.progress_state) {
    $("#modelFilename").text(model.filename);
    $("#modelFilepath").text(model.filepath);
    $("#modelCreateDatetime").text(model.create_date);
    $("#progressState").text("학습완료");
    $("#modelDetailDiv").fadeIn();
    $("#modelTestBtn").show(); // 모델 학습정보

    fnSetModelInfo(model); // 종료
    fnComNotify("success", "모델 생성이 완료되었습니다.");

    if (MODEL_INTERVA_LID != 0) {
      clearInterval(MODEL_INTERVA_LID);
    }
  } else if ("standby" == model.progress_state) {
    var reStartHtml = "<button class='btn btn-danger btn-xs' onclick='fnRestartModel();'>재시작</button>";
    $("#modelFilename").html("<td>중지</td>");
    $("#modelFilepath").html("<td>중지</td>");
    $("#progressState").html(reStartHtml);
    $("#modelCreateDatetime").text(model.create_date);

    if (MODEL_INTERVA_LID != 0) {
      clearInterval(MODEL_INTERVA_LID);
    }
  } else if ("fail" == model.progress_state) {
    $("#modelFilename").html("<td>실패</td>");
    $("#modelFilepath").html("<td>실패</td>");
    $("#modelCreateDatetime").html("<td>실패</td>");
    $("#progressState").html("<td>실패</td>");
    fnComNotify("error", "모델 생성을 실패 하였습니다. 관리자에게 문의해주세요.");

    if (MODEL_INTERVA_LID != 0) {
      clearInterval(MODEL_INTERVA_LID);
    }
  }
};

/*
 * 모델 테스트 모달
 * jstree fileBrowser로 교체
 */
var fnModelTestModal = function () {
  var localFiles = fnGetFileBrowserByAjax();
  var html = "";

  for (var i in localFiles) {
    if (i == 0) {
      fnGetModelLocalFileSample(localFiles[i]);
      html += "<li class='active' role='button'><a href='javascript:' class='modelLocalFiles'>" + localFiles[i] + "</a></li>";
    } else {
      html += "<li role='button'><a href='javascript:' class='modelLocalFiles'>" + localFiles[i] + "</a></li>";
    }
  }

  $("#modelLocalFiles").html(html);
};

/*모델 테스트*/
var fnModelTest = function (popup) {
  var data = {};
  data["mode"] = "TEST";
  data["test_data_path"] = TARGET_PATH;

  var response = fnModelTestByAjax(PROJECT_SEQUENCE_PK, SELECTED_MODEL_PK, data);
  if (response.data == null) {
    if (response.detail.type == 409) {
      fnComNotify("warning", "존재하지 않는 데이터로 만든 도메인입니다.");
    } else {
      fnComNotify("warning", "테스트에 적합한 데이터를 선택해주세요.");
    }
    return;
  }

  MODEL_TEST_SCORE = response.data.score;

  if (response.result == "success") {
    $("#modelTestResult").html(showScore(response.data));
    $("#testResultTable").html(formatData(response.data));
    if (popup != true) {
      $(".modelTestModal").modal("hide");
    }
  } else {
    fnComNotify("error", "모델 테스트 에러 \n" + response.detail.title, response.detail);
  }

  fnModelTestResultChart();
};

/**
 * 점수 화면에 추가
 * 
 * @param {JSONObject} data 
 */
var showScore = function (data) {
  var html = "";
  html += "<p class='fontSize_20 fontWeight_bold'>TRAIN SCORE: " + parseFloat(MODEL_TRAIN_SCORE).toFixed(3) + "</p>";
  html += "<p class='fontSize_20 fontWeight_bold'>TEST SCORE: " + parseFloat(data.score).toFixed(3) + "</p>";
  return html;
}

/**
 * 데이터 포맷하여 나타내기
 * 
 * @param {Object} data 
 */
var formatData = function (data) {
  var html = "";
  html += " <thead>";
  html += "    <tr>";
  html += "      <th>predict</th>";
  html += "      <th>real_value</th>";
  html += "    </tr>";
  html += " </thead>";
  html += "<tbody>";

  for (var i = 0; i < data.predict.length; i++) {
    html += "<tr>";
    html += " <td>" + data.predict[i] + "</td>";
    html += " <td>" + data.real_value[i] + "</td>";
    html += "</tr>";
  }

  html += "</tbody>";
  return html;
};

/*모델 테스트 결과 차트*/
var fnModelTestResultChart = function () {
  am4core.ready(function () {
    am4core.options.commercialLicense = true;
    var preprocessedDataChart;

    am4core.useTheme(am4themes_animated);

    preprocessedDataChart = am4core.create("modelResultChart", am4charts.XYChart);

    var list = new Array();
    var data1 = {
      "범주값": "학습(Train)",
      "frequency": parseFloat(MODEL_TRAIN_SCORE).toFixed(3)
    };
    var data2 = {
      "범주값": "검증(Test)",
      "frequency": parseFloat(MODEL_TEST_SCORE).toFixed(3)
    };
    list.push(data1);
    list.push(data2);
    preprocessedDataChart.data = list; // Create axes

    var categoryAxis = preprocessedDataChart.xAxes.push(new am4charts.CategoryAxis());
    categoryAxis.dataFields.category = "범주값";

    categoryAxis.renderer.grid.template.location = 0;
    categoryAxis.renderer.minGridDistance = 30;
    categoryAxis.renderer.labels.template.horizontalCenter = "right";
    categoryAxis.renderer.labels.template.verticalCenter = "middle";
    categoryAxis.renderer.labels.template.rotation = 0;
    categoryAxis.tooltip.disabled = true;
    categoryAxis.renderer.minHeight = 50;
    var valueAxis = preprocessedDataChart.yAxes.push(new am4charts.ValueAxis());

    valueAxis.renderer.minWidth = 50;
    valueAxis.title.text = "정확도(accuracy)";

    var series = preprocessedDataChart.series.push(new am4charts.ColumnSeries());
    series.sequencedInterpolation = true;
    series.dataFields.valueY = "frequency";
    series.dataFields.categoryX = "범주값";
    series.tooltipText = "[{categoryX}: bold]{valueY}[/]";
    series.columns.template.strokeWidth = 0;
    series.columns.template.width = am4core.percent(30);
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
  });
};

