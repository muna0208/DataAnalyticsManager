"use strict";

/**
 *  머신러닝 관리 및 딥러닝 관리 매뉴에서 타입을 구분하여 같이 쓰이는 js
 *  D-딥러닝관리, M-머신러닝 관리
 */

var CLICK_ROW;
var USER_AUTH = $("#userAuth").val();

$("#loading").show();
$(function () {
  fnInit();

  $("#name").keydown(function (key) {
    if (key.keyCode == 13) {
      if ("등록" == $("#saveProjectBtn").text()) fnSaveProject("regist"); else fnSaveProject("update");
    }
  });
  /*프로젝트 등록/수정*/
  $(document).on("click", "#saveProjectBtn", function () {
    if ("등록" == $(this).text()) fnSaveProject("regist"); else fnSaveProject("update");
  });

  /* 테이블 클릭시 */
  $(document).on("click", "#tbodyHtml td", function () {
    var index = 4;
    if ("admin" == USER_AUTH) index = 5;

    if ($(this).index() == index) {
      var clickRows = $("#datatable-checkbox").dataTable().fnGetPosition(this); // 변경하고자 하는 CLICK_ROW

      CLICK_ROW = clickRows[0];
    } else if ($(this).index() == 1) {
      var data = $("#datatable-checkbox").dataTable().fnGetData($(this).parent());

      if ("M" == $("#projectType").val())
        fnMovePage("/projectDetail", ["projectSequencePk"], [data[1]]);
      else if ("D" == $("#projectType").val())
        fnMovePage("/deepLearningDetail", ["projectSequencePk"], [data[1]]);

    }
  });
});

var fnInit = function () {
  fnSearch();
  createTable();
  search(searchLogic);
};

/*프로젝트 목록 조회*/
var fnSearch = function () {
  var paging = Object.create(Paging);
  var projectList = fnGetProjectListByAjax(paging);
  paging.init("#datatable-checkbox", fnGetProjectListByAjax, fnCreateListHtml, createTable);
  $("#tbodyHtml").html(fnCreateListHtml(projectList));
  $("#loading").hide();
};

/*프로젝트 목록 생성*/
var fnCreateListHtml = function (projects) {
  var html = "";

  for (var i in projects) {
    var data = projects[i];
    html += projectTemplate(data);
  }

  return html;
};

/**
 * 프로젝트를 나타내기 위한 html 템플릿
 * 
 * @param {Object} data 
 */
var projectTemplate = function (data) {
  var html = "";

  html += "<tr>";
  html += "	<td><input type='checkbox' name='table_records' id='" + data.id + "' class='flat projectCheckbox' value='" + data.id + "' /></td>";
  html += "	<td>" + data.id + "</td>";
  html += "	<td class='cusor' role='button'>" + data.name + "</td>";
  html += "	<td>" + data.description + "</td>";
  html += "	<td>" + data.create_date + "</td>";

  if (USER_AUTH == "admin") {
    html += "	<td>" + data.user_id + "</td>";
  }

  html += "	<td><button type='button' class='btn btn-info btn-xs' onclick='fnUpdateModal(\"" + data.id + "\");'><i class='fa fa-edit'> 수정</i></button></td>";
  html += "</tr>";

  return html;
};

/**
 * 테이블 정의
 */
var createTable = function () {
  $('#datatable-checkbox').dataTable({
    'info': false,
    'order': [],
    'columnDefs': [{
      orderable: false,
      targets: [0]
    }, {
      targets: [1],
      visible: false
    }],
    'searching': false,
    'paging': false,
    'drawCallback': function drawCallback() {
      /* iCheck 생성 */
      $('.flat').iCheck({
        checkboxClass: 'icheckbox_flat-green',
        radioClass: 'iradio_flat-green'
      });

      // TODO 추후에 더 나은 방법 찾기!!!
      setTimeout(function () {
        checkAll();
        freeCheckAll();
        $("#check_all").iCheck("uncheck");
      }, 100);
    }
  });
};

/*프로젝트 등록 모달*/
var fnAddProjcetModal = function () {
  $("#modalTitle").text("프로젝트 등록");
  $("#saveProjectBtn").text("등록");
  $("#name").val("").focus();
  $("#description").val("");
};

/*프로젝트 수정 모달*/
var fnUpdateModal = function (projectSequencePk) {
  $("#modalTitle").text("프로젝트 수정");
  $("#saveProjectBtn").text("수정");
  $("#name").val("");
  $("#description").val("");
  var project = fnGetProjectByAjax(projectSequencePk);
  $("#projectSequencePk").val(project.id);
  $("#name").val(project.name);
  $("#description").val(project.description);
  $(".registDiv").hide();
  $(".infoModal").modal("show");
};

/*머신러닝 관리 프로젝트 등록/수정*/
var fnSaveProject = function (option) {
  if ($.trim($("#name").val()) == "") {
    fnComNotify("warning", "프로젝트명을 입력해주세요.");
    $("#name").focus();
    return false;
  } else {
    var data = {
      "id": $("#projectSequencePk").val(),
      "name": $("#name").val(),
      "description": $("#description").val(),
      "type": $("#projectType").val() // 머신러닝 관리 타입-M, 딥러닝 관리 타입-D
    };
    var subject = "등록";
    var url = "/projects/insert";
    var type = "POST";

    if ("update" == option) {
      subject = "수정";
      url = "/projects/update";
      type = "POST";
    }

    if (data != "" && confirm(subject + "하시겠습니까?")) {
      $.ajax({
        url: url,
        type: type,
        dataType: "json",
        data: data,
        success: function success(data) {
          if (data.result.result == "success") {
            fnUpdateTable(data.result.project, option);
            fnComNotify("success", "프로젝트를  " + subject + "하였습니다.");
            $("#loading").hide();
            $("#search_word").val("");
            $(".infoModal").modal("hide");
            $("#datatable-checkbox").DataTable().destroy();
            fnSearch();
            createTable();
          } else if (data.result.detail == "duplicateName") {
            $("#name").focus();
            fnComNotify("warning", "프로젝트명이 중복되었습니다.");
          } else {
            fnComErrorMessage("프로젝트 " + subject + " 에러!!", data.result.detail);
          }
        },
        error: function error(request, _error) {
          fnComErrorMessage("프로젝트 " + subject + " 에러!!");
          console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error);
        },
        complete: function complete() {
          $("#loading").hide();
        }
      });
    }
  }
};

/*업데이트 data*/
var fnUpdateTable = function (data, option) {
  var checkbox = "<input type='checkbox' name='table_records' id='" + data.id + "' class='flat projectCheckbox'/>";
  var modifyBtn = "<button type='button' class='btn btn-info btn-xs' onclick='fnUpdateModal(\"" + data.id + "\");'><i class='fa fa-edit'> 수정</i></button>";

  if (option == "regist") {
    var num = $("#datatable-checkbox").DataTable().rows().count() + 1;

    if (USER_AUTH == "admin") {
      $("#datatable-checkbox").dataTable().fnAddData([checkbox, data.id, data.name, data.description, data.create_date, data.user_id, modifyBtn]);
    } else {
      $("#datatable-checkbox").dataTable().fnAddData([checkbox, data.id, data.name, data.description, data.create_date, modifyBtn]);
    }

    $("#datatable-checkbox").DataTable().order([1, "desc"]).draw();
  } else {
    if (USER_AUTH == "admin") {
      $("#datatable-checkbox").dataTable().fnUpdate([checkbox, data.id, data.name, data.description, data.create_date, data.user_id, modifyBtn], CLICK_ROW);
    } else {
      $("#datatable-checkbox").dataTable().fnUpdate([checkbox, data.id, data.name, data.description, data.create_date, modifyBtn], CLICK_ROW);
    }
  }
};

/**
 * 프로젝트 삭제
 */
var fnDeleteProjcet = function () {
  // 체크된 항목 가져오기
  var checkMap = fnTableCheckList("tbodyHtml");
  var checkIdList = checkMap.checkIdList;
  var checkRowList = checkMap.checkRowList;
  var successFlug = false;

  if (checkIdList.length > 0) {
    if (confirm("프로젝트 삭제시 관련데이터(원본데이터, 전처리, 모델)도 같이 삭제됩니다. \n삭제 하시겠습니까?")) {
      for (var i in checkIdList) {
        var response = fnDeleteProjcetByAjax(checkIdList[i]);

        if (response.result == "success") {
          $("input").iCheck("uncheck");
          fnComNotify("success", "프로젝트를 삭제하였습니다.");
          successFlug = true;
        } else {
          fnComErrorMessage("프로젝트 삭제 에러!!", response.detail);
        }
      }
      /* 테이블 삭제 */

      if (successFlug) fnComDeleteTable("datatable-checkbox", checkRowList);
    }
  } else {
    fnComNotify("warning", "삭제할 목록을 선택해주세요.");
  }
};

/**
 * 검색 기능 처리 로직
 */
var searchLogic = function () {
  var option = $("#search_option option:selected").val();
  var value = $("#search_word").val();
  $("#datatable-checkbox").DataTable().destroy();
  var paging = Object.create(Paging);
  paging.setOption(option);
  paging.setValue(value);

  var projectList = fnGetProjectListByAjax(paging);
  paging.init("#datatable-checkbox", fnGetProjectListByAjax, fnCreateListHtml, createTable);
  $("#tbodyHtml").html(fnCreateListHtml(projectList));
  createTable();
};
