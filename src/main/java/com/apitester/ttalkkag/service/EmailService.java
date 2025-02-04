package com.apitester.ttalkkag.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 이메일 전송 서비스
 *
 * @author 정다영
 * @date 2025-02-01
 * @description 회원가입 인증 코드 및 프로젝트 초대 코드를 이메일로 전송하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * 회원가입 인증 코드 이메일 전송
     *
     * @param email 수신자 이메일 주소
     * @param code 인증 코드
     */
    public void sendVerificationCode(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("[딸깍] 회원가입 인증 코드입니다.");
        message.setText("인증 코드는 다음과 같습니다: " + code);
        message.setTo(email);
        mailSender.send(message);
    }

    /**
     * 프로젝트 초대 코드 이메일 전송
     *
     * @param email 수신자 이메일 주소
     * @param code 프로젝트 초대 코드
     */
    public void sendProjectInviteCode(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setSubject("[딸깍] 프로젝트 초대 코드");
        message.setText("프로젝트에 초대 받았습니다. 초대 코드는 다음과 같습니다: " + code);
        message.setTo(email);
        mailSender.send(message);
    }
}

