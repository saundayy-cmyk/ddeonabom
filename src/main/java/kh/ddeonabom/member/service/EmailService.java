package kh.ddeonabom.member.service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    // =========================
    // 인증번호 생성 + 메일 발송
    // =========================
    public String sendAuthCode(String email) {

        String code = generateCode();

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("이메일 인증번호");
        message.setText("인증번호: " + code);

        mailSender.send(message); // 👉 실제 Gmail SMTP 발송

        return code;
    }

    // =========================
    // 6자리 코드 생성
    // =========================
    private String generateCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
 // =========================================
    // 임시 비밀번호 메일 발송 (HTML 서식 버전)
    // =========================================
    public void sendTempPassword(String email, String rawTempPw) {
        // HTML 메일을 발송하기 위해 MimeMessage 생성
        jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
        
        try {
            // true 설정을 주어야 멀티파트(HTML, 인코딩) 설정이 활성화됩니다.
            org.springframework.mail.javamail.MimeMessageHelper helper = 
                new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(email);
            helper.setSubject("[떠나봄] 요청하신 임시 비밀번호가 발급되었습니다.");

            // 보기 편한 HTML 구조 본문 조립
            String htmlContent = "<div style='font-family: Arial, sans-serif; max-width: 500px; margin: 0 auto; padding: 20px; border: 1px solid #e5e7eb; border-radius: 8px;'>"
                    + "  <h2 style='color: #059669; font-size: 20px; margin-bottom: 16px;'>떠나봄 임시 비밀번호 발급</h2>"
                    + "  <p style='color: #4b5563; font-size: 14px; line-height: 1.6; margin-bottom: 20px;'>안녕하세요. 떠나봄입니다.<br>"
                    + "  회원님의 계정에 임시 비밀번호가 안전하게 발급되었습니다.</p>"
                    + "  <div style='background-color: #f3f4f6; border-radius: 6px; padding: 12px 20px; text-align: center; margin-bottom: 20px;'>"
                    + "    <span style='font-size: 18px; font-weight: bold; color: #111827; letter-spacing: 1px;'>" + rawTempPw + "</span>"
                    + "  </div>"
                    + "  <p style='color: #ef4444; font-size: 12px; margin-bottom: 0;'>⚠️ 로그인 후 보안을 위해 회원정보 수정 메뉴에서 비밀번호를 꼭 변경해 주세요.</p>"
                    + "</div>";

            // 두 번째 인자를 true로 주어야 HTML 태그가 정상 렌더링됩니다.
            helper.setText(htmlContent, true);

            mailSender.send(message); // 발송
            
        } catch (jakarta.mail.MessagingException e) {
            System.err.println("❌ 임시 비밀번호 메일 발송 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }
}