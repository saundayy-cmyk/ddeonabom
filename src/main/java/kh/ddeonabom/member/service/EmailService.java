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
}