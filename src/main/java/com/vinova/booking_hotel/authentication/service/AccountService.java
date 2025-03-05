package com.vinova.booking_hotel.authentication.service;

import com.vinova.booking_hotel.authentication.dto.request.*;
import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.authentication.dto.response.AccountResponseDto;
import com.vinova.booking_hotel.authentication.dto.response.SignInResponseDto;

import java.util.List;

public interface AccountService {

    APICustomize<List<AccountResponseDto>> accounts(String fullName, String role, Boolean isBlocked, int pageIndex, int pageSize, String sortBy, String sortOrder);

    APICustomize<SignInResponseDto> signIn(SignInRequest request);

    APICustomize<String> signUp(SignUpRequest request);

    APICustomize<String> resendVerificationCode(String email);

    APICustomize<AccountResponseDto> verifyEmail(String email, String code);

    APICustomize<String> sendVerificationForPasswordReset(String emailOrUsername);

    APICustomize<String> resetPassword(ResetPasswordRequest request);

    APICustomize<String> changePassword(ChangePasswordRequest request, String token);
    
    APICustomize<String> UnBlockAccount(Long id);

    APICustomize<AccountResponseDto> updateAccountInfo(UpdateInfoRequest request, String token);

    APICustomize<AccountResponseDto> getAccountByToken(String token);

    APICustomize<String> deleteAccountById(Long accountId);

    public void blockInactiveAccounts();
    
}
