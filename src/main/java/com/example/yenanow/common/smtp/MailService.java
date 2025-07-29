package com.example.yenanow.common.smtp;

public interface MailService {

    void sendEmail(String toEmail, String title, String text);
}
