package com.vinova.booking_hotel.service;

import com.vinova.booking_hotel.authentication.dto.request.*;
import com.vinova.booking_hotel.authentication.dto.response.*;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.model.AccountRole;
import com.vinova.booking_hotel.authentication.model.Role;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.repository.AccountRoleRepository;
import com.vinova.booking_hotel.authentication.repository.RoleRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;

import com.vinova.booking_hotel.authentication.service.EmailService;
import com.vinova.booking_hotel.authentication.service.impl.AccountServiceImpl;
import com.vinova.booking_hotel.authentication.service.impl.CloudinaryService;
import com.vinova.booking_hotel.common.exception.*;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private AccountRoleRepository accountRoleRepository;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private HttpServletResponse httpServletResponse;

    @InjectMocks
    private AccountServiceImpl accountService;

    private final String TEST_TOKEN = "Bearer test_token";
    private final Long TEST_ACCOUNT_ID = 1L;
    private final String TEST_USERNAME = "testuser";
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PASSWORD = "password";
    private final String ENCODED_PASSWORD = "encodedPassword";
    private final String TEST_FULL_NAME = "Test User";
    private final String TEST_ROLE_USER = "ROLE_USER";

    @Test
    void accounts_shouldReturnListOfAccountResponseDto() {
        // Arrange
        List<Account> accounts = new ArrayList<>();

        // Khởi tạo accountRoles là ArrayList rỗng ở đúng vị trí (tham số thứ 17)
        Account account1 = new Account(1L, "user1", "pass1", "email1", "Full Name 1", null, null, null, LocalDateTime.now(), null, null, ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>(), null, null, new ArrayList<>(), null, null);
        Role userRole1 = new Role(1L, TEST_ROLE_USER, null, null, null);
        AccountRole accountRole1 = new AccountRole(1L, account1, userRole1, ZonedDateTime.now(), ZonedDateTime.now());
        account1.getAccountRoles().add(accountRole1);
        accounts.add(account1);

        // Khởi tạo accountRoles là ArrayList rỗng ở đúng vị trí (tham số thứ 17)
        Account account2 = new Account(2L, "user2", "pass2", "email2", "Full Name 2", null, null, null, LocalDateTime.now(), null, null, ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>(), null, null, new ArrayList<>(), null, null);
        Role userRole2 = new Role(2L, TEST_ROLE_USER, null, null, null);
        AccountRole accountRole2 = new AccountRole(2L, account2, userRole2, ZonedDateTime.now(), ZonedDateTime.now());
        account2.getAccountRoles().add(accountRole2);
        accounts.add(account2);

        Page<Account> accountPage = new PageImpl<>(accounts);
        when(accountRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(accountPage);

        // Act
        List<AccountResponseDto> response = accountService.accounts(null, null, null, 0, 10, null, null);

        // Assert
        assertEquals(2, response.size());
        assertEquals("Full Name 1", response.getFirst().getFullName());
        assertEquals(TEST_ROLE_USER, response.getFirst().getRoles().getFirst());
    }

    @Test
    void signIn_shouldReturnSignInResponseDto_whenCredentialsAreValid() {
        // Arrange
        SignInRequestDto request = new SignInRequestDto(TEST_USERNAME, TEST_PASSWORD);
        Account mockAccount = new Account(TEST_ACCOUNT_ID, TEST_USERNAME, ENCODED_PASSWORD, TEST_EMAIL, TEST_FULL_NAME, null, null, null, LocalDateTime.now(), "testRefreshToken", LocalDateTime.now().plusDays(30), ZonedDateTime.now(), ZonedDateTime.now(), List.of(), null, null, null, null, null); // Đặt sẵn refreshToken đã mock vào mockAccount
        UserDetails userDetails = new User(TEST_USERNAME, ENCODED_PASSWORD, new ArrayList<>());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, ENCODED_PASSWORD, new ArrayList<>());
        String jwtToken = "testJwtToken";
        String refreshToken = "testRefreshToken"; // Sử dụng biến này để so sánh

        when(accountRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(mockAccount));
        when(passwordEncoder.matches(TEST_PASSWORD, ENCODED_PASSWORD)).thenReturn(true);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.generateTokenFromUserDetails(userDetails)).thenReturn(jwtToken);
        when(jwtUtils.generateRefreshTokenFromUserDetails(userDetails)).thenReturn(refreshToken); // Mock trả về giá trị kỳ vọng
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);

        // Act
        SignInResponseDto response = accountService.signIn(request, httpServletResponse);

        // Assert
        assertEquals(jwtToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void signIn_shouldThrowErrorSignInException_whenUsernameOrEmailNotFound() {
        // Arrange
        SignInRequestDto request = new SignInRequestDto(TEST_USERNAME, TEST_PASSWORD);
        when(accountRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());
        when(accountRepository.findByEmail(TEST_USERNAME)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ErrorSignInException.class, () -> accountService.signIn(request, httpServletResponse));
        verify(authenticationManager, never()).authenticate(any());
        verify(jwtUtils, never()).generateTokenFromUserDetails(any());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void signUp_shouldCreateAccountAndSendVerificationEmail() {
        // Arrange
        SignUpRequestDto request = new SignUpRequestDto(TEST_FULL_NAME, TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD);
        Account mockAccount = new Account(null, TEST_USERNAME, ENCODED_PASSWORD, TEST_EMAIL, TEST_FULL_NAME, "unverified_account", null, null, null, null, null, ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>(), null, null, new ArrayList<>(), null, null);
        Role userRole = new Role(1L, TEST_ROLE_USER, null, null, new ArrayList<>());
        when(accountRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());
        when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);
        when(roleRepository.findByName(TEST_ROLE_USER)).thenReturn(Optional.of(userRole));
        when(accountRoleRepository.save(any(AccountRole.class))).thenReturn(new AccountRole());

        // Act
        String response = accountService.signUp(request);

        // Assert
        assertEquals("Account created successfully. Verification code sent. Please verify to activate your account.", response);
        verify(accountRepository, times(1)).save(any(Account.class));
        verify(roleRepository, times(1)).findByName(TEST_ROLE_USER);
        verify(accountRoleRepository, times(1)).save(any(AccountRole.class));
        verify(emailService, times(1)).sendAccountVerificationEmail(eq(TEST_EMAIL), anyString());
    }

    @Test
    void signUp_shouldThrowNotMatchPasswordException_whenPasswordsDoNotMatch() {
        // Arrange
        SignUpRequestDto request = new SignUpRequestDto(TEST_USERNAME, TEST_PASSWORD, "differentPassword", TEST_FULL_NAME, TEST_EMAIL);

        // Act & Assert
        assertThrows(NotMatchPasswordException.class, () -> accountService.signUp(request));
        verify(accountRepository, never()).save(any(Account.class));
        verify(roleRepository, never()).findByName(anyString());
        verify(accountRoleRepository, never()).save(any(AccountRole.class));
        verify(emailService, never()).sendAccountVerificationEmail(anyString(), anyString());
    }

    @Test
    void signUp_shouldThrowResourceAlreadyExistsException_whenUsernameExistsAndIsActive() {
        // Arrange
        SignUpRequestDto request = new SignUpRequestDto(TEST_FULL_NAME, TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD);
        Account existingAccount = new Account();
        existingAccount.setBlockReason(null);
        when(accountRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(existingAccount));

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> accountService.signUp(request));
        verify(accountRepository, never()).save(any(Account.class));
        verify(roleRepository, never()).findByName(anyString());
        verify(accountRoleRepository, never()).save(any(AccountRole.class));
        verify(emailService, never()).sendAccountVerificationEmail(anyString(), anyString());
    }

    @Test
    void signUp_shouldResendVerificationEmail_whenEmailExistsButAccountIsNotActive() {
        // Arrange
        SignUpRequestDto request = new SignUpRequestDto(TEST_FULL_NAME, TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD);
        Account existingAccount = new Account();
        existingAccount.setEmail(TEST_EMAIL); // Đảm bảo existingAccount có email
        existingAccount.setBlockReason("unverified_account");
        when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingAccount));

        // Act
        String response = accountService.signUp(request);

        // Assert
        assertEquals("Account exists but not activated. Verification code sent again. Please verify to activate your account.", response);
        verify(emailService, times(1)).sendAccountReactivationEmail(eq(TEST_EMAIL), anyString());
        verify(accountRepository, never()).save(any());
        verify(roleRepository, never()).findByName(any());
        verify(accountRoleRepository, never()).save(any());
    }

    @Test
    void signUp_shouldThrowResourceAlreadyExistsException_whenEmailExistsAndIsActive() {
        // Arrange
        SignUpRequestDto request = new SignUpRequestDto(TEST_FULL_NAME, TEST_USERNAME, TEST_EMAIL, TEST_PASSWORD, TEST_PASSWORD);
        Account existingAccount = new Account();
        existingAccount.setBlockReason(null);
        when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(existingAccount));

        // Act & Assert
        assertThrows(ResourceAlreadyExistsException.class, () -> accountService.signUp(request));
        verify(accountRepository, never()).save(any(Account.class));
        verify(roleRepository, never()).findByName(anyString());
        verify(accountRoleRepository, never()).save(any(AccountRole.class));
        verify(emailService, never()).sendAccountVerificationEmail(anyString(), anyString());
    }

    @Test
    void verifyEmail_shouldActivateAccount_whenVerificationInfoIsValid() {
        // Arrange
        String verificationCode = "123456";
        Account mockAccount = new Account();
        mockAccount.setBlockReason("unverified_account");
        VerificationInfo verificationInfo = new VerificationInfo(verificationCode, LocalDateTime.now().minusMinutes(1), TEST_USERNAME, TEST_FULL_NAME);
        when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);

        // Mock verificationMap (cần truy cập trực tiếp vì là private)
        java.lang.reflect.Field verificationMapField = null;
        try {
            verificationMapField = AccountServiceImpl.class.getDeclaredField("verificationMap");
            verificationMapField.setAccessible(true);
            ((Map<String, VerificationInfo>) verificationMapField.get(accountService)).put(TEST_EMAIL, verificationInfo);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to access verificationMap: " + e.getMessage());
        }

        // Act
        String response = accountService.verifyEmail(TEST_EMAIL, verificationCode);

        // Assert
        assertEquals("Account activated successfully.", response);
        assertNull(mockAccount.getBlockReason());
        assertEquals(TEST_FULL_NAME, mockAccount.getFullName());
        assertEquals(TEST_USERNAME, mockAccount.getUsername());
        verify(accountRepository, times(1)).save(mockAccount);
        // Kiểm tra xem verificationInfo đã bị xóa
        try {
            assertNull(((Map<String, VerificationInfo>) verificationMapField.get(accountService)).get(TEST_EMAIL));
        } catch (IllegalAccessException e) {
            fail("Failed to access verificationMap: " + e.getMessage());
        }
    }

    @Test
    void verifyEmail_shouldThrowInValidVerifyEmailException_whenVerificationInfoIsInvalid() {
        // Arrange
        String verificationCode = "wrongCode";
        VerificationInfo verificationInfo = new VerificationInfo("123456", LocalDateTime.now().minusMinutes(1), TEST_USERNAME, TEST_FULL_NAME);

        // Mock verificationMap
        java.lang.reflect.Field verificationMapField = null;
        try {
            verificationMapField = AccountServiceImpl.class.getDeclaredField("verificationMap");
            verificationMapField.setAccessible(true);
            ((Map<String, VerificationInfo>) verificationMapField.get(accountService)).put(TEST_EMAIL, verificationInfo);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to access verificationMap: " + e.getMessage());
        }

        // Act & Assert
        assertThrows(InValidVerifyEmailException.class, () -> accountService.verifyEmail(TEST_EMAIL, verificationCode));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void sendVerificationForPasswordReset_shouldSendEmailWithVerificationCode() {
        // Arrange
        Account mockAccount = new Account();
        mockAccount.setEmail(TEST_EMAIL);
        when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockAccount));

        // Act
        String response = accountService.sendVerificationForPasswordReset(TEST_EMAIL);

        // Assert
        assertEquals("Verification code sent to your email. Please check your email to reset your password.", response);
        verify(emailService, times(1)).sendPasswordResetEmail(eq(TEST_EMAIL), anyString());
        // Kiểm tra xem verificationInfo đã được thêm vào map (khó kiểm tra giá trị chính xác)
        java.lang.reflect.Field verificationMapField = null;
        try {
            verificationMapField = AccountServiceImpl.class.getDeclaredField("verificationMap");
            verificationMapField.setAccessible(true);
            assertNotNull(((Map<String, VerificationInfo>) verificationMapField.get(accountService)).get(TEST_EMAIL));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to access verificationMap: " + e.getMessage());
        }
    }

    @Test
    void sendVerificationForPasswordReset_shouldThrowResourceNotFoundException_whenAccountNotFound() {
        // Arrange
        when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> accountService.sendVerificationForPasswordReset(TEST_EMAIL));
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void resetPassword_shouldResetPasswordAndRemoveVerificationInfo_whenCodeIsValid() {
        // Arrange
        ResetPasswordRequestDto request = new ResetPasswordRequestDto(TEST_EMAIL, "validCode", "newPassword", "newPassword");
        Account mockAccount = new Account();
        when(accountRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(mockAccount));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);
        VerificationInfo verificationInfo = new VerificationInfo("validCode", LocalDateTime.now().minusMinutes(1), TEST_USERNAME, TEST_FULL_NAME);

        // Mock verificationMap
        java.lang.reflect.Field verificationMapField = null;
        try {
            verificationMapField = AccountServiceImpl.class.getDeclaredField("verificationMap");
            verificationMapField.setAccessible(true);
            ((Map<String, VerificationInfo>) verificationMapField.get(accountService)).put(TEST_EMAIL, verificationInfo);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to access verificationMap: " + e.getMessage());
        }

        // Act
        String response = accountService.resetPassword(request);

        // Assert
        assertEquals("Password has been reset successfully.", response);
        verify(accountRepository, times(1)).save(mockAccount);
        assertEquals("encodedNewPassword", mockAccount.getPassword());
        // Kiểm tra xem verificationInfo đã bị xóa
        try {
            assertNull(((Map<String, VerificationInfo>) verificationMapField.get(accountService)).get(TEST_EMAIL));
        } catch (IllegalAccessException e) {
            fail("Failed to access verificationMap: " + e.getMessage());
        }
    }

    @Test
    void resetPassword_shouldThrowInValidVerifyEmailException_whenCodeIsInvalid() {
        // Arrange
        ResetPasswordRequestDto request = new ResetPasswordRequestDto(TEST_EMAIL, "invalidCode", "newPassword", "newPassword");
        VerificationInfo verificationInfo = new VerificationInfo("validCode", LocalDateTime.now().minusMinutes(1), TEST_USERNAME, TEST_FULL_NAME);

        // Mock verificationMap
        java.lang.reflect.Field verificationMapField = null;
        try {
            verificationMapField = AccountServiceImpl.class.getDeclaredField("verificationMap");
            verificationMapField.setAccessible(true);
            ((Map<String, VerificationInfo>) verificationMapField.get(accountService)).put(TEST_EMAIL, verificationInfo);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to access verificationMap: " + e.getMessage());
        }

        // Act & Assert
        assertThrows(InValidVerifyEmailException.class, () -> accountService.resetPassword(request));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void changePassword_shouldChangePassword_whenOldPasswordIsCorrect() {
        // Arrange
        ChangePasswordRequestDto request = new ChangePasswordRequestDto("oldPassword", "newPassword", "newPassword");
        Account mockAccount = new Account(TEST_ACCOUNT_ID, TEST_USERNAME, ENCODED_PASSWORD, TEST_EMAIL, TEST_FULL_NAME, null, null, null, LocalDateTime.now(), null, null, ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>(), null, null, null, null, null);
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));
        when(passwordEncoder.matches("oldPassword", ENCODED_PASSWORD)).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);

        // Act
        String response = accountService.changePassword(request, TEST_TOKEN);

        // Assert
        assertEquals("Password has been changed successfully.", response);
        assertEquals("encodedNewPassword", mockAccount.getPassword());
        verify(accountRepository, times(1)).save(mockAccount);
    }

    @Test
    void changePassword_shouldThrowOldPasswordNotMatch_whenOldPasswordIsIncorrect() {
        // Arrange
        ChangePasswordRequestDto request = new ChangePasswordRequestDto("wrongPassword", "newPassword", "newPassword");
        Account mockAccount = new Account(TEST_ACCOUNT_ID, TEST_USERNAME, ENCODED_PASSWORD, TEST_EMAIL, TEST_FULL_NAME, null, null, null, LocalDateTime.now(), null, null, ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>(), null, null, null, null, null);
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));
        when(passwordEncoder.matches("wrongPassword", ENCODED_PASSWORD)).thenReturn(false);

        // Act & Assert
        assertThrows(OldPasswordNotMatch.class, () -> accountService.changePassword(request, TEST_TOKEN));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void changePassword_shouldThrowNotMatchPasswordException_whenNewPasswordsDoNotMatch() {
        // Arrange
        ChangePasswordRequestDto request = new ChangePasswordRequestDto("oldPassword", "newPassword", "differentPassword");
        Account mockAccount = new Account(TEST_ACCOUNT_ID, TEST_USERNAME, ENCODED_PASSWORD, TEST_EMAIL, TEST_FULL_NAME, null, null, null, LocalDateTime.now(), null, null, ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>(), null, null, null, null, null);
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));
        when(passwordEncoder.matches("oldPassword", ENCODED_PASSWORD)).thenReturn(true);

        // Act & Assert
        assertThrows(NotMatchPasswordException.class, () -> accountService.changePassword(request, TEST_TOKEN));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void blockAccount_shouldBlockAccountAndSetReason() {
        // Arrange
        BlockAccountRequestDto request = new BlockAccountRequestDto("test reason");
        Account mockAccount = new Account();
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);

        // Act
        String response = accountService.blockAccount(TEST_ACCOUNT_ID, request);

        // Assert
        assertEquals("Account has been blocked successfully.", response);
        assertEquals("test reason", mockAccount.getBlockReason());
        verify(accountRepository, times(1)).save(mockAccount);
    }

    @Test
    void unBlockAccount_shouldUnblockAccountAndSetReasonToNull() {
        // Arrange
        Account mockAccount = new Account();
        mockAccount.setBlockReason("some reason");
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);

        // Act
        String response = accountService.unBlockAccount(TEST_ACCOUNT_ID);

        // Assert
        assertEquals("Account has been unblocked successfully.", response);
        assertNull(mockAccount.getBlockReason());
        verify(accountRepository, times(1)).save(mockAccount);
    }

    @Test
    void updateAccountInfo_shouldUpdateAccountDetailsAndAvatar() {
        // Arrange
        // Tạo một mock MultipartFile
        MultipartFile mockAvatarFile = mock(MultipartFile.class);
        when(mockAvatarFile.isEmpty()).thenReturn(false);

        UpdateInfoRequestDto request = new UpdateInfoRequestDto("New Name", "123456789", mockAvatarFile); // Truyền mock MultipartFile
        Account mockAccount = new Account(TEST_ACCOUNT_ID, TEST_USERNAME, ENCODED_PASSWORD, TEST_EMAIL, TEST_FULL_NAME, null, "0987654321", "oldAvatarUrl", LocalDateTime.now(), null, null, ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>(), null, null, new ArrayList<>(), null, null);
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));

        when(cloudinaryService.uploadImage(mockAvatarFile)).thenReturn("newAvatarUrl");

        // Giả sử service set avatar sau khi upload thành công
        mockAccount.setAvatar("newAvatarUrl");
        when(accountRepository.save(any(Account.class))).thenReturn(mockAccount);

        // Act
        AccountResponseDto response = accountService.updateAccountInfo(request, TEST_TOKEN);

        // Assert
        assertEquals("New Name", response.getFullName());
        assertEquals("123456789", response.getPhone());
        assertEquals("newAvatarUrl", response.getAvatar());
        assertEquals(TEST_USERNAME, response.getUsername());
        assertEquals(TEST_EMAIL, response.getEmail());
        verify(accountRepository, times(1)).save(mockAccount);
        verify(cloudinaryService, times(1)).uploadImage(mockAvatarFile); // Verify với mock MultipartFile cụ thể
    }

    @Test
    void getAccountByToken_shouldReturnAccountResponseDto() {
        // Arrange
        Account mockAccount = new Account(TEST_ACCOUNT_ID, TEST_USERNAME, ENCODED_PASSWORD, TEST_EMAIL, TEST_FULL_NAME, null, "0987654321", "oldAvatarUrl", LocalDateTime.now(), null, null, ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>(), null, null, new ArrayList<>(), null, null);
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));

        // Act
        AccountResponseDto response = accountService.getAccountByToken(TEST_TOKEN);

        // Assert
        assertEquals(TEST_ACCOUNT_ID, response.getId());
        assertEquals(TEST_FULL_NAME, response.getFullName());
        assertEquals(TEST_USERNAME, response.getUsername());
        assertEquals(TEST_EMAIL, response.getEmail());
        assertEquals("oldAvatarUrl", response.getAvatar()); // Sửa lại assert cho avatar
        assertEquals("0987654321", response.getPhone());
    }

    @Test
    void deleteAccountById_shouldDeleteAccount() {
        // Arrange
        Account mockAccount = new Account();
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));
        doNothing().when(accountRepository).delete(mockAccount);

        // Act
        accountService.deleteAccountById(TEST_ACCOUNT_ID);

        // Assert
        verify(accountRepository, times(1)).findById(TEST_ACCOUNT_ID);
        verify(accountRepository, times(1)).delete(mockAccount);
    }

    @Test
    void blockInactiveAccounts_shouldBlockAccountsInactiveFor30Days() {
        // Arrange
        LocalDateTime threshold = LocalDateTime.now(ZoneId.of("Asia/Saigon")).minusDays(30);
        List<Account> inactiveAccounts = List.of(
                new Account(1L, "inactive1", "pass", "inactive1@example.com", "Inactive User 1", null, null, null, threshold.minusDays(1), null, null, ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>(), null, null, null, null, null),
                new Account(2L, "inactive2", "pass", "inactive2@example.com", "Inactive User 2", null, null, null, threshold.minusDays(30), null, null, ZonedDateTime.now(), ZonedDateTime.now(), new ArrayList<>(), null, null, null, null, null)
        );
        when(accountRepository.findByLatestLoginBefore(any(LocalDateTime.class))).thenReturn(inactiveAccounts);
        when(accountRepository.save(any(Account.class))).thenReturn(new Account());

        // Act
        accountService.blockInactiveAccounts();

        // Assert
        verify(accountRepository, times(2)).save(any(Account.class));
        for (Account account : inactiveAccounts) {
            assertEquals("long_time_no_login", account.getBlockReason());
        }
    }
}