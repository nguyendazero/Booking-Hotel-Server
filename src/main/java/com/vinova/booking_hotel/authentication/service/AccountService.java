package com.vinova.booking_hotel.authentication.service;

import com.vinova.booking_hotel.authentication.dto.request.ResetPasswordRequest;
import com.vinova.booking_hotel.authentication.dto.request.SignInRequest;
import com.vinova.booking_hotel.authentication.dto.request.SignUpRequest;
import com.vinova.booking_hotel.authentication.dto.request.UpdateInfoRequest;
import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.authentication.dto.response.AccountResponseDto;
import com.vinova.booking_hotel.authentication.dto.response.SignInResponseDto;

public interface AccountService {

    public APICustomize<SignInResponseDto> signIn(SignInRequest request);

    public APICustomize<String> signUp(SignUpRequest request);

    public APICustomize<AccountResponseDto> verifyEmail(String email, String code);

    public APICustomize<String> sendVerificationForPasswordReset(String emailOrUsername);

    public APICustomize<String> resetPassword(ResetPasswordRequest request);
    
    public APICustomize<String> UnBlockAccount(Long id);

    APICustomize<AccountResponseDto> updateAccountInfo(UpdateInfoRequest request, Long accountId);

    public void blockInactiveAccounts();
    
}
