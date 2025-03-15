package com.vinova.booking_hotel.authentication.controller;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.service.AccountService;
import com.vinova.booking_hotel.common.enums.ApiError;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.vinova.booking_hotel.authentication.dto.request.*;
import com.vinova.booking_hotel.authentication.dto.response.*;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import jakarta.validation.Valid;
import lombok.*;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AccountController {

    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String clientSecret;

    private final AccountService accountService;
    private final JwtUtils jwtUtils;
    private final RestTemplate restTemplate = new RestTemplate();

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
    public ResponseEntity<APICustomize<String>> forgotPassword(@RequestBody ForgotPasswordRequest requestDto) {
        APICustomize<String> response = accountService.sendVerificationForPasswordReset(requestDto.getEmailOrUsername());
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

    @GetMapping("/public/login/github")
    public APICustomize<String> loginWithGithub() {
        String redirectUrl = "https://github.com/login/oauth/authorize?client_id=Ov23linKwYC60PzEtp9w&scope=read:user";
        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), redirectUrl);
    }

    @GetMapping("/public/login/oauth2/code/github")
    public APICustomize<Account> oauth2Callback(@RequestParam("code") String code) {
        // Bước 1: Đổi mã xác thực lấy access token
        String tokenUri = "https://github.com/login/oauth/access_token";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("code", code);

        // Gửi yêu cầu lấy access token
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                tokenUri, HttpMethod.POST, new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<>() {}
        );
        String accessToken = (String) Objects.requireNonNull(response.getBody()).get("access_token");

        // Bước 2: Lấy thông tin người dùng
        String userInfoUri = "https://api.github.com/user";
        headers.setBearerAuth(accessToken);
        ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                userInfoUri, HttpMethod.GET, new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
        );

        // Chuyển đổi sang OAuth2User và tạo tài khoản mới
        OAuth2User oAuth2User = new DefaultOAuth2User(
                Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
                Objects.requireNonNull(userResponse.getBody()),
                "login"
        );

        Account newAccount = accountService.createAccount(oAuth2User);
        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), newAccount);
    }
    
}
