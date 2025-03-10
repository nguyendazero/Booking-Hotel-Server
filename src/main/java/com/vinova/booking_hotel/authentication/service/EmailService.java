package com.vinova.booking_hotel.authentication.service;

import jakarta.mail.MessagingException;

public interface EmailService {

    void sendEmail(String to, String subject, String text);

    void sendAccountVerificationEmail(String to, String verificationCode);

    void sendAccountReactivationEmail(String to, String verificationCode);

    void sendPasswordResetEmail(String to, String resetToken);

    void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath) throws MessagingException;
    
}
