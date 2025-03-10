package com.vinova.booking_hotel.common.scheduling;

import com.vinova.booking_hotel.authentication.service.impl.EmailServiceImpl;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.property.model.Booking;
import com.vinova.booking_hotel.property.repository.BookingRepository;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.property.service.PdfService;
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

    @Scheduled(cron = "0 */2 * * * ?") // Chạy mỗi 2 phút
    public void generateDailyBookingReport() {
        LocalDate today = LocalDate.now();
        LocalDateTime dateTime = today.atStartOfDay(); // Chuyển đổi LocalDate sang LocalDateTime

        // Lấy danh sách các booking được tạo vào ngày hôm nay
        List<Booking> bookings = bookingRepository.findByCreateDt(dateTime);

        // Tạo PDF báo cáo
        String pdfFilePath = pdfService.createBookingReport(bookings, today);

        // Tạo nội dung email
        StringBuilder emailContent = new StringBuilder();
        emailContent.append("<h2>Booking Report for ").append(today).append("</h2><br>")
                .append("Please find the attached booking report PDF for details.<br><br>")
                .append("<strong>Total Bookings Today: </strong>").append(bookings.size()).append("<br><br>")
                .append("Thank you!");

        // Gửi email cho tất cả các chủ khách sạn
        List<String> hotelOwnerEmails = getHotelOwnerEmails();
        for (String email : hotelOwnerEmails) {
            // Gửi email với nội dung và đính kèm PDF
            emailService.sendEmailWithAttachment(email, "Daily Booking Report", emailContent.toString(), pdfFilePath); // true để chỉ định nội dung là HTML
        }
    }

    private List<String> getHotelOwnerEmails() {
        List<Account> owners = accountRepository.findByAccountRoles_RoleName("ROLE_OWNER");
        return owners.stream()
                .map(Account::getEmail)
                .collect(Collectors.toList());
    }
}