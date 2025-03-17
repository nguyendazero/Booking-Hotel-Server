package com.vinova.booking_hotel.authentication.controller;

import com.vinova.booking_hotel.authentication.dto.response.OwnerRegistrationDto;
import com.vinova.booking_hotel.authentication.service.OwnerRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class OwnerRegistrationController {
    
    private final OwnerRegistrationService ownerRegistrationService;

    @PostMapping("/user/owner-registration")
    public ResponseEntity<String> registration(@RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        String response = ownerRegistrationService.registerOwner(accessToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/admin/owner-registrations")
    public ResponseEntity<List<OwnerRegistrationDto>> registrations() {
        List<OwnerRegistrationDto> response = ownerRegistrationService.ownerRegistrations();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/admin/owner-registration/accept-registration/{id}")
    public ResponseEntity<Void> acceptRegistration(@PathVariable Long id) {
        ownerRegistrationService.acceptRegistration(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/admin/owner-registration/reject-registration/{id}")
    public ResponseEntity<Void> rejectRegistration(@PathVariable Long id) {
        ownerRegistrationService.rejectRegistration(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
