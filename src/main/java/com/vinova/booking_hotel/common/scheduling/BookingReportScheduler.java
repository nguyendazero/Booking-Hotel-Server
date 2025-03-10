package com.vinova.booking_hotel.common.scheduling;

import com.vinova.booking_hotel.authentication.service.impl.EmailServiceImpl;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.property.model.Booking;
import com.vinova.booking_hotel.property.repository.BookingRepository;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.property.service.PdfService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookingReportScheduler {

    private final BookingRepository bookingRepository;
    private final EmailServiceImpl emailService;
    private final PdfService pdfService;
    private final AccountRepository accountRepository;

//    @Scheduled(cron = "0 0 22 * * ?") // Chạy mỗi ngày lúc 22:00
    @Scheduled(cron = "0 */2 * * * ?") // Chạy mỗi 2 phút
    public void generateDailyBookingReport() throws MessagingException {
        LocalDate today = LocalDate.now();
        LocalDateTime dateTime = today.atStartOfDay(); // Chuyển đổi LocalDate sang LocalDateTime

        // Lấy danh sách các booking được tạo vào ngày hôm nay
        List<Booking> bookings = bookingRepository.findByCreateDt(dateTime);

        // Tạo PDF báo cáo
        String pdfFilePath = pdfService.createBookingReport(bookings, today);

        // Tạo nội dung email
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<html>")
                .append("<head>")
                .append("<style>")
                .append("body { font-family: Arial, sans-serif; margin: 20px; }")
                .append("h2 { color: #4CAF50; }")
                .append("strong { color: #333; }")
                .append("ul { list-style-type: none; padding: 0; }")
                .append("li { margin: 5px 0; }")
                .append("</style>")
                .append("</head>")
                .append("<body>")
                .append("<h2>Booking Report for ").append(today).append("</h2>")
                .append("<p>Please find the attached booking report PDF for details.</p>")
                .append("<p><strong>Total Bookings Today: </strong>").append(bookings.size()).append("</p>")
                .append("<p>Thank you!</p>")
                .append("</body>")
                .append("</html>");

        // Gửi email cho tất cả các chủ khách sạn
        List<String> hotelOwnerEmails = getHotelOwnerEmails();
        for (String email : hotelOwnerEmails) {
            emailService.sendEmailWithAttachment(email, "Daily Booking Report", emailContent.toString(), pdfFilePath);
        }
    }

    private List<String> getHotelOwnerEmails() {
        List<Account> owners = accountRepository.findByAccountRoles_RoleName("ROLE_OWNER");
        return owners.stream()
                .map(Account::getEmail)
                .collect(Collectors.toList());
    }
}