package com.vinova.booking_hotel.service;

import com.vinova.booking_hotel.authentication.service.impl.EmailServiceImpl;
import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private EmailServiceImpl emailService;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> simpleMailMessageCaptor;

    @Captor
    private ArgumentCaptor<MimeMessage> mimeMessageCaptor;

    @Test
    void sendEmail_shouldSendSimpleMailMessage_whenCalled() {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test Body";

        // Act
        emailService.sendEmail(to, subject, text);

        // Assert
        verify(javaMailSender, times(1)).send(simpleMailMessageCaptor.capture());
        SimpleMailMessage sentMessage = simpleMailMessageCaptor.getValue();
        assertEquals(to, Objects.requireNonNull(sentMessage.getTo())[0]);
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(text, Objects.requireNonNull(sentMessage.getText()));
    }

    @Test
    void sendEmail_shouldThrowRuntimeException_whenMailExceptionOccurs() {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test Body";
        doThrow(new MailSendException("Sending failed")).when(javaMailSender).send(any(SimpleMailMessage.class));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> emailService.sendEmail(to, subject, text));
        assertEquals("Email sending failed. Please try again!", exception.getMessage());
    }

    @Test
    void sendAccountVerificationEmail_shouldSendEmailWithCorrectContent() {
        // Arrange
        String to = "user@example.com";
        String verificationCode = "123456";
        LocalDate now = LocalDate.now();
        LocalTime nowWithNoNanos = LocalTime.now().withNano(0);

        // Act
        emailService.sendAccountVerificationEmail(to, verificationCode);

        // Assert
        verify(javaMailSender, times(1)).send(simpleMailMessageCaptor.capture());
        SimpleMailMessage sentMessage = simpleMailMessageCaptor.getValue();
        assertEquals(to, Objects.requireNonNull(sentMessage.getTo())[0]);
        assertEquals("Welcome to Vinova! Please Verify Your Account", sentMessage.getSubject());
        String expectedText = String.format(
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
                now,
                nowWithNoNanos
        );
        assertEquals(expectedText, Objects.requireNonNull(sentMessage.getText()));
    }

    @Test
    void sendAccountReactivationEmail_shouldSendEmailWithCorrectContent() {
        // Arrange
        String to = "blocked@example.com";
        String verificationCode = "ABCDEF";
        LocalDate now = LocalDate.now();
        LocalTime nowWithNoNanos = LocalTime.now().withNano(0);

        // Act
        emailService.sendAccountReactivationEmail(to, verificationCode);

        // Assert
        verify(javaMailSender, times(1)).send(simpleMailMessageCaptor.capture());
        SimpleMailMessage sentMessage = simpleMailMessageCaptor.getValue();
        assertEquals(to, Objects.requireNonNull(sentMessage.getTo())[0]);
        assertEquals("Account Reactivation Request - Vinova", sentMessage.getSubject());
        String expectedText = String.format(
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
                now,
                nowWithNoNanos
        );
        assertEquals(expectedText, Objects.requireNonNull(sentMessage.getText()));
    }

    @Test
    void sendPasswordResetEmail_shouldSendEmailWithCorrectContent() {
        // Arrange
        String to = "reset@example.com";
        String resetToken = "GHIJKL";
        LocalDate now = LocalDate.now();
        LocalTime nowWithNoNanos = LocalTime.now().withNano(0);

        // Act
        emailService.sendPasswordResetEmail(to, resetToken);

        // Assert
        verify(javaMailSender, times(1)).send(simpleMailMessageCaptor.capture());
        SimpleMailMessage sentMessage = simpleMailMessageCaptor.getValue();
        assertEquals(to, Objects.requireNonNull(sentMessage.getTo())[0]);
        assertEquals("Password Reset Request - Vinova", sentMessage.getSubject());
        String expectedText = String.format(
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
                now,
                nowWithNoNanos
        );
        assertEquals(expectedText, Objects.requireNonNull(sentMessage.getText()));
    }

    @Test
    void sendEmailWithAttachment_shouldSendMimeMessageWithAttachment_whenCalled() throws MessagingException, IOException {
        // Arrange
        String to = "attachment@example.com";
        String subject = "Attachment Subject";
        String body = "<p>Attachment Body</p>";
        String attachmentPath = "test_attachment.txt";

        // Mock tạo MimeMessage
        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mockMimeMessage);

        // Mock getRecipients()
        Address[] toAddresses = {new InternetAddress(to)};
        when(mockMimeMessage.getRecipients(jakarta.mail.Message.RecipientType.TO)).thenReturn(toAddresses);
        when(mockMimeMessage.getSubject()).thenReturn(subject);
        when(mockMimeMessage.getContent()).thenReturn(body);

        // Act
        emailService.sendEmailWithAttachment(to, subject, body, attachmentPath);

        // Assert
        verify(javaMailSender, times(1)).send(mimeMessageCaptor.capture());
        MimeMessage sentMessage = mimeMessageCaptor.getValue();

        assertNotNull(sentMessage.getRecipients(jakarta.mail.Message.RecipientType.TO));
        assertEquals(to, ((InternetAddress) sentMessage.getRecipients(jakarta.mail.Message.RecipientType.TO)[0]).getAddress());
        assertEquals(subject, sentMessage.getSubject());
        assertTrue(sentMessage.getContent().toString().contains(body));
    }

    @Test
    void sendEmailWithAttachment_shouldThrowMailPreparationException_whenCreateMimeMessageFails() throws MessagingException {
        // Arrange
        String to = "attachment@example.com";
        String subject = "Attachment Subject";
        String body = "<p>Attachment Body</p>";
        String attachmentPath = "test_attachment.txt";

        // Mock việc tạo MimeMessage ném ra MailPreparationException
        when(javaMailSender.createMimeMessage()).thenThrow(new MailPreparationException("Failed to create MimeMessage"));

        // Act & Assert
        assertThrows(MailPreparationException.class, () -> emailService.sendEmailWithAttachment(to, subject, body, attachmentPath));
        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    @Test
    void sendEmailWithAttachment_shouldThrowMailSendException_whenSendFails() throws MessagingException, IOException {
        // Arrange
        String to = "attachment@example.com";
        String subject = "Attachment Subject";
        String body = "<p>Attachment Body</p>";
        String attachmentPath = "test_attachment.txt";
        MimeMessage mockMimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mockMimeMessage);
        doThrow(new MailSendException("Sending failed")).when(javaMailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThrows(MailSendException.class, () -> emailService.sendEmailWithAttachment(to, subject, body, attachmentPath));
    }
}