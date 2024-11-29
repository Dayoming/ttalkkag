package com.apitester.ttalkkag.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendVerificationCode(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("[딸깍] 회원가입 인증 코드입니다.");
        message.setText("인증 코드는 다음과 같습니다: " + code);
        message.setTo(email);
        mailSender.send(message);
    }
}

