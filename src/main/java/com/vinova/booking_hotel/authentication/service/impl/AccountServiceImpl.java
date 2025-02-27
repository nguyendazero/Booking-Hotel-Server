package com.vinova.booking_hotel.authentication.service.impl;

import com.vinova.booking_hotel.authentication.dto.request.SignInRequest;
import com.vinova.booking_hotel.authentication.dto.request.SignUpRequest;
import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.authentication.dto.response.AccountResponse;
import com.vinova.booking_hotel.authentication.dto.response.SignInResponse;
import com.vinova.booking_hotel.authentication.dto.response.VerificationInfo;
import com.vinova.booking_hotel.authentication.enums.ApiError;
import com.vinova.booking_hotel.authentication.exception.*;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.authentication.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    
    private final AccountRepository accountRepository;
    //Security
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    private final JavaMailSender javaMailSender;
    private final Map<String, VerificationInfo> verificationMap = new HashMap<>();
    
    @Override
    public APICustomize<SignInResponse> signIn(SignInRequest request) {
        // Tìm kiếm tài khoản bằng username hoặc email
        Account account = accountRepository.findByUsername(request.getUsernameOrEmail())
                .orElseGet(() -> accountRepository.findByEmail(request.getUsernameOrEmail())
                        .orElseThrow(ErrorSignInException::new));

        // Kiểm tra xem tài khoản có bị block không
        if (!account.isEnabled()) {
            throw new AccountIsBlockException();
        }

        // Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), account.getPassword())) {
            throw new ErrorSignInException();
        }

        // Xác thực tài khoản
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(account.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Lấy danh sách roles từ authorities
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Tạo JWT token mới
        String jwtToken = jwtUtils.generateTokenFromUserDetails(userDetails);

        // Kiểm tra refresh token
        String currentRefreshToken = account.getRefreshToken();
        LocalDateTime refreshExpiresAt = account.getRefreshExpiresAt();

        // Kiểm tra nếu refresh token là null hoặc đã hết hạn
        if (currentRefreshToken == null || (refreshExpiresAt != null && LocalDateTime.now().isAfter(refreshExpiresAt))) {
            // Tạo refresh token mới
            String refreshToken = jwtUtils.generateRefreshTokenFromUserDetails(userDetails);
            account.setRefreshToken(refreshToken);
            // Cập nhật thời gian hết hạn cho refresh token mới
            account.setRefreshExpiresAt(LocalDateTime.now().plusDays(30)); // 30 ngày
        }

        accountRepository.save(account); // Cập nhật tài khoản

        // Tạo response với đầy đủ các trường cần thiết
        SignInResponse response = new SignInResponse(
                account.getId(),
                account.getUsername(),
                account.getFullName(),
                account.getEmail(),
                roles,
                jwtToken,
                account.getRefreshToken()
        );

        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), response);
    }

    @Override
    public APICustomize<String> signUp(SignUpRequest request) {
        // Kiểm tra xem email đã tồn tại chưa
        if (accountRepository.existsByEmail(request.getEmail())) {
            throw new ResourceAlreadyExistsException("Account", "email", request.getEmail());
        }

        // Kiểm tra xem username đã tồn tại chưa
        if (accountRepository.existsByUsername(request.getUsername())) {
            throw new ResourceAlreadyExistsException("Account", "username", request.getUsername());
        }

        // Kiểm tra xem password và rePassword có khớp nhau không
        if (!request.getPassword().equals(request.getRePassword())) {
            throw new NotMatchPasswordException();
        }

        // Tạo mã xác thực
        String verificationCode = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime sentTime = LocalDateTime.now();

        // Lưu thông tin tạm thời
        verificationMap.put(request.getEmail(), new VerificationInfo(verificationCode, sentTime, request.getUsername(), request.getFullName()));

        // Gửi email xác thực
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(request.getEmail());
            message.setSubject("Verify account");
            message.setText("Your verification code is: " + verificationCode + "\nThe verification code is valid for 60 seconds.");
            javaMailSender.send(message);
        } catch (MailException e) {
            throw new RuntimeException("Email verification failed. Please try again!", e);
        }

        // Tạo tài khoản mới
        Account newAccount = new Account();
        newAccount.setUsername(request.getUsername());

        // Mã hóa mật khẩu trước khi lưu
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        newAccount.setPassword(encodedPassword);

        newAccount.setFullName(request.getFullName());
        newAccount.setEmail(request.getEmail());
        newAccount.setEnabled(false);
        newAccount.setRole("ROLE_USER");
        newAccount.setCreatedAt(LocalDateTime.now());
        newAccount.setUpdatedAt(LocalDateTime.now());

        // Lưu tài khoản
        Account savedAccount = accountRepository.save(newAccount);
        accountRepository.save(savedAccount);

        // Tạo đối tượng UserResponse
        AccountResponse accountResponse = new AccountResponse(
                savedAccount.getId(),
                savedAccount.getFullName(),
                savedAccount.getUsername(),
                savedAccount.getEmail(),
                savedAccount.getAvatar(),
                savedAccount.getRole(),
                savedAccount.isEnabled(),
                savedAccount.getCreatedAt(),
                savedAccount.getUpdatedAt()
        );

        return new APICustomize<>(ApiError.CREATED.getCode(), ApiError.CREATED.getMessage(), "Verification code sent. Please verify to activate account.");
    }

    @Override
    public APICustomize<AccountResponse> verifyEmail(String email, String code) {
        VerificationInfo verificationInfo = verificationMap.get(email);
        if (verificationInfo == null ||
                !verificationInfo.getVerificationCode().equals(code) ||
                Duration.between(verificationInfo.getSentTime(), LocalDateTime.now()).getSeconds() > 60) {
            throw new InValidVerifyEmailException();
        }

        // Cập nhật tài khoản đã tạo trước đó
        Account existingAccount = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "email", email));

        // Kích hoạt tài khoản
        existingAccount.setEnabled(true);
        existingAccount.setUpdatedAt(LocalDateTime.now());

        // Lưu tài khoản
        accountRepository.save(existingAccount);

        // Tạo đối tượng UserResponse
        AccountResponse accountResponse = new AccountResponse(
                existingAccount.getId(),
                existingAccount.getFullName(),
                existingAccount.getUsername(),
                existingAccount.getEmail(),
                existingAccount.getAvatar(),
                existingAccount.getRole(),
                existingAccount.isEnabled(),
                existingAccount.getCreatedAt(),
                existingAccount.getUpdatedAt()
        );

        // Xóa thông tin xác thực sau khi xác thực thành công
        verificationMap.remove(email);
        return new APICustomize<>(ApiError.CREATED.getCode(), ApiError.CREATED.getMessage(), accountResponse);
    }
}
