"use strict";

/**
 * ajax가 동작할 시 로딩 바 나타내기
 */
$(document).ajaxStart(function () {
  $('#loading').show();
}).ajaxStop(function () {
  $('#loading').hide();
});

$(function () {
  // a href="#" 적용 안되게 막기
  $(document).on('click', 'a[href="#"]', function (e) {
    e.preventDefault();
  });
});
/*********************************************** Common function start ***********************************************/

/* 
 * 동기화 ajax로 데이터 가져오기/보내기
 * url(필수) : url
 * errorMessage : 에러시 메시지
 * fn_success : 성공시 호출 될 함수
 * fnAjaxPostData
 */
var fnAjaxGetDataSync = function (url, errorMessage, fn_success) {
  $.ajax({
    url: url,
    type: "GET",
    async: false,
    success: function success(response) {
      if (fn_success) fn_success(response);
    },
    error: function error(response) {
      fnComNotify("error", "요청이 정상적으로 처리되지 않았습니다.");
    }
  });
};

/* 
 * 동기화 ajax로 데이터 가져오기/보내기
 * url(필수) : url
 * req_data : 파라메터. json 타입"
 * errorMessage : 에러시 메시지
 * fn_success : 성공시 호출 될 함수
 * fnAjaxPostData
 */
var fnAjaxDataSync = function (url, type, req_data, errorMessage, fn_success) {
  $.ajax({
    url: url,
    type: type,
    dataType: "json",
    contentType: 'application/json',
    data: req_data,
    async: false,
    success: function success(response) {
      if (fn_success) fn_success(response);
    },
    error: function error(response) {
      fnComErrorMessage(errorMessage + "(statusCode:" + response.status + ") \n" + response.responseJSON.detail, response.responseJSON.detail);
    }
  });
};

var fnAjaxDeleteDataSync = function (url, errorMessage, fn_success) {
  $.ajax({
    url: url,
    type: "DELETE",
    contentType: 'application/json',
    async: false,
    success: function success(response) {
      if (fn_success) fn_success(response);
    },
    error: function error(response) {
      fnComErrorMessage(errorMessage + "(statusCode:" + response.status + ") \n" + response.responseJSON.detail, response.responseJSON.detail);
    }
  });
};

function fnSetDatepicker() {
  $('.startDate').datepicker({
    language: 'ko',
    weekStart: 1,
    format: "yyyy-mm-dd"
  });
  $('.endDate').datepicker({
    language: 'ko',
    weekStart: 1,
    format: "yyyy-mm-dd"
  }); //	$(".startDate").val("2018-08-01");

  $(".startDate").val(fnGetDate("lastMonth").searchStartDate);
  $(".endDate").val(getFormatDate(new Date()));
}
/*********************************************** 페이징 처리 ***********************************************/

/*
divId : 페이징 태그가 그려질 div
pageIndx : 현재 페이지 위치가 저장될 input 태그 id
recordCount : 페이지당 레코드 수
totalCount : 전체 조회 건수
eventName : 페이징 하단의 숫자 등의 버튼이 클릭되었을 때 호출될 함수 이름
*/
var gfv_pageIndex = null;
var gfv_eventName = null;

function gfn_renderPaging(params) {
  var divId = params.divId; //페이징이 그려질 div id

  gfv_pageIndex = params.pageIndex; //현재 위치가 저장될 input 태그

  var totalCount = params.totalCount; //전체 조회 건수

  var currentIndex = $("#" + params.pageIndex).val(); //현재 위치

  if ($("#" + params.pageIndex).length == 0 || currentIndex.trim() == "") {
    currentIndex = 1;
  }

  var recordCount = params.recordCount; //페이지당 레코드 수

  if (recordCount == "") {
    recordCount = 20;
  }

  var totalIndexCount = Math.ceil(totalCount / recordCount); // 전체 인덱스 수

  gfv_eventName = params.eventName;
  $("#" + divId).empty();
  var preStr = "",
    postStr = "",
    str = "",
    first,
    last,
    prev,
    next;
  first = parseInt((currentIndex - 1) / 10) * 10 + 1;
  if (totalIndexCount == 0) last = 0; else {
    last = first + 9;
    if (last > totalIndexCount) last = totalIndexCount;
  }

  prev = parseInt((currentIndex - 1) / 10) * 10 - 9 > 0 ? parseInt((currentIndex - 1) / 10) * 10 - 9 : 1;
  next = (parseInt((currentIndex - 1) / 10) + 1) * 10 + 1 < totalIndexCount ? (parseInt((currentIndex - 1) / 10) + 1) * 10 + 1 : totalIndexCount;

  if (totalIndexCount > 10) {
    //전체 인덱스가 10이 넘을 경우, 맨앞, 앞 태그 작성
    preStr += "<a href='javascript:void(0);' class='first'  onclick='fnPageMove(1)'>처음</a>";
    preStr += "<a href='javascript:void(0);' class='prev' onclick='fnPageMove(" + prev + ")'>이전</a>";
  } else if (totalIndexCount <= 10 && totalIndexCount > 1) {//전체 인덱스가 10보다 작을경우, 맨앞 태그 작성

    /*preStr += "<a href='javascript:void(0);' class='first'  onclick='fnPageMove(1)'>처음</a>";*/
  }

  if (totalIndexCount > 10) {
    //전체 인덱스가 10이 넘을 경우, 맨뒤, 뒤 태그 작성
    postStr += "<a href='javascript:void(0);' class='next' onclick='fnPageMove(" + next + ")'>다음</a>";
    postStr += "<a href='javascript:void(0);' class='last' onclick='fnPageMove(" + totalIndexCount + ")'>마지막</a>";
  } else if (totalIndexCount <= 10 && totalIndexCount > 1) { } //전체 인덱스가 10보다 작을경우, 맨뒤 태그 작성

  /*postStr += "<a href='javascript:void(0);' class='last' onclick='fnPageMove("+next+")'>마지막</a>";*/
  //for(let i=first; i<(first+last); i++){


  for (var i = first; i <= last; i++) {
    if (i != currentIndex) {
      str += "<a href='javascript:void(0);' onclick='fnPageMove(" + i + ")'>" + i + "</a>";
    } else {
      str += "<a href='javascript:void(0);' class='active cursor_default'>" + i + "</a>";
    }
  }

  $("#" + divId).append(preStr + str + postStr);
  $("#" + divId + "_TOP").html(preStr + str + postStr);
}

function fnPageMove(value) {
  $("#" + gfv_pageIndex).val(value);

  if (typeof gfv_eventName == "function") {
    gfv_eventName(value);
  } else {
    eval(gfv_eventName + "(value);");
  }
}

/**
 * 페이징 처리
 * @param method 검색함수
 * @param totalCount 총 카운트
 * @returns
 */
function fnPagingPrc(method, totalCount) {
  var params = {
    divId: "PAGE_NAVI",
    pageIndex: "pageIndex",
    totalCount: totalCount,
    recordCount: 10,
    eventName: method
  };
  gfn_renderPaging(params);
}
/*********************************************** 페이징 처리 End ***********************************************/

/**
 * POST 방식 페이지 이동
 * @param url
 * @param keyList
 * @param valueList
 * @returns
 */
function fnMovePage(url, keyList, valueList) {
  var form = $('<form></form>');
  form.prop('action', url);
  form.prop('method', 'POST');
  form.appendTo('body');

  for (var i in keyList) {
    if (fnNotNullAndEmpty(keyList[i])) {
      form.append('<input type="hidden" name="' + keyList[i] + '" value="' + valueList[i] + '">');
    }
  }

  form.submit();
}

/**
 * Popup 페이지 이동
 * @param url
 * @param option
 * @param keyList
 * @param valueList
 * @returns
 */
function fnMovePopupPage(url, option, keyList, valueList) {
  if (!fnNotNullAndEmpty(option)) option = "width=1024,height=768";
  var form = $('<form></form>');
  form.prop('action', url);
  form.prop('method', 'POST');
  form.prop('target', 'movePoupuPage');
  form.appendTo('body');

  for (var i in keyList) {
    if (fnNotNullAndEmpty(keyList[i])) form.append('<input type="hidden" name="' + keyList[i] + '" value="' + valueList[i] + '">');
  }

  window.open(url, "movePoupuPage", option);
  form.submit();
}

/**
 * Get방식 페이지 이동
 * @param url
 * @param seq
 * @param option
 */
function fnGetMovePage(url, param1, param2) {
  if (fnNotNullAndEmpty(param1)) url += "?param1=" + encodeURIComponent(param1);
  if (fnNotNullAndEmpty(param2)) url += "&param2=" + encodeURIComponent(param2);
  location.href = url;
}

/**
 * Null 또는 공백 또는 undefined일 아닐 경우 true 반환
 * @param val
 * @returns {Boolean}
 */
function fnNotNullAndEmpty(val) {
  if (typeof val == 'undefined') return false; else if (val == null) return false; else if (val == "null") return false; else if ($.trim(val) == "") return false; else if (val.length < 1) return false; else return true;
}

/**
 * Null, 공백, undefined일 경우 separator로 반환
 * @param val
 * @param separator
 * @returns
 */
function fnReplaceNull(val, separator) {
  var sp = separator != null ? separator : "";
  if (typeof val == 'undefined') return sp; else if (val == null) return sp; else if (val == "null") return sp; else if (val.length < 1) return sp; else return val;
}

/**
 * 파일 다운로드
 * @param url
 * @param fileName
 * @param filePath
 * @param checkDelete
 */
function fnFileDownload(url, fileName, filePath, checkDelete) {
  var form = $('<form></form>');
  form.prop('action', url);
  form.prop('method', 'POST');
  form.appendTo('body');
  form.append('<input type="hidden" name="fileName" value="' + fileName + '">');
  form.append('<input type="hidden" name="filePath" value="' + filePath + '">');
  form.append('<input type="hidden" name="checkDelete" value="' + checkDelete + '">');
  form.submit();
}

/**
 *  yyyy-MM-dd 포맷으로 반환(separator)
 */
function getFormatDate(date, sep) {
  var separator = "-";
  if (fnNotNullAndEmpty(sep)) separator = sep;
  var year = date.getFullYear(); //yyyy

  var month = 1 + date.getMonth(); //M

  month = month >= 10 ? month : '0' + month; // month 두자리로 저장

  var day = date.getDate(); //d

  day = day >= 10 ? day : '0' + day; //day 두자리로 저장

  return year + separator + month + separator + day;
}

/**
 * 날짜 변경
 * @param option
 * @param fm
 * $.datepicker.formatDate 안될때...
 */
function fnGetDate(option, fm) {
  var nowDate = new Date();
  var format = "yy-mm-dd";
  if (fm != null) format = fm;

  var endDate = getFormatDate(nowDate);
  var startDate = "";

  if (option === "lastWeek") {
    nowDate.setDate(nowDate.getDate() - 7);
    startDate = getFormatDate(nowDate);
  } else if (option === "lastTwoWeek") {
    nowDate.setDate(nowDate.getDate() - 14);
    startDate = getFormatDate(nowDate);
  } else if (option === "lastThreeWeek") {
    nowDate.setDate(nowDate.getDate() - 21);
    startDate = getFormatDate(nowDate);
  } else if (option === "lastMonth") {
    nowDate.setMonth(nowDate.getMonth() - 1);
    startDate = getFormatDate(nowDate);
  } else if (option === "lastThreeMonth") {
    nowDate.setMonth(nowDate.getMonth() - 3);
    startDate = getFormatDate(nowDate);
  } else if (option === "lastYear") {
    nowDate.setFullYear(nowDate.getFullYear() - 1);
    startDate = getFormatDate(nowDate);
  } else if (option === "today") {
    startDate = getFormatDate(nowDate);
  }

  var date = {
    "searchStartDate": startDate,
    "searchEndDate": endDate
  };
  return date;
}

/**
 * 현재 날짜와 비교 후 true/false 반환
 * @param date
 * @returns {Boolean}
 */
function fnCompareNowDate(date) {
  var result = false;

  if (date != null) {
    var nowDate = new Date();
    var expireDate = new Date(date);
    if (expireDate > nowDate) result = true;
  }

  return result;
}

/**
 * 에러 메시지창
 */
function fnErrorMessage(error) {
  fnComNotify("error", "서비스가 원활하지 않습니다. 잠시뒤에 이용해주시길 바랍니다.");
  console.log(error);
}

/*숫자만 입력가능*/
function fnOnlyNumber(event) {
  event = event || window.event;
  var keyID = event.which ? event.which : event.keyCode;

  if (keyID >= 48 && keyID <= 57 || keyID >= 96 && keyID <= 105 || keyID == 8 || keyID == 9 || keyID == 46 || keyID == 37 || keyID == 39) {
    return;
  } else {
    return false;
  }
}

/*숫자, 콤마 입력*/
function fnOnlyNumberDot(event) {
  event = event || window.event;
  var keyID = event.which ? event.which : event.keyCode; // Comma keyID == 188 , Dot keyID == 190

  if (keyID == 190 || keyID >= 48 && keyID <= 57 || keyID >= 96 && keyID <= 105 || keyID == 8 || keyID == 9 || keyID == 46 || keyID == 37 || keyID == 39) {
    return;
  } else {
    return false;
  }
}

/*숫자 . , / *  입력*/
function fnOnlyNumberCommaDot(event) {
  event = event || window.event;
  var keyID = event.which ? event.which : event.keyCode; // Comma keyID == 188 , Dot keyID == 190

  if (keyID == 188 || keyID == 190 || keyID == 191 || keyID == 56 || keyID >= 48 && keyID <= 57 || keyID >= 96 && keyID <= 105 || keyID == 8 || keyID == 9 || keyID == 46 || keyID == 37 || keyID == 39) {
    return;
  } else {
    return false;
  }
}

/* 숫자, 콤마 제외한 나머지 제거 */
function fnRemoveChar(event) {
  event = event || window.event;
  var keyID = event.which ? event.which : event.keyCode;

  if (keyID == 8 || keyID == 9 || keyID == 46 || keyID == 37 || keyID == 39) {
    return;
  } else {
    event.target.value = event.target.value.replace(/[^0-9|^.]/g, "");
  }
}

/**
 * 긴 단어/문장 변경
 * @param text
 * @param separator
 * @param limit
 * @returns
 */
function fnLongWordTranslation(text, separator, limit) {
  if (!fnNotNullAndEmpty(separator)) separator = "...";
  if (!fnNotNullAndEmpty(limit)) limit = 20;

  if (fnNotNullAndEmpty(text) && text.length > limit) {
    text = text.substring(0, limit) + separator;
  }

  return text;
}

/**
 * 3자리 콤마 찍기
 * @param text
 * @returns
 */
var numberWithCommas = function (x) {
  if (fnNotNullAndEmpty(x)) {
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
  } else {
    return "";
  }
};

/**
 * Null을 -1로 변경
 * @param val
 * @returns
 */
function fnChangeNullToNum(val) {
  if (typeof val == 'undefined') return -1; else if (val == null) return -1; else if (val.length < 1) return -1;
  return val;
}

/**
 * Object sort
 * @param obj
 * @param option
 * @returns
 */
function fnSortObj(obj, option) {
  if (fnNotNullAndEmpty(obj)) {
    // keyword sort asc
    obj.sort(function (a, b) {
      if (a.keyword > b.keyword) return 1;
      if (a.keyword < b.keyword) return -1;
      return 0;
    }); // keyword_id sort desc

    obj.sort(function (a, b) {
      if (fnChangeNullToNum(a.keyword_id) < fnChangeNullToNum(b.keyword_id)) return 1;
      if (fnChangeNullToNum(a.keyword_id) > fnChangeNullToNum(b.keyword_id)) return -1;
      return 0;
    }); // weight sort desc

    obj.sort(function (a, b) {
      if (fnChangeNullToNum(a.weight) < fnChangeNullToNum(b.weight)) return 1;
      if (fnChangeNullToNum(a.weight) > fnChangeNullToNum(b.weight)) return -1;
      return 0;
    });

    if (option == "string") {
      return fnArrValueToString(obj, "keyword", " ,", 5);
    } else {
      return obj;
    }
  }
}

/**
 * 관련기관 및 인문 sort
 * @param personOrgan
 * @param option
 * @returns
 */
function fnSortPersonOrgan(personOrgan, option) {
  if (fnNotNullAndEmpty(personOrgan)) {
    // name sort asc
    personOrgan.sort(function (a, b) {
      if (a.name > b.name) return 1;
      if (a.name < b.name) return -1;
      return 0;
    }); // type sort desc

    personOrgan.sort(function (a, b) {
      if (a.type < b.type) return 1;
      if (a.type > b.type) return -1;
      return 0;
    });

    if (option == "string") {
      return fnArrValueToString(personOrgan, "name", " ,", 5);
    } else {
      return personOrgan;
    }
  } else {
    return "-";
  }
}

/**
 * 배열의 value를 한줄로 생성
 * @param dataArr
 * @param value
 * @param separator
 * @param count
 * @returns {String}
 */
function fnArrValueToString(dataArr, value, separator, count) {
  var result = "-";

  if (fnNotNullAndEmpty(dataArr)) {
    for (var index in dataArr) {
      if (fnNotNullAndEmpty(value) && value === "keyword" && fnNotNullAndEmpty(dataArr[index].keyword)) {
        if (index == 0) result = dataArr[index].keyword; else result += separator + "&nbsp;" + dataArr[index].keyword;
      } else if (fnNotNullAndEmpty(value) && value === "name" && fnNotNullAndEmpty(dataArr[index].name)) {
        if (index == 0) result = dataArr[index].name; else result += separator + "&nbsp;" + dataArr[index].name;
      } else if (fnNotNullAndEmpty(dataArr[index]) && value == null) {
        if (index == 0) result = dataArr[index]; else result += separator + "&nbsp;" + dataArr[index];
      }

      if (fnNotNullAndEmpty(count) && index == count) break;
    }
  }

  return result;
}

//한글체크
var fnCheckKorean = function (value) {
  var check = /[ㄱ-ㅎ|ㅏ-ㅣ|가-힣]/;

  if (check.test(value)) {
    return true;
  } else {
    return false;
  }
};
/*********************************************** 입력값 검증 ***********************************************/
var Validate = (function () {

  function Validate() {

  }

  Validate.prototype.init = function (user) {
    if (!(this.verifyUserName(user.userName) && this.verifyEmail(user.email) && this.verifyPassword(user.userPw, user.userPw2))) {
      return false;
    }

    return true;
  }

  /**
   * 빈 칸인지 검증
   */
  Validate.prototype.verifyBlank = function (value) {
    if (value == null || value == "") {
      return false;
    }

    return true;
  }

  /**
   * 사용자 ID 검증
   */
  Validate.prototype.verifyUserId = function (userId) {
    if (this.verifyBlank(userId)) {
      var regExpId = /^(?=.*?[a-zA-Z])(?=.*?[0-9]).{4,12}$/;

      if (!regExpId.test(userId)) {
        fnComNotify("warning", "아이디를 형식에 맞게 입력해주세요.");
        return false;
      }
    } else {
      fnComNotify("warning", "아이디를 입력해주세요.");
      return false;
    }

    return true;
  }

  /**
   * 사용자 이름 검증
   */
  Validate.prototype.verifyUserName = function (userName) {
    if (this.verifyBlank(userName)) {
      var regExpName = /^[가-힣a-zA-Z]+$/;

      if (!regExpName.test(userName)) {
        fnComNotify("warning", "이름을 형식에 맞게 입력해주세요.");
        return false;
      }
    } else {
      fnComNotify("warning", "이름을 입력해주세요.");
      return false;
    }

    return true;
  }

  /**
   * 이메일 검증
   */
  Validate.prototype.verifyEmail = function (email) {
    if (this.verifyBlank(email)) {
      var regExpEmail = /^[0-9a-zA-Z]([-_\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\.]?[0-9a-zA-Z])*\.[a-zA-Z]{2,3}$/i;

      if (!regExpEmail.test(email)) {
        fnComNotify("warning", "이메일을 형식에 맞게 입력해주세요.");
        return false;
      }
    } else {
      fnComNotify("warning", "이메일을 입력해주세요.");
      return false;
    }

    return true;
  }

  /**
   * 비밀번호 검증
   */
  Validate.prototype.verifyPassword = function (userPw, userPw2) {
    if (!(this.verifyBlank(userPw) && this.verifyBlank(userPw2))) {
      fnComNotify("warning", "비밀번호 칸을 모두 입력해주세요.");
      return false;
    }

    if (userPw !== userPw2) {
      fnComNotify("warning", "비밀번호 칸과 비밀번호 확인 칸의 입력값이 다릅니다.");
      return false;
    }

    var regExpPw = /^(?=.*?[a-zA-Z])(?=.*?[0-9]).{8,15}$/;
    if (regExpPw.test(userPw)) {
      return true;
    }

    if (userPw.length > 15 || userPw.length < 8) {
      fnComNotify("warning", "비밀번호 길이는 8~15 자입니다.");
      return false;
    }

    var numExp = /[0-9]/;
    if (numExp.test(userPw)) {
      fnComNotify("warning", "비밀번호에 영문자를 추가해주세요.");
      return false;
    }

    var charExp = /[a-zA-Z]/;
    if (charExp.test(userPw)) {
      fnComNotify("warning", "비밀번호에 숫자를 추가해주세요.");
      return false;
    }

    return false;
  }

  return Validate;
}())

/**
 * 사용자 등록을 위한 요청
 * 
 * @param {String} url 
 * @param {Object} user 
 */
var fnPostUserByAjax = function (url, user) {
  var result = null;
  $.ajax({
    url: url,
    type: "POST",
    contentType: 'application/json',
    data: JSON.stringify(user),
    async: false,
    success: function success(response) {
      result = response;
    }
  });
  return result;
};

/**
 * 사용자 개별 조회 요청
 * 
 * @param {String} id 
 */
var fnGetUserByAjax = function (id) {
  var result;
  var url = "/users/" + id;
  var errorMessage = "사용자 조회 에러";

  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.user;
  });
  return result;
};

// 눈표시 클릭 시 패스워드 보이기 
var toggleEye = function () {
  $('.eye').on('click', function (event) {
    event.stopImmediatePropagation();

    var target = event.target.previousElementSibling;

    if (target.classList.contains('active') === false) {
      target.type = "text";
      event.target.classList.toggle("fa-eye-slash");
      event.target.classList.toggle("fa-eye");
    } else {
      target.type = "password";
      event.target.classList.toggle("fa-eye-slash");
      event.target.classList.toggle("fa-eye");
    }

    target.classList.toggle('active');
  });
};

/**
 * 전체 선택
 */
var checkAll = function () {
  $("#check_all").on("ifChecked", function () {
    $(".icheckbox_flat-green").iCheck("check");
  });
};

/**
 * 전체 선택 해제
 */
var freeCheckAll = function () {
  $(".icheckbox_flat-green").on("ifUnchecked", function () {
    $("#check_all").iCheck('indeterminate');
  });

  $("#check_all").on("ifUnchecked", function () {
    $(".icheckbox_flat-green").iCheck("uncheck");
  });
};

// 검색 기능
var search = function (searchLogic) {
  $("#search_button").on("click", function () {
    searchLogic();
  });
  $("#search_word").keydown(function (key) {
    var ENTER = 13;

    if (key.keyCode === ENTER) {
      searchLogic();
      return false;
    }
  });
};

/**
 * 삭제 기능 객체
 */
var Deletion = {

  init: function (url, objectForDelete, showContents) {
    var _this = this;
    $("#delete_button").on("click", function () {
      var requestBody = objectForDelete();

      if (requestBody === '') {
        return;
      }

      if (requestBody.list.length === 0) {
        fnComNotify("warning", "삭제할 요소를 선택해주세요");
        return;
      }

      if (confirm("정말 삭제하시겠습니까?")) {
        _this.fnAjaxDeleteDataSync(url, requestBody, showContents);
      }
    });
  },

  /**
   * 삭제 요청
   * 
   * @param {String} url 
   * @param {Object} requestBody 
   */
  fnAjaxDeleteDataSync: function (url, requestBody, showContents) {
    $.ajax({
      url: url,
      type: "DELETE",
      dataType: 'json',
      contentType: 'application/json',
      data: JSON.stringify(requestBody),
      async: false,
      success: function success() {
        fnComNotify("success", "삭제가 정상적으로 처리되었습니다.");
        refreshPartially();
      },
      error: function error() {
        fnComNotify("warning", "삭제할 요소를 선택해주세요.");
      }
    });
  }
}

/*********************************************** 페이징 ***********************************************/
var Paging = {
  _rowNumber: 1,
  _currentPage: 0,
  _countPerPage: 10,
  _totalPage: undefined,
  _option: undefined,
  _value: undefined,
  _startDate: undefined,
  _endDate: undefined,
  _mine: false,
  _admin: false,

  init: function (location, getList, makeList, defineDataTable) {
    this.makePageNumbers();
    this.changeDisabledEffect();
    this.addEventOnPageButton(location, getList, makeList, defineDataTable);
  },

  setRowNumber: function (number) {
    this._rowNumber = number;
  },

  getRowNumber: function () {
    return this._rowNumber;
  },

  getCurrentPage: function () {
    return this._currentPage;
  },

  setTotalPage: function (page) {
    this._totalPage = page;
  },

  getTotalPage: function (_totalPage) {
    return this._totalPage;
  },

  getOption: function () {
    return this._option;
  },

  setOption: function (option) {
    this._option = option;
  },

  getValue: function () {
    return this._value;
  },

  setValue: function (value) {
    this._value = value;
  },

  getStartDate: function () {
    return this._startDate;
  },

  setStartDate: function (startDate) {
    this._startDate = startDate;
  },

  getEndDate: function () {
    return this._endDate;
  },

  setEndDate: function (endDate) {
    this._endDate = endDate;
  },

  setMine: function (mine) {
    this._mine = mine;
  },

  getMine: function () {
    return this._mine;
  },

  setAdmin: function (admin) {
    this._admin = admin;
  },

  getAdmin: function () {
    return this._admin;
  },

  /**
   * 컨텐츠 개수에 따른 적정 페이지 개수 나타내기
   */
  makePageNumbers: function () {
    var pagingPart = $(".pagination");
    var page = 2;
    var html = "";
    html += "<li class='paginate_button previous disabled'";
    html += "    id='datatable-buttons_previous'>";
    html += "    <a href='#'>Previous</a>";
    html += "</li>";
    html += "    <li class='paginate_button active'><a href='#'>1</a></li>";

    for (var i = 0; i < this._totalPage; i++) {
      html += "    <li class='paginate_button'><a href='#'>" + page++ + "</a></li>";
    }

    html += "    <li class='paginate_button next'";
    html += "        id='datatable-buttons_next'>";
    html += "        <a href='#'>Next</a>";
    html += "    </li>";
    pagingPart.html(html);
  },

  /**
   * 페이지 이벤트 추가
   * 
   * @param {Element} location 
   * @param {Function} getList 
   * @param {Function} makeList 
   * @param {Function} defineDataTable 
   */
  addEventOnPageButton: function (location, getList, makeList, defineDataTable) {
    var _this2 = this;

    $(".paginate_button").on("click", function (event) {
      event.preventDefault();

      if (event.target.parentNode.classList.contains("disabled")) {
        return;
      }

      if (_this2.verifyPageButton(event.target.innerText) === false) {
        return;
      }

      _this2.operateButton(location, event.target, _this2, getList, makeList, defineDataTable);
    });
  },

  /**
   * 유효하지 않은 페이지 클릭시 동작 안하게
   * 
   * @param {String | Number} clickedPage 
   */
  verifyPageButton: function (clickedPage) {
    if (this._currentPage === clickedPage - 1
      || this._currentPage === 0 && clickedPage === "Previous"
      || this._currentPage === this._totalPage && clickedPage === "Next") {
      return false;
    }

    return true;
  },

  /**
   * 페이지 버튼 클릭시 동작할 로직
   * 
   * @param {Element} location 
   * @param {Element} target 
   * @param {Object} paging 
   * @param {Function} getList 
   * @param {Function} makeList 
   * @param {Function} defineDataTable 
   */
  operateButton: function (location, target, paging, getList, makeList, defineDataTable) {
    var previousActivedPage = $(".paginate_button.active");

    if (target.innerText === "Next" && paging.getCurrentPage() < paging.getTotalPage()) {
      previousActivedPage.next().addClass("active");
    } else if (target.innerText === "Previous" && paging.getCurrentPage() > 0) {
      previousActivedPage.prev().addClass("active");
    } else {
      target.parentNode.classList.add("active");
    }

    previousActivedPage.removeClass("active");
    this.operatePage(location, target.innerText, paging, getList, makeList, defineDataTable);
  },

  /**
   * 새로운 콘텐츠 나타내기
   * 
   * @param {Element} location 
   * @param {Element} target 
   * @param {Object} paging 
   * @param {Function} getList 
   * @param {Function} makeList 
   * @param {Function} defineDataTable 
   */
  operatePage: function (location, targetPage, paging, getList, makeList, defineDataTable) {
    this.changePageValue(targetPage);
    $(location).DataTable().destroy();
    var list = getList(paging);
    $("#tbodyHtml").html(makeList(list, paging.getRowNumber()));
    defineDataTable();
  },

  /**
   * 페이지 클릭에 따른 효과 변경
   * 
   * @param {String | Number} targetPage 
   */
  changePageValue: function (targetPage) {
    if (targetPage === "Next") {
      this._currentPage += 1;
    } else if (targetPage === "Previous") {
      this._currentPage -= 1;
    } else {
      this._currentPage = targetPage - 1;
    }

    this._rowNumber = this._currentPage * 10 + 1;
    this.changeDisabledEffect();
  },

  /**
   * disabled 효과 주기
   */
  changeDisabledEffect: function () {
    var previousPage = $(".paginate_button.previous");
    var nextPage = $(".paginate_button.next");

    if (this._currentPage > 0) {
      previousPage.removeClass("disabled");
    } else if (this._currentPage === 0) {
      previousPage.addClass("disabled");
    }

    if (this._currentPage === this._totalPage || this._totalPage === -1) {
      nextPage.addClass("disabled");
    } else if (this._currentPage < this._totalPage) {
      nextPage.removeClass("disabled");
    }
  },
}

/**
 * 다운로드
 * 
 * @param {String} id 
 * @param {Element} element 
 */
var download = function (id, element) {
  $("#" + id).on("click", ".downloadable", function (event) {
    if (!confirm("다운로드 하시겠습니까?")) {
      return;
    }

    var target = event.target.closest(element);
    var domainId = target.dataset.id;
    var learningType = target.dataset.type;
    var dataType = event.target.value;
    var url = "/domains/" + domainId + "/" + learningType + "/" + dataType + "/download";
    window.location.href = url;
  });
};

/**
 * 테스트
 * 
 * @param {String} id 
 * @param {Element} element 
 */
var modelTest = function (id, element) {
  $("#" + id).on("click", ".domain_test", function (event) {
    $("#modelTestResult").html("");
    $("#testResultTable").html("");
    $("#modelResultChart").html("");

    var domainId = event.target.closest(element).dataset.id;
    var type = event.target.closest(element).dataset.type;
    var modelId = event.target.dataset.modelId;
    SELECTED_MODEL_ID = modelId;

    if (type === 'D') {
      setModelDataInfo(modelId);
    }
    execTest(domainId, type);
  });
};

/**
 * 테스트 로직
 * 
 * @param {Number} domainId 
 * @param {String} type 
 */
var execTest = function (domainId, type) {
  $("#testButton").off("click").on("click", function () {
    if (type === 'D') {
      fn_deeplearningTest();
      return;
    }

    var domain = fnGetDomainByAjax(domainId);
    PROJECT_SEQUENCE_PK = domain.projectId;
    SELECTED_MODEL_PK = domain.modelId;

    var score = getHoldoutScore(SELECTED_MODEL_PK);
    MODEL_TRAIN_SCORE = JSON.parse(score.value).holdout_score;
    fnModelTest(true);
  })
}

/**
 * 모델 테스트 점수 조회 요청
 * 
 * @param {Number} modelSequencePk 
 */
var getHoldoutScore = function (modelSequencePk) {
  var result;
  url = "/models/" + modelSequencePk;
  errorMessage = "모델 테스트 점수 조회 에러";

  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.score.validation_summary;
  });
  return result;
};

/**
 * 도메인 개별 조회 요청
 * 
 * @param {String} id 
 */
var fnGetDomainByAjax = function (id) {
  var result;
  url = "/domains/" + id;
  errorMessage = "도메인 조회 에러";

  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.domain;
  });
  return result;
};


/**
 * 등록, 수정, 삭제 비동기 작업 후 부분적 새로고침
 */
var refreshPartially = function () {
  $("#search_word").val("");
  var paging = Object.create(Paging);
  showContents(paging);
}

/**
 * 이미지 클릭시 팝업
 */
var popupImg = function (className) {
  var img = document.getElementsByClassName(className);
  var popupX = (window.screen.width / 2) - (800 / 2);
  var popupY = (window.screen.height / 2) - (400 / 2);
  for (var x = 0; x < img.length; x++) {
      img.item(x).onclick = function () {
          window.open(this.src, "new", "width=800, height=500, left="
              + popupX + ", top=100, scrollbars=no,titlebar=no,status=no,resizable=no,fullscreen=no");
          console.log(this.src);
      };
  }
}

/*********************************************** Common function end ***********************************************/
