package com.vinova.booking_hotel.authentication.service;

import com.vinova.booking_hotel.authentication.dto.request.*;
import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.authentication.dto.response.AccountResponseDto;
import com.vinova.booking_hotel.authentication.dto.response.SignInResponseDto;
import com.vinova.booking_hotel.authentication.model.Account;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;

public interface AccountService {

    APICustomize<List<AccountResponseDto>> accounts(String fullName, String role, Boolean isBlocked, int pageIndex, int pageSize, String sortBy, String sortOrder);

    APICustomize<SignInResponseDto> signIn(SignInRequest request);

    APICustomize<String> signUp(SignUpRequest request);

    APICustomize<String> resendVerificationCode(String email);

    APICustomize<String> verifyEmail(String email, String code);

    APICustomize<String> sendVerificationForPasswordReset(String emailOrUsername);

    APICustomize<String> resetPassword(ResetPasswordRequest request);

    APICustomize<String> changePassword(ChangePasswordRequest request, String token);
    
    APICustomize<String> UnBlockAccount(Long id);

    APICustomize<AccountResponseDto> updateAccountInfo(UpdateInfoRequest request, String token);

    APICustomize<AccountResponseDto> getAccountByToken(String token);

    APICustomize<String> deleteAccountById(Long accountId);

    Account createAccount(OAuth2User user);

    public void blockInactiveAccounts();
    
}
