package com.example.yenanow.auth.service;

public interface MailService {

    void sendEmail(String toEmail, String title, String text);
}
