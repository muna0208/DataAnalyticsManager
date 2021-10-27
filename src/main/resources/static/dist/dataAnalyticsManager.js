"use strict";

$(function () {
  var urlName = window.location.pathname;
  $(".commonMenuUl").find("li").each(function () {
    $(this).removeClass("active");
  });

  if (urlName == "/algorithmManage") {
    $(".algorithmActive").addClass("active");
  } else if (urlName == "/dataManage") {
    $(".dataManageActive").addClass("active");
  } else if (urlName == "/projectManage" || urlName == "/projectDetail") {
    $(".projectManageActive").addClass("active");
  } else if (urlName == "/serviceManage") {
    $(".serviceManageActive").addClass("active");
  } else if (urlName == "/userManage") {
    $(".userManageActive").addClass("active");
  } else if (urlName == "/deepLearningManage" || urlName == "/deepLearningDetail") {
    $(".deepLearningManageActive").addClass("active");
  }

  /*체크박스의 모두체크버튼*/
  $("#check-all_batchList").click(function () {
    var chkFlug = false;
    if ($(this).is(":checked")) chkFlug = true;
    $("input[name='table_records']").each(function () {
      $(this).prop("checked", chkFlug);
    });
  });
  $("#check-all_batchRequestList").click(function () {
    var chkFlug = false;
    if ($(this).is(":checked")) chkFlug = true;
    $("input[name='table_records']").each(function () {
      $(this).prop("checked", chkFlug);
    });
  });
  $("#check-all").click(function () {
    var chkFlug = false;
    if ($(this).is(":checked")) chkFlug = true;
    $("input[name='table_records']").each(function () {
      $(this).prop("checked", chkFlug);
    });
  });
});

/*********************************************** project function start ***********************************************/


/**
 * Notification
 * type = info, success, warning, danger
 * @param type
 * @param text
 * @returns
 */
var fnComNotify = function fnComNotify(type, text) {
  new PNotify({
    title: type,
    text: text,
    type: type,
    styling: 'bootstrap3'
  });
};

/**
 * fnComErrorMessage
 * @param text
 * @param message
 * @returns
 */
var fnComErrorMessage = function fnComErrorMessage(text, message) {
  fnComNotify("error", text);
  console.log(text + " : ");
  console.log(message);
};

/*목록 체크된 값 가져오기*/
var fnTableCheckList = function fnTableCheckList(id) {
  var checkMap = {};
  var checkIdList = [];
  var checkRowList = [];
  $("#" + id).find("input[name='table_records']").each(function () {
    if ($(this).is(":checked")) {
      checkRowList.push($(this).parent().parent().parent());
      checkIdList.push($(this).attr("id"));
    }
  });
  checkMap["checkIdList"] = checkIdList;
  checkMap["checkRowList"] = checkRowList;
  return checkMap;
};

/* 테이블 삭제 */
var fnComDeleteTable = function fnComDeleteTable(id, checkRowList) {
  for (var i in checkRowList) {
    $("#" + id).dataTable().fnDeleteRow(checkRowList[i]);
  }
};

/*타입에 따른 validation*/
var fnCheckTypeValue = function fnCheckTypeValue(type, value) {
  var result = false;

  if (type == "string" || type == "bool" || type == "none") {
    return true;
  } else {
    switch (type) {
      case "int":
        // '예) 5'
        if (value.replace(/^[0-9]+$/, "") == "") result = true;
        break;

      case "float":
        // 예) 5.5
        if (value.replace(/^\d+(?:[.]?[\d]?[\d]?[\d]?[\d])?$/, "") == "") result = true;
        break;

      case "numerical":
        // 예) 5.5
        if (value.replace(/^\d+(?:[.]?[\d]?[\d]?[\d]?[\d])?$/, "") == "") result = true;
        break;

      case "string, numerical":
        // 예) String or 8 
        if (value.replace(/^[a-zA-Z]+|^\d+(?:[.]?[\d]?[\d]?[\d]?[\d])?$/, "") == "") result = true;
        break;

      case "numerical, string, np.nan":
        // 예) String or 8.8
        if (value.replace(/^[a-zA-Z]+|^\d+(?:[.]?[\d]?[\d]?[\d]?[\d])?$/, "") == "") result = true;
        break;

      case "string, int, array":
        // 예) String or 1 or [String,String] or [3,5]
        if (value.replace(/^[a-zA-Z]+|^\d+|^\[+[a-zA-Z,]+\]+|^\[+[0-9,]+\]+$/, "") == "") result = true;
        break;

      case "string, array":
        // 예) String or [String,String]
        if (value.replace(/^[a-zA-Z]+|^\[+([a-zA-Z]+,?)*\]+$/, "") == "") result = true;
        break;

      case "string, list of ints, array":
        // 예) String or [3,5] or [String,String] or [3,5]
        if (value.replace(/^[a-zA-Z]+|^\[+[a-zA-Z,]+\]+|^\[+[0-9,]+\]+$/, "") == "") result = true;
        break;

      case "string, list, array":
        // 예) String or [String,String]
        if (value.replace(/^[a-zA-Z]+|^\[+([a-zA-Z]+,?)*\]+$/, "") == "") result = true;
        break;

      case "string, list of lists, array":
        // 예) String or [String,String] or [[String,String],[String,String]]
        if (value.replace(/^[a-zA-Z]+|^\[+([a-zA-Z]+,?)*\]+$/, "") == "") result = true;
        break;

      case "array-like":
        //예) [String,String] or [[String,String],[String,String]]
        if (value.replace(/^\[+([a-zA-Z]+,?)*\]+$/, "") == "") result = true;
        break;

      case "int, array-like":
        // 예) 2 or [3,5]
        if (value.replace(/^[0-9]+|^\[+[0-9,]+\]+$/, "") == "") result = true;
        break;

      case "string, sparse matrix":
        // 예) [[String,String],[String,String]]
        if (value.replace(/^\[+([a-zA-Z]+,?)*\]+$/, "") == "") result = true;
        break;

      case "array":
        // 예) [3]
        if (value.replace(/^\[+[0-9,]+\]+$/, "") == "") result = true;
        break;

      case "tuple":
        // 예) (2,3)
        if (value.replace(/^\(+[0-9,-]+\)+$/, "") == "") result = true;
        break;

      case "int, string, list":
        // 예) 2 or String or [3,5] or [String,String]
        if (value.replace(/^[0-9]+|^\[+[0-9,]+\]|^[a-zA-Z]+|^\[+([a-zA-Z]+,?)*\]+$/, "") == "") result = true;
        break;

      case "int, list":
        // 예) 2 or [3,5]
        if (value.replace(/^[0-9]+|^\[+[0-9,]+\]+$/, "") == "") result = true;
        break;

      case "string, list":
        // 예) String or [String,String]
        if (value.replace(/^[a-zA-Z]+|^\[+([a-zA-Z]+,?)*\]+$/, "") == "") result = true;
        break;

      case "string, list of list, array":
        // 예) String or [String,String]
        if (value.replace(/^[a-zA-Z]+|^\[+([a-zA-Z]+,?)*\]+$/, "") == "") result = true;
        break;

      case "int, string":
        // 예) 2 or String
        if (value.replace(/^[a-zA-Z]+|^\d+$/, "") == "") result = true;
        break;

      default:
        "";
        break;
    }

    return result;
  }
};

/*날짜 포멧 체크*/
var fnDateFormatCheck = function fnDateFormatCheck(value) {
  var expText = /\{+([YYYY|yyyy]{4})+([.:\-_@])*([MM]{2})+(([.:\-_@])*([dd]{2})+)*(([.:\-_@])*([HH]{2})+(([.:\-_@])*([mm]{2})+)*)*\}+/;

  if (expText.test(value)) {
    return false;
  }

  return true;
};

var fnArraySplitBR = function fnArraySplitBR(value) {
  if ($.isArray(value)) {
    var result = "";

    for (var i in value) {
      if (i == 0) {
        result += value[i];
      } else if (i % 5 == 0) {
        result += ", <br>" + value[i];
      } else {
        result += ", " + value[i];
      }
    }

    return result;
  } else {
    return value;
  }
};

/*체크박스 체크해지*/
var fnUnCheckbox = function fnUnCheckbox(id) {
  $("#check-all").prop("checked", false);
  if (fnNotNullAndEmpty(id)) $("#" + id).prop("checked", false);
  $("input[name='table_records']").each(function () {
    $(this).prop("checked", false);
  });
};

/*사용자별 프로젝트 목록 가져오기*/
var fnGetProjectOfUserId = function fnGetProjectOfUserId() {
  var html = "";
  var userProjectList = fnGetProjectListByAjax();

  for (var i in userProjectList) {
    var project = userProjectList[i];

    if (html == "") {
      fnGetModelsOfProjectPk(project.PROJECT_SEQUENCE_PK);
      html += "<li class='projectList active pointerCorsor' data-projectSequencePk=" + project.PROJECT_SEQUENCE_PK + ">" + project.NAME + "</li>";
    } else {
      html += "<li class='projectList pointerCorsor' data-projectSequencePk=" + project.PROJECT_SEQUENCE_PK + ">" + project.NAME + "</li>";
    }
  }

  $("#selectedProject").html(html);
};

/*사용자 프로젝트별 모델 목록 가져오기*/
var fnGetModelsOfProjectPk = function fnGetModelsOfProjectPk(projectSequencePk, option) {
  var html = "";
  var modelList = fnGetModelsByAjax(projectSequencePk, "");

  for (var i in modelList) {
    var model = modelList[i]; // 성공인 모델만... 

    if (option == "useOfBatch" && model.PROGRESS_STATE == "success") {
      if (html == "") {
        html += "<li class='modelList active pointerCorsor' data-modelSequenceFk1=" + model.MODEL_SEQUENCE_PK + " data-instanceSequenceFk2=" + model.INSTANCE_SEQUENCE_FK3 + " data-projectSequenceFk3=" + model.PROJECT_SEQUENCE_FK4 + ">" + model.NAME + "</li>";
      } else {
        html += "<li class='modelList pointerCorsor' data-modelSequenceFk1=" + model.MODEL_SEQUENCE_PK + " data-instanceSequenceFk2=" + model.INSTANCE_SEQUENCE_FK3 + " data-projectSequenceFk3=" + model.PROJECT_SEQUENCE_FK4 + ">" + model.NAME + "</li>";
      }
    } else {
      if (html == "") {
        html += "<li class='modelList active pointerCorsor' data-modelSequenceFk1=" + model.MODEL_SEQUENCE_PK + " data-instanceSequenceFk2=" + model.INSTANCE_SEQUENCE_FK3 + " data-projectSequenceFk3=" + model.PROJECT_SEQUENCE_FK4 + ">" + model.NAME + "</li>";
      } else {
        html += "<li class='modelList pointerCorsor' data-modelSequenceFk1=" + model.MODEL_SEQUENCE_PK + " data-instanceSequenceFk2=" + model.INSTANCE_SEQUENCE_FK3 + " data-projectSequenceFk3=" + model.PROJECT_SEQUENCE_FK4 + ">" + model.NAME + "</li>";
      }
    }
  }

  if (html == "") html += "<li class='modelList'>모델이 없습니다.</li>";
  $("#selectedModel").html(html);
};