"use strict";
var TARGET_PARENT_PATH; 		//파일제어를 위한 부모 경로
var TARGET_PATH; 					//파일제어를 위한 경로
var TARGET_FILE_NAME; 			//파일제어를 위한 파일 이름
var TARGET;
var MULTI_SELECT_PATH_ARR = new Array();					//다중선택 배열


$("#loading").show();
$(function () {
  if ($("#jstree_fileBrowser").length > 0) {
    InitJsTree("jstree_fileBrowser", "jstree_dataInfoTable", "jstree_dataSampleTable", "jstree_currentDir");
  }

  if ($("#jstree_fileBrowser2").length > 0) {
    InitJsTree("jstree_fileBrowser2", "jstree_dataInfoTable2", "jstree_dataSampleTable2", "jstree_currentDir2");
  }

  //딥러닝 데이터 업로드 예시 초기화
  if ($("#jstree_showSample_dl").length > 0) {
    InitJsTree_example_dl("jstree_showSample_dl");
  }

  //머신러닝 샘플데이터 차트 초기화
  if ($("#jstree_sampleData_ml").length > 0) {
    InitJsTree_sample("jstree_sampleData_ml", "jstree_dataInfoTable", "jstree_dataSampleTable");
    setSampleFileList_ml("jstree_sampleData_ml");
  }

  //딥러닝 샘플데이터 차트 초기화
  if ($("#jstree_sampleData_dl").length > 0) {
    InitJsTree_sample("jstree_sampleData_dl", "jstree_dataInfoTable", "jstree_dataSampleTable");
    setSampleFileList_dl("jstree_sampleData_dl");
  }
});

/**
 * jsTree 초기화 (편집가능)
 * @param treeId
 * @param dataInfoId
 * @param dataSampleId
 * @param currentDirId
 * @returns
 */
function InitJsTree(treeId, dataInfoId, dataSampleId, currentDirId) {
  $('#' + treeId).jstree({
    "plugins": ["themes", "json_data", "ui", "crrm", "cookies", "dnd", "search", "types", "hotkeys", "contextmenu", "wholerow"],
    'core': {
      "check_callback": true,
      'data': ''
    },
    "contextmenu": {
      "items": function items($node) {
        var tree = $("#" + treeId).jstree(true);
        return {
          "Create": {
            "separator_before": false,
            "separator_after": false,
            "label": "추가",
            "action": function action(obj) {
              if ($node.original.type == "file") {
                fnComNotify("warning", "파일에서는 하위 폴더를 추가 할 수 없습니다.");
                return;
              }

              $node = tree.create_node($node);
              tree.edit($node);
            }
          },
          "Rename": {
            "separator_before": false,
            "separator_after": false,
            "label": "수정",
            "action": function action(obj) {
              TARGET_PATH = $node.parent == "#" ? $node.original.path : $node.original.parentPath;
              tree.edit($node);
            }
          },
          "Remove": {
            "separator_before": false,
            "separator_after": false,
            "label": "삭제",
            "action": function action(obj) {
              tree.delete_node($node);
            }
          }
        };
      }
    }
  }).bind("create_node.jstree", function (e, data) {
    createDirectory(TARGET_PATH, data.node.text, treeId);
  }).bind('delete_node.jstree', function (evt, data) {
    deleteDirectory(currentDirId, dataSampleId, treeId);
  }).bind('rename_node.jstree', function (evt, data) {
    var currentNodeId = $("#" + treeId).jstree("get_selected");
    renameFileFolder(TARGET_PATH, TARGET_PARENT_PATH, TARGET.original.type, data.text, data.old, treeId);
  }).bind('edit_node.jstree', function (evt, data) {

  }).bind('move_node.jstree', function (evt, data) {
    evt.stopPropagation();
    var newPath = '';
    for (var i = data.node.parents.length - 3; i >= 0; i--) {
      if (i < data.node.parents.length - 2) {
        newPath += '/';
      }
      newPath += $('#' + data.node.parents[i] + '_anchor').text();
    }
    moveFileDirectory(data.node.original.path, newPath, treeId);
  }).bind('open_node.jstree', function (evt, data) { }).on('activate_node.jstree', function (e, data) {
    if (data.node.state.selected == false) {
      var idx = MULTI_SELECT_PATH_ARR.indexOf(data.node.original.path);
      MULTI_SELECT_PATH_ARR.splice(idx, 1);
    }
  }).bind('open_node.jstree', function (evt, data) { }).on('select_node.jstree', function (e, data) {
    //선택된것들이 있을땐 다중선택에 추가
    MULTI_SELECT_PATH_ARR = new Array();
    for (var i = 0, iSize = data.selected.length; i < iSize; i++) {
      MULTI_SELECT_PATH_ARR.push(data.instance.get_node(data.selected[i]).original.path);
    }

    TARGET_PATH = data.node.original.path;
    TARGET = data.node;

    //컨트롤러에서 지정한 root 값 일경우 부모패스가 없으니.. 기본 path 를 넣는다
    TARGET_PARENT_PATH = data.node.parent == "#" ? data.node.original.path : data.node.original.parentPath;
    TARGET_FILE_NAME = data.node.original.text;

    if (data.node.icon == "jstree-file") {
      var isImage = TARGET_FILE_NAME.slice(TARGET_FILE_NAME.indexOf(".") + 1).toLowerCase();

      //TODO 이미지 검증방법 다양화 필요
      if (isImage == "jpg" || isImage == "png" || isImage == "gif" || isImage == "bmp") {
        setImageInfo(dataInfoId, dataSampleId);
      } else {
        //file 이면서 csv 및 json 일경우.. 샘플파일을 보여주자
        getFileSampleData(dataInfoId, dataSampleId);
      }
    } else {
      checkImageDirectory(dataInfoId, dataSampleId);
      //경로일경우 업로드 path 기입
      $("#uploadFilePath").val(TARGET_PATH);
    }

  }) // 초기에 root 폴더만 열리게 설정
    .on('load_node.jstree', function () {
      var rootFolder = treeId === 'jstree_fileBrowser' ? 'j1_1_anchor' : 'j2_1_anchor';
      $(this).jstree('open_node', rootFolder);
    });

  //jstree 버튼
  $('#treeCreate_' + treeId).click(function (obj) {
    if (TARGET == null) {
      fnComNotify("warning", "선택된 노드가 없습니다.");
      return;
    }

    if (TARGET.original.type == "file") {
      fnComNotify("warning", "파일에서는 하위 폴더를 추가 할 수 없습니다.");
      return;
    }

    var tree = $("#" + treeId).jstree(true);
    TARGET = tree.create_node(TARGET);
    tree.edit(TARGET);
  });
  $('#treeUpdate_' + treeId).click(function (obj) {
    if (TARGET == null) {
      fnComNotify("warning", "선택된 노드가 없습니다.");
      return;
    }

    var tree = $("#" + treeId).jstree(true);
    TARGET_PATH = TARGET.parent == "#" ? TARGET.original.path : TARGET.original.parentPath;
    tree.edit(TARGET);
  });
  $('#treeDelete_' + treeId).click(function (obj) {
    if (TARGET == null) {
      fnComNotify("warning", "선택된 노드가 없습니다.");
      return;
    }
    if (!confirm("정말 삭제하시겠습니까?")) {
      return;
    }
    var tree = $("#" + treeId).jstree(true);
    tree.delete_node(TARGET);
  });

  //------------DROP ZONE SETTING -----------//
  if ($('#uploadFileForm').length) {
    Dropzone.autoDiscover = false;
    var dropzoneOptions = {
      url: "/dataManage/upload",
      dictDefaultMessage: '업로드 할 경로를 클릭한 후<br>업로드 파일을 <br>드래그&드랍 해주세요',
      paramName: "file",
      addRemoveLinks: true,
      init: function () {
        this.on("complete", function (file) {
          if (this.getUploadingFiles().length === 0 && this.getQueuedFiles().length === 0) {
            refreshTree(treeId);
          }
        });
      },
      accept: function (file, done) {
        if ($("#uploadFilePath").val() == "") {
          fnComNotify("warning", "업로드 할 경로를 선택해 주세요");
          return;
        }

        checkFullUserSpace(done);
      }
    };
    var uploader = document.querySelector('#uploadFileForm');
    var newDropzone = new Dropzone(uploader, dropzoneOptions);
  }
  //------------FILE LIST 가져오기 -----------//
  getFileList(treeId);
}


/**
 * 샘플 jsTree 초기화 ( 이동, 삭제, 생성, 등등 변경이 안됨)
 * @param treeId
 * @param dataInfoId
 * @param dataSampleId
 * @returns
 */
function InitJsTree_sample(treeId, dataInfoId, dataSampleId) {
  $('#' + treeId).jstree({
    "plugins": ["wholerow"],
    'core': { 'data': '' }
  })
    .bind('open_node.jstree', function (evt, data) { }).on('select_node.jstree', function (e, data) {
      TARGET_PATH = data.node.original.path;
      TARGET = data.node;

      //컨트롤러에서 지정한 root 값 일경우 부모패스가 없으니.. 기본 path 를 넣는다
      TARGET_PARENT_PATH = data.node.parent == "#" ? data.node.original.path : data.node.original.parentPath;
      TARGET_FILE_NAME = data.node.original.text;

      if (data.node.icon == "jstree-file") {
        var isImage = TARGET_FILE_NAME.slice(TARGET_FILE_NAME.indexOf(".") + 1).toLowerCase();

        //TODO 이미지 검증방법 다양화 필요
        if (isImage == "jpg" || isImage == "png" || isImage == "gif" || isImage == "bmp") {
          setImageInfo(dataInfoId, dataSampleId);
        } else {
          //file 이면서 csv 및 json 일경우.. 샘플파일을 보여주자
          getFileSampleData(dataInfoId, dataSampleId, true);
        }
      } else {
        checkImageDirectory(dataInfoId, dataSampleId);
        //경로일경우 업로드 path 기입
        $("#uploadFilePath").val(TARGET_PATH);
      }

    });
}

/**
 * jsTree Sample 초기화
 * @returns
 */
function InitJsTree_example_dl(treeId) {
  $('#' + treeId).jstree({
    "plugins": ["themes", "json_data", "ui", "crrm", "cookies", "dnd", "search", "types", "hotkeys", "contextmenu", "wholerow"],
    'core': {
      'data':
        [{
          "id": "1.0",
          "text": "catdog",
          "icon": "",
          "state": {
            "opened": true,
            "disabled": false,
            "selected": false
          },
          "children": [{
            "id": "2.06.0",
            "text": "cat",
            "icon": "",
            "state": {
              "opened": true,
              "disabled": false,
              "selected": false
            },
            "children": [{ "id": "2.06.1", "text": "cat1.png", "icon": "glyphicon glyphicon-file", "children": false, "liAttributes": null, "aAttributes": null }
              , { "id": "2.06.2", "text": "cat2.png", "icon": "glyphicon glyphicon-file", "children": false, "liAttributes": null, "aAttributes": null }
              , { "id": "2.06.3", "text": "cat3.png", "icon": "glyphicon glyphicon-file", "children": false, "liAttributes": null, "aAttributes": null }
            ],
            "liAttributes": null,
            "aAttributes": null
          }, {
            "id": "2.07.0",
            "text": "dog",
            "icon": "",
            "state": {
              "opened": true,
              "disabled": false,
              "selected": false
            },
            "children": [{ "id": "2.07.1", "text": "dog1.png", "icon": "glyphicon glyphicon-file", "children": false, "liAttributes": null, "aAttributes": null }
              , { "id": "2.07.2", "text": "dog2.png", "icon": "glyphicon glyphicon-file", "children": false, "liAttributes": null, "aAttributes": null }
              , { "id": "2.07.3", "text": "dog3.png", "icon": "glyphicon glyphicon-file", "children": false, "liAttributes": null, "aAttributes": null }
            ],
            "liAttributes": null,
            "aAttributes": null
          }],
          "liAttributes": null,
          "aAttributes": null
        }]
    }
  });
}

/**
 * jstree 파일리스트 불러오기
 * 
 * 
 * @returns
 */
function getFileList(treeId) {
  $.ajax({
    type: "post",
    data: $("#dataManageForm").serialize(),
    url: '/dataManage/getFileList',
    dataType: "json",
    success: function success(data) {
      $('#' + treeId).jstree(true).settings.core.data = data.fileList;
      $('#' + treeId).jstree(true).refresh();
    },
    error: function error(request, _error) {
      console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error);
    },
    complete: function complete() {
      $("#" + treeId).jstree("open_all");
    }
  });
}

/**
 * 머신러닝 sample file 리스트 가져오기
 * @param treeId
 * @returns
 */
function setSampleFileList_ml(treeId) {
  $.ajax({
    type: "post",
    url: '/machineLearning/getSampleFilePath',
    dataType: "json",
    success: function success(data) {
      $('#' + treeId).jstree(true).settings.core.data = data.fList;
      $('#' + treeId).jstree(true).refresh();
    },
    error: function error(request, _error) {
      console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error);
    },
    complete: function complete() {
      $("#" + treeId).jstree("open_all");
    }
  });
}

/**
 * 딥러닝 sample file 리스트 가져오기
 * @param treeId
 * @returns
 */
function setSampleFileList_dl(treeId) {
  $.ajax({
    type: "post",
    url: '/deepLearning/getSampleFilePath',
    dataType: "json",
    success: function success(data) {
      $('#' + treeId).jstree(true).settings.core.data = data.fList;
      $('#' + treeId).jstree(true).refresh();
    },
    error: function error(request, _error) {
      console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error);
    },
    complete: function complete() {
      $("#" + treeId).jstree("open_all");
    }
  });
}

/**
 * jstree 폴더 생성 후 실제 폴더 생성
 * 
 * 
 * @param targetFullPath
 * @returns
 */
function createDirectory(targetFullPath, fileName, treeId) {
  $.ajax({
    type: "post",
    data: {
      "targetFullPath": targetFullPath,
      "fileName": fileName
    },
    url: '/dataManage/createDirectory',
    dataType: "json",
    success: function success(data) {
      if (data.error != null) {
        fnComNotify("error", data.error);
        $('#' + treeId).jstree("refresh");
      }
      if (data.info != null) {
        fnComNotify("info", data.info);
      }
    },
    error: function error(request, _error2) {
      console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error2);
    },
    complete: function complete() {
      getFileList(treeId);
    }
  });
}

/**
 * jstree 삭제후 실제 파일/폴더 삭제요청 
 */
function deleteDirectory(currentDirId, dataSampleId, treeId) {
  $("#" + dataSampleId).html("");
  $.ajax({
    type: "post",
    data: {
      "multiSelectPath": MULTI_SELECT_PATH_ARR.join("###")
    },
    url: '/dataManage/deleteDirectory',
    dataType: "json",
    success: function success(data) {
      if (data.error != null) {
        fnComNotify("error", data.error);
      }

      if (data.info != null) {
        fnComNotify("info", data.info);
      }
    },
    error: function error(request, _error3) {
      console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error3);
    },
    complete: function complete() {
      TARGET_PATH = null;
      $("#" + currentDirId).html("");
      refreshTree(treeId);
    }
  });
}

/**
 * 파일/폴더 이동
 * 
 * @param {String} oldPath 
 * @param {String} newPath 
 */
function moveFileDirectory(oldPath, newPath, treeId) {
  $.ajax({
    type: "post",
    data: {
      "oldPath": oldPath,
      "newPath": newPath,
    },
    url: '/dataManage/moveDirectory',
    dataType: "json",
    success: function success(data) {
      if (data.error != null) {
        fnComNotify("error", data.error);
      }

      if (data.info != null) {
        fnComNotify("info", data.info);
      }
      refreshTree(treeId);
    },
    error: function error(request, _error4) {
      console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error4);
      refreshTree(treeId);
    }
  })
}

/**
 * jstree 수정후 실제 파일/폴더 수정
 * 
 * 
 * @param targetPath
 * @param targetParentPath
 * @param targetType
 * @param newName
 * @param oldName
 * @returns
 */
function renameFileFolder(targetPath, targetParentPath, targetType, newName, oldName, treeId) {
  $.ajax({
    type: "post",
    data: {
      "targetPath": targetPath,
      "targetParentPath": targetParentPath,
      "targetType": targetType,
      "newName": newName,
      "oldName": oldName
    },
    url: '/dataManage/renameFileFolder',
    dataType: "json",
    success: function success(data) {
      if (data.error != null) {
        fnComNotify("error", data.error);
        $('#' + treeId).jstree("refresh");
      }

      if (data.info != null) {
        fnComNotify("info", data.info);
      }
    },
    error: function error(request, _error5) {
      console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error5);
    },
    complete: function complete() {
      getFileList(treeId);
    }
  });
}

/**
 * 선택한 파일의 샘플데이터를 가져온다
 * 
 * 
 * @returns
 */
function getFileSampleData(dataInfoId, dataSampleId, sample) {
  console.log("file name: " + TARGET_FILE_NAME);
  console.log("file path: " + TARGET_PATH);
  $.ajax({
    type: "post",
    data: {
      "fileName": TARGET_FILE_NAME,
      "fileFullPath": TARGET_PATH,
      "sample": sample
    },
    url: '/dataManage/getSampleData',
    dataType: "json",
    success: function success(data) {
      var html = "";
      html += "<thead>";
      html += "<tr>";
      html += "<th>파일명</th>";
      html += "<th>확장자</th>";
      html += "<th>용량</th>";
      html += "<th>마지막 수정시간</th>";
      html += "</tr>";
      html += "</thead>";
      html += "<tbody><tr>";

      if (data.dataInfo != null) {
        html += "<td>" + data.dataInfo[0] + "</td>";
        html += "<td>" + data.dataInfo[1] + "</td>";
        html += "<td>" + data.dataInfo[2] + "</td>";
        html += "<td>" + data.dataInfo[3] + "</td>";
      }

      html += "</tbody>";
      html += "</tr>";
      $("#" + dataInfoId).html(html);

      var sampleHtml = "";

      if (data.result.localFile.result != null) {
        var sampleData = data.result.localFile.result.samples;
        sampleHtml += "<thead>";
        sampleHtml += "<tr>";

        for (var key in sampleData[0]) {
          sampleHtml += "<th>" + key + "</th>";
        }

        sampleHtml += "</tr>";
        sampleHtml += "</thead>";
        sampleHtml += "<tbody>";

        for (var i = 0, iSize = sampleData.length; i < iSize; i++) {
          sampleHtml += "<tr>";

          for (var _key in sampleData[i]) {
            sampleHtml += "<td>" + sampleData[i][_key] + "</td>";
          }

          sampleHtml += "</tr>";
        }

        sampleHtml += "</tbody>";
      }

      $("#" + dataSampleId).html(sampleHtml);
    },
    error: function error(request, _error5) {
      console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error5);
    }
  });
}

/**
 * 이미지 정보 세팅
 * @param dataInfoId
 * @returns
 */
function setImageInfo(dataInfoId, dataSampleId) {
  $.ajax({
    type: "post",
    data: {
      "fileName": TARGET_FILE_NAME,
      "fileFullPath": TARGET_PATH
    },
    url: '/dataManage/getDataInfo',
    dataType: "json",
    success: function success(data) {
      var html = "";
      html += "<thead>";
      html += "<tr>";
      html += "<th>파일명</th>";
      html += "<th>확장자</th>";
      html += "<th>용량</th>";
      html += "<th>마지막 수정시간</th>";
      html += "</tr>";
      html += "</thead>";
      html += "<tbody><tr>";

      if (data.dataInfo != null) {
        html += "<td>" + data.dataInfo[0] + "</td>";
        html += "<td>" + data.dataInfo[1] + "</td>";
        html += "<td>" + data.dataInfo[2] + "</td>";
        html += "<td>" + data.dataInfo[3] + "</td>";
      }
      html += "</tbody>";
      html += "</tr>";
      $("#" + dataInfoId).html(html);
    },
    error: function error(request, _error3) {
      console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error3);
    },
    complete: function complete() {
      var html = "";
      html += "<div class=\"deeplearningWrap\">";
      html += "<ul class=\"data_img\">";
      html += "<li>";
      html += "<div class=\"img_area\">";
      html += "<img src=\"/dataManage/display?fileFullPath=" + encodeURI(TARGET_PATH) + "\" alt=\"\">";
      html += "</div>";
      html += "<p class=\"img_title\">" + TARGET_FILE_NAME + "</p>";
      html += "</li>";
      html += "</ul>";
      html += "</div>";

      $("#" + dataSampleId).html(html);
    }
  });
}

/**
 * 이미지 디렉토리일 경우 임시 이미지 개수만 보여준다.
 * @param refreshTree
 * @param treeId
 * @returns
 */
function checkImageDirectory(dataInfoId, dataSampleId) {
  var dirArr = new Array();
  var dirName = new Array();

  $.ajax({
    type: "post",
    data: {
      "directoryPath": TARGET_PATH
    },
    url: '/dataManage/checkImageDirectory',
    dataType: "json",
    success: function success(data) {
      if (data.dataInfoList != null && data.dataInfoList.length > 0) {
        var html = "";
        html += "<thead>";
        html += "<tr>";
        html += "<th>파일명</th>";
        html += "<th>확장자</th>";
        html += "<th>용량</th>";
        html += "<th>마지막 수정시간</th>";
        html += "</tr>";
        html += "</thead>";
        html += "<tbody>";

        for (var i = 0, iSize = data.dataInfoList.length; i < iSize; i++) {
          html += "<tr>";
          html += "<td>" + data.dataInfoList[i].name + "</td>";
          html += "<td>" + data.dataInfoList[i].type + "</td>";
          html += "<td>" + data.dataInfoList[i].size + "</td>";
          html += "<td>" + data.dataInfoList[i].date + "</td>";
          html += "</tr>";
          dirArr.push(data.dataInfoList[i].dir);
          dirName.push(data.dataInfoList[i].name);
        }
        html += "</tbody>";
        $("#" + dataInfoId).html(html);
      }
    },
    error: function error(request, _error3) {
      console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error3);
    },
    complete: function complete() {
      var html = "";
      html += "<div class=\"deeplearningWrap\">";
      html += "<ul class=\"data_img\">";
      for (var i in dirArr) {
        html += "<li>";
        html += "<div class=\"img_area\">";
        html += "<img src=\"/dataManage/display?fileFullPath=" + encodeURI(dirArr[i]) + "\" class=\"img-thumbnail\" alt=\"\">";
        html += "</div>";
        html += "<p class=\"img_title\">" + dirName[i] + "</p>";
        html += "</li>";
      }
      html += "</ul>";
      html += "</div>";
      $("#" + dataSampleId).html(html);
    }
  });
}

function refreshTree(treeId) {
  getFileList(treeId);
}

/**
 * 해당유저의 저장공간이 풀 여부 확인
 * @param done
 * @returns
 */
function checkFullUserSpace(done, treeId) {
  $.ajax({
    type: "post",
    url: '/dataManage/checkFullUserSpace',
    dataType: "json",
    success: function success(data) {
      if (data.result == true) {
        fnComNotify("warning", "할당 사용용량이 초과 되었습니다. 최대:" + data.maxSize + " 현재:" + data.size);
      } else {
        fnComNotify("success", "업로드 되었습니다. 최대:" + data.maxSize + " 현재:" + data.size);
        done();
      }
    },
    error: function error(request, _error5) {
      console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error5);
    },
    complete: function complete() {
    }
  });
}

// /**
//  * 멀티파트form 업로드 ajax
//  * @returns
//  */
// function uploadFileAjax() {
//   var form = $('#uploadFileForm')[0];
//   var data = new FormData(form);
//   jQuery.each(jQuery('#uploadFileForm')[0].files, function (i, file) {
//     data.append('file-' + i, file);
//   });

//   $.ajax({
//     url: '/dataManage/upload',
//     type: 'POST',
//     data: data,
//     enctype: 'multipart/form-data',
//     processData: false,
//     contentType: false,
//     cache: false
//   }).done(function (data) {
//     callback(data);
//   });
// }
