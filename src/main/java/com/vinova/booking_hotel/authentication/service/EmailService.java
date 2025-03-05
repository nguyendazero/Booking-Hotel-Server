package com.vinova.booking_hotel.authentication.service;

public interface EmailService {

    public void sendEmail(String to, String subject, String text);

    public void sendAccountVerificationEmail(String to, String verificationCode);

    public void sendAccountReactivationEmail(String to, String verificationCode);

    public void sendPasswordResetEmail(String to, String resetToken);
    
}
