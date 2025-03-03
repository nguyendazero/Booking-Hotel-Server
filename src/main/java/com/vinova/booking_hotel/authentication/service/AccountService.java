package com.vinova.booking_hotel.authentication.service;

import com.vinova.booking_hotel.authentication.dto.request.ResetPasswordRequest;
import com.vinova.booking_hotel.authentication.dto.request.SignInRequest;
import com.vinova.booking_hotel.authentication.dto.request.SignUpRequest;
import com.vinova.booking_hotel.authentication.dto.request.UpdateInfoRequest;
import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.authentication.dto.response.AccountResponseDto;
import com.vinova.booking_hotel.authentication.dto.response.SignInResponseDto;

public interface AccountService {

    APICustomize<SignInResponseDto> signIn(SignInRequest request);

    APICustomize<String> signUp(SignUpRequest request);

    APICustomize<AccountResponseDto> verifyEmail(String email, String code);

    APICustomize<String> sendVerificationForPasswordReset(String emailOrUsername);

    APICustomize<String> resetPassword(ResetPasswordRequest request);
    
    APICustomize<String> UnBlockAccount(Long id);

    APICustomize<AccountResponseDto> updateAccountInfo(UpdateInfoRequest request, String token);

    APICustomize<AccountResponseDto> getAccountByToken(String token);

    public void blockInactiveAccounts();
    
}
