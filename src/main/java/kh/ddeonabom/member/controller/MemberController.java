package kh.ddeonabom.member.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
import kh.ddeonabom.member.model.exception.MemberException;
import kh.ddeonabom.member.model.vo.Member;
import kh.ddeonabom.member.service.EmailService;
import kh.ddeonabom.member.service.MemberService;
import lombok.RequiredArgsConstructor;



@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
	private final MemberService mService;
	private final BCryptPasswordEncoder bcrypt;
	private final EmailService emailService;
	
	@GetMapping("/join")
	public String joinPage() {
		
		return "views/member/join";
	}
	
	// 이메일 인증번호 발송 (AJAX)
    // 입력한 이메일로 인증번호 전송
    // 서버 세션에 인증코드 저장
    @PostMapping("/send")
    @ResponseBody
    public String sendEmail(@RequestParam("email") String email,
                            HttpSession session) {

        // 이메일로 인증번호 생성 + 메일 발송
        String code = emailService.sendAuthCode(email);

        // 서버 세션에 저장 (진짜 인증 기준)
        session.setAttribute("authCode", code);
        session.setAttribute("targetEmail", email);

        return "success";
    }


 // 인증번호 검증 (AJAX)
    //  사용자가 입력한 코드와 세션 코드 비교
    //  성공 시 인증 완료 상태 저장
    // =========================================================
    @PostMapping("/verify")
    @ResponseBody
    public String verifyEmail(@RequestParam("code") String code,
                              HttpSession session) {

        String savedCode = (String) session.getAttribute("authCode");
        String email = (String) session.getAttribute("targetEmail");

        // 인증 성공
        if (savedCode != null && savedCode.equals(code)) {

            // 인증 완료 상태 저장
            session.setAttribute("emailVerified", true);
            session.setAttribute("verifiedEmail", email);

            return "success";
        }

        // 인증 실패
        return "fail";
    }

 //  회원가입 처리
    // - 이메일 인증 여부 확인 (필수)
    // - 이메일 일치 여부 확인 (보안)
    // - 이메일 중복 체크
    // - 비밀번호 암호화 후 저장
    // =========================================================
    @PostMapping("/join")
    @ResponseBody
    public String join(@ModelAttribute Member m, HttpSession session) {

        // -------------------------------
        // 1) 이메일 인증 여부 체크
        // -------------------------------
        Boolean emailVerified = (Boolean) session.getAttribute("emailVerified");
        String verifiedEmail = (String) session.getAttribute("verifiedEmail");

        if (emailVerified == null || !emailVerified) {
            return "EMAIL_NOT_VERIFIED";
        }

        // -------------------------------
        // 2) 인증된 이메일과 실제 가입 이메일 비교
        // (중간 변조 방지)
        // -------------------------------
        if (verifiedEmail == null || !verifiedEmail.equals(m.getEmail())) {
            return "EMAIL_NOT_VERIFIED";
        }

        // -------------------------------
        // 3) 이메일 중복 체크
        // -------------------------------
        if (mService.existsByEmail(m.getEmail())) {
            return "DUPLICATE_EMAIL";
        }

        // -------------------------------
        // 4) 비밀번호 암호화
        // -------------------------------
        m.setPwd(bcrypt.encode(m.getPwd()));

        // -------------------------------
        // 5) DB 저장
        // -------------------------------
        int result = mService.insertMember(m);

        if (result > 0) {

            // -------------------------------
            // 6) 가입 완료 후 세션 정리
            // -------------------------------
            session.removeAttribute("emailVerified");
            session.removeAttribute("verifiedEmail");
            session.removeAttribute("authCode");

            return "SUCCESS";
        }

        return "FAIL";
    }
	@GetMapping("/login")
	public String loginPage() {
		return "views/member/login";
	}
	 // =========================================================
    // 6. 로그인 처리
    // =========================================================
    @PostMapping("/login")
    @ResponseBody
    public String login(@ModelAttribute Member m, HttpSession session) {

        Member loginUser = mService.login(m);

        // 비밀번호 검증
        if (loginUser != null && bcrypt.matches(m.getPwd(), loginUser.getPwd())) {

            session.setAttribute("loginUser", loginUser);
            return "SUCCESS";
        }

        return "FAIL";
    }
    
    @GetMapping("/edit")
    public String editPage() {
        return "views/member/edit";
    }

    @PostMapping("/update")
    public String updateMember(
            @ModelAttribute Member m,
            @RequestParam(required = false) String newPassword, // 선택 입력이므로 required = false
            @RequestParam String authCode,                      // 이메일 인증번호
            HttpSession session) {
        
        // [백엔드 로직 팁]
        // 1. 세션에서 로그인한 유저 정보 꺼내기
        // 2. 인증번호(authCode)가 맞는지 최종 검증
        // 3. 만약 newPassword가 비어있지 않다면(새 비번을 입력했다면) 암호화해서 m 객체에 세팅
        // 4. 서비스 레이어 호출하여 DB 업데이트 수행 (update member ...)
        
        return "redirect:/member/mypage"; // 수정 완료 후 마이페이지로 이동
    }

    // 수정 페이지에서 [회원 탈퇴] 링크를 클릭했을 때 (GET)
    @GetMapping("/withdraw")
    public String withdrawPage() {
        return "withdraw"; 
    }

    // 탈퇴 뷰 페이지에서 [탈퇴하기] 버튼을 눌렀을 때 (POST)
    @PostMapping("/withdraw")
    public String doWithdraw(@RequestParam String password, HttpSession session) {
        // 탈퇴 로직 처리...
        return "redirect:/"; 
    }
} // 괄호 갯수 정돈 완료 (에러 해결!)

	

