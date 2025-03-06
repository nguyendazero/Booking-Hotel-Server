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

import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final JwtUtils jwtUtils;

    @GetMapping("/admin/accounts")
    public ResponseEntity<APICustomize<List<AccountResponseDto>>> searchAccounts(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isBlocked,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "5") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        APICustomize<List<AccountResponseDto>> response = accountService.accounts(fullName, role, isBlocked, pageIndex, pageSize, sortBy, sortOrder);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PostMapping("/public/sign-up")
    public ResponseEntity<APICustomize<String>> signUp(@RequestBody @Valid SignUpRequest accountRequest) {
        APICustomize<String> response = accountService.signUp(accountRequest);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PostMapping("/public/verify-email")
    public ResponseEntity<APICustomize<String>> verifyEmail(@RequestParam String email, @RequestParam String code) {
        APICustomize<String> response = accountService.verifyEmail(email, code);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PostMapping("/public/sign-in")
    public ResponseEntity<APICustomize<SignInResponseDto>> signIn(@RequestBody SignInRequest request) {
        APICustomize<SignInResponseDto> response = accountService.signIn(request);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PostMapping("/public/resend-verification-code")
    public ResponseEntity<APICustomize<String>> resendVerificationCode(@RequestParam String email) {
        APICustomize<String> response = accountService.resendVerificationCode(email);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PostMapping("/public/refresh-token")
    public ResponseEntity<APICustomize<String>> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        APICustomize<String> response = jwtUtils.refreshAccessToken(refreshToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PostMapping("/public/forgot-password")
    public ResponseEntity<APICustomize<String>> forgotPassword(@RequestParam String emailOrUsername) {
        APICustomize<String> response = accountService.sendVerificationForPasswordReset(emailOrUsername);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PostMapping("/public/reset-password")
    public ResponseEntity<APICustomize<String>> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        APICustomize<String> response = accountService.resetPassword(request);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PostMapping("/user/change-password")
    public ResponseEntity<APICustomize<String>> changePassword(@RequestHeader("Authorization") String token,
                                               @RequestBody @Valid ChangePasswordRequest request) {
        String accessToken = token.substring(7);
        APICustomize<String> response = accountService.changePassword(request, accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @GetMapping("/admin/unblock-account/{id}")
    public ResponseEntity<APICustomize<String>> unBlockAccount(@PathVariable Long id) {
        APICustomize<String> response = accountService.UnBlockAccount(id);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PutMapping("/user/update-info")
    public ResponseEntity<APICustomize<AccountResponseDto>> updateAccountInfo(@RequestHeader("Authorization") String token,
                                               @ModelAttribute @Valid UpdateInfoRequest request) {
        String accessToken = token.substring(7);
        APICustomize<AccountResponseDto> response = accountService.updateAccountInfo(request, accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @GetMapping("/user/me")
    public ResponseEntity<APICustomize<AccountResponseDto>> getAccountByToken(@RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<AccountResponseDto> response = accountService.getAccountByToken(accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<APICustomize<String>> deleteAccountById(@PathVariable Long id) {
        APICustomize<String> response = accountService.deleteAccountById(id);
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
