package com.vinova.booking_hotel.common.scheduling;

import com.vinova.booking_hotel.authentication.service.impl.EmailServiceImpl;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.property.model.Booking;
import com.vinova.booking_hotel.property.model.Hotel;
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

    // @Scheduled(cron = "0 0 22 * * ?") // Run every day at 22:00
    @Scheduled(cron = "0 */2 * * * ?") // Run every 2 minutes
    public void generateDailyBookingReport() throws MessagingException {
        LocalDate today = LocalDate.now();
        LocalDateTime dateTime = today.atStartOfDay(); // Convert LocalDate to LocalDateTime

        // Retrieve the list of bookings created today
        List<Booking> bookings = bookingRepository.findByCreateDt(dateTime);

        // Retrieve the list of hotel owners
        List<Account> owners = accountRepository.findByAccountRoles_RoleName("ROLE_OWNER");

        // Create PDF report and email content for each hotel owner
        for (Account owner : owners) {
            List<Hotel> ownerHotels = owner.getHotels(); // Get the list of hotels owned by the owner

            List<Booking> ownerBookings = bookings.stream()
                    .filter(booking -> ownerHotels.stream()
                            .anyMatch(hotel -> hotel.getId().equals(booking.getHotel().getId())))
                    .collect(Collectors.toList());

            // Create PDF report for the owner's bookings
            String pdfFilePath;
            if (ownerBookings.isEmpty()) {
                // Tạo PDF thông báo không có booking nào
                pdfFilePath = pdfService.createBookingReportNoBookings(owner, today);
            } else {
                // Tạo PDF cho các booking của chủ sở hữu
                pdfFilePath = pdfService.createBookingReport(ownerBookings, today);
            }

            // Create email content
            String emailContent = "<html>" +
                    "<head>" +
                    "<style>" +
                    "body { font-family: Arial, sans-serif; margin: 20px; }" +
                    "h2 { color: #4CAF50; }" +
                    "strong { color: #333; }" +
                    "ul { list-style-type: none; padding: 0; }" +
                    "li { margin: 5px 0; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<h2>Booking Report for " + today + "</h2>" +
                    "<p>Please find the attached PDF report for details.</p>" +
                    "<p><strong>Total bookings today: </strong>" + ownerBookings.size() + "</p>" +
                    "<p>Thank you!</p>" +
                    "</body>" +
                    "</html>";

            // Send email to the hotel owner
            emailService.sendEmailWithAttachment(owner.getEmail(), "Daily Booking Report", emailContent, pdfFilePath);
        }
    }
}