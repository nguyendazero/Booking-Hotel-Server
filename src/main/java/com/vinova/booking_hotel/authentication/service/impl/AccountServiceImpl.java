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
import java.util.concurrent.ConcurrentHashMap;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    
    private final AccountRepository accountRepository;
    //Security
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    //Email
    private final JavaMailSender javaMailSender;
    private final Map<String, VerificationInfo> verificationMap = new HashMap<>();

    //Login
    private final ConcurrentHashMap<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private static final int MAX_FAILED_ATTEMPTS = 5;
    
    
    @Override
    public APICustomize<SignInResponse> signIn(SignInRequest request) {
        String usernameOrEmail = request.getUsernameOrEmail();

        // Tìm kiếm tài khoản bằng username hoặc email
        Account account = accountRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> accountRepository.findByEmail(usernameOrEmail)
                        .orElseThrow(ErrorSignInException::new));

        // Kiểm tra xem tài khoản có bị block không
        if (!account.isEnabled()) {
            throw new AccountIsBlockException(account.getBlockReason());
        }

        // Lấy số lần đăng nhập không thành công
        int attempts = failedAttempts.getOrDefault(usernameOrEmail, 0);

        // Kiểm tra mật khẩu
        boolean passwordMatch = passwordEncoder.matches(request.getPassword(), account.getPassword());

        // Nếu mật khẩu không khớp, tăng số lần không thành công
        if (!passwordMatch) {
            failedAttempts.merge(usernameOrEmail, 1, Integer::sum); // Tăng số lần không thành công lên 1

            // Kiểm tra nếu đã vượt quá số lần cho phép
            attempts++; // Cập nhật số lần không thành công
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                account.setEnabled(false);
                account.setBlockReason("Too many failed login attempts, please register again to verify your account.");
                accountRepository.save(account); // Cập nhật tài khoản
                failedAttempts.put(usernameOrEmail, attempts);
            }

            // Cập nhật lại số lần không thành công
            failedAttempts.put(usernameOrEmail, attempts);

            throw new ErrorSignInException();
        }

        // Nếu đăng nhập thành công, xóa thông tin trong bộ nhớ
        failedAttempts.remove(usernameOrEmail);

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

        // Cập nhật tài khoản
        account.setLatestLogin(LocalDateTime.now());
        accountRepository.save(account);

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

    private void sendVerificationEmail(Account account) {
        String verificationCode = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime sentTime = LocalDateTime.now();

        // Lưu thông tin tạm thời
        verificationMap.put(account.getEmail(), new VerificationInfo(verificationCode, sentTime, account.getUsername(), account.getFullName()));

        // Gửi email xác thực
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(account.getEmail());
            message.setSubject("Verify account");
            message.setText("Your verification code is: " + verificationCode + "\nThe verification code is valid for 60 seconds.");
            javaMailSender.send(message);
        } catch (MailException e) {
            throw new RuntimeException("Email verification failed. Please try again!", e);
        }
    }

    @Override
    public APICustomize<String> signUp(SignUpRequest request) {

        // Kiểm tra xem password và rePassword có khớp nhau không
        if (!request.getPassword().equals(request.getRePassword())) {
            throw new NotMatchPasswordException();
        }
        
        // Kiểm tra xem username đã tồn tại chưa
        Optional<Account> existingAccountByUsername = accountRepository.findByUsername(request.getUsername());
        if (existingAccountByUsername.isPresent() && existingAccountByUsername.get().isEnabled()) {
            throw new ResourceAlreadyExistsException("Account", "username", request.getUsername());
        }
        
        // Kiểm tra xem email đã tồn tại chưa
        Optional<Account> existingAccountByEmail = accountRepository.findByEmail(request.getEmail());

        // Nếu tài khoản đã tồn tại
        if (existingAccountByEmail.isPresent()) {
            Account existingAccount = existingAccountByEmail.get();

            // Nếu tài khoản chưa được kích hoạt, gửi lại mã xác thực
            if (!existingAccount.isEnabled()) {
                sendVerificationEmail(existingAccount);
                return new APICustomize<>(ApiError.OK.getCode(), "Account exists but not activated. Verification code sent again.", "Please verify to activate your account.");
            } else {
                // Nếu tài khoản đã được kích hoạt, ném ra lỗi
                throw new ResourceAlreadyExistsException("Account", "email", request.getEmail());
            }
        }

        // Tạo tài khoản mới
        Account newAccount = new Account();
        newAccount.setUsername(request.getUsername());

        // Mã hóa mật khẩu trước khi lưu
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        newAccount.setPassword(encodedPassword);

        newAccount.setFullName(request.getFullName());
        newAccount.setEmail(request.getEmail());
        newAccount.setEnabled(false); // Đặt enable là false
        newAccount.setBlockReason("unverified account");
        newAccount.setRole("ROLE_USER");
        newAccount.setCreatedAt(LocalDateTime.now());
        newAccount.setUpdatedAt(LocalDateTime.now());

        // Lưu tài khoản
        accountRepository.save(newAccount);

        // Gửi email xác thực cho tài khoản mới
        sendVerificationEmail(newAccount);

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
        existingAccount.setBlockReason(null);
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
