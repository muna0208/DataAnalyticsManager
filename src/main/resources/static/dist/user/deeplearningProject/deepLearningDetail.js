"use strict";

//선택된 원본데이터 경로
var SELECTED_ORG_DATA_DIR;

// 딥러닝 관련 ID 전역변수
var PROJECT_ID, SELECTED_ORG_DATA_ID, SELECTED_MODEL_ID

// 모델생성 관련 전역변수
var PRETRAIND_MODEL, SELECTED_PRETRAIND_MODEL;
var PRETRAIND_NETWORK_TABLE_LAST_IDX, PRETRAIND_NETWORK_TABLE_BODY, NEURAL_LAYER, NEURAL_LAYER_OPTIMIZERS;
var TRAINABLE_IDX, ADDITIONAL_LAYERS;
var MODEL_CREATE_STATUS, MODEL_CREATE_TIMER;
var RESULT_ACCURACY, TEST_ACCURACY;
var IS_BEGINNER;

$(function () {
	PROJECT_ID = $("#projectSequencePk").val(); // 딥러닝 프로젝트 정보(아이디)를 가져온다.
	if (PROJECT_ID == null) {
		return;
	}

	fnGetProjectInfo(PROJECT_ID);

	// 원본리스트 조회
	fnGetOriginalList();

	// 원본리스트 클릭시
	$(document).on("click", ".originalData", function () {
		clearInterval(MODEL_CREATE_TIMER);
		$(".originalData").removeClass("active");
		$(this).addClass("active");
		SELECTED_ORG_DATA_ID = $(this).attr("id");
		setOrgDataInfo(SELECTED_ORG_DATA_ID);
		fnGetModelList();
	});

	// 모델리스트 클릭시
	$(document).on("click", ".modelData", function () {
		clearInterval(MODEL_CREATE_TIMER);
		$(".modelData").removeClass("active");
		$(this).addClass("active");
		SELECTED_MODEL_ID = $(this).attr("id");
		setModelDataInfo(SELECTED_MODEL_ID);
	});

	// preTrained 모델 목록 조회
	getPreTrainedModelList();
	getNeuralLayerParameter();
	getNeuralLayerOptimizers();

	// 모델생성 프로세스 확대축소 mouseOver
	$('.collapse-link').hover(function () {
		$(this).css('cursor', 'pointer');
	});
});

/**
 * 프로젝트 정보 가져오기
 * 
 * @param {Number} id 
 */
var fnGetProjectInfo = function (id) {
	var project = fnGetProjectByAjax(id);
	$("#proejectName").html("<h2 class=\"pdTop_15\"><strong>" + project.name + "</strong></h2>");
	$("#proejectDesc").html(project.description);
	$("#projectCreateDate").html("생성일 :" + project.create_date);
};

/**
 * 딥러닝 원본데이터 생성
 */
function fnCreateOriginalData() {
	$.ajax({
		type: "post",
		data: {
			"targetPath": TARGET_PATH
			, "projectId": PROJECT_ID
		},
		url: '/deepLearning/createOrgData',
		dataType: "json",
		success: function success(data) {
			if (data.result.result == "fail") {
				fnComNotify("error", data.result.detail);
			} else if (data.result.result == "success") {
				fnComNotify("info", data.result.detail);
			} else if (data.result.result == "warning") {
				fnComNotify("warning", data.result.detail);
			}
		},
		error: function error(request, _error3) {
			fnComNotify("error", "요청이 실패 하였습니다.");
			console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error3);
		},
		complete: function complete() {
			location.reload();
		}
	});

}

/**
 * 딥러닝 원본데이터 불러오기
 */
function fnGetOriginalList() {
	$.ajax({
		type: "post",
		data: {
			"projectId": PROJECT_ID
		},
		url: '/deepLearning/getOrgDataList',
		dataType: "json",
		success: function success(data) {
			if (data.result.result == "fail") {
				fnComNotify("error", data.result.detail);
				return;
			}

			var originalDataTemplate = getOriginalDataTemplate(data);

			if (originalDataTemplate == "") {
				originalDataTemplate += "<li>원본데이터가 없습니다.</li>";
				$("#modelList").html("<li>모델이 없습니다.</li>");
				$("#deepLearningMain2").hide();
				$("#deepLearningMain3").hide();
				$("#deepLearningMain4").hide();

				var m_html = "";
				m_html += "<div class=\"x_panel\" id=\"deepLearningMain_explain\">";
				m_html += "<div class=\"x_title posiRelative\"><strong><i class=\"fa fa-info-circle\"></i> 원본데이터를 생성해주세요</strong><div class=\"clearfix\"></div></div>";
				m_html += "<div><h5>좌측의 <code>+</code>버튼을 이용하여 원본 데이터를 생성해주세요. </h5></div></div>";
				$("#deepLearningMain").html(m_html);
			}
			$("#originalDataList").html(originalDataTemplate);
		},
		error: function error(request, _error3) {
			fnComNotify("error", "요청이 실패 하였습니다.");
			console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error3);
		},
		complete: function complete() {
			//해당원본리스트의 모델 조회
			fnGetModelList();
		}
	});
}

/**
 * 딥러닝 원본데이터 화면 구성요소
 * 
 * @param {JSONObject} data 
 */
function getOriginalDataTemplate(data) {
	var html = "";

	for (var i in data.data) {
		var oData = data.data[i];
		var name = "orgData_" + oData.id;
		if (i == 0) {
			html += "<li class='pdTB_5 originalData active' role='button'" + " id=" + oData.id + " title=\"" + name + "\">" + name + "<i class=\"fa fa-trash floatR lineHeight_20 pdright_10\" onclick=\"fnDeleteOriginalData('" + oData.id + "');\" ></i></li>";
			SELECTED_ORG_DATA_ID = oData.id;
			setOrgDataInfo(oData.id);
		} else {
			html += "<li class='pdTB_5 originalData' role='button' " + "id=" + oData.id + " title=\"" + name + "\">" + name + "<i class=\"fa fa-trash floatR lineHeight_20 pdright_10\" onclick=\"fnDeleteOriginalData('" + oData.id + "');\"></i></li>";
		}
	}

	return html;
}

/**
 * 딥러닝 원본데이터 정보 가져오기
 * 
 * @param {Number} orgId 
 */
function setOrgDataInfo(orgId) {
	$.ajax({
		type: "post",
		data: {
			"orgId": orgId
		},
		url: '/deepLearning/getOrgData',
		dataType: "json",
		success: function success(data) {
			$("#orgDataInfo").html("데이터 경로:" + data.data.parent_dir);
			SELECTED_ORG_DATA_DIR = data.data.parent_dir;

			var originalDataInfoTemplate = getOriginalDataInfoTemplate(data);
			$("#expert_process1_explain").html(originalDataInfoTemplate);
		},
		error: function error(request, _error3) {
			fnComNotify("error", "요청이 실패 하였습니다.");
			console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error3);
		},
		complete: function complete() {

			//데이터 경로를 가지고 데이터 보기 활성
			setDataInfo();
		}
	});
}

/**
 * 딥러닝 원본데이터 정보 화면 구성요소
 * 
 * @param {JSONObject} data 
 */
function getOriginalDataInfoTemplate(data) {
	var explainHtml = "";

	explainHtml += "<div class=\"alert alert-success alert-dismissible\" role=\"alert\">";
	explainHtml += "<button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-label=\"Close\"><span aria-hidden=\"true\">×</span></button>";
	explainHtml += "<strong>" + data.data.parent_dir + "</strong> 경로의 임의의 10개 파일을 보여줍니다.";
	explainHtml += " 딥러닝 대상 원본데이터가 맞는지 확인해 주세요.";
	explainHtml += "</div>";

	return explainHtml;
}

/**
 * 데이터 보기의 정보를 세팅한다
 */
function setDataInfo() {
	$.ajax({
		type: "post",
		data: {
			"directoryPath": SELECTED_ORG_DATA_DIR
		},
		url: '/dataManage/checkImageDirectory',
		dataType: "json",
		success: function success(data) {
			var imgTemplate = getImgTemplate(data);
			$("#org_data_img_div").html(imgTemplate);
			popupImg('deepLearningOrgImg');

			var lableTemplate = getLableTemplate(data);
			$("#org_data_lable_div").html(lableTemplate);
		},
		error: function error(request, _error3) {
			fnComNotify("error", "데이터 보기의 정보 요청이 실패 하였습니다.");
			console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error3);
		}
	});
}

/**
 * 이미지 보기 화면 구성요소
 * 
 * @param {JSONObject} data 
 */
function getImgTemplate(data) {
	var html = "";

	html += "<div class=\"row\">";
	for (var i in data.dataInfoList) {
		html += "<div class=\"col-md-6\">";
		html += "	<div class=\"thumbnail\">";
		html += "		<div class=\"image view view-first width_100p height_80p cursor\">";
		html += "		<img class='deepLearningOrgImg width_100p disp_block' src=\"/dataManage/display?fileFullPath=" + encodeURI(data.dataInfoList[i].dir) + "\" alt=\"image\">";
		html += "		</div>";
		html += "		<div class=\"caption\">";
		html += "			<p>" + data.dataInfoList[i].name + "</p>";
		html += "		</div>";
		html += "	</div>";
		html += "</div>";
	}
	html += "</div>";

	return html;
}

/**
 * Lable 화면 구성요소
 * 
 * @param {JSONObject} data 
 */
function getLableTemplate(data) {
	var lableHtml = "";

	lableHtml += "<thead><tr><th>No</th><th>Categories (분류)</th><th>Number of Image (이미지 개수)</th></tr></thead>";
	lableHtml += "<tbody>";

	var idx = 0;
	for (var key in data.dataLableMap) {
		idx++;
		lableHtml += "<tr>";
		lableHtml += "<td>" + idx + "</td>";
		lableHtml += "<td>" + key + "</td>";
		lableHtml += "<td>" + data.dataLableMap[key] + "</td>";

		lableHtml += "</tr>";
	}
	lableHtml += "</ul>";
	lableHtml += "</tbody>";

	return lableHtml;
}


/**
 * 딥러닝 원본데이터 삭제
 * 
 * @param {Number} orgId 
 */
function fnDeleteOriginalData(orgId) {
	//TODO 관련 모델도 삭제 되도록 수정
	if (confirm("원본 데이터를 삭제하면 생성된 모델이 같이 삭제됩니다. \n삭제하시겠습니까?")) {
		$.ajax({
			type: "post",
			data: {
				"orgId": orgId
				, "projectId": PROJECT_ID
			},
			url: '/deepLearning/deleteOrgData',
			dataType: "json",
			success: function success(data) {
				if (data.result.result == "fail") {
					fnComNotify("error", data.result.detail);
				} else if (data.result.result == "success") {
					fnComNotify("info", data.result.detail);
				} else if (data.result.result == "warning") {
					fnComNotify("warning", data.result.detail);
				}
			},
			error: function error(request, _error3) {
				fnComNotify("error", "딥러닝 원본데이터 삭제 요청이 실패 하였습니다.");
				console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error3);
			},
			complete: function complete() {
				location.reload();
			}
		});
	}
}


/**
 * pre-TrainedModel 목록 가져오기
 */
function getPreTrainedModelList() {
	$.ajax({
		type: "post",
		url: '/deepLearning/getPreTrainedModelList',
		dataType: "json",
		success: function success(data) {
			PRETRAIND_MODEL = data.resultList;
			var html = "";
			html += "<option id=\"\" selected>선택</option>";
			for (var i in data.resultList) {
				html += "<option id=\"" + data.resultList[i].id + "\">" + data.resultList[i].neural_network + "</option>";
			}
			$("#preTrainedModel_expert_selectBox").html(html);
			$("#preTrainedModel_beginner_selectBox").html(html);
			if (data.result.result == "fail") {
				fnComNotify("error", data.result.detail);
			}
		},
		error: function error(request, _error3) {
			fnComNotify("error", "pre-TrainedModel 목록 요청이 실패 하였습니다.");
			console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error3);
		},
		complete: function complete() {
		}
	});
}

/**
 * Pre-Trained 모델 선택 팝업창의 select 박스 변경 (전문가용)
 */
function changePreTrainedModelDesc_expert() {
	if ($("#preTrainedModel_expert_selectBox option:selected").val() == "선택") {
		SELECTED_PRETRAIND_MODEL = "";
		$("#preTrainedNetwork_table").html("");
		$("#preTrainedNetwork_btn").html("");
	} else {
		for (var i in PRETRAIND_MODEL) {
			if (PRETRAIND_MODEL[i].neural_network == $("#preTrainedModel_expert_selectBox option:selected").val()) {
				SELECTED_PRETRAIND_MODEL = PRETRAIND_MODEL[i];
				$("#preTrainedModel_expert_desc").val(PRETRAIND_MODEL[i].description);
			}
		}
		setPreTrainedNetworkTable();
		getNeuralLayerParameter();

		$("#expertProcess4_alert").hide();
		$("#expertProcess4_table").show();
		$("#expertProcess5_alert").hide();
		$("#expertProcess5_table").show();
	}
}

/**
 * Pre-Trained 모델 선택 팝업창의 select 박스 변경 (초보자용)
 */
function changePreTrainedModelDesc_beginner() {
	if ($("#preTrainedModel_beginner_selectBox option:selected").val() == "선택") {
		SELECTED_PRETRAIND_MODEL = "";
	} else {
		for (var i in PRETRAIND_MODEL) {
			if (PRETRAIND_MODEL[i].neural_network == $("#preTrainedModel_beginner_selectBox option:selected").val()) {
				SELECTED_PRETRAIND_MODEL = PRETRAIND_MODEL[i];
				$("#preTrainedModel_beginner_desc").val(PRETRAIND_MODEL[i].description);
			}
		}
	}
}


/**
 * Pre-Trained Network 분석 테이블 세팅
 */
function setPreTrainedNetworkTable() {
	var preTrainedNetworkTableTemplate = getPreTrainedNetworkTableTemplate();
	$("#preTrainedNetwork_table").html(preTrainedNetworkTableTemplate);

	var btnTemplate = "<div class=\"alert\"><button type=\"button\" class=\"btn btn-primary btn-block\" onclick=\"add_preTrainedNetwork_tableRow();\">사용자 레이어 추가 +</button></div>";
	$("#preTrainedNetwork_btn").html(btnTemplate);

	PRETRAIND_NETWORK_TABLE_BODY = $("#preTrainedNetwork_table_body").html();
}

/**
 *  Pre-Trained Network 분석 테이블 화면 구성요소
 */
function getPreTrainedNetworkTableTemplate() {
	var html = "";

	html += "	<table class=\"table table-bordered jambo_table\">";
	html += "<thead>";
	html += "	<tr class=\"odd\">";
	html += "		<th class=\"col-md-1\">레이어</th>";
	html += "		<th class=\"col-md-1\">이름</th>";
	html += "		<th class=\"col-md-2\">종류</th>";
	html += "		<th class=\"col-md-1\">활성</th>";
	html += "		<th class=\"col-md-2\">입력값</th>";
	html += "		<th class=\"col-md-1\">삭제</th>";
	html += "	</tr>";
	html += "</thead>";
	html += "<tbody id=\"preTrainedNetwork_table_body\">";

	var arc = JSON.parse(SELECTED_PRETRAIND_MODEL.architecture);
	PRETRAIND_NETWORK_TABLE_LAST_IDX = arc.length;

	for (var i in arc) {
		var desc = "";
		for (var j in NEURAL_LAYER) {
			if (NEURAL_LAYER[j].name == arc[i].Type)
				desc = NEURAL_LAYER[j].library_function_description;
		}

		html += "<tr>";
		html += "<td title=\"" + desc + "\">*" + arc[i].Layer + "</td>";
		html += "<td title=\"" + desc + "\">" + arc[i].Name + "</td>";
		html += "<td title=\"" + desc + "\">" + arc[i].Type + "</td>";
		if (arc[i].Trainable == "False") {
			html += "<td><div class=\"switch_box\"><label class=\"switch\"><input name=\"preTrainedChkbox\" id=\"preTrained" + arc[i].Layer + "\" type=\"checkbox\" value=\"" + arc[i].Layer + "\"><span class=\"slider round\"></span></label></div></td>";
		} else {
			html += "<td><div class=\"switch_box\"><label class=\"switch\"><input name=\"preTrainedChkbox\" id=\"preTrained" + arc[i].Layer + "\" type=\"checkbox\" value=\"" + arc[i].Layer + "\" checked><span class=\"slider round\"></span></label></div></td>";
		}
		html += "<td>";
		html += "기본값";
		html += "</td>";
		html += "<td></td>";
		html += "</tr>";
	}
	html += "</tbody>";
	html += "</table>";

	return html;
}

/**
 * Pre-Trained Network 분석팝업 table 한 줄 추가
 */
function add_preTrainedNetwork_tableRow() {
	PRETRAIND_NETWORK_TABLE_LAST_IDX++;

	var add_preTrainedNetwork_tableRowTemplate = getAdd_preTrainedNetwork_tableRowTemplate();
	$("#preTrainedNetwork_table_body").append(add_preTrainedNetwork_tableRowTemplate);

	$("#userParameter" + PRETRAIND_NETWORK_TABLE_LAST_IDX).focus();
	change_neuralLayer_parameter(PRETRAIND_NETWORK_TABLE_LAST_IDX);
}

/**
 * Pre-Trained Network 분석팝업 table 한줄 추가 화면 구성요소
 */
function getAdd_preTrainedNetwork_tableRowTemplate() {
	var html = "";

	html += "<tr id=\"trow" + PRETRAIND_NETWORK_TABLE_LAST_IDX + "\">";
	html += "<td>" + PRETRAIND_NETWORK_TABLE_LAST_IDX + "</td>";
	html += "<td><input type=\"text\" class=\"form-control\" id=\"userParameter" + PRETRAIND_NETWORK_TABLE_LAST_IDX + "\" value=\"userParameter" + PRETRAIND_NETWORK_TABLE_LAST_IDX + "\"></td>";

	//type 선택
	html += "<td>";
	html += "<select id=\"userSelectBox" + PRETRAIND_NETWORK_TABLE_LAST_IDX + "\" onchange=\"change_neuralLayer_parameter(" + PRETRAIND_NETWORK_TABLE_LAST_IDX + ");\" class=\"form-control\">";
	for (var i in NEURAL_LAYER) {
		html += "<option value=\"" + NEURAL_LAYER[i].name + "\" title=\"" + NEURAL_LAYER[i].library_function_description + "\">" + NEURAL_LAYER[i].name + "</option>";
	}
	html += "</td>";
	html += "<td><div class=\"switch_box\"><label class=\"switch\"><input type=\"checkbox\" name=\"userPreTrainedChkbox\" value=\"" + PRETRAIND_NETWORK_TABLE_LAST_IDX + "\" checked><span class=\"slider round\"></span></label></div></td>";
	html += "<td id=\"userTd" + PRETRAIND_NETWORK_TABLE_LAST_IDX + "\"><input type=\"text\" class=\"form-control\" id=\"userPlaceholder" + PRETRAIND_NETWORK_TABLE_LAST_IDX + "\" placeholder=\"{default:0}\"></td>";
	html += "<td class='ta_center fontSize_15'><i class=\"fa fa-trash\" style='cursor: pointer;' onclick=\"remove_preTrainedNetwork_tableRow(" + PRETRAIND_NETWORK_TABLE_LAST_IDX + ");\"></i></td>";
	html += "</tr>";

	return html;
}

/**
 * Pre-Trained Network 분석팝업 table 해당줄 삭제
 * 
 * @param {Number} idx 
 */
function remove_preTrainedNetwork_tableRow(idx) {
	$("#trow" + idx).remove();
}

/**
 * Pre-Trained Network 분석팝업 종류selectbox 변경
 * 
 * @param {Number} idx 
 */
function change_neuralLayer_parameter(idx) {
	for (var i in NEURAL_LAYER) {
		if (NEURAL_LAYER[i].name == $("#userSelectBox" + idx + " option:selected").val()) {
			var userTdHtml = "";
			if (NEURAL_LAYER[i].parameter != null) {
				var param = JSON.parse(NEURAL_LAYER[i].parameter);
				for (var j in param) {
					if (param[j].default == "") {
						userTdHtml += param[j].name + ":<input type=\"text\" title=\"" + param[j].type + "\" class=\"form-control\" id=\"userTd" + idx + "_" + j + "\" placeholder=\"" + param[j].note + " (입력포멧:" + param[j].type + ")\">";
					}
				}
				if (userTdHtml == "")
					userTdHtml += "";
				$("#userTd" + idx).html(userTdHtml);
			}
		}
	}
}

/**
 * Pre-Trained Network 파라미터 정보 가져오기
 */
function getNeuralLayerParameter() {
	$.ajax({
		type: "post",
		url: '/deepLearning/getNeuralLayerParameterList',
		dataType: "json",
		success: function success(data) {
			NEURAL_LAYER = data.resultList;
		},
		error: function error(request, _error3) {
			fnComNotify("error", "Pre-Trained Network 파라미터 정보 가져오기 요청이 실패 하였습니다.");
			console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error3);
		},
		complete: function complete() {
		}
	});
}

/**
 * 파리미터 설정 팝업의  optimizer 가져오기
 */
function getNeuralLayerOptimizers() {
	$.ajax({
		type: "post",
		url: '/deepLearning/getNeuralLayerOptimizers',
		dataType: "json",
		success: function success(data) {
			NEURAL_LAYER_OPTIMIZERS = data.resultList;

			var neuralLayerOptimizersTemplate = getNeuralLayerOptimizersTemplate();
			$("#train_optimizer_seletbox").html(neuralLayerOptimizersTemplate);
		},
		error: function error(request, _error3) {
			fnComNotify("error", "Pre-Trained Network 파라미터 정보 가져오기 요청이 실패 하였습니다.");
			console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error3);
		},
		complete: function complete() {
		}
	});
}

/**
 * 파리미터 설정 팝업의  optimizer 화면 구성요소
 */
function getNeuralLayerOptimizersTemplate() {
	var html = "";

	for (var i in NEURAL_LAYER_OPTIMIZERS) {
		if (i == 0)
			html += "<option value=\"" + NEURAL_LAYER_OPTIMIZERS[i].name + "\" selected>" + NEURAL_LAYER_OPTIMIZERS[i].name + "</option>";
		else
			html += "<option value=\"" + NEURAL_LAYER_OPTIMIZERS[i].name + "\">" + NEURAL_LAYER_OPTIMIZERS[i].name + "</option>";
	}

	return html;
}


/**
 * preTrained 네트워크 분석 중 활성된 파라미터 가져오기
 */
function getActivePreTrained() {
	//기본 파라미터
	TRAINABLE_IDX = new Array();
	$("input:checkbox[name='preTrainedChkbox']").each(function () {
		if ($(this).context.checked == true) {
			TRAINABLE_IDX.push(parseInt($(this).val()));
		}
	});

	//사용자 추가 파라미터
	ADDITIONAL_LAYERS = new Array();
	$("input:checkbox[name='userPreTrainedChkbox']").each(function () {
		if ($(this).context.checked == true) {
			var idx = $(this).val();
			var toks = $("#userTd" + idx)[0].innerText.split(":");
			var jsonStringParam = "";

			//jquery JSON 파싱이 되도록 만들어야 한다
			jsonStringParam += "{";
			for (var i = 0; i < toks.length; i++) {
				var s = toks[i].trim();
				// innerText를 :로 쪼갯을때 내용이 없는 배열을 넘김
				if (s == "")
					continue;

				//첫번째는 , 제외
				if (i != 0)
					jsonStringParam += ",";

				//사용자 추가 td 중 입력타입이 string 일경우 "" 을 붙혀줌 
				//다른자료형에 대해 필요하다면 처리
				if ($("#userTd" + idx + "_" + i).attr("title") == "string") {
					jsonStringParam += "\"" + s + "\"" + ":\"" + $("#userTd" + idx + "_" + i).val() + "\"";
				} else {
					jsonStringParam += "\"" + s + "\"" + ":" + $("#userTd" + idx + "_" + i).val() + "";
				}
			}
			jsonStringParam += "}";

			var additionalLayer = new Object();
			additionalLayer.index = idx;
			additionalLayer.layer_name = $("#userParameter" + idx).val();
			additionalLayer.layer_type = $("#userSelectBox" + idx + " option:selected").val();
			additionalLayer.param = JSON.parse(jsonStringParam);
			ADDITIONAL_LAYERS.push(additionalLayer);
		}
	});
}


/**
 * 학습 요청- 모델 생성
 */
function fnRequestTrain() {

	//최종적으로 요청할 parameter만들기
	var requestParam_string = "";
	var userType = "";

	//초보자용
	if (IS_BEGINNER) {
		userType = "beginner";
		var epochVal = 20;
		var b_pModel = $("#preTrainedModel_beginner_selectBox option:selected").val();
		requestParam_string = '{"project_id": "' + PROJECT_ID + '","dl_original_data_id": "' + SELECTED_ORG_DATA_ID + '","train_request": {"pre_trained_model": "' + b_pModel + '","train_param": {"epoch":' + epochVal + '}}}'
		if (requestParam_string == "")
			return;

		//전문가용
	} else {
		userType = "expert";
		var requestParam = new Object();
		getActivePreTrained();

		requestParam.project_id = parseInt(PROJECT_ID);
		requestParam.dl_original_data_id = parseInt(SELECTED_ORG_DATA_ID);

		var trainRequestList = new Object();
		trainRequestList.pre_trained_model = $("#preTrainedModel_expert_selectBox option:selected").val();

		var modifiedNetworkList = new Object();
		modifiedNetworkList.trainable_index = TRAINABLE_IDX;
		modifiedNetworkList.additional_layers = ADDITIONAL_LAYERS;
		trainRequestList.modified_network = modifiedNetworkList;

		var generatorParamObj = makeGeneratorParam();
		var trainParamObj = makeTrainParamObj();
		var paramObj = makeParamObj(generatorParamObj, trainParamObj);

		trainRequestList.train_summary = paramObj;
		requestParam.train_request = trainRequestList;
		requestParam_string = JSON.stringify(requestParam);
	}

	//최종적으로 만들어진 model 생성 request param
	createDeepLearningModel(requestParam_string, userType);
}

/**
 * generator 파라미터 객체 생성
 */
function makeGeneratorParam() {
	var generatorParamObj = new Object();

	generatorParamObj.random_state = ($("#generator_random_state").val() == "" || $("#generator_random_state").val() == null) ? "None" : parseInt($("#generator_random_state").val());
	generatorParamObj.rotation = ($("#generator_rotation")[0].checked == true) ? "True" : "False";
	generatorParamObj.shifting = ($("#generator_shifting")[0].checked == true) ? "True" : "False";
	generatorParamObj.rescaling = ($("#generator_rescaling")[0].checked == true) ? "True" : "False";
	generatorParamObj.flipping = ($("#generator_flipping")[0].checked == true) ? "True" : "False";
	generatorParamObj.shearing = ($("#generator_shearing")[0].checked == true) ? "True" : "False";

	return generatorParamObj;
}

/**
 * train 파라미터 객체 생성
 */
function makeTrainParamObj() {
	var trainParamObj = new Object();

	trainParamObj.epoch = parseInt($("#train_epoch").val());
	trainParamObj.validation_ratio = parseFloat($("#train_validation_ratio").val());
	trainParamObj.batch_size = parseInt($("#train_batch_size").val());
	trainParamObj.shuffle = ($("#train_shuffle")[0].checked == true) ? "True" : "False";
	trainParamObj.optimizer = $("#train_optimizer_seletbox option:selected").val();
	trainParamObj.learning_rate = parseFloat($("#train_learning_rate").val());

	return trainParamObj;
}

/**
 * 파라미터 객체 생성
 * 
 * @param {JSONObject} generatorParamObj 
 * @param {JSONObject} trainParamObj 
 */
function makeParamObj(generatorParamObj, trainParamObj) {
	var paramObj = new Object();

	paramObj.generator_param = generatorParamObj;
	paramObj.train_param = trainParamObj;

	return paramObj;
}

/**
 * 딥러닝 모델 생성 요청
 * 
 * @param {String} requestParam_string 
 * @param {String} userType 
 */
function createDeepLearningModel(requestParam_string, userType) {
	var dataResult;
	$.ajax({
		type: "post",
		url: '/deepLearning/createModel',
		data: {
			"requestParam": requestParam_string
			, "userType": userType
		},
		dataType: "json",
		success: function success(data) {
			dataResult = data.result.result;
			if (data.result.result == "fail") {
				fnComNotify("error", "모델 생성 요청을 실패 하였습니다.");
				fnComNotify("error", data.result.detail.data.title + data.result.detail.data.detail);
				return;
			}
			SELECTED_MODEL_ID = data.result.data.id;
		},
		error: function error(request, _error3) {
			fnComNotify("error", "모델 생성 요청을 실패 하였습니다.");
			console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error3);
		},
		complete: function complete() {
			if (dataResult != "fail") {
				var html = "<li>모델을 생성중 입니다...</li>";
				$("#modelList").html(html);
				fnCheck_interval();
			}
		}
	});
}

/**
 * 모델 생성 상태 확인
 */
function fnCheck_interval() {
	$("#deepLearningMain").html("<div class=\"x_panel\"><div class='progress-bar progress-bar-striped width_100p active' role='progressbar'>모델을 생성중 입니다 잠시만 기다려 주세요</div></div>");
	MODEL_CREATE_TIMER = setInterval(function () {
		checkModelStatus();
		if (MODEL_CREATE_STATUS == "success") {
			clearInterval(MODEL_CREATE_TIMER);
			fnComNotify("info", "모델이 생성 되었습니다.");
			fnGetModelList();
			location.reload();
		} else if (MODEL_CREATE_STATUS == "fail") {
			clearInterval(MODEL_CREATE_TIMER);
			fnComNotify("error", "모델이 생성 실패하였습니다.");
			setTimeout(function () { location.reload(); }, 1500);
		}
	}, 5000);
}

/**
 * 학습 요청- 모델 상태확인
 */
function checkModelStatus() {
	if (SELECTED_MODEL_ID == null)
		return;

	$.ajax({
		type: "post",
		url: '/deepLearning/checkModelStatus',
		data: {
			"modelId": SELECTED_MODEL_ID
		},
		dataType: "json",
		global: false,
		success: function success(data) {
			MODEL_CREATE_STATUS = data.status.progress_state;
			var now = new Date();
			var runTime = secondToHHMMSS(getTimefommat(data.status.create_date, now.getTime()));
			$("#deepLearningMain").html("<div class=\"x_panel\"><div class='progress-bar progress-bar-striped width_100p active' role='progressbar'>model_" + data.status.id + "을 생성중 입니다 잠시만 기다려 주세요 (진행시간 : " + runTime + ")</div></div>");
		},
		error: function error(request, _error3) {
			fnComNotify("error", "모델 생성 요청을 실패 하였습니다.");
			console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error3);
		}
	});
}

/**
 * 선택된 원본데이터가 있는경우 모델리스트를 가져온다
 */
function fnGetModelList() {
	if (SELECTED_ORG_DATA_ID == null)
		return;

	$.ajax({
		type: "post",
		data: {
			"projectId": PROJECT_ID
			, "orgId": SELECTED_ORG_DATA_ID
		},
		url: '/deepLearning/getModelList',
		dataType: "json",
		success: function success(data) {
			if (data.result.result == "fail") {
				fnComNotify("error", data.result.detail);
				return;
			}

			var modelListTemplate = getModelListTemplate(data);

			if (modelListTemplate == "") {
				modelListTemplate += "<li>모델이 없습니다.</li>";
				$("#deepLearningMain2").hide();
				$("#deepLearningMain3").hide();
				$("#deepLearningMain4").hide();
			}

			$("#modelList").html(modelListTemplate);
		},
		error: function error(request, _error3) {
			fnComNotify("error", "요청이 실패 하였습니다.");
			console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error3);
		},
		complete: function complete() {
		}
	});
}

/**
 * 모델 리스트 화면 구성요소
 * 
 * @param {JSONObject} data 
 */
function getModelListTemplate(data) {
	var html = "";

	for (var i in data.data) {
		var mData = data.data[i];

		//모델 리스트 중에 ongoing 인 모델이이 있으면 interval 실행
		if (mData.progress_state == 'ongoing') {
			SELECTED_MODEL_ID = mData.id;
			fnCheck_interval();
			html += "<li>모델을 생성중 입니다...</li>";
			$("#deepLearningMain2").hide();
			$("#deepLearningMain3").hide();

			$("#deepLearningMain4").hide();
			$("#modelList").html(html);
			return;
		}

		var command = JSON.parse(data.data[i].command.replace(/'/gi, "\""));
		var name = "model_" + mData.id + "_" + command.train_request.pre_trained_model;

		if (i == 0) {
			html += "<li class='pdTB_5 modelData active' role='button'" + " id=" + mData.id + " title=\"" + name + "\">" + name + "<i class=\"fa fa-trash floatR lineHeight_20 pdright_10\" onclick=\"fnDeleteModelData('" + mData.id + "');\"></i></li>";
			$("#deepLearningMain2").show();
			$("#deepLearningMain3").show();
			$("#deepLearningMain4").show();
			SELECTED_MODEL_ID = mData.id;
			setModelDataInfo();
		} else {
			html += "<li class='pdTB_5 modelData' role='button' " + "id=" + mData.id + " title=\"" + name + "\">" + name + "<i class=\"fa fa-trash floatR lineHeight_20 pdright_10\" onclick=\"fnDeleteModelData('" + mData.id + "');\"></i></li>";
		}
	}

	return html;
}

/**
 * 모델 상세정보 입력
 * 
 * @param {Number} modelId 
 */
function setModelDataInfo(modelId) {
	$.ajax({
		type: "post",
		data: {
			"modelId": SELECTED_MODEL_ID
		},
		url: '/deepLearning/getModel',
		dataType: "json",
		success: function success(data) {
			var validationSummary = JSON.parse(data.data.validation_summary.replace(/'/gi, "\""));
			var trainSummary = JSON.parse(data.data.train_summary.replace(/'/gi, "\""));
			var networkSummary = JSON.parse(data.data.network_summary.replace(/'/gi, "\""));
			var command = JSON.parse(data.data.command.replace(/'/gi, "\""));
			RESULT_ACCURACY = parseFloat(validationSummary.val_acc[validationSummary.val_acc.length - 1]) * 100;

			if (modelId != null) {
				return;
			}

			//데이터 제너레이터 테이블
			var generatorTemplate = getGeneratorTemplate(trainSummary);
			$("#data_generator_table").html(generatorTemplate);

			//파라미터설정 테이블
			var paramsTemplate = getParamsTemplate(trainSummary);
			$("#data_params_table").html(paramsTemplate);

			//모델 구조 정보 테이블
			var modelInfoTemplate = getModelInfoTemplate(networkSummary);
			$("#data_modelInfo_table").html(modelInfoTemplate);

			//학습결과 정확도
			var learningOutcomeTemplate = getLearningOutcomeTemplate(validationSummary);
			$("#data_result1_table").html(learningOutcomeTemplate);

			$("#accuracyResult").html(command.train_request.pre_trained_model + " 정확도 : " + RESULT_ACCURACY.toFixed(3) + "%");
			$("#model_name").html(command.train_request.pre_trained_model);

			//학습결과 이미지
			fn_setResultImage(validationSummary);

			var lossChartData = new Array();
			lossChartData.push(validationSummary.val_loss);
			lossChartData.push(JSON.parse(data.data.loss_summary));
			var accuracyChartData = new Array();
			accuracyChartData.push(validationSummary.val_acc);
			accuracyChartData.push(JSON.parse(data.data.accuracy_summary));

			//손실차트
			fnModelResult_chart(lossChartData, "loss", "loss_chartDiv");

			//정확도차트
			fnModelResult_chart(accuracyChartData, "accuracy", "accuracy_chartDiv");

			//학습소요시간
			$("#trainRunTime").html(secondToHHMMSS(getTimefommat(data.data.progress_start_date, data.data.progress_end_date)));

		},
		error: function error(request, _error3) {
			fnComNotify("error", "요청이 실패 하였습니다.");
			console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error3);
		},
		complete: function complete() {
		}
	});
}

/**
 * generator 화면 구성요소
 * 
 * @param {JSONObject} trainSummary 
 */
function getGeneratorTemplate(trainSummary) {
	var generatorHtml = "";

	generatorHtml += "<thead><tr><th>파라메터</th><th>값</th></tr></thead>";
	generatorHtml += "<tbody>";
	generatorHtml += "<tr><td>rescale</td><td>" + parseFloat(trainSummary.generator.rescale).toFixed(3) + "</td></tr>";
	generatorHtml += "<tr><td>validation_split</td><td>" + trainSummary.generator.validation_split + "</td></tr>";
	generatorHtml += "<tr><td>rotation_range</td><td>" + trainSummary.generator.rotation_range + "</td></tr>";
	generatorHtml += "<tr><td>width_shift_range</td><td>" + trainSummary.generator.width_shift_range + "</td></tr>";
	generatorHtml += "<tr><td>height_shift_range</td><td>" + trainSummary.generator.height_shift_range + "</td></tr>";
	generatorHtml += "<tr><td>zoom_range</td><td>" + trainSummary.generator.zoom_range + "</td></tr>";
	generatorHtml += "<tr><td>horizontal_flip</td><td>" + trainSummary.generator.horizontal_flip + "</td></tr>";
	generatorHtml += "<tr><td>vertical_flip</td><td>" + trainSummary.generator.vertical_flip + "</td></tr>";
	generatorHtml += "<tr><td>shear_range</td><td>" + trainSummary.generator.shear_range + "</td></tr>";
	generatorHtml += "</tbody>";

	return generatorHtml;
}

/**
 * 파라미터 설정 화면 구성요소
 * 
 * @param {JSONObject} trainSummary 
 */
function getParamsTemplate(trainSummary) {
	var paramsHtml = "";

	paramsHtml += "<thead><tr><th>파라메터</th><th>값</th></tr></thead>";
	paramsHtml += "<tbody>";
	paramsHtml += (trainSummary.params.epochs != null) ? "<tr><td>epochs</td><td>" + trainSummary.params.epochs + "</td></tr>" : "";
	paramsHtml += (trainSummary.params.steps != null) ? "<tr><td>steps</td><td>" + trainSummary.params.steps + "</td></tr>" : "";
	paramsHtml += (trainSummary.params.verbose != null) ? "<tr><td>verbose</td><td>" + trainSummary.params.verbose + "</td></tr>" : "";
	paramsHtml += (trainSummary.params.batch_size != null) ? "<tr><td>batchSize</td><td>" + trainSummary.params.batch_size + "</td></tr>" : "";
	paramsHtml += (trainSummary.params.shuffle != null) ? "<tr><td>shuffle</td><td>" + trainSummary.params.shuffle + "</td></tr>" : "";
	paramsHtml += (trainSummary.params.optimizer != null) ? "<tr><td>optimizer</td><td>" + trainSummary.params.optimizer + "</td></tr>" : "";
	paramsHtml += (trainSummary.params.learning_rate != null) ? "<tr><td>learningRate</td><td>" + trainSummary.params.learning_rate + "</td></tr>" : "";
	paramsHtml += "</tbody>";

	return paramsHtml;
}

/**
 * 모델 구조 정보 화면 구성요소
 * 
 * @param {JSONObject} networkSummary 
 */
function getModelInfoTemplate(networkSummary) {
	var modelInfoHtml = "";

	modelInfoHtml += "<thead><tr><th>레이어</th><th>이름</th><th>종류</th><th>레이어 사용 여부</th></tr></thead>";
	modelInfoHtml += "<tbody>";

	for (var i in networkSummary) {
		modelInfoHtml += "<tr>";
		modelInfoHtml += "<td>" + networkSummary[i].Layer + "</td>";
		modelInfoHtml += "<td>" + networkSummary[i].Name + "</td>";
		modelInfoHtml += "<td>" + networkSummary[i].Type + "</td>";
		modelInfoHtml += "<td>" + networkSummary[i].Trainable + "</td>";
		modelInfoHtml += "</tr>";
	}

	modelInfoHtml += "</tbody>";

	return modelInfoHtml;
}

/**
 * 학습 결과 화면 구성요소
 * 
 * @param {JSONObject} validationSummary 
 */
function getLearningOutcomeTemplate(validationSummary) {
	var resultHtml = "";

	resultHtml += "<thead><tr><th>No</th><th>이미지</th><th>실제</th><th>예측</th></tr></thead>";
	resultHtml += "<tbody>";

	if (validationSummary.validation_data_file_name != null) {
		for (var i = 0, iSize = validationSummary.validation_data_file_name.length; i < iSize; i++) {
			resultHtml += "<tr>";
			resultHtml += "<td>" + (i + 1) + "</td>";
			resultHtml += "<td>" + validationSummary.validation_data_file_name[i] + "</td>";
			resultHtml += "<td>" + validationSummary.validation_data_y[i] + "</td>";
			resultHtml += "<td>" + validationSummary.validation_data_pred[i] + "</td>";
			resultHtml += "</tr>";
		}
	}
	resultHtml += "</tbody>";

	return resultHtml
}

/**
 * 딥러닝 모델 삭제
 * 
 * @param {Number} modelId 
 */
function fnDeleteModelData(modelId) {
	if (confirm("해당 모델을 삭제하시겠습니까?")) {
		$.ajax({
			type: "post",
			data: {
				"modelId": modelId
				, "projectId": PROJECT_ID
			},
			url: '/deepLearning/deleteModel',
			dataType: "json",
			success: function success(data) {
				if (data.result.result == "fail") {
					fnComNotify("error", data.result.detail);
				} else if (data.result.result == "success") {
					fnComNotify("info", data.result.detail);
				} else if (data.result.result == "warning") {
					fnComNotify("warning", data.result.detail);
				}
			},
			error: function error(request, _error3) {
				fnComNotify("error", "딥러닝 원본데이터 삭제 요청이 실패 하였습니다.");
				console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error3);
			},
			complete: function complete() {
				location.reload();
			}
		});
	}
}

/**
 * 모델 결과 차트
 * 
 * @param {Number[][]} param 
 * @param {String} chartType 
 * @param {Number} chartId 
 */
function fnModelResult_chart(param, chartType, chartId) {
	am4core.ready(function () {
		am4core.options.commercialLicense = true;
		am4core.useTheme(am4themes_animated); // Themes end
		var resultChart = am4core.create(chartId, am4charts.XYChart);

		// Add legend
		resultChart.legend = new am4charts.Legend();
		// Add cursor
		resultChart.cursor = new am4charts.XYCursor();

		var list = [];
		if (param != null) {
			for (var i = 0, iSize = param[0].length; i < iSize; i++) {
				var data = {
					"Epoch": (i + 1)
					, "training": param[1][i]
					, "validation": param[0][i]
				};
				list.push(data);
			}
		}

		resultChart.data = list; // Create axes
		var categoryAxis = resultChart.xAxes.push(new am4charts.CategoryAxis());
		categoryAxis.dataFields.category = "Epoch";
		categoryAxis.title.text = "Epoch";
		categoryAxis.renderer.grid.template.location = 0;
		categoryAxis.renderer.minGridDistance = 30;
		categoryAxis.renderer.labels.template.horizontalCenter = "right";
		categoryAxis.renderer.labels.template.verticalCenter = "middle";
		categoryAxis.renderer.labels.template.rotation = 0;
		//	  categoryAxis.renderer.minHeight = 50;

		var valueAxis = resultChart.yAxes.push(new am4charts.ValueAxis());
		valueAxis.tooltip.disabled = true;
		//	  valueAxis.renderer.minWidth = 50;

		var series = resultChart.series.push(new am4charts.LineSeries());
		series.sequencedInterpolation = true;
		series.dataFields.valueY = "training";
		series.dataFields.categoryX = "Epoch";
		series.tooltipText = "training " + chartType + ":{valueY}[/]";
		series.tooltip.pointerOrientation = "vertical";
		series.name = "training";

		var bullet = series.bullets.push(new am4charts.CircleBullet());
		bullet.circle.radius = 6;
		bullet.circle.fill = am4core.color("#fff");
		bullet.circle.strokeWidth = 3;

		var series2 = resultChart.series.push(new am4charts.LineSeries());
		series2.sequencedInterpolation = true;
		series2.dataFields.valueY = "validation";
		series2.dataFields.categoryX = "Epoch";
		series2.tooltipText = "validation " + chartType + ":{valueY}[/]";
		series2.tooltip.pointerOrientation = "vertical";
		series2.name = "validation";

		var bullet2 = series2.bullets.push(new am4charts.CircleBullet());
		bullet2.circle.radius = 4;
		bullet2.circle.fill = am4core.color("#fff");
		bullet2.circle.strokeWidth = 3;

	}); // end am4core.ready()
};

/**
 * 이미지경로의 key:파일 value:경로 맵 가져오기
 * 
 * @param {JSONObject} validationSummary 
 */
function fn_setResultImage(validationSummary) {
	$.ajax({
		type: "post",
		data: {
			"fileFullPath": SELECTED_ORG_DATA_DIR
		},
		url: '/dataManage/getImageMap',
		dataType: "json",
		success: function success(data) {
			var resultImageTemplate = getResultImageTemplate(data, validationSummary);
			$("#data_result2_table").html(resultImageTemplate);
		},
		error: function error(request, _error3) {
			fnComNotify("error", "딥러닝 원본데이터 삭제 요청이 실패 하였습니다.");
			console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error3);
		}
	});
}

/**
 * 이미지 div 화면 구성요소
 * 
 * @param {JSONObject} data 
 * @param {JSONObject} validationSummary 
 */
function getResultImageTemplate(data, validationSummary) {
	var html = "";
	html += "<div class=\"row\">";
	for (var i = 0, iSize = validationSummary.validation_data_file_name.length; i < iSize; i++) {
		var key = validationSummary.validation_data_file_name[i];

		html += "<div class=\"col-md-6\">";
		html += "	<div class=\"thumbnail\">";
		html += "		<div class=\"image view view-first width_100p height_80p\">";
		html += "		<img class='width_100p disp_block' src=\"/dataManage/display?fileFullPath=" + data.imageMap[key] + "\" alt=\"image\">";
		html += "		</div>";
		html += "		<div class=\"caption\">";
		html += "			<p>" + key + "</p>";
		html += "		</div>";
		html += "	</div>";
		html += "</div>";
	}

	html += "</div>";

	return html;
}

/**
 * 딥러닝 테스트 요청
 */
function fn_deeplearningTest() {
	$("#modelTestResult").html("");
	$("#testResultTable").html("");
	$("#modelResultChart").html("");
	if (TARGET_PATH == null) {
		fnComNotify("warning", "좌측상단의 파일을 선택해 주세요");
		return;
	}
	$.ajax({
		type: "post",
		data: {
			"targetPath": TARGET_PATH
			, "modelId": SELECTED_MODEL_ID
		},
		url: '/deepLearning/testModel',
		dataType: "json",
		success: function success(data) {
			if (data.result.result == "fail") {
				fnComNotify("error", data.result.detail + "\n적합한 테스트 데이터를 선택해 주세요.");
				return;
			}

			var predict = data.result.data.data.predict;
			var realValue = data.result.data.data.real_value;
			var accuracy = data.result.data.data.accuracy;

			TEST_ACCURACY = parseFloat(accuracy) * 100
			$("#modelTestResult").html("테스트 정확도 : <span>" + TEST_ACCURACY.toFixed(3) + "%</span>");

			var testResultTableTemplate = getTestResultTableTemplate(predict, realValue);
			$("#testResultTable").html(testResultTableTemplate);

			fnModelTestResultChart2();
		},
		error: function error(request, _error3) {
			fnComNotify("error", "딥러닝 테스트 요청이 실패 하였습니다.");
			console.log("code:" + request.status + "\n" + "message:" + request.responseText + "\n" + "error:" + _error3);
		}
	});
}

/**
 * 테스트 결과 테이블 화면 구성요소
 * 
 * @param {Array} predict 
 * @param {Array} realValue 
 */
function getTestResultTableTemplate(predict, realValue) {
	var testResultTableHtml = "";

	testResultTableHtml += "<thead><tr><th>실제</th><th>예측</th></tr>";
	testResultTableHtml += "</thead>";
	testResultTableHtml += "<tbody>";

	for (var i = 0, iSize = predict.length; i < iSize; i++) {
		testResultTableHtml += "<tr>";
		testResultTableHtml += "<td>" + realValue[i] + "</td>";
		testResultTableHtml += "<td>" + predict[i] + "</td>";
		testResultTableHtml += "</tr>";
	}

	testResultTableHtml += "</tbody>";

	return testResultTableHtml;
}

/**
 * 모델 테스트 결과 차트
 */
function fnModelTestResultChart2() {
	am4core.ready(function () {
		am4core.options.commercialLicense = true;

		am4core.useTheme(am4themes_animated); // Themes end
		// Create chart instance

		var testResultChart = am4core.create("modelResultChart", am4charts.XYChart);

		var list = new Array();
		var data1 = {
			"범주값": "학습(Train)",
			"frequency": parseFloat(RESULT_ACCURACY).toFixed(3)
		};
		var data2 = {
			"범주값": "검증(Test)",
			"frequency": parseFloat(TEST_ACCURACY).toFixed(3)
		};
		list.push(data1);
		list.push(data2);
		testResultChart.data = list; // Create axes

		var categoryAxis = testResultChart.xAxes.push(new am4charts.CategoryAxis());
		categoryAxis.dataFields.category = "범주값"; //		categoryAxis.title.text = "범주값";

		categoryAxis.renderer.grid.template.location = 0;
		categoryAxis.renderer.minGridDistance = 10;
		categoryAxis.renderer.labels.template.horizontalCenter = "right";
		categoryAxis.renderer.labels.template.verticalCenter = "middle";
		categoryAxis.renderer.labels.template.rotation = 0;
		categoryAxis.tooltip.disabled = true;
		categoryAxis.renderer.minHeight = 0;
		var valueAxis = testResultChart.yAxes.push(new am4charts.ValueAxis());
		valueAxis.renderer.minWidth = 50;
		valueAxis.title.text = "정확도%(accuracy)"; // Create series

		var series = testResultChart.series.push(new am4charts.ColumnSeries());
		series.sequencedInterpolation = true;
		series.dataFields.valueY = "frequency";
		series.dataFields.categoryX = "범주값";
		series.tooltipText = "[{범주값}: bold]{valueY}%[/]";
		series.columns.template.strokeWidth = 0;
		series.columns.template.width = am4core.percent(30);
		series.tooltip.pointerOrientation = "vertical";
		series.columns.template.column.cornerRadiusTopLeft = 10;
		series.columns.template.column.cornerRadiusTopRight = 10;
		series.columns.template.column.fillOpacity = 0.8; // on hover, make corner radiuses bigger

		var hoverState = series.columns.template.column.states.create("hover");
		hoverState.properties.cornerRadiusTopLeft = 0;
		hoverState.properties.cornerRadiusTopRight = 0;
		hoverState.properties.fillOpacity = 1;
		series.columns.template.adapter.add("fill", function (fill, target) {
			return testResultChart.colors.getIndex(target.dataItem.index);
		}); // Cursor

		testResultChart.cursor = new am4charts.XYCursor();
	}); // end am4core.ready()
};

/**
 * 시간 표현 방식 변경
 * 
 * @param {Number} oldTime 
 * @param {Number} nowTime 
 */
function getTimefommat(oldTime, nowTime) {
	var second = 1000;
	var minutes = 1000 * 60;
	var hours = minutes * 60;
	var days = hours * 24;
	var years = days * 365;

	var time;
	if (nowTime != null) {
		time = nowTime - oldTime;
	} else {
		time = oldTime;
	}
	return parseInt(time / second);
}

/**
 * 초를 시분초로 변경
 * 
 * @param {Number} sec_num 
 */
function secondToHHMMSS(sec_num) {
	//    var sec_num = parseInt(this, 10); // don't forget the second param
	var hours = Math.floor(sec_num / 3600);
	var minutes = Math.floor((sec_num - (hours * 3600)) / 60);
	var seconds = sec_num - (hours * 3600) - (minutes * 60);

	if (hours < 10) { hours = "0" + hours; }
	if (minutes < 10) { minutes = "0" + minutes; }
	if (seconds < 10) { seconds = "0" + seconds; }
	return hours + ':' + minutes + ':' + seconds;
}

/*초보자 여부 확인*/
function onBeginnerMode(bool) {
	IS_BEGINNER = bool;
}
