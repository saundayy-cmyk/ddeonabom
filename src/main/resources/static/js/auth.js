/**
 * 떠나봄 회원 인증 관련 공통 모듈 (auth.js)
 */

// 비밀번호 정규식 및 일치 여부 실시간 검증 세팅
function initPasswordValidation(pwdInputSelector, pwdCheckSelector, ruleMsgSelector, mismatchMsgSelector) {
    const reg = /^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*])[A-Za-z\d!@#$%^&*]{8,20}$/;

    $(pwdInputSelector).on("keyup", function() {
        let pwd = $(this).val();
        if (reg.test(pwd)) {
            $(ruleMsgSelector).text("사용 가능한 비밀번호입니다.").css("color", "green");
        } else {
            $(ruleMsgSelector).text("8~20자, 영문·숫자·특수문자(!@#$%^&*)를 각각 1개 이상 포함해야 합니다.").css("color", "red");
        }
        checkPasswordMatch(pwdInputSelector, pwdCheckSelector, mismatchMsgSelector);
    });

    $(pwdCheckSelector).on("keyup", function() {
        checkPasswordMatch(pwdInputSelector, pwdCheckSelector, mismatchMsgSelector);
    });
}

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


// 이메일 인증 통합 세팅 
function initEmailVerification({
    sendBtn, verifyBtn, authArea, emailMsg, authCodeInput,
    getEmailFunction,
    successCallback //인증 성공 후 페이지별로 다르게 처리할 로직을 담는 콜백 함수
}) {
    
    // 인증번호 발송
    $(sendBtn).click(function () {
        let email = getEmailFunction();
        if(!email || email.trim() === "") {
            $(emailMsg).text("이메일을 입력해 주세요.").css("color", "red");
            return;
        }
        $("#fullEmail").val(email); 

        $(emailMsg).text("인증번호 발송 중...").css("color", "orange");

        // 현재 주소창에 'find'가 있으면 계정 찾기용 발송 URL로 자동 스위칭
        // 회원가입(/member/join), 수정(/member/edit) 시에는 기존 주소(/member/send) 그대로 동작
        let sendUrl = window.location.href.includes("find") ? "/member/find-account/send" : "/member/send";

        $.ajax({
            url: sendUrl,
            type: "post",
            data: { email: email },
            success: function (res) {
                if (res === "SUCCESS") {
                    sessionStorage.setItem("emailVerified", "false");
                    // 아이디 찾기 화면처럼 기본적으로 열려있는 구조면 slideDown이 안 먹거나 무해함
                    if(authArea) $(authArea).slideDown(); 
                    $(emailMsg).text("인증번호가 발송되었습니다. 메일함을 확인해 주세요.").css("color", "green");
                    $(sendBtn).prop("disabled", true);

                    setTimeout(function () {
                        $(sendBtn).prop("disabled", false);
                    }, 30000);
                } else if (res === "NOT_FOUND") {
                    $(emailMsg).text("❌ 가입되지 않은 이메일 주소입니다.").css("color", "red");
                } else {
                    $(emailMsg).text("발송 실패").css("color", "red");
                }
            },
            error: function() {
                $(emailMsg).text("🚨 서버 통신 에러").css("color", "red");
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
                if (res === "SUCCESS") {
                    sessionStorage.setItem("emailVerified", "true");
                    $(emailMsg).text("✔ 이메일 인증 완료").css("color", "green");
                    $(authCodeInput).prop("disabled", true);
                    $(verifyBtn).prop("disabled", true);
                    $(sendBtn).prop("disabled", true);
                    
                    // 💡 [추가] 인증 성공 시, 주입받은 페이지별 추가 후처리 로직 실행
                    if(typeof successCallback === "function") {
                        successCallback();
                    }
                } else {
                    sessionStorage.setItem("emailVerified", "false");
                    $(emailMsg).text("인증 실패").css("color", "red");
                }
            },
            error: function() {
                $(emailMsg).text("🚨 서버 통신 에러").css("color", "red");
            }
        });
    });
}