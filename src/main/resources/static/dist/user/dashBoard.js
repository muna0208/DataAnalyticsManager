"use strict";

$("#loading").show();
$(function () {
  $("#testModal").load("domainManage .modelTestModal", function () {
    if ($("#jstree_fileBrowser").length > 0) {
      InitJsTree("jstree_fileBrowser", "jstree_dataInfoTable", "jstree_dataSampleTable", "jstree_currentDir");
    }
  });

  fnInit();
});

var fnInit = function () {
  var result = getData();
  fillCounts(result);
  showChart(result.size);
  $("#content").html(fnCreateListHtml(result.domainList));
  download("content", ".analysis_box");
  modelTest("content", ".analysis_box");
};

/**
 * 도메인 목록 및 개수 조회
 */
var getData = function () {
  var result;
  url = "/domains/dashboard";
  errorMessage = "대쉬보드 데이터 조회 에러";

  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response;
  });
  return result;
};

/**
 * 도메인 목록 생성
 * 
 * @param {Arrays} domains 
 */
var fnCreateListHtml = function (domains) {
  var html = "";

  for (var i in domains) {
    var data = domains[i];
    html += domainTemplate(data);
  }

  return html;
};

/**
 * 대쉬보드를 위한 도메인 html 템플릿
 * 
 * @param {Object} data 
 */
var domainTemplate = function (data) {
  var html = "";

  html += "<div class='analysis_box' data-id=" + data.id + " data-type=" + data.type + ">";
  html += "    <p class='title'>" + data.title + "</p>";
  html += "    <div>";
  html += "        <button class='downloadable' value='model'>모델</button>";

  if (data.type === 'M') {
    html += "        <button class='downloadable' value='preprocessedData'>전처리 데이터</button>";
  }

  html += "        <button class='downloadable' value='originalData'>원본 데이터</button>";
  html += "        <button class='domain_test' data-model-id=" + data.modelId + " data-toggle='modal' data-target='.modelTestModal'>테스트</button>";
  html += "    </div>";
  html += "</div>";
  
  return html;
};

/**
 * 건 수 채우기
 * 
 * @param {Object} result 
 */
var fillCounts = function (result) {
  $(".domainCount").text(result.domainCount);
  $(".machineModelCount").text(result.machineModelCount);
  $(".deepModelCount").text(result.deepModelCount);
  $(".machineProjectCount").text(result.machineProjectCount);
  $(".deepProjectCount").text(result.deepProjectCount);
};

/**
 * 사용 용량 / 전체 용량 차트 보여주기
 * 
 * @param {Number} size 
 */
var showChart = function (size) {
  am4core.ready(function () {
    am4core.options.commercialLicense = true;
    am4core.useTheme(am4themes_animated);

    var chart = am4core.create("sizeChart", am4charts.PieChart3D);

    if (size != null) {
      size = size.toFixed(2);
    } else {
      fnComNotify("error", "용량을 표시할 수 없습니다.");
      return;
    }

    var totalSize = 3;
    var userAuth = $("#userAuth").val();
    if (userAuth === "admin") {
      totalSize = 1024;
    }

    chart.data = [{
      "usage": "사용한 용량",
      "size": size
    }, {
      "usage": "남은 용량",
      "size": totalSize - size
    }
    ];

    var pieSeries = chart.series.push(new am4charts.PieSeries3D());
    pieSeries.dataFields.value = "size";
    pieSeries.dataFields.category = "usage";
    pieSeries.slices.template.stroke = am4core.color("#fff");
    pieSeries.slices.template.strokeOpacity = 1;
    pieSeries.slices.template.tooltipText = "{value.value}GB";

    pieSeries.hiddenState.properties.opacity = 1;
    pieSeries.hiddenState.properties.endAngle = -90;
    pieSeries.hiddenState.properties.startAngle = -90;

    var sizeStr = "1TB";
    if (userAuth === "user") {
      sizeStr = "3GB";
    }

    var title = chart.titles.create();
    title.text = size + "GB / " + sizeStr;
    title.fontSize = 25;
  });
}