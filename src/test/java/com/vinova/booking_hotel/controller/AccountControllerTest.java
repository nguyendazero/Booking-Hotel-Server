package com.vinova.booking_hotel.controller;

import com.vinova.booking_hotel.authentication.controller.AccountController;
import com.vinova.booking_hotel.authentication.dto.request.*;
import com.vinova.booking_hotel.authentication.dto.response.*;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.authentication.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AccountController accountController;
    
    private final MockHttpServletResponse httpServletResponse = new MockHttpServletResponse();

    private static final String TEST_TOKEN = "Bearer test_token";
    private static final Long TEST_ACCOUNT_ID = 1L;
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password";
    private static final String TEST_FULL_NAME = "Test User";
    private static final String TEST_ROLE_USER = "ROLE_USER";
    private static final String TEST_ACCESS_TOKEN = "test_token";
    private static final String TEST_REFRESH_TOKEN = "refreshToken";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void searchAccounts_shouldReturnOkAndListOfAccounts() {
        List<AccountResponseDto> mockAccounts = List.of(
                new AccountResponseDto(TEST_ACCOUNT_ID, TEST_FULL_NAME, TEST_USERNAME, TEST_EMAIL, null, null, null, List.of(TEST_ROLE_USER)),
                new AccountResponseDto(2L, "Full Name 2", "user2", "email2", null, null, null, List.of("ROLE_ADMIN"))
        );
        when(accountService.accounts(any(), any(), any(), anyInt(), anyInt(), any(), any())).thenReturn(mockAccounts);

        ResponseEntity<List<AccountResponseDto>> response = accountController.searchAccounts(null, null, null, 0, 8, "id", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockAccounts, response.getBody());
    }

    @Test
    void signUp_shouldReturnCreatedAndSuccessMessage() {
        SignUpRequestDto request = new SignUpRequestDto(TEST_FULL_NAME, TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD);
        when(accountService.signUp(any(SignUpRequestDto.class))).thenReturn("Account created successfully. Verification code sent.");

        ResponseEntity<String> response = accountController.signUp(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Account created successfully. Verification code sent.", response.getBody());
    }

    @Test
    void verifyEmail_shouldReturnOkAndSuccessMessage() {
        when(accountService.verifyEmail(TEST_EMAIL, "123456")).thenReturn("Account activated successfully.");

        ResponseEntity<String> response = accountController.verifyEmail(TEST_EMAIL, "123456");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Account activated successfully.", response.getBody());
    }

    @Test
    void signIn_shouldReturnOkAndSignInResponseDto() {
        SignInRequestDto request = new SignInRequestDto(TEST_USERNAME, TEST_PASSWORD);
        SignInResponseDto responseDto = new SignInResponseDto(TEST_ACCESS_TOKEN, TEST_REFRESH_TOKEN);
        when(accountService.signIn(any(SignInRequestDto.class), eq(httpServletResponse))).thenReturn(responseDto);

        ResponseEntity<SignInResponseDto> response = accountController.signIn(request, httpServletResponse);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void resendVerificationCode_shouldReturnOkAndSuccessMessage() {
        when(accountService.resendVerificationCode(TEST_EMAIL)).thenReturn("Verification code sent again.");

        ResponseEntity<String> response = accountController.resendVerificationCode(TEST_EMAIL);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Verification code sent again.", response.getBody());
    }

    @Test
    void refreshAccessToken_shouldReturnCreatedAndNewTokens() {
        Map<String, String> tokens = Map.of("accessToken", "newAccessToken", "refreshToken", "newRefreshToken");
        when(jwtUtils.refreshAccessToken(TEST_REFRESH_TOKEN)).thenReturn(tokens);

        ResponseEntity<Map<String, String>> response = accountController.refreshAccessToken(Map.of("refreshToken", TEST_REFRESH_TOKEN));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(tokens, response.getBody());
    }

    @Test
    void forgotPassword_shouldReturnOkAndSuccessMessage() {
        ForgotPasswordRequestDto request = new ForgotPasswordRequestDto();
        request.setEmailOrUsername(TEST_EMAIL);
        when(accountService.sendVerificationForPasswordReset(TEST_EMAIL)).thenReturn("Verification code sent to your email.");

        ResponseEntity<String> response = accountController.forgotPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Verification code sent to your email.", response.getBody());
    }

    @Test
    void resetPassword_shouldReturnOkAndSuccessMessage() {
        ResetPasswordRequestDto request = new ResetPasswordRequestDto(TEST_EMAIL, "123456", "newPassword", "newPassword");
        when(accountService.resetPassword(any(ResetPasswordRequestDto.class))).thenReturn("Password has been reset successfully.");

        ResponseEntity<String> response = accountController.resetPassword(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password has been reset successfully.", response.getBody());
    }

    @Test
    void changePassword_shouldReturnOkAndSuccessMessage() {
        ChangePasswordRequestDto request = new ChangePasswordRequestDto("oldPassword", "newPassword", "newPassword");
        when(accountService.changePassword(any(ChangePasswordRequestDto.class), eq(TEST_ACCESS_TOKEN))).thenReturn("Password has been changed successfully.");

        ResponseEntity<String> response = accountController.changePassword(TEST_TOKEN, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Password has been changed successfully.", response.getBody());
    }

    @Test
    void blockAccount_shouldReturnNoContent() {
        BlockAccountRequestDto request = new BlockAccountRequestDto("blocking reason");
        when(accountService.blockAccount(TEST_ACCOUNT_ID, request)).thenReturn("Account has been blocked successfully.");

        ResponseEntity<String> response = accountController.blockAccount(TEST_ACCOUNT_ID, request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void unBlockAccount_shouldReturnNoContent() {
        when(accountService.unBlockAccount(TEST_ACCOUNT_ID)).thenReturn("Account has been unblocked successfully.");

        ResponseEntity<String> response = accountController.unBlockAccount(TEST_ACCOUNT_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void updateAccountInfo_shouldReturnOkAndAccountResponseDto() {
        AccountResponseDto responseDto = new AccountResponseDto(TEST_ACCOUNT_ID, "New Name", TEST_USERNAME, TEST_EMAIL, "newAvatarUrl", "123456789", null, List.of(TEST_ROLE_USER));
        when(accountService.updateAccountInfo(any(UpdateInfoRequestDto.class), eq(TEST_ACCESS_TOKEN))).thenReturn(responseDto);

        MockMultipartFile avatarFile = new MockMultipartFile("avatar", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "some image".getBytes());
        UpdateInfoRequestDto request = new UpdateInfoRequestDto("New Name", "123456789", avatarFile);

        ResponseEntity<AccountResponseDto> response = accountController.updateAccountInfo(TEST_TOKEN, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void getAccountByToken_shouldReturnOkAndAccountResponseDto() {
        AccountResponseDto responseDto = new AccountResponseDto(TEST_ACCOUNT_ID, TEST_FULL_NAME, TEST_USERNAME, TEST_EMAIL, "oldAvatarUrl", "0987654321", null, List.of(TEST_ROLE_USER));
        when(accountService.getAccountByToken(eq(TEST_ACCESS_TOKEN))).thenReturn(responseDto);

        ResponseEntity<AccountResponseDto> response = accountController.getAccountByToken(TEST_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
    }

    @Test
    void deleteAccountById_shouldReturnNoContent() {
        doNothing().when(accountService).deleteAccountById(TEST_ACCOUNT_ID);

        ResponseEntity<Void> response = accountController.deleteAccountById(TEST_ACCOUNT_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(accountService, times(1)).deleteAccountById(TEST_ACCOUNT_ID);
    }

    // Các test case cho loginWithGithub, oauth2CallbackGithub, loginWithGoogle, oauth2CallbackGoogle có thể được thêm tương tự.
}