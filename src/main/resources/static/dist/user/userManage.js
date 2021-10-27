"use strict";

var ADMIN = false;
var SEARCHED = false;

$("#loading").show();
document.addEventListener("DOMContentLoaded", function () {
  fnInit();
});

var fnInit = function () {
  var paging = Object.create(Paging);
  showContents(paging);
  toggleUseFlag();
  eventForAdmin();
  toggleEye();
  search(searchLogic);
  registerUser();
  modifyUser();
  var deletion = Object.create(Deletion);
  deletion.init("/users", usersForDelete, showContents);
  var userForSelfModify = Object.create(UserForSelfModify);
  userForSelfModify.init();
};

var showContents = function (paging) {
  $("#user_board").DataTable().destroy();
  var userList = fnGetUsersByAjax(paging);
  paging.init("#user_board", fnGetUsersByAjax, fnCreateListHtml, defineDataTable);
  $("#tbodyHtml").html(fnCreateListHtml(userList, paging.getRowNumber()));
  defineDataTable();
}

/**
 * 테이블 정의
 */
var defineDataTable = function () {
  $('#user_board').dataTable({
    'info': false,
    'order': [],
    "aoColumnDefs": [{
      'bSortable': false,
      'aTargets': [0, 7]
    }],
    'searching': false,
    'paging': false
    // "pageLength": 10, //기본 데이터건수
    // "lengthMenu": [10, 20, 30],
    ,
    'drawCallback': function drawCallback() {
      /* iCheck 생성 */
      $('.flat').iCheck({
        checkboxClass: 'icheckbox_flat-green',
        radioClass: 'iradio_flat-green'
      });
      checkAll();
      freeCheckAll();
      $("#check_all").iCheck("uncheck");
    }
  });
};

/**
 * 사용자 목록 생성
 * 
 * @param {Object} paging 
 */
var fnGetUsersByAjax = function (paging) {
  var result;
  url = getUrl(paging);

  fnAjaxGetUsersDataSync(url, function (response) {
    result = response.users;
    paging.setTotalPage(Math.floor((response.count - 1) / 10));
  });
  return result;
};

/**
 * 상황에 맞는 url 가져오기
 * 
 * @param {Object} paging 
 */
var getUrl = function (paging) {
  var url = "/users?page=" + paging.getCurrentPage();

  var admin = paging.getAdmin();
  if (admin == true) {
    url += "&admin=" + admin;
  }

  var option = paging.getOption();
  if (option !== undefined) {
    url += "&option=" + option;
  }

  var value = paging.getValue();
  if (value !== undefined) {
    url += "&value=" + value;
  }

  return url;
}

/**
 * 사용자 목록 조회 요청
 * 
 * @param {String} url 
 * @param {Function} fn_success 
 */
var fnAjaxGetUsersDataSync = function (url, fn_success) {
  $.ajax({
    url: url,
    type: "GET",
    async: false,
    success: function success(response) {
      if (fn_success) fn_success(response);
    },
    error: function error() {
      fnComNotify("error", "사용자 목록을 가져오는데 실패하였습니다.");
    }
  });
};

/**
 * 사용자 목록 생성
 * 
 * @param {Arrays} users 
 * @param {Number} rowNumber 
 */
var fnCreateListHtml = function (users, rowNumber) {
  var html = "";

  for (var i in users) {
    var data = users[i];
    html += userTemplate(data, rowNumber++);
  }

  return html;
};

/**
 * 사용자를 나타내기 위한 html 템플릿
 * 
 * @param {Object} data 
 * @param {Number} rowNumber 
 */
var userTemplate = function (data, rowNumber) {
  var html = "";

  html += "<tr class='user_info' data-pk=" + data.id + ">";
  html += "    <td class='a-center'><input type='checkbox' class='flat'";
  html += "            name='table_records'></td>";
  html += "    <td>" + rowNumber + "</td>";
  html += "    <td>" + data.userName + "</td>";
  html += "    <td data-toggle='modal' data-target='.usermodalPopup_2'";
  html += "        id='user_id' class='cusor'>" + data.userId + "</td>";
  html += "    <td>" + data.email + "</td>";
  html += "    <td>" + data.lastAccessDate + "</td>";
  html += "    <td>" + data.registerer + "</td>";
  html += "    <td>";
  html += "        <div class='switch_box'>";
  html += "            <label class='switch'>";

  if (data.useFlag === true) {
    html += "                <input type='checkbox' checked>";
  } else {
    html += "                <input type='checkbox'>";
  }

  html += "                <span class='slider round'></span>";
  html += "            </label>";
  html += "        </div>";
  html += "    </td>";
  html += "</tr>";
  
  return html;
};

/**
 * 사용 여부 변경
 */
var toggleUseFlag = function () {
  $('#tbodyHtml').on("click", ".slider.round", function (event) {
    var userId = event.target.closest("tr").querySelector("#user_id").innerText;
    fnPatchUseFlagByAjax(userId);
  });
};

/**
 * 사용 여부 변경 처리 로직
 * 
 * @param {String} userId 
 */
var fnPatchUseFlagByAjax = function (userId) {
  var result;
  var url = "/users/" + userId + "/useFlag";
  errorMessage = "사용 여부 변경 에러";

  fnAjaxPatchUseFlagDataSync(url, errorMessage, function (response) {
    result = response;
  });
  return result;
};

/**
 * 사용 여부 변경 요청
 * 
 * @param {String} url 
 * @param {Function} fn_success 
 */
var fnAjaxPatchUseFlagDataSync = function (url, fn_success) {
  $.ajax({
    url: url,
    type: "PATCH",
    contentType: 'application/json',
    async: false,
    error: function error(response) {
      fnComNotify("error", "사용 여부 변경에 실패했습니다.");
    }
  });
};

/**
 * 사용자 등록
 */
var registerUser = function () {
  var userRegister = Object.create(UserRegister);
  $("#store_button").on("click", function (event) {
    if (!userRegister.checkId()) {
      return;
    }
    executeRegister();
  });

  userRegister.checkDuplicateId("dup2", "register_id", "usermodalPopup_1");
};

/**
 * 사용자 등록 처리 로직
 */
var executeRegister = function () {
  var user = new UserForRegisterByAdmin();

  if (!verify("POST", user)) {
    return;
  }

  if (!confirm("정말 등록하시겠습니까?")) {
    return;
  }

  var response = fnPostUserByAjax("/users", user);
  if (response.result === "success") {
    $("#usermodalPopup_1").modal("hide");
    fnComNotify("success", "등록이 정상적으로 처리되었습니다.");
    refreshPartially();
  } else {
    fnComNotify("error", "등록이 정상적으로 처리되지 않았습니다.");
  }
};

/**
 * 사용자 등록을 위한 객체
 */
var UserForRegisterByAdmin = function () {
  this.userId = $("#register_id").val();
  this.userName = $("#register_name").val();
  this.email = $("#register_email").val();
  this.userPw = $("#register_password").val();
  this.userPw2 = $("#register_password2").val();
  this.userAuth = $("input[name='iCheck_register']:checked").val();
  this.useFlag = $("input[name='iCheck2_register']:checked").val();
};

/**
 * 사용자 정보 수정
 */
var modifyUser = function () {
  $("#tbodyHtml").on("click", "#user_id", function (event) {
    var modifyingId = event.target.innerText;
    var user = fnGetUserByAjax(modifyingId);
    fillInputValues(user);

    $("#modify_button").on("click", function (event) {
      event.stopImmediatePropagation();
      fnPatchUserByAjax(new UserForModify(), modifyingId);
    });
  });
};

/**
 * 기존 입력값 채우기
 * 
 * @param {Object} user 
 */
var fillInputValues = function (user) {
  $("#modify_id").val(user.userId);
  $("#modify_name").val(user.userName);
  $("#modify_email").val(user.email);
  $("#modify_password").val("");
  $("#modify_password2").val("");
  fillUserAuthValue(user.userAuth);
  fillUseFlagValue(user.useFlag);
};

/**
 * 사용자 권한 채우기
 * 
 * @param {String} userAuth
 */
var fillUserAuthValue = function (userAuth) {
  if (userAuth === "admin") {
    $(".userAuth.admin").parent().addClass('checked');
    $(".userAuth.admin").prop("checked", true);
    $(".userAuth.user").parent().removeClass('checked');
  } else {
    $(".userAuth.admin").parent().removeClass('checked');
    $(".userAuth.user").prop("checked", true);
    $(".userAuth.user").parent().addClass('checked');
  }
};

/**
 * 사용 여부 채우기
 * 
 * @param {Boolean} useFlag 
 */
var fillUseFlagValue = function (useFlag) {
  if (useFlag === false) {
    $("#unuse").parent().addClass('checked');
    $("#unuse").prop("checked", true);
    $("#use").parent().removeClass('checked');
  } else {
    $("#use").parent().addClass('checked');
    $("#use").prop("checked", true);
    $("#unuse").parent().removeClass('checked');
  }
};

/**
 * 사용자 정보 수정을 위한 객체
 */
var UserForModify = function () {
  this.userName = $("#modify_name").val();
  this.email = $("#modify_email").val();
  this.userPw = $("#modify_password").val();
  this.userPw2 = $("#modify_password2").val();
  this.userAuth = $("input[name='iCheck_modify']:checked").val();
  this.useFlag = $("input[name='iCheck2_modify']:checked").val();
};

/**
 * 사용자 정보 수정 처리 로직
 * 
 * @param {Object} user 
 * @param {Number} modifyingId 
 */
var fnPatchUserByAjax = function (user, modifyingId, self) {
  if (!verify("PATCH", user)) {
    return;
  }

  if (!confirm("정말 변경하시겠습니까?")) {
    return;
  }

  var url = "/users/" + modifyingId;
  if (self === "self") {
    fnAjaxPatchUserDataSync(url, user, self);
  } else {
    fnAjaxPatchUserDataSync(url, user);
  }
};

/**
 * 사용자 정보 수정 요청
 * 
 * @param {String} url 
 * @param {Object} user 
 */
var fnAjaxPatchUserDataSync = function (url, user, self) {
  $.ajax({
    url: url,
    type: "PATCH",
    contentType: 'application/json',
    data: JSON.stringify(user),
    async: false,
    success: function success(response) {
      if (response.successMessage === "duplication userId") {
        fnComNotify("warning", "다른 아이디를 사용해주세요.");
      } else {
        if (self === "self") {
          $("#usermodalPopup_3").modal("hide");
        } else {
          $("#usermodalPopup_2").modal("hide");
        }
        fnComNotify("success", "정상적으로 변경되었습니다.");
        refreshPartially();
      }
    },
    error: function error() {
      fnComNotify("error", "정상적으로 변경되지 않았습니다.");
    }
  });
};

/**
 * 사용자 입력값 검증
 * 
 * @param {String} httpMethod 
 * @param {Object} user 
 * @param {String} self 
 */
var verify = function (httpMethod, user, self) {
  var validate = new Validate();

  if (httpMethod === "POST") {
    if (!verifyUserInputForRegister(validate, user)) {
      return false;
    }
  } else if (httpMethod === "PATCH") {
    if (!validate.init(user)) {
      return false;
    }
  }

  return true;
};

/**
 * 등록 관련 입력값 검증
 * 
 * @param {Object} validate 
 * @param {Object} user 
 */
var verifyUserInputForRegister = function (validate, user) {
  if (!(validate.verifyUserId(user.userId) && validate.init(user))) {
    return false;
  }

  return true;
};

/**
 * 사용자 삭제를 위한 배열 생성
 */
var usersForDelete = function () {
  var requestBody = {};
  var list = [];
  var checkBoxes = document.querySelectorAll("#tbodyHtml .icheckbox_flat-green.checked");

  for (var i = 0; i < checkBoxes.length; i++) {
    var stuff = checkBoxes[i].closest(".user_info").querySelector("#user_id").innerText;
    list.push(stuff);
  }

  requestBody.list = list;
  return requestBody;
};

/**
 * 검색 기능 처리 로직
 */
var searchLogic = function () {
  var option = $("#search_option option:selected").val();
  var value = $("#search_word").val();

  var paging = Object.create(Paging);
  paging.setOption(option);
  paging.setValue(value);
  paging.setAdmin(ADMIN);

  SEARCHED = true;
  showContents(paging);
};

/**
 * 관리자 
 */
var eventForAdmin = function () {
  $("#admin").on("click", function (event) {
    ADMIN = !ADMIN;
    event.preventDefault();
    var paging = Object.create(Paging);
    paging.setAdmin(ADMIN);

    if (SEARCHED) {
      var option = $("#search_option option:selected").val();
      var value = $("#search_word").val();

      paging.setOption(option);
      paging.setValue(value);
    }

    showContents(paging);
  });
};

/**
 * 사용자 정보 수정을 위한 객체
 */
var UserForSelfModify = {

  init() {
    var _this = this;

    var userId;
    $("#self_modify").on("click", function (event) {
      event.preventDefault();
      var user = fnGetUserByAjax("myself");
      userId = user.userId;

      _this.fillInputValuesForMySelf(user);
    });
    $("#alter_button").on("click", function () {
      var user = _this.User();
      fnPatchUserByAjax(user, userId, "self");
    });
  },

  /**
   * input 값 채우기
   * 
   * @param {Object} user 
   */
  fillInputValuesForMySelf(user) {
    $("#myself_id").val(user.userId);
    $("#myself_name").val(user.userName);
    $("#myself_email").val(user.email);
  },

  /**
   * 정보 수정을 위한 객체 생성
   */
  User() {
    var object = {};
    object.userName = $("#myself_name").val();
    object.email = $("#myself_email").val();
    object.userPw = $("#myself_password").val();
    object.userPw2 = $("#myself_password2").val();
    return object;
  }
}
