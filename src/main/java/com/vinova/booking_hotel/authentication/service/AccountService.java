package com.vinova.booking_hotel.authentication.service;

import com.vinova.booking_hotel.authentication.dto.request.ResetPasswordRequest;
import com.vinova.booking_hotel.authentication.dto.request.SignInRequest;
import com.vinova.booking_hotel.authentication.dto.request.SignUpRequest;
import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.authentication.dto.response.AccountResponse;
import com.vinova.booking_hotel.authentication.dto.response.SignInResponse;

public interface AccountService {

    public APICustomize<SignInResponse> signIn(SignInRequest request);

    public APICustomize<String> signUp(SignUpRequest request);

    public APICustomize<AccountResponse> verifyEmail(String email, String code);

    public APICustomize<String> sendVerificationForPasswordReset(String emailOrUsername);

    public APICustomize<String> resetPassword(ResetPasswordRequest request);

    public void blockInactiveAccounts();
    
}
