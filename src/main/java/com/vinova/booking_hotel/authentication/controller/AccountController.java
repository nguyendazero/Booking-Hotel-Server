package com.vinova.booking_hotel.authentication.controller;

import com.vinova.booking_hotel.authentication.service.AccountService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vinova.booking_hotel.authentication.dto.request.*;
import com.vinova.booking_hotel.authentication.dto.response.*;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;


import java.util.List;
import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AccountController {

    @Value("${frontend.home.url}")
    private String frontendHomeUrl;

    // Github
    @Value("${spring.security.oauth2.client.provider.github.authorization-uri}")
    private String authorizationUri;

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String clientIdGithub;

    @Value("${spring.security.oauth2.client.registration.github.scope}")
    private String scope;

    // Google
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientIdGoogle;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;
    

    private final AccountService accountService;
    private final JwtUtils jwtUtils;
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/admin/accounts")
    public ResponseEntity<List<AccountResponseDto>> searchAccounts(
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Boolean isBlocked,
            @RequestParam(defaultValue = "0") int pageIndex,
            @RequestParam(defaultValue = "8") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        List<AccountResponseDto> response = accountService.accounts(fullName, role, isBlocked, pageIndex, pageSize, sortBy, sortOrder);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/sign-up")
    public ResponseEntity<String> signUp(@RequestBody @Valid SignUpRequestDto accountRequest) {
        String response = accountService.signUp(accountRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/public/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String email, @RequestParam String code) {
        String response = accountService.verifyEmail(email, code);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/public/sign-in")
    public ResponseEntity<SignInResponseDto> signIn(@RequestBody SignInRequestDto request, HttpServletResponse httpServletResponse) {
        SignInResponseDto response = accountService.signIn(request, httpServletResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/public/resend-verification-code")
    public ResponseEntity<String> resendVerificationCode(@RequestParam String email) {
        String response = accountService.resendVerificationCode(email);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/public/refresh-token")
    public ResponseEntity<Map<String, String>> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        Map<String, String> tokens = jwtUtils.refreshAccessToken(refreshToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(tokens);
    }

    @PostMapping("/public/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequestDto requestDto) {
        String response = accountService.sendVerificationForPasswordReset(requestDto.getEmailOrUsername());
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/public/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ResetPasswordRequestDto request) {
        String response = accountService.resetPassword(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/user/change-password")
    public ResponseEntity<String> changePassword(@RequestHeader("Authorization") String token,
                                               @RequestBody @Valid ChangePasswordRequestDto request) {
        String accessToken = token.substring(7);
        String response = accountService.changePassword(request, accessToken);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PutMapping("/admin/block-account/{id}")
    public ResponseEntity<String> blockAccount(@PathVariable Long id, @RequestBody BlockAccountRequestDto request) {
        String response = accountService.blockAccount(id, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @PutMapping("/admin/unblock-account/{id}")
    public ResponseEntity<String> unBlockAccount(@PathVariable Long id) {
        String response = accountService.unBlockAccount(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
    }

    @PutMapping("/user/update-info")
    public ResponseEntity<AccountResponseDto> updateAccountInfo(@RequestHeader("Authorization") String token,
                                               @ModelAttribute @Valid UpdateInfoRequestDto request) {
        String accessToken = token.substring(7);
        AccountResponseDto response = accountService.updateAccountInfo(request, accessToken);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/user/me")
    public ResponseEntity<AccountResponseDto> getAccountByToken(@RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        AccountResponseDto response = accountService.getAccountByToken(accessToken);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/admin/delete/{id}")
    public ResponseEntity<Void> deleteAccountById(@PathVariable Long id) {
        accountService.deleteAccountById(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/public/login/github")
    public RedirectView loginWithGithub() {
        String githubAuthUrl = authorizationUri + "?client_id=" + clientIdGithub + "&scope=" + scope;
        return new RedirectView(githubAuthUrl);
    }

    @PostMapping("/public/login/oauth2/code/github")
    public ResponseEntity<SignInResponseDto> oauth2CallbackGithub(@RequestParam("code") String code, HttpServletResponse httpServletResponse) {
        SignInResponseDto response = accountService.handleGithubOAuth(code, httpServletResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/public/login/google")
    public RedirectView loginWithGoogle() {
        String googleAuthUrl = String.format(
                "https://accounts.google.com/o/oauth2/auth?client_id=%s&scope=profile email&redirect_uri=%s&response_type=code",
                clientIdGoogle, redirectUri
        );
        return new RedirectView(googleAuthUrl);
    }

    @PostMapping("/public/login/oauth2/code/google")
    public ResponseEntity<SignInResponseDto> oauth2CallbackGoogle(@RequestParam("code") String code, HttpServletResponse httpServletResponse) {
        SignInResponseDto response = accountService.handleGoogleOAuth(code, httpServletResponse);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
}
