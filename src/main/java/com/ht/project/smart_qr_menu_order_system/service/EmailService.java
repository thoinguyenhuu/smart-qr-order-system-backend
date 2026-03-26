package com.ht.project.smart_qr_menu_order_system.service;

public interface EmailService {
    void sendEmail(String to, String subject, String content) ;
    void sendAccountVerificationEmail(String to, String verificationUrl);
}
