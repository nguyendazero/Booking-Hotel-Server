package com.vinova.booking_hotel.payment.controller;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.vinova.booking_hotel.common.enums.BookingStatus;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.model.Booking;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import com.vinova.booking_hotel.property.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    @Value("${stripe.secretKey}")
    private String secretKey;

    private final BookingRepository bookingRepository;

    @SneakyThrows
    @GetMapping("/public/payment/success")
    public void success(@RequestParam("session_id") String sessionId, HttpServletResponse response) {
        Stripe.apiKey = secretKey;

        try {
            // Lấy thông tin session từ Stripe
            Session session = Session.retrieve(sessionId);

            // Lấy bookingId từ metadata đã lưu khi tạo session
            Long bookingId = Long.valueOf(session.getMetadata().get("bookingId"));
            Booking booking = bookingRepository.findById(bookingId)
                    .orElseThrow(() -> new ResourceNotFoundException("booking"));
            booking.setStatus(BookingStatus.CONFIRMED);

            bookingRepository.save(booking);

            // Thực hiện chuyển hướng đến trang thành công
            response.sendRedirect("http://localhost:5173/user/booking-success");
        } catch (Exception e) {
            response.sendRedirect("http://localhost:5173/error-page");
        }
    }

    @GetMapping("/public/payment/cancel")
    public ResponseEntity<String> fail() {
        return ResponseEntity.ok("Payment canceled! Please try again.");
    }

}
