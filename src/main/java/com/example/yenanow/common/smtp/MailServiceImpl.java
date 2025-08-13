package com.example.yenanow.common.smtp;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender javaMailSender;

    @Override
    public void sendEmail(String toEmail, String title, String text, boolean isHtml) {
        try {
            if (isHtml) {
                // HTML 이메일 전송
                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true); // true: HTML 사용
                helper.setTo(toEmail);
                helper.setSubject(title);
                helper.setText(text, true); // true: HTML 콘텐츠임을 명시
                javaMailSender.send(message);
            } else {
                // 기존의 일반 텍스트 이메일 전송
                SimpleMailMessage emailForm = createEmailForm(toEmail, title, text);
                javaMailSender.send(emailForm);
            }
        } catch (jakarta.mail.MessagingException e) {
            log.warn("HTML 메일 전송 실패: to={}, title={}", toEmail, title, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        } catch (RuntimeException e) {
            log.warn("일반 메일 전송 실패: to={}, title={}", toEmail, title, e);
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
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
