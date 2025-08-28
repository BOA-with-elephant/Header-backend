package com.header.header.auth.model.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpSession;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
public class EmailService {
    private final JavaMailSender emailSender;
    private final HttpSession httpSession;
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender emailSender, HttpSession httpSession) {
        this.emailSender = emailSender;
        this.httpSession = httpSession;
    }

    private int SignupCodeGenerator() {
        Random rn = new Random();
        String randomNumber = "";
        for (int i = 0; i < 6; i++){
            randomNumber += Integer.toString(rn.nextInt(10));
        }
        return Integer.parseInt(randomNumber);
    }

    private MimeMessage CreateMail(String mail, int authNumber){
        MimeMessage message = emailSender.createMimeMessage();
        try
        {
            message.setRecipients(MimeMessage.RecipientType.TO, mail);
            message.setFrom("headercrmprogram@gmail.com");
            message.setSubject("Header CRM 이메일 인증"); // 이메일 제목
            String body = "";
            body += "<h3>Header CRM 가입 본인 인증 </h3>";
            body += "<h3>요청하신 인증 번호입니다.</h3>";
            body += "<h1>" + authNumber + "</h1>";
            body += "<h3>  감사합니다. </h3>";
            message.setText(body, "UTF-8", "html"); // 이메일 본문
            log.info("sent email: {}", "@90Company");
        }
        catch (MessagingException e){
            log.error("[EmailService.send()] error {}", e.getMessage());
        }

        return message;
    }
    @Transactional
    public int sendMail(String mail){
        int authNumber = SignupCodeGenerator();
        MimeMessage mimeMessage = CreateMail(mail, authNumber);
        emailSender.send(mimeMessage);
        return authNumber;
    }

    public boolean checkAuthNum(String email, String authNumber) {
        String sessionAuthNumber = (String) httpSession.getAttribute(email);
        System.out.println("Session Auth Number: " + sessionAuthNumber);
        System.out.println("User Input Auth Number: " + authNumber);

        // 세션에 인증번호가 없거나, 세션의 인증번호와 입력된 인증번호가 일치하지 않으면 false 반환
        if (sessionAuthNumber == null || !sessionAuthNumber.equals(authNumber)) {
            return false;
        }

        // 인증 성공 시 세션에서 해당 인증번호를 제거
        httpSession.removeAttribute(email);
        return true;
    }
}