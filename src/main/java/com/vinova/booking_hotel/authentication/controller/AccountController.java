package com.vinova.booking_hotel.authentication.controller;

import com.vinova.booking_hotel.authentication.dto.request.SignInRequest;
import com.vinova.booking_hotel.authentication.dto.request.SignUpRequest;
import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.authentication.dto.response.AccountResponse;
import com.vinova.booking_hotel.authentication.dto.response.SignInResponse;
import com.vinova.booking_hotel.authentication.security.AccessTokenResponse;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.authentication.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final JwtUtils jwtUtils;

    @PostMapping("/public/sign-up")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest accountRequest) {
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
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        String newAccessToken = jwtUtils.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(new AccessTokenResponse(newAccessToken));
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
