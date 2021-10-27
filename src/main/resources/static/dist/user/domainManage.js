"use strict";

var SEARCHED = false;
var MINE = false;

$("#loading").show();
document.addEventListener("DOMContentLoaded", function () {
  setDatePicker("single_cal1");
  setDatePicker("single_cal2");
  fnInit();
});

/**
 * 달력 인터페이스 변경
 * 
 * @param {String} id 
 */
var setDatePicker = function (id) {
  $("#" + id).datepicker({
    dateFormat: 'yy/mm/dd',
    monthNamesShort: ['1월', '2월', '3월', '4월', '5월', '6월', '7월', '8월', '9월', '10월', '11월', '12월'],
    dayNamesMin: ['일', '월', '화', '수', '목', '금', '토'],
    changeMonth: true,
    changeYear: true,
    showMonthAfterYear: true // 년 뒤에 월표시
  }).datepicker("setDate", new Date());
};

var fnInit = function () {
  fillSelects();
  var paging = Object.create(Paging);
  showContents(paging);
  doMyDomain();
  search(searchLogic);
  registerDomain();
  modifyDomain();
  download("tbodyHtml", "tr");
  modelTest("tbodyHtml", "tr");
  var deletion = Object.create(Deletion);
  deletion.init("/domains", domainsForDelete, showContents);
};

/**
 * 도메인 목록 나타내기
 */
var showContents = function (paging) {
  $("#domain_board").DataTable().destroy();
  var domainList = fnGetDomainsByAjax(paging);
  paging.init("#domain_board", fnGetDomainsByAjax, fnCreateListHtml, defineDataTable);
  $("#tbodyHtml").html(fnCreateListHtml(domainList, paging.getRowNumber()));
  defineDataTable();
}

/**
 * 테이블 정의
 */
var defineDataTable = function () {
  $('#domain_board').dataTable({
    'info': false,
    'order': [],
    "aoColumnDefs": [{
      'bSortable': false,
      'aTargets': [0]
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
      type();
    }
  });
};

/**
 * 도메인 목록 조회
 * 
 * @param {Object} paging 
 */
var fnGetDomainsByAjax = function (paging) {
  var result;
  url = getUrl(paging);
  errorMessage = "도메인 목록 조회 에러";

  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.domains;
    paging.setTotalPage(Math.floor((response.count - 1) / 10));
  });
  return result;
};

/**
 * 도메인 목록 조회를 위한 api 주소 가져오기
 * 
 * @param {Object} paging 
 */
var getUrl = function (paging) {
  var url = "/domains?page=" + paging.getCurrentPage();

  var type = $('#type option:selected').val();
  if (type !== 'all') {
    url += "&type=" + type;
  }

  var mine = paging.getMine();
  if (mine) {
    url += "&mine=" + mine;
  }

  var option = paging.getOption();
  if (option !== undefined) {
    option = option.replace(/&/g, "%26");
    url += "&option=" + option;
  }

  var value = paging.getValue();
  if (value !== undefined && value !== '') {
    url += "&value=" + value;
  }

  var startDate = paging.getStartDate();
  if (startDate !== undefined) {
    url += "&startDate=" + startDate;
  }

  var endDate = paging.getEndDate();
  if (endDate !== undefined) {
    url += "&endDate=" + endDate;
  }

  return url;
}

/**
 * 도메인 목록 생성
 * 
 * @param {Arrays} domains 
 * @param {Number} rowNumber 
 */
var fnCreateListHtml = function (domains, rowNumber) {
  var html = "";

  for (var i in domains) {
    var data = domains[i];
    html += domainTemplate(data, rowNumber++);
  }

  return html;
};

/**
 * 도메인을 나타내기 위한 html 템플릿
 * 
 * @param {Object} data 
 * @param {Number} rowNumber 
 */
var domainTemplate = function (data, rowNumber) {
  var html = "";

  html += "<tr data-id=" + data.id + " data-user-id=" + data.userId + " data-type=" + data.type + ">";
  html += "    <td class='a-center'><input type='checkbox' class='flat' name='table_records'></td>";
  html += "    <td>" + rowNumber + "</td>";
  html += "    <td>" + data.categoryName + "</td>";
  html += "    <td class='bold table_title'>";
  html += "    <span class='domain_modify cusor disabled' data-toggle='modal' data-target='.domainModifyModal' disabled>" + data.title + "</span>";
  html += "        <div class='btngroup domaintitle_btn'>";
  html += "            <button class='downloadable btn btn-xs' value='model' type='button'>모델</button>";

  if (data.type === "M") {
    html += "            <button class='downloadable btn btn-xs' value='preprocessedData' type='button'>전처리 데이터</button>";
  }

  html += "            <button class='downloadable btn btn-xs' value='originalData' type='button'>원본 데이터</button>";
  html += "            <button class='btn btn-xs domain_test' data-model-id=" + data.modelId + " type='button' data-toggle='modal' data-target='.modelTestModal'>테스트</button>";
  html += "        </div>";
  html += "    </td>";
  html += "    <td>" + data.registerer + "</td>";
  html += "    <td>" + data.createDate + "</td>";
  html += "</tr>";
  
  return html;
};

/**
 * 마이 도메인
 */
var doMyDomain = function () {
  $("#my_domain").on("click", function () {
    clickMyDomainEffect(this);

    MINE = !MINE;
    var paging = Object.create(Paging);
    paging.setMine(MINE);

    if (SEARCHED) {
      paging = setPaging(paging);
    }

    showContents(paging);
    return paging;
  });
};

/**
 * 마이 도메인 클릭시 효과
 * 
 * @param {Element} _this 
 */
var clickMyDomainEffect = function (_this) {
  $(_this).toggleClass("active");
  if ($(_this).hasClass("active")) {
    $(_this).text("전체 도메인");
  } else {
    $(_this).text("마이 도메인");
  }
}

/**
 * 페이징 객체의 상태 변경
 * 
 * @param {Object} paging 
 */
var setPaging = function (paging) {
  var option = $("#search_option option:selected").text();
  var value = $("#search_word").val();
  var startDate = $("#single_cal1").val();
  var endDate = $("#single_cal2").val();
  endDate = tomorrow(endDate);

  paging.setOption(option);
  paging.setValue(value);
  paging.setStartDate(startDate);
  paging.setEndDate(endDate);
  paging.setMine(MINE);

  return paging;
}

/**
 * 도메인 삭제를 위한 객체
 */
var domainsForDelete = function () {
  var requestBody = {};
  var list = [];
  var currentUserId = $("#userId").val();
  var currentUserAuth = $("#userAuth").val();
  var checkBoxes = document.querySelectorAll("#tbodyHtml .icheckbox_flat-green.checked");

  for (var i = 0; i < checkBoxes.length; i++) {
    var container = checkBoxes[i].closest("tr");

    if (currentUserAuth === 'user' && currentUserId !== container.dataset.userId) {
      fnComNotify("warning", "자신이 만든 도메인만 삭제할 수 있습니다.");
      return '';
    }

    var stuff = container.dataset.id;
    list.push(parseInt(stuff));
  }

  requestBody.list = list;
  return requestBody;
};

/**
 * 도메인 검색 처리 로직
 */
var searchLogic = function () {
  var paging = Object.create(Paging);
  paging = setPaging(paging);
  showContents(paging);
  SEARCHED = true;
  return paging;
};

/**
 * 다음 날짜로 변경
 * 
 * @param {String} target 
 */
var tomorrow = function (target) {
  var targetArr = target.split("/");
  var year = targetArr[0];
  var month = parseInt(targetArr[1] - 1);
  var day = targetArr[2];
  var nowDate = new Date(year, month, day);
  var tomorrowDate = nowDate.getTime() + 1 * 24 * 60 * 60 * 1000; // 하루를 초로 계산한 것을 더함

  nowDate = new Date(tomorrowDate);
  return format(nowDate);
};

/**
 * 날짜를 포맷화
 * 
 * @param {Date} date 
 */
var format = function format(date) {
  var year = date.getFullYear();

  var month = date.getMonth() + 1;
  if (month < 10) {
    month = "0" + month;
  }

  var tomorrow = date.getDate();
  if (tomorrow < 10) {
    tomorrow = "0" + tomorrow;
  }

  return year + "/" + month + "/" + tomorrow;
};

/**
 * option 채우기
 */
var fillSelects = function () {
  fillCategorySelect();
  fillProjectSelect();
  fillModelSelect();
};

/**
 * 카테고리 select 채우기
 */
var fillCategorySelect = function () {
  var html = getCategoryHtml();
  $("#search_option").append(html);
  $("#register_category").append(html);
  $("#modify_category").append(html);
};

/**
 * 프로젝트 select 채우기
 */
var fillProjectSelect = function () {
  var html = getProjectHtml();
  $("#register_project").html(html);
  $("#modify_project").html(html);
};

/**
 * 모델 select 채우기
 */
var fillModelSelect = function () {
  var projectPk = $("#register_project option:selected").val();
  var projectType = $("#register_project option:selected").data('type');
  var html = getModelHtml(projectPk, projectType);
  $("#register_model").html(html);
  $("#modify_model").html(html);

  $("#register_project").on("change", function () {
    projectPk = $("#register_project option:selected").val();
    projectType = $("#register_project option:selected").data('type');
    var registerModelHtml = getModelHtml(projectPk, projectType);
    $("#register_model").html(registerModelHtml);
  });

  $("#modify_project").on("change", function () {
    projectPk = $("#modify_project option:selected").val();
    projectType = $("#modify_project option:selected").data('type');
    var modifyModelHtml = getModelHtml(projectPk, projectType);
    $("#modify_model").html(modifyModelHtml);
  });
};

/**
 * 카테고리 목록 조회
 */
var getCategories = function () {
  var result;
  url = "/categories";
  errorMessage = "카테고리 목록 조회 에러";

  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.categories;
  });
  return result;
};

/**
 * 카테고리 목록 나타내기
 */
var getCategoryHtml = function () {
  var categories = getCategories();
  var html = "";

  for (var i = 0; i < categories.length; i++) {
    var data = categories[i];
    html += "<option value=" + data.name + ">" + data.name + "</option>";
  }

  return html;
};

/**
 * 프로젝트 목록 조회
 */
var getProjects = function () {
  var result;
  url = "/projectsName";
  errorMessage = "프로젝트 목록 조회 에러";

  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.projects;
  });
  return result;
};

/**
 * 프로젝트 목록 나타내기
 */
var getProjectHtml = function () {
  var projects = getProjects();
  if (projects.length === 0) {
    return "";
  }

  var html = "";
  for (var i = 0; i < projects.length; i++) {
    var data = projects[i];
    html += "<option data-type=" + data.type + " value=" + data.id + ">" + data.name + "</option>";
  }

  return html;
};

/**
 * 모델 목록 조회
 * 
 * @param {Number} projectPk 
 */
var getModels = function (projectPk, projectType) {
  var result;
  url = "/projects/" + projectPk + "/models?projectType=" + projectType;
  errorMessage = "모델 목록 조회 에러";

  fnAjaxGetDataSync(url, errorMessage, function (response) {
    result = response.modelsList;
  });
  return result;
};

/**
 * 모델 목록 나타내기
 * 
 * @param {Number} projectPk 
 */
var getModelHtml = function (projectPk, projectType) {
  if (projectPk == null || projectType == null) {
    return "";
  }

  var models = getModels(projectPk, projectType);
  var html = "";

  for (var i = 0; i < models.length; i++) {
    var data = models[i];
    var name;
    if (data.name == null) {
      name = data.filename.split(".")[0];
    } else {
      name = data.name;
    }

    html += "<option value=" + data.id + ">" + name + "</option>";
  }

  return html;
};


/**
 * 도메인 등록
 */
var registerDomain = function () {
  $("#register_button").on("click", function (event) {
    var domain = new DomainForRegister();

    if (!verify(domain)) {
      event.stopPropagation();
      return;
    }

    if (!confirm("정말 등록하시겠습니까?")) {
      return;
    }

    var response = fnPostDomainByAjax("/domains", domain);
    if (response.result === "success") {
      fnComNotify("success", "도메인이 정상적으로 등록되었습니다.");
      refreshPartially();
    } else {
      fnComNotify("success", "요청이 정상적으로 처리되지 않았습니다.");
    }
  });
};

/**
 * 도메인 등록을 위한 객체
 */
var DomainForRegister = function () {
  this.categoryName = $("#register_category option:selected").val();
  this.title = $("#register_title").val();
  this.description = $("#register_description").val();
  this.projectId = $("#register_project option:selected").val();
  this.modelId = $("#register_model option:selected").val();
};

/**
 * 도메인 등록 관련 입력값 검증
 * 
 * @param {Object} domain 
 */
var verify = function (domain) {
  var validate = new Validate();

  if (!validate.verifyBlank(domain.title)) {
    fnComNotify("warning", "도메인 제목을 입력해주세요.");
    return false;
  }

  if (!validate.verifyBlank(domain.projectId)) {
    fnComNotify("warning", "프로젝트를 선택해주세요.");
    return false;
  }

  if (!validate.verifyBlank(domain.modelId)) {
    fnComNotify("warning", "모델을 선택해주세요.");
    return false;
  }

  return true;
};

/**
 * 도메인 등록 요청
 * 
 * @param {String} url 
 * @param {Object} domain 
 */
var fnPostDomainByAjax = function (url, domain) {
  var result = null;
  $.ajax({
    url: url,
    type: "POST",
    contentType: 'application/json',
    data: JSON.stringify(domain),
    async: false,
    success: function success(response) {
      result = response;
    }
  });
  return result;
};

/**
 * 도메인 수정
 */
var modifyDomain = function () {
  var domainId;

  $("#tbodyHtml").on("click", ".domain_modify", function (event) {
    domainId = noticeModify(event.target.closest("tr"));
  });

  $("#modifyBtn").on("click", function (event) {
    var checkedDomain = $("#tbodyHtml .icheckbox_flat-green.checked");

    if (checkedDomain.length === 0) {
      event.stopPropagation();
      fnComNotify("warning", "수정할 도메인을 선택해주세요.");
      return;
    } else if (checkedDomain.length > 1) {
      event.stopPropagation();
      fnComNotify("warning", "수정할 도메인을 1개만 선택해주세요.");
      return;
    }

    domainId = noticeModify(checkedDomain.closest("tr")[0]);
  });

  $("#modify_button").on("click", function () {
    var domain = new DomainForModify(domainId);

    if (!verify(domain)) {
      return;
    }

    if (!confirm("정말 변경하시겠습니까?")) {
      return;
    }

    var response = fnPatchDomainByAjax("/domains", domain);
    if (response.result === "success") {
      $("#domainModifyModal").modal("hide");
      fnComNotify("success", "정상적으로 변경되었습니다.");
      refreshPartially();
    } else {
      fnComNotify("error", "변경이 정상적으로 처리되지 않았습니다.");
    }
  });
};

/**
 * 권한에 따라 변경 여부 설정
 * 
 * @param {Element} container 
 */
var noticeModify = function (container) {
  var currentUserId = $("#userId").val();
  var currentUserAuth = $("#userAuth").val();

  if (currentUserAuth === 'user' && currentUserId !== container.dataset.userId) {
    fnComNotify("warning", "자신이 만든 도메인만 변경할 수 있습니다.");
    event.stopPropagation();
    return;
  }

  var domainId = container.dataset.id;
  var domain = fnGetDomainByAjax(domainId);
  fillDomainValues(domain);
  return domainId;
};

/**
 * 도메인 수정을 위한 select 채우기
 * 
 * @param {Object} domain 
 */
var fillDomainValues = function (domain) {
  $("#modify_category").val(domain.categoryName).prop("selected", true);
  $("#modify_title").val(domain.title);
  $("#modify_description").val(domain.description);
  $("#modify_project").val(domain.projectId).trigger("change");
  $("#modify_model").val(domain.modelId).prop("selected", true);
};

/**
 * 도메인 수정을 위한 객체
 * 
 * @param {Number} domainId 
 */
var DomainForModify = function (domainId) {
  this.id = domainId;
  this.categoryName = $("#modify_category option:selected").val();
  this.title = $("#modify_title").val();
  this.description = $("#modify_description").val();
  this.projectId = $("#modify_project option:selected").val();
  this.modelId = $("#modify_model option:selected").val();
};

/**
 * 도메인 수정 요청
 * 
 * @param {String} url 
 * @param {Object} domain 
 */
var fnPatchDomainByAjax = function (url, domain) {
  var result;
  $.ajax({
    url: url,
    type: "PATCH",
    contentType: 'application/json',
    data: JSON.stringify(domain),
    async: false,
    success: function success(response) {
      result = response;
    }
  });
  return result;
};

/**
 * 도메인 유형 별로 보여주기
 */
var type = function () {
  $('#type').on('change', function (event) {
    event.stopImmediatePropagation();

    if (SEARCHED) {
      searchLogic();
      return;
    }

    if (MINE) {
      doMyDomain();
      return;
    }

    var paging = Object.create(Paging);
    showContents(paging);
  })
}
