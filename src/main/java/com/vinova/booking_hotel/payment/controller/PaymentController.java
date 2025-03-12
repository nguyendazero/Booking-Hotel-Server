package com.vinova.booking_hotel.payment.controller;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.vinova.booking_hotel.common.enums.BookingStatus;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.model.Booking;
import org.springframework.beans.factory.annotation.Value;
import com.vinova.booking_hotel.property.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PaymentController {

    @Value("${stripe.secretKey}")
    private String secretKey;

    private final BookingRepository bookingRepository;

    @GetMapping("/public/payment/success")
    public ResponseEntity<String> success(@RequestParam("session_id") String sessionId) {
        Stripe.apiKey = secretKey;

        try {
            // Lấy thông tin session từ Stripe
            Session session = Session.retrieve(sessionId);

            // Lấy bookingId từ metadata đã lưu nó khi tạo session
            Long bookingId = Long.valueOf(session.getMetadata().get("bookingId"));
            Booking booking = bookingRepository.findById(bookingId).orElseThrow(ResourceNotFoundException::new);
            booking.setStatus(BookingStatus.CONFIRMED);

            bookingRepository.save(booking);
            
            return ResponseEntity.ok("Payment successful! Booking confirmed.");
        }catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating booking: " + e.getMessage());
        }
    }

    @GetMapping("/public/payment/fail")
    public ResponseEntity<String> fail() {
        return ResponseEntity.ok("fail");
    }
    
}
