/**
 * 떠나봄 회원 인증 관련 공통 모듈 (auth.js)
 */

// 1. 비밀번호 정규식 및 일치 여부 실시간 검증 세팅
function initPasswordValidation(pwdInputSelector, pwdCheckSelector, ruleMsgSelector, mismatchMsgSelector) {
    const reg = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,20}$/;

    // 비밀번호 입력 시 규칙 검증
    $(pwdInputSelector).on("keyup", function() {
        let pwd = $(this).val();
        if (reg.test(pwd)) {
            $(ruleMsgSelector).text("사용 가능한 비밀번호입니다.").css("color", "green");
        } else {
            $(ruleMsgSelector).text("8~20자, 영문·숫자·특수문자(!@#$%^&*)를 각각 1개 이상 포함해야 합니다.").css("color", "red");
        }
        
        // 비밀번호를 다시 바꿨을 때 일치 여부도 같이 업데이트
        checkPasswordMatch(pwdInputSelector, pwdCheckSelector, mismatchMsgSelector);
    });

    // 비밀번호 확인 입력 시 일치 여부 검증
    $(pwdCheckSelector).on("keyup", function() {
        checkPasswordMatch(pwdInputSelector, pwdCheckSelector, mismatchMsgSelector);
    });
}

// 비밀번호 일치 확인 내부 함수
function checkPasswordMatch(pwdInputSelector, pwdCheckSelector, mismatchMsgSelector) {
    let pwd = $(pwdInputSelector).val();
    let check = $(pwdCheckSelector).val();

    if (!check) {
        $(mismatchMsgSelector).text("");
        return;
    }

    if (pwd !== check) {
        $(mismatchMsgSelector).text("비밀번호 불일치").css("color", "red");
    } else {
        $(mismatchMsgSelector).text("비밀번호 일치").css("color", "green");
    }
}


// 2. 이메일 인증 통합 세팅
// - 가입과 수정 페이지의 HTML 구조가 조금 다르므로, 이메일 값을 추출하는 이벤트를 유연하게 바인딩합니다.
function initEmailVerification({
    sendBtn, verifyBtn, authArea, emailMsg, authCodeInput,
    getEmailFunction // 호출되면 전체 이메일 주소를 반환하는 함수를 인자로 받음
}) {
    
    // 인증번호 발송
    $(sendBtn).click(function () {
        let email = getEmailFunction();
        $("#fullEmail").val(email); // hidden input이 있다면 세팅

        $.ajax({
            url: "/member/send",
            type: "post",
            data: { email: email },
            success: function (res) {
                if (res === "success") {
                    sessionStorage.setItem("emailVerified", "false");
                    $(authArea).slideDown();
                    $(emailMsg).text("인증번호 발송 완료").css("color", "green");
                    $(sendBtn).prop("disabled", true);

                    setTimeout(function () {
                        $(sendBtn).prop("disabled", false);
                    }, 30000);
                } else {
                    $(emailMsg).text("발송 실패").css("color", "red");
                }
            }
        });
    });

    // 인증번호 확인
    $(verifyBtn).click(function () {
        $.ajax({
            url: "/member/verify",
            type: "post",
            data: { code: $(authCodeInput).val() },
            success: function(res) {
                if (res === "success") {
                    sessionStorage.setItem("emailVerified", "true");
                    $(emailMsg).text("✔ 이메일 인증 완료").css("color", "green");
                    $(authCodeInput).prop("disabled", true);
                    $(verifyBtn).prop("disabled", true);
                    $(sendBtn).prop("disabled", true);
                } else {
                    sessionStorage.setItem("emailVerified", "false");
                    $(emailMsg).text("인증 실패").css("color", "red");
                }
            }
        });
    });
}