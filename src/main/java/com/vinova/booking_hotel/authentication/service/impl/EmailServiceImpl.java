package com.vinova.booking_hotel.authentication.service.impl;

import com.vinova.booking_hotel.authentication.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;


    @Override
    public void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            javaMailSender.send(message);
        } catch (MailException e) {
            throw new RuntimeException("Email sending failed. Please try again!", e);
        }
    }

    @Override
    public void sendAccountVerificationEmail(String to, String verificationCode) {
        String subject = "Welcome to Vinova! Please Verify Your Account";
        String text = String.format(
                "Dear User,\n\n" +
                        "Thank you for registering with Vinova!\n\n" +
                        "To complete your registration, please verify your account by using the following verification code:\n\n" +
                        "    Verification Code: %s\n\n" +
                        "This code is valid for 60 seconds. If you did not request this, please ignore this email.\n\n" +
                        "Best Regards,\n" +
                        "The Vinova Team\n" +
                        "Date: %s\n" +
                        "Time: %s\n",
                verificationCode,
                LocalDate.now(),
                LocalTime.now().withNano(0)
        );
        sendEmail(to, subject, text);
    }

    @Override
    public void sendAccountReactivationEmail(String to, String verificationCode) {
        String subject = "Account Reactivation Request - Vinova";
        String text = String.format(
                "Dear User,\n\n" +
                        "We noticed that your account was previously blocked. If you would like to reactivate your account, please use the following verification code:\n\n" +
                        "    Verification Code: %s\n\n" +
                        "This code is valid for 60 seconds. If you did not request this, please ignore this email.\n\n" +
                        "Thank you for being a valued member of the Vinova community!\n\n" +
                        "Best Regards,\n" +
                        "The Vinova Team\n" +
                        "Date: %s\n" +
                        "Time: %s\n",
                verificationCode,
                LocalDate.now(),
                LocalTime.now().withNano(0)
        );
        sendEmail(to, subject, text);
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        String subject = "Password Reset Request - Vinova";
        String text = String.format(
                "Dear User,\n\n" +
                        "We received a request to reset your password for your Vinova account.\n\n" +
                        "To proceed with the password reset, please click on the link below:\n\n" +
                        "    Verification Code: %s\n\n" +
                        "If you did not request this, please ignore this email. Your account will remain secure.\n\n" +
                        "Best Regards,\n" +
                        "The Vinova Team\n" +
                        "Date: %s\n" +
                        "Time: %s\n",
                resetToken,
                LocalDate.now(),
                LocalTime.now().withNano(0)
        );
        sendEmail(to, subject, text);
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);

        // Đính kèm tệp
        FileSystemResource file = new FileSystemResource(new File(attachmentPath));
        helper.addAttachment(Objects.requireNonNull(file.getFilename()), file);

        // Gửi email
        javaMailSender.send(message);
    }
    
}
