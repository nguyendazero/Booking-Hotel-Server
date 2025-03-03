package com.vinova.booking_hotel.authentication.controller;

import com.vinova.booking_hotel.authentication.dto.request.*;
import com.vinova.booking_hotel.authentication.dto.response.*;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.authentication.service.AccountService;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final JwtUtils jwtUtils;

    @PostMapping("/public/sign-up")
    public ResponseEntity<?> signUp(@RequestBody @Valid SignUpRequest accountRequest) {
        APICustomize<String> response = accountService.signUp(accountRequest);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PostMapping("/public/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String email, @RequestParam String code) {
        APICustomize<AccountResponse> response = accountService.verifyEmail(email, code);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PostMapping("/public/sign-in")
    public ResponseEntity<?> signIn(@RequestBody SignInRequest request) {
        APICustomize<SignInResponse> response = accountService.signIn(request);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PostMapping("/public/refresh-token")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        APICustomize<String> response = jwtUtils.refreshAccessToken(refreshToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PostMapping("/public/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String emailOrUsername) {
        APICustomize<String> response = accountService.sendVerificationForPasswordReset(emailOrUsername);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PostMapping("/public/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        APICustomize<String> response = accountService.resetPassword(request);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @GetMapping("/admin/unblock-account/{id}")
    public ResponseEntity<?> unBlockAccount(@PathVariable Long id) {
        APICustomize<String> response = accountService.UnBlockAccount(id);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @GetMapping("/admin/hello")
    public ResponseEntity<?> admin(){
        return ResponseEntity.ok("Hello Admin");
    }

    @GetMapping("/owner/hello")
    public ResponseEntity<?> owner(){
        return ResponseEntity.ok("Hello Owner");
    }

    @GetMapping("/user/hello")
    public ResponseEntity<?> user(){
        return ResponseEntity.ok("Hello User");
    }

    @GetMapping("/public/hello")
    public ResponseEntity<?> publicApi(){
        return ResponseEntity.ok("Hello public");
    }
    
}
