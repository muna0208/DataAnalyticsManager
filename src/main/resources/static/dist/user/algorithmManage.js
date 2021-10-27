"use strict";

var SEARCHED = false;

$("#loading").show();
$(function () {
  fnInit();
});

var fnInit = function () {
  var paging = Object.create(Paging);
  showContents(paging);
  createTable();
  search(searchLogic);
  type();
  $("#loading").hide();
};

/**
 * 알고리즘 목록 나타내기
 */
var showContents = function (paging) {
  $("#datatable").DataTable().destroy();
  var algorithms = fnAlgorithmListByAjax(paging);
  paging.init("#datatable", fnAlgorithmListByAjax, fnCreateListHtml, createTable);
  $("#tbodyHtml").html(fnCreateListHtml(algorithms));
};

/**
 * 테이블 정의
 */
var createTable = function () {
  $('#datatable').dataTable({
    'info': false,
    'order': [],
    "aoColumnDefs": [{
      'bSortable': false,
      'aTargets': [4]
    }],
    'searching': false,
    'paging': false
  });
};

/**
 * 알고리즘 목록 생성
 * 
 * @param {Arrays} algorithms 
 */
var fnCreateListHtml = function (algorithms) {
  var html = "";
  $("#totalCount").text(algorithms.length);

  for (var i in algorithms) {
    var data = algorithms[i];
    html += algorithmTemplate(data);
  }

  return html;
};

/**
 * 알고리즘 목록을 나타내기 위한 html 템플릿
 * 
 * @param {Object} data 
 */
var algorithmTemplate = function (data) {
  var html = "";

  html += "<tr>";
  html += "	<td>" + data.library_function_usage + "</td>";
  html += "	<td>" + data.name + "</td>";
  html += "	<td>" + data.library_name + "</td>";
  html += "	<td>" + data.support_data_type + "</td>";
  html += "	<td><button type='button' class='btn btn-success btn-xs' data-toggle='modal' " + "data-target='.bs-example-modal-lg' onClick='fnSetModal(" + data.id + ")'>" + "See detail</button></td>";
  html += "</tr>";
  
  return html;
};

/**
 * 모달 설정
 * 
 * @param {Number} id 
 */
var fnSetModal = function (id) {
  var data = fnAlgorithmByAjax(id);
  $("#algorithmName").text(fnReplaceNull(data.name));
  $("#libraryName").text(fnReplaceNull(data.library_name));
  $("#libraryFunctionUasge").text(fnReplaceNull(data.library_function_usage));
  $("#libraryFunctionDescription").text(fnReplaceNull(data.library_function_description));
  $("#modelParamList").html(fnCreateModalHtml(data.model_parameter));
  $("#trainParamList").html(fnCreateModalHtml(data.train_parameter));
};

/**
 * 모달 요소 나타내기
 * 
 * @param {Array} list 
 */
var fnCreateModalHtml = function (list) {
  var html = "";

  for (var i in list) {
    var data = list[i];
    html += "";
    html += "<tr>";
    html += "	<td>" + data.name + "</td>";
    html += "	<td>" + data.default + "</td>";
    html += "	<td>" + data.type + "</td>";
    html += "	<td>" + data.note + "</td>";
    html += "</tr>";
  }

  return html;
};

/**
 * 검색 기능 처리 로직
 */
var searchLogic = function () {
  var option = $("#search_option option:selected").val();
  var value = $("#search_word").val();
  var paging = Object.create(Paging);
  SEARCHED = true;

  paging.setOption(option);
  paging.setValue(value);
  showContents(paging);
};

/**
 * 머신러닝, 딥러닝 타입에 따른 목록 비동기 변경
 */
var type = function () {
  $('#type').on('change', function (event) {
    event.stopImmediatePropagation();

    if (SEARCHED) {
      searchLogic();
      return;
    }

    var paging = Object.create(Paging);
    showContents(paging);
  })
}
