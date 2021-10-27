"use strict";

$(function () {
    fnInit();
});

var fnInit = function () {
    popupImg('guide');
    backToTop();
    toggleBackToTopButton();
    dropdownNav();
};

/**
 * 맨 위로 가기 버튼 클릭시 애니메이션 효과
 */
var backToTop = function () {
    $("#back-to-top").on("click", function () {
        $('html,body').animate({ scrollTop: 0 }, 600);
    })
}

/**
 * 맨 위로 가기 버튼 토글 효과
 */
var toggleBackToTopButton = function () {
    $(window).scroll(function () {
        if ($(document).scrollTop() > 100) {
            $('#back-to-top').addClass('show');
        } else {
            $('#back-to-top').removeClass('show');
        }
    });
}

/**
 * 네비게이션 바 동적 변경
 */
var dropdownNav = function () {
    $(".menu_title").on("click", function (event) {
        var target = event.target;
        var menu = target.nextElementSibling;

        if (target.tagName !== 'SPAN') {
            target = $(target).find("span");
        } else {
            menu = $(target).closest("a")[0].nextElementSibling;
        }
        $(target).toggleClass("fa-caret-down");
        $(target).toggleClass("fa-caret-right");

        if ($(target).hasClass("fa-caret-down")) {
            $(menu).css("display", "block");
        } else {
            $(menu).css("display", "none");
        }
    })
}