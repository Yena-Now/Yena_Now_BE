package com.example.yenanow.common.smtp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendEmail(String toEmail, String title, String text) {
        SimpleMailMessage emailForm = createEmailForm(toEmail, title, text);
        try {
            javaMailSender.send(emailForm);
        } catch (RuntimeException e) {
            log.warn("메일 전송 실패: to={}, title={}, text={}", toEmail, title, text, e);
            throw new RuntimeException("이메일 전송에 실패했습니다.");
        }
    }

    private SimpleMailMessage createEmailForm(String toEmail, String title, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(title);
        message.setText(text);
        return message;
    }
}
