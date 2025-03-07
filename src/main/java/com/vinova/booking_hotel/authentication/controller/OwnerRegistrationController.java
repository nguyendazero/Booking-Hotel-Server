package com.vinova.booking_hotel.authentication.controller;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.authentication.dto.response.OwnerRegistrationDto;
import com.vinova.booking_hotel.authentication.service.OwnerRegistrationService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<APICustomize<String>> registration(@RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<String> response = ownerRegistrationService.registerOwner(accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @GetMapping("/admin/owner-registrations")
    public ResponseEntity<APICustomize<List<OwnerRegistrationDto>>> registrations() {
        APICustomize<List<OwnerRegistrationDto>> response = ownerRegistrationService.ownerRegistrations();
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PutMapping("/admin/owner-registration/accept-registration/{id}")
    public ResponseEntity<APICustomize<Void>> acceptRegistration(@PathVariable Long id) {
        APICustomize<Void> response = ownerRegistrationService.acceptRegistration(id);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PutMapping("/admin/owner-registration/reject-registration/{id}")
    public ResponseEntity<APICustomize<Void>> rejectRegistration(@PathVariable Long id) {
        APICustomize<Void> response = ownerRegistrationService.rejectRegistration(id);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

}
