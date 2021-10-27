document.addEventListener("DOMContentLoaded", function () {
    toggleEye();
    var login = new Login();
    var idFind = new IdFind();
    var passwordFind = new PasswordFind(idFind);
    var userRegister = Object.create(UserRegister);
    userRegister.init();
});

var Login = (function () {
    function Login() {
        var _this = this;
        var key = this.fnGetCookie("key");
        $("#userId").val(key);

        $("#userPw").bind("keypress", function (e) {
            var ENTER = 13;
            if (e.which === ENTER) {
                _this.fnLogin();
            }
        });

        $("#loginBtn").on("click", function () {
            _this.fnLogin();
        })

        $("input[name='userId']").focus();
        var error = window.location.href.split("error=")[1];
        if (error === "accountError") {
            fnComNotify("error", "계정 정보가 정확하지 않습니다.");
        }
    }

    /**
     * 로그인 동작
     */
    Login.prototype.fnLogin = function () {
        if ($.trim($("#userId").val()) == "") {
            fnComNotify("warning", "ID를 입력해주세요.");
            $("#userId").focus();

        } else if ($.trim($("#userPw").val()) == "") {
            fnComNotify("warning", "비밀번호를 입력해주세요.");
            $("#userPw").focus();

        } else {
            // 아이디 저장 체크
            if ($("#saveID").is(":checked")) {
                this.fnSetCookie("key", $("#userId").val(), 7); // 7일 동안 쿠키 보관
            } else {
                this.fnDeleteCookie("key");
            }

            $("#loginForm").submit();
        }
    }

    /**
     * 아이디 저장을 위한 쿠키 설정
     */
    Login.prototype.fnSetCookie = function (cookieName, value, exdays) {
        var exdate = new Date();
        exdate.setDate(exdate.getDate() + exdays);
        var cookieValue = escape(value) + ((exdays == null) ? "" : "; expires=" + exdate.toGMTString());
        document.cookie = cookieName + "=" + cookieValue;
    }

    /**
     * 쿠키 삭제
     */
    Login.prototype.fnDeleteCookie = function (cookieName) {
        var expireDate = new Date();
        expireDate.setDate(expireDate.getDate() - 1);
        document.cookie = cookieName + "= " + "; expires=" + expireDate.toGMTString();
    }

    /**
     * 쿠키 가져오기
     */
    Login.prototype.fnGetCookie = function (cookieName) {
        cookieName = cookieName + '=';
        var cookieData = document.cookie;
        var start = cookieData.indexOf(cookieName);
        var cookieValue = '';
        if (start != -1) {
            start += cookieName.length;
            var end = cookieData.indexOf(';', start);
            if (end == -1) end = cookieData.length;
            cookieValue = cookieData.substring(start, end);
        }
        return unescape(cookieValue);
    }

    return Login;
}())

var IdFind = (function () {
    function IdFind() {
        var _this = this;
        $(".modal-footer.user_id").on("click", "#find_id", function () {
            _this._fnFindUserId();
        })

        $("#id_area").on("keypress", "#findUserId_email", function (e) {
            var enter = 13;
            if (e.which === enter) {
                _this._fnFindUserId();
            }
        })

        $("#loginidModal").on("hide.bs.modal", function () {
            $("#id_area").html(_this._findIdTemplate());
            $(".modal-footer.user_id").html(_this._findUserButtonTemplate());

        })
    }

    /**
     * 버튼 동적 변경을 위한 html 템플릿
     */
    IdFind.prototype._findUserButtonTemplate = function () {
        return '<button id="find_id" type="button" class="btn btn-success">확인</button>';
    }

    /**
     * 아이디 찾기 내용 동적 변경을 위한 html 템플릿
     */
    IdFind.prototype._findIdTemplate = function () {
        var html = "";

        html += '<div class="form-group">';
        html += '<label class="control-label col-md-3 col-sm-3 col-xs-12 text-left"';
        html += 'for="domain_name">* 이름</label>';
        html += '<div class="col-md-9 col-sm-9 col-xs-12">';
        html += '<input type="text" class="form-control" placeholder="홍길동"';
        html += 'id="findUserId_userName" name="findUserId_userName"';
        html += 'required="required">';
        html += '</div>';
        html += '</div>';
        html += '<div class="form-group">';
        html += '<label class="control-label col-md-3 col-sm-3 col-xs-12 text-left"';
        html += 'for="domain_model">* 이메일</label>';
        html += '<div class="col-md-9 col-sm-9 col-xs-12">';
        html += '<input type="email" class="form-control" placeholder="이메일"';
        html += 'id="findUserId_email" name="findUserId_email" required="required">';
        html += '</div>';
        html += '</div>';

        return html;
    }

    /**
     * 아이디 찾기
     */
    IdFind.prototype._fnFindUserId = function () {
        var data = {
            "userName": $("#findUserId_userName").val()
            , "email": $("#findUserId_email").val()
            , "option": "findUserId"
        }
        var response = this.fnFindUserByAjax(data);
        if (response.result == "success") {
            $("#id_area").html("사용자 ID는 " + response.user.userId + "입니다.");
            $("#id_area").css("text-align", "center");
            $(".modal-footer.user_id").html(this._closeUserIdButton());
        } else {
            fnComNotify("error", "해당 이름과 이메일이 일치하는 사용자가 없습니다.");
        }
    }

    /**
     * 아이디 찾기 처리 로직
     */
    IdFind.prototype.fnFindUserByAjax = function (data) {
        var result;
        var url = "/users/find";
        this._fnPatchAjaxDataSync(url, JSON.stringify(data), function (response) {
            result = response;
        });
        return result;
    }

    /**
     * 아이디를 통한 사용자 조회 요청
     */
    IdFind.prototype._fnPatchAjaxDataSync = function (url, req_data, fn_success) {
        $.ajax({
            url: url,
            type: "POST",
            dataType: "json",
            contentType: 'application/json',
            data: req_data,
            async: false,
            success: function (response) {
                if (fn_success) fn_success(response);
            },
            error: function () {
                fnComNotify("error", "존재하지 않는 계정 정보입니다.");
            }
        });
    };

    /**
     * 아이디 찾기 버튼 동적 변경을 위한 html 템플릿
     */
    IdFind.prototype._closeUserIdButton = function () {
        return '<button type="button" class="btn btn-success" data-dismiss="modal">close</button>';
    }

    return IdFind;
}())

var PasswordFind = (function () {
    function PasswordFind(idFind) {
        var _this = this;

        $(".modal-footer.password").on("click", "#find_password", function () {
            _this._fnFindUserPw(idFind);
        })

        $("#password_area").bind("keypress", "#findUserPw_email", function (e) {
            var ENTER = 13;
            if (e.which === ENTER) {
                _this._fnFindUserPw(idFind);
            }
        })

        $("#loginpwModal").on("hide.bs.modal", function () {
            $("#password_area").html(_this._findPwTemplate());
            $(".modal-footer.password").html(_this._findPwButtonTemplate());
        })
    }

    /**
     * 비밀번호 찾기
     */
    PasswordFind.prototype._fnFindUserPw = function (idFind) {
        var data = {
            "userId": $("#findUserPw_userId").val()
            , "email": $("#findUserPw_email").val()
            , "option": "findUserPw"
        }
        var response = idFind.fnFindUserByAjax(data);
        if (response.result === "success") {
            $("#password_area").html("임시 비밀번호는 " + response.tempPw + "입니다.");
            $("#password_area").css("text-align", "center");
            $(".modal-footer.password").html(this._closeUserPwButton());
        } else {
            fnComNotify("error", "해당 아이디의 사용자가 없습니다.");
        }
    }

    /**
     * 비밀번호 찾기 내용 동적 변경을 위한 html 템플릿
     */
    PasswordFind.prototype._findPwTemplate = function () {
        var html = "";

        html += '<div class="form-group">';
        html += '<label class="control-label col-md-3 col-sm-3 col-xs-12 text-left"';
        html += 'for="domain_name">* 아이디</label>';
        html += '<div class="col-md-9 col-sm-9 col-xs-12">';
        html += '<input type="text" class="form-control" placeholder="사용자 아이디"';
        html += 'id="findUserPw_userId" name="findUserPw_userId" required="required">';
        html += '</div>';
        html += '</div>';
        html += '<div class="form-group">';
        html += '<label class="control-label col-md-3 col-sm-3 col-xs-12 text-left"';
        html += 'for="domain_model">* 이메일</label>';
        html += '<div class="col-md-9 col-sm-9 col-xs-12">';
        html += '<input type="text" class="form-control" placeholder="abc@daumsoft.com"';
        html += 'id="findUserPw_email" name="findUserPw_email" required="required">';
        html += '</div>';
        html += '</div>';

        return html;
    }

    /**
     * 비밀번호 찾기 버튼 동적 변경을 위한 html 템플릿
     */
    PasswordFind.prototype._findPwButtonTemplate = function () {
        return '<button type="button" id="find_password" class="btn btn-success">확인</button>';
    }

    /**
     * 비밀번호 찾기 버튼 동적 변경을 위한 html 템플릿
     */
    PasswordFind.prototype._closeUserPwButton = function () {
        return '<button type="button" class="btn btn-success" data-dismiss="modal">close</button>';
    }

    return PasswordFind;
}())

var UserRegister = {
    _duplication: null,

    init: function () {
        var _this = this;
        $("#register_button").on("click", function () {
            if (!_this.checkId()) {
                return;
            }
            _this._fnRegisterUser();
        })
        _this.checkDuplicateId("dup1", "m_userId", "loginjoinModal");
    },

    /**
     * 아이디 중복 여부 검증
     */
    checkId: function () {
        if (this._duplication === true) {
            fnComNotify("warning", "다른 아이디를 사용해주세요.");
            return false;
        } else if (this._duplication === null) {
            fnComNotify("warning", "아이디 중복을 먼저 확인해주세요.");
            return false;
        }
        return true;
    },

    /**
     * 회원가입
     */
    _fnRegisterUser: function () {
        var user = {
            "userId": $("#m_userId").val()
            , "userName": $("#m_userName").val()
            , "email": $("#email_part option:selected").val() === '직접입력' ?
                $("#email").val() : $("#email").val() + $("#email_part option:selected").val()
            , "userPw": $("#password").val()
            , "userPw2": $("#confirmPassword").val()
            , "phoneNumber": $("#phoneNumber").val()
        }

        if (!this._verify(user)) {
            return;
        }

        if (confirm("가입 하시겠습니까?")) {
            var response = fnPostUserByAjax("/users/signUp", user);
            if (response.successMessage === "duplication userId") {
                $("#m_userId").focus();
                fnComNotify("warning", "이미 사용 중인 아이디입니다. 다른 아이디를 입력해주세요.");
            } else if (response.result === "success") {
                fnComNotify("success", "가입되었습니다.");
                $(".loginjoinModal").modal("hide");
            } else {
                fnComNotify("error", "요청이 정상적으로 처리되지 않았습니다.");
            }
        }
    },

    /**
     * 회원가입할 정보 유효성 검증
     * 
     * @param {Object} user 
     */
    _verify: function (user) {
        var validate = new Validate();
        if (!(validate.verifyUserId(user.userId)
            && validate.init(user)
            && this.verifyPhoneNumber(validate, user.phoneNumber))) {
            return false;
        }
        return true;
    },

    /**
     * 휴대전화번호 유효성 검증
     * 
     * @param {Object} validate 
     * @param {String} phoneNumber 
     */
    verifyPhoneNumber: function (validate, phoneNumber) {
        if (!validate.verifyBlank(phoneNumber)) {
            fnComNotify("warning", "휴대전화번호를 입력해주세요.");
            return false;
        }

        var regExp = /^\d{3}\d{3,4}\d{4}$/;
        if (!regExp.test(phoneNumber)) {
            fnComNotify("warning", "휴대전화번호를 형식에 맞게 입력해주세요.");
            return false;
        }

        return true;
    },

    /**
     * 아이디 중복 여부에 따른 로직
     */
    checkDuplicateId: function (id, location, modal) {
        var _this = this;
        var validate = new Validate();

        $("#" + location).on("keyup", function () {
            _this._duplication = null;
            $(".valid-feedback").hide();
            $(".invalid-feedback").hide();
        })

        $("#" + id).on("click", function () {
            var userId = $("#" + location).val();

            if (!validate.verifyUserId(userId)) {
                return;
            }

            var user = fnGetUserByAjax(userId);
            if (user != null) {
                _this._duplication = true;
                $(".valid-feedback").hide();
                $(".invalid-feedback").show();
            } else {
                _this._duplication = false;
                $(".invalid-feedback").hide();
                $(".valid-feedback").show();
            }
        })
        this._hideDuplicationMessage(modal);
    },

    /**
     * 중복 여부 관련 메시지 나타내기
     */
    _hideDuplicationMessage: function (modal) {
        var _this = this;
        $("#" + modal).on("hide.bs.modal", function () {
            _this._duplication = null;
            $(".valid-feedback").hide();
            $(".invalid-feedback").hide();
        })
    }
}

