package com.vinova.booking_hotel.payment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {
    
    @GetMapping("/public/payment/success")
    public ResponseEntity<String> success() {
        return ResponseEntity.ok("success");
    }

    @GetMapping("/public/payment/fail")
    public ResponseEntity<String> fail() {
        return ResponseEntity.ok("fail");
    }
    
}
