package kh.ddeonabom.member.controller;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import jakarta.servlet.http.HttpSession;
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
	// =========================================================
    // 1. 이메일 인증번호 발송 (auth.js의 /member/send와 매칭)
    // =========================================================
    @PostMapping("/send")
    @ResponseBody
    public String sendEmail(@RequestParam("email") String email, HttpSession session) {
        // 이메일로 인증번호 생성 + 메일 발송
        String code = emailService.sendAuthCode(email);

        // auth.js가 공통으로 검증할 수 있도록 세션에 Key 박제
        session.setAttribute("authCode", code);
        session.setAttribute("targetEmail", email);

        return "success"; // 소문자 success 반환
    }


 // 인증번호 검증 (AJAX)
    //  사용자가 입력한 코드와 세션 코드 비교
    //  성공 시 인증 완료 상태 저장
    // =========================================================
 // =========================================================
    // 2. 인증번호 검증 (auth.js의 /member/verify와 매칭)
    // =========================================================
    @PostMapping("/verify")
    @ResponseBody
    public String verifyEmail(@RequestParam("code") String code, HttpSession session) {
        String savedCode = (String) session.getAttribute("authCode");
        String email = (String) session.getAttribute("targetEmail");

        // auth.js에서 넘겨준 code와 세션의 savedCode 비교
        if (savedCode != null && savedCode.equals(code)) {
            // 인증 성공 상태를 세션에 저장 (가입/수정 공통)
            session.setAttribute("emailVerified", true);
            session.setAttribute("verifiedEmail", email);

            return "success"; // 소문자 success 반환
        }

        return "fail"; // 소문자 fail 반환
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
    
    public String login(@ModelAttribute Member m, @RequestParam(name="targetUrl", required=false) String targetUrl,
    		HttpSession session) {

        Member loginUser = mService.login(m);

        // 비밀번호 검증
        if (loginUser != null && bcrypt.matches(m.getPwd(), loginUser.getPwd())) {

            session.setAttribute("loginUser", loginUser);
         // 히든 인풋으로 넘어온 targetUrl(목적지)이 진짜로 존재한다면?
            if (targetUrl != null && !targetUrl.isEmpty()) {
                return "redirect:" + targetUrl; // 🚀 그 목적지(/member/edit)로 바로 튕겨줍니다!
            }
            
            // 만약에 나중에 메인페이지 등에서 로그인해서 targetUrl이 없을 때를 대비한 안전빵 주소
            return "redirect:/"; 
        }

        return "redirect:/member/login?error";
    }
    
    @GetMapping("/edit")
    public String editPage(HttpSession session,Model model) {
    	// 1. 세션에서 로그인된 회원 정보 가져오기
        Member loginUser = (Member) session.getAttribute("loginUser");
        
        // 2. 혹시나 세션이 끊긴 상태로 접근했다면 로그인 페이지로 튕겨내기
        if (loginUser == null) {
            return "redirect:/member/login";
        }
        
        // 3. edit.html이 데이터를 꺼내 쓸 수 있도록 모델에 담아서 배달하기
        model.addAttribute("loginUser", loginUser);
        
        return "views/member/edit";
   
    }
 // =========================================================
    // 3. 회원정보 수정 최종 처리 (교정 및 버그 박멸 완료 구역)
    // =========================================================
    @ResponseBody
    @PostMapping("/update")
    public String updateMember(
            @ModelAttribute Member m,            
            @RequestParam(name="currentPassword", required = false) String currentPassword, 
            @RequestParam(name="newPassword", required = false) String newPassword, 
            @RequestParam(name="authCode", required = false) String authCode,                     
            HttpSession session) {
        
        // ① 로그인 세션 체크
        Member loginUser = (Member) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "NOT_LOGGED_IN"; 
        }
        
        // 💡 [버그 해결 핵심]: 세션 대신 기존 로그인 로직(mService.login)을 재사용해서
        // DB에 들어있는 오염되지 않은 진짜 암호 원본을 실시간으로 새로 가져옵니다.
        Member dbUser = mService.login(loginUser); 
        if (dbUser == null) {
            return "NOT_LOGGED_IN";
        }
        
        // ② 새로운 비밀번호를 입력하려 할 때 현재 비밀번호 검증 진행
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            // 💡 갓 퍼온 진짜 암호문(dbUser.getPwd())과 화면 입력값(currentPassword)을 실시간 대조!
            if (!bcrypt.matches(currentPassword, dbUser.getPwd())) {
                return "WRONG_CURRENT_PASSWORD"; // 틀려도 세션이 오염되지 않고 여기서 즉시 안전하게 탈출!
            }
        }
        
        // 세션의 안전한 ID를 모델에 강제 세팅
        m.setId(loginUser.getId()); 

        // ③ 백엔드 최종 방어선: 세션에 저장된 인증 상태와 이메일 일치 여부 확인
        Boolean emailVerified = (Boolean) session.getAttribute("emailVerified");
        String verifiedEmail = (String) session.getAttribute("verifiedEmail");
        
        if (emailVerified == null || !emailVerified || verifiedEmail == null || !verifiedEmail.equals(m.getEmail())) {
            return "EMAIL_NOT_VERIFIED"; 
        }

        // ④ DB 업데이트 실행 (비밀번호 변경 여부에 따른 분기)
        int result = 0;
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            m.setPwd(bcrypt.encode(newPassword)); // 변경할 새 비밀번호 암호화
            result = mService.updateMemberWithPassword(m);
        } else {
            result = mService.updateMemberWithoutPassword(m);
        }
        
        // ⑤ 결과 처리 및 세션 정리
        if (result > 0) {
            session.removeAttribute("authCode");
            session.removeAttribute("targetEmail");
            session.removeAttribute("emailVerified");
            session.removeAttribute("verifiedEmail");
            
            // 데이터가 최종 성공했을 때만 세션 객체를 안전하게 동기화 갱신
            if (m.getNickName() != null) loginUser.setNickName(m.getNickName());
            if (m.getEmail() != null) loginUser.setEmail(m.getEmail());
            
            // 💡 [추가]: 비밀번호도 정상 변경되었다면 새 암호문으로 세션을 완벽 동기화해줍니다.
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                loginUser.setPwd(m.getPwd()); 
            }
            
            session.setAttribute("loginUser", loginUser);
            return "SUCCESS"; 
        } else {
            return "FAIL";
        }
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
 // 괄호 갯수 정돈 완료 (에러 해결!)
//1. 아이디 찾기 페이지 열기
@GetMapping("/find-id")
public String findIdPage() {
    return "views/member/find-id"; // views/member/find-id.html 파일을 만드시면 됩니다!
}

// 2. 비밀번호 찾기 페이지 열기
@GetMapping("/find-pw")
public String findPwPage() {
    return "views/member/find-pw"; // views/member/find-pw.html 파일을 만드시면 됩니다!
}
}

