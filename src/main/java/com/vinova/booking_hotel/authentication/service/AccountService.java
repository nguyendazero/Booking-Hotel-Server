package com.vinova.booking_hotel.authentication.service;

import com.vinova.booking_hotel.authentication.dto.request.*;
import com.vinova.booking_hotel.authentication.dto.response.AccountResponseDto;
import com.vinova.booking_hotel.authentication.dto.response.SignInResponseDto;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

public interface AccountService {

    List<AccountResponseDto> accounts(String fullName, String role, Boolean isBlocked, int pageIndex, int pageSize, String sortBy, String sortOrder);

    SignInResponseDto signIn(SignInRequest request, HttpServletResponse httpServletResponse);

    String signUp(SignUpRequest request);

    String resendVerificationCode(String email);

    String verifyEmail(String email, String code);

    String sendVerificationForPasswordReset(String emailOrUsername);

    String resetPassword(ResetPasswordRequest request);

    String changePassword(ChangePasswordRequest request, String token);

    String blockAccount(Long id, BlockAccountRequest request);
    
    String unBlockAccount(Long id);

    AccountResponseDto updateAccountInfo(UpdateInfoRequest request, String token);

    AccountResponseDto getAccountByToken(String token);

    void deleteAccountById(Long accountId);

    AccountResponseDto handleGithubOAuth(String code);

    AccountResponseDto handleGoogleOAuth(String code) ;

    void blockInactiveAccounts();
    
}
