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
	// =========================================================
	// [추가] 아이디 중복 체크 (Ajax)
	// =========================================================
	@ResponseBody
	@GetMapping("/check-id")
	public String checkId(@RequestParam("id") String id) {
		// 만약 STATUS='Y'인 회원이 이미 존재한다면 중복된 아이디
	    Member m = mService.selectOneMember(id);
	    
	    if (m != null) {
	        return "DUPLICATE"; // 이미 사용 중인 아이디
	    }
	    return "AVAILABLE"; // 사용 가능한 아이디
	}
	
	// 이메일 인증번호 발송 (AJAX)
    // 입력한 이메일로 인증번호 전송
    // 서버 세션에 인증코드 저장
	// =========================================================
    // 이메일 인증번호 발송 (auth.js의 /member/send와 매칭)
    // =========================================================
    @PostMapping("/send")
    @ResponseBody
    public String sendEmail(@RequestParam("email") String email, HttpSession session) {
        // 이메일로 인증번호 생성 + 메일 발송
        String code = emailService.sendAuthCode(email);

        // auth.js가 공통으로 검증할 수 있도록 세션에 Key 박제
        session.setAttribute("authCode", code);
        session.setAttribute("targetEmail", email);

        return "SUCCESS"; // 대문자 SUCCESS
    }
 // 인증번호 검증 (AJAX)
    //  사용자가 입력한 코드와 세션 코드 비교
    //  성공 시 인증 완료 상태 저장
    // =========================================================
 // =========================================================
    // 인증번호 검증 (auth.js의 /member/verify와 매칭)
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

            return "SUCCESS"; // success 반환
        }

        return "FAIL"; // fail 반환
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
        // 이메일 인증 여부 체크
        // -------------------------------
        Boolean emailVerified = (Boolean) session.getAttribute("emailVerified");
        String verifiedEmail = (String) session.getAttribute("verifiedEmail");

        if (emailVerified == null || !emailVerified) {
            return "EMAIL_NOT_VERIFIED";
        }

        // -------------------------------
        // 인증된 이메일과 실제 가입 이메일 비교
        // (중간 변조 방지)
        // -------------------------------
        if (verifiedEmail == null || !verifiedEmail.equals(m.getEmail())) {
            return "EMAIL_NOT_VERIFIED";
        }

        // -------------------------------
        // 이메일 중복 체크
        // -------------------------------
        if (mService.existsByEmail(m.getEmail())) {
            return "DUPLICATE_EMAIL";
        }
        if (mService.existsByNickName(m.getNickName())) { 
            return "DUPLICATE_NICKNAME"; // 프론트엔드에 닉네임 중복 알림 전달
        }

        // -------------------------------
        // 비밀번호 암호화
        // -------------------------------
        m.setPwd(bcrypt.encode(m.getPwd())); // 👈 주입받은 'bcrypt' 객체를 그대로 사용
        
        System.out.println("==============================================");
        System.out.println("★ [회원가입] 화면에서 서버로 넘어온 원본 비밀번호: [" + m.getPwd() + "]");
        System.out.println("==============================================");
        // -------------------------------
        // DB 저장
        // -------------------------------
        int result = mService.insertMember(m);
        

        if (result > 0) {

            // -------------------------------
            // 가입 완료 후 세션 정리
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
    // 로그인 처리
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
    	// 세션에서 로그인된 회원 정보 가져오기
        Member loginUser = (Member) session.getAttribute("loginUser");
        
        // 혹시나 세션이 끊긴 상태로 접근했다면 로그인 페이지로 튕겨내기
        if (loginUser == null) {
            return "redirect:/member/login";
        }
        
        // edit.html이 데이터를 꺼내 쓸 수 있도록 모델에 담아서 배달하기
        model.addAttribute("loginUser", loginUser);
        
       return "views/member/edit";
   
    
        
        
        
    }
 // =========================================================
    // 회원정보 수정 최종 처리
    // =========================================================
    @ResponseBody
    @PostMapping("/update")
    public String updateMember(
            @ModelAttribute Member m,            
            @RequestParam(name="currentPassword", required = false) String currentPassword, 
            @RequestParam(name="newPassword", required = false) String newPassword, 
            @RequestParam(name="authCode", required = false) String authCode,                     
            HttpSession session) {
        
        // 로그인 세션 체크
        Member loginUser = (Member) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "NOT_LOGGED_IN"; 
        }
        
        // [버그 해결 핵심]: 세션 대신 기존 로그인 로직(mService.login)을 재사용해서
        // DB에 들어있는 오염되지 않은 진짜 암호 원본을 실시간으로 새로 가져옵니다.
        Member dbUser = mService.selectOneMember(loginUser.getId()); 
        if (dbUser == null) {
            return "NOT_LOGGED_IN";
        }
        System.out.println("★ 입력된 현재비번: " + currentPassword);
        System.out.println("★ 입력된 새비번: " + newPassword);
        System.out.println("★ DB의 암호문: " + (dbUser != null ? dbUser.getPwd() : "null"));
        // ② 새로운 비밀번호를 입력하려 할 때 현재 비밀번호 검증 진행
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            // 💡 갓 퍼온 진짜 암호문(dbUser.getPwd())과 화면 입력값(currentPassword)을 실시간 대조
            if (!bcrypt.matches(currentPassword, dbUser.getPwd())) {
                return "WRONG_CURRENT_PASSWORD"; // 틀려도 세션이 오염되지 않고 여기서 즉시 안전하게 탈출
            }
        }
        
        // 세션의 안전한 ID를 모델에 강제 세팅
        m.setId(loginUser.getId()); 

        // 백엔드 최종 방어선: 세션에 저장된 인증 상태와 이메일 일치 여부 확인
        Boolean emailVerified = (Boolean) session.getAttribute("emailVerified");
        String verifiedEmail = (String) session.getAttribute("verifiedEmail");
        
        if (emailVerified == null || !emailVerified || verifiedEmail == null || !verifiedEmail.equals(m.getEmail())) {
            return "EMAIL_NOT_VERIFIED"; 
        }

        // DB 업데이트 실행 (비밀번호 변경 여부에 따른 분기)
        int result = 0;
        if (newPassword != null && !newPassword.trim().isEmpty()) {
        	m.setPwd(bcrypt.encode(newPassword)); // 변경할 새 비밀번호 암호화
            result = mService.updateMemberWithPassword(m);
        } else {
            result = mService.updateMemberWithoutPassword(m);
        }
        
        // 결과 처리 및 세션 정리
        if (result > 0) {
            session.removeAttribute("authCode");
            session.removeAttribute("targetEmail");
            session.removeAttribute("emailVerified");
            session.removeAttribute("verifiedEmail");
            
            // 데이터가 최종 성공했을 때만 세션 객체를 안전하게 동기화 갱신
            if (m.getNickName() != null) loginUser.setNickName(m.getNickName());
            if (m.getEmail() != null) loginUser.setEmail(m.getEmail());
            
            //  비밀번호도 정상 변경되었다면 새 암호문으로 세션을 완벽 동기화
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
        return "/views/member/withdraw"; 
    }

 // =========================================================
 // [버그 박멸] 회원 탈퇴 최종 처리 (POST)
 // =========================================================
 @PostMapping("/withdraw")
 public String doWithdraw(@RequestParam("password") String password, HttpSession session) {
     
     // 세션에서 현재 로그인한 유저 정보 확인
     Member loginUser = (Member) session.getAttribute("loginUser");
     if (loginUser == null) {
         return "redirect:/member/login"; // 로그인 안 되어 있으면 로그인창으로
     }

     //  [버그 해결의 핵심]: 세션 비밀번호는 변조/오염되었을 수 있으므로
     // DB에서 실시간으로 해당 회원의 원본 데이터를 새로 조회
     Member dbUser = mService.selectOneMember(loginUser.getId());
     if (dbUser == null) {
         return "redirect:/member/login";
     }

     //  실시간으로 갓 퍼온 진짜 암호문(dbUser.getPwd())과 화면 입력값(password)
     if (!bcrypt.matches(password, dbUser.getPwd())) {
         // ❌ 비밀번호가 틀리면 withdraw.html 화면에 에러 문구를 들고 리다이렉트
         return "redirect:/member/withdraw?error";
     }

     //비밀번호가 일치하면 서비스-매퍼를 타고 DB 상태 변경 혹은 삭제 수행
     // 보통 서비스단에서 mMapper.withdrawMember 또는 deleteMember를 호출
     int result = mService.withdrawMember(loginUser.getId()); 

     if (result > 0) {
         //  탈퇴가 최종 성공했다면 현재 세션을 완전히 파기(로그아웃) 처리
         session.invalidate(); 
         return "redirect:/"; // 메인 페이지로 안전하게 튕겨주기
     }

     // 알 수 없는 DB 오류 대비 안전빵 튕기기
     return "redirect:/member/withdraw?fail";
 }
 // 괄호 갯수 정돈 완료 (에러 해결)
// 아이디 찾기 페이지 열기
    @GetMapping("/find-id")
    public String findIdPage() {
    	return "views/member/find-id";
    }

// 비밀번호 찾기 페이지 열기
@GetMapping("/find-pwd")
public String findPwPage() {
    return "views/member/find-pwd";
}
//=========================================================
// 아이디 찾기 최종 처리 (Ajax)
// =========================================================
@ResponseBody
@PostMapping("/find-id")
public String findId(@RequestParam("email") String email, HttpSession session) {
    // 세션 검증 (인증 완료 여부 및 타겟 이메일 크로스 체크)
    Boolean emailVerified = (Boolean) session.getAttribute("emailVerified");
    String verifiedEmail = (String) session.getAttribute("verifiedEmail");
    
    if (emailVerified == null || !emailVerified || !email.equals(verifiedEmail)) {
        return "EMAIL_NOT_VERIFIED";
    }

    // 이메일로 회원 ID 조회 (서비스단 구현 필요)
    String foundId = mService.findIdByEmail(email); 
    
    if (foundId != null) {
        // 마스킹 처리 예시: travel1234 -> trav****
        if(foundId.length() > 4) {
            foundId = foundId.substring(0, 4) + "*".repeat(foundId.length() - 4);
        }
        session.removeAttribute("emailVerified"); // 세션 클린업
        session.removeAttribute("verifiedEmail");
        return foundId; // 찾은 마스킹 아이디를 통째로 반환
    }

    return "FAIL";
}

// =========================================================
// 비밀번호 찾기 (임시 비밀번호 생성 + BCrypt 암호화 + 메일 전송)
// =========================================================
@ResponseBody
@PostMapping("/find-pwd")
public String findPw(@RequestParam("id") String id, @RequestParam("email") String email, HttpSession session) {
    // 세션 검증
    Boolean emailVerified = (Boolean) session.getAttribute("emailVerified");
    String verifiedEmail = (String) session.getAttribute("verifiedEmail");
    
    if (emailVerified == null || !emailVerified || !email.equals(verifiedEmail)) {
        return "EMAIL_NOT_VERIFIED";
    }

    // 실제 가입된 회원 아이디와 이메일이 매칭되는지 체크하는 단선 방어
    Member m = mService.selectOneMember(id);
    if (m == null || !email.equals(m.getEmail())) {
        return "MEMBER_NOT_FOUND";
    }

    // 8자리 안전한 임시 비밀번호 랜덤 생성 (예시 규격)
    String rawTempPw = java.util.UUID.randomUUID().toString().substring(0, 8) + "!";
    
    // 오리지널 BCrypt 엔진으로 암호화 후 DB 저장
    String encryptedTempPw = bcrypt.encode(rawTempPw);
    m.setPwd(encryptedTempPw);
    
    int result = mService.updatePasswordOnly(m); // 비밀번호만 새로 바꾸는 서비스 호출

    if (result > 0) {
        // 원본 글자(rawTempPw)를 유저 메일로 발송 (emailService 연동)
        emailService.sendTempPassword(email, rawTempPw);
        
        session.removeAttribute("emailVerified"); // 세션 클린업
        session.removeAttribute("verifiedEmail");
        return "SUCCESS";
    }

    return "FAIL";
}
//=========================================================
// 계정 찾기 전용 인증번호 발송 (기존 회원 대상 우회로)
// =========================================================
@ResponseBody
@PostMapping("/find-account/send")
public String sendAuthCodeForFind(@RequestParam("email") String email, HttpSession session) {
    
    // 실제로 가입된 이메일이 맞는지 먼저 체크 (없으면 메일 안감)
    boolean isExist = mService.existsByEmail(email); 
    if (!isExist) {
        return "NOT_FOUND"; // 가입되지 않은 메일이면 프론트에 즉시 알림
    }

    // 가입된 회원이 맞으므로 6자리 랜덤 인증번호 생성 후 발송
    // (기존 회원가입 시 사용하던 emailService의 인증코드 생성 로직 그대로 재활용)
    String code = emailService.sendAuthCode(email);

    // 기존의 auth.js 및 검증 컨트롤러(/member/verify)가 그대로 읽을 수 있도록 세션 Key 바인딩
    session.setAttribute("authCode", code);
    session.setAttribute("targetEmail", email);
    session.setMaxInactiveInterval(5 * 60); // 안전하게 5분간 세션 유지

    return "SUCCESS";
}






}

