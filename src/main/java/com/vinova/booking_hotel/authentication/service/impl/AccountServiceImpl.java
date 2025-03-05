package com.vinova.booking_hotel.authentication.service.impl;

import com.vinova.booking_hotel.authentication.dto.request.*;
import com.vinova.booking_hotel.authentication.dto.response.*;
import com.vinova.booking_hotel.authentication.enums.ApiError;
import com.vinova.booking_hotel.authentication.exception.*;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.model.AccountRole;
import com.vinova.booking_hotel.authentication.model.Role;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.repository.AccountRoleRepository;
import com.vinova.booking_hotel.authentication.repository.RoleRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.authentication.service.AccountService;
import com.vinova.booking_hotel.authentication.specification.AccountSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final AccountRoleRepository accountRoleRepository;
    
    //Cloudinary
    private final CloudinaryService cloudinaryService;
    
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
    public APICustomize<List<AccountResponseDto>> accounts(String fullName, String role, Boolean isBlocked, int pageIndex, int pageSize, String sortBy, String sortOrder) {
        // Kiểm tra hợp lệ cho pageIndex và pageSize
        if (pageIndex < 0 || pageSize <= 0) {
            throw new InvalidPageOrSizeException();
        }

        // Tạo Specification với các tiêu chí tìm kiếm
        Specification<Account> spec = Specification
                .where(AccountSpecification.hasFullName(fullName))
                .and(AccountSpecification.isBlocked(isBlocked))
                .and(AccountSpecification.hasRole(role));

        // Xác định hướng sắp xếp
        Sort sort = Sort.by(Sort.Direction.ASC, "id");
        if (sortBy != null) {
            Sort.Direction direction = sortOrder != null && sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, sortBy);
        }

        // Sử dụng Pageable từ Spring Data
        Pageable pageable = PageRequest.of(pageIndex, pageSize, sort);

        // Tìm danh sách tài khoản với Specification và phân trang
        List<Account> accounts = accountRepository.findAll(spec, pageable).getContent();

        // Chuyển đổi danh sách tài khoản sang danh sách AccountResponseDto
        List<AccountResponseDto> accountResponses = new ArrayList<>();

        for (Account account : accounts) {
            // Truy xuất vai trò liên quan đến tài khoản
            List<String> roles = account.getAccountRoles().stream()
                    .map(accountRole -> accountRole.getRole().getName())
                    .collect(Collectors.toList());

            // Chuyển đổi Account thành AccountResponseDto
            AccountResponseDto accountResponse = new AccountResponseDto(
                    account.getId(),
                    account.getFullName(),
                    account.getUsername(),
                    account.getEmail(),
                    account.getAvatar(),
                    account.getPhone(),
                    roles
            );

            accountResponses.add(accountResponse);
        }

        // Trả về kết quả
        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), accountResponses);
    }

    @Override
    public APICustomize<SignInResponseDto> signIn(SignInRequest request) {
        String loginIdentifier = request.getUsernameOrEmail();

        // Tìm kiếm tài khoản bằng username hoặc email
        Account account = accountRepository.findByUsername(loginIdentifier)
                .orElseGet(() -> accountRepository.findByEmail(loginIdentifier)
                        .orElseThrow(ErrorSignInException::new));

        // Kiểm tra xem tài khoản có bị block không
        if (account.getBlockReason() != null) {
            throw new AccountIsBlockException(account.getBlockReason());
        }

        // Khai báo số lần đăng nhập không thành công
        int attempts;

        // Kiểm tra mật khẩu
        boolean passwordMatch = passwordEncoder.matches(request.getPassword(), account.getPassword());

        // Nếu mật khẩu không khớp, tăng số lần không thành công
        if (!passwordMatch) {
            // Sử dụng ID tài khoản làm khóa cho các lần thất bại
            String accountIdKey = String.valueOf(account.getId());
            failedAttempts.merge(accountIdKey, 1, Integer::sum);

            // Lấy số lần không thành công sau khi cập nhật
            attempts = failedAttempts.get(accountIdKey);

            // Kiểm tra nếu đã vượt quá số lần cho phép
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                account.setBlockReason("Too many failed login attempts, please register again to verify your account.");
                accountRepository.save(account);

                // Đặt lại số lần không thành công về 0
                failedAttempts.remove(accountIdKey);
            }

            throw new ErrorSignInException();
        }

        // Nếu đăng nhập thành công, xóa thông tin trong bộ nhớ
        String accountIdKey = String.valueOf(account.getId());
        failedAttempts.remove(accountIdKey);

        // Xác thực tài khoản
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(account.getUsername(), request.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

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
        SignInResponseDto response = new SignInResponseDto(
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
        if (existingAccountByUsername.isPresent() && existingAccountByUsername.get().getBlockReason() == null) {
            throw new ResourceAlreadyExistsException("Account", "username");
        }

        // Kiểm tra xem email đã tồn tại chưa
        Optional<Account> existingAccountByEmail = accountRepository.findByEmail(request.getEmail());
        if (existingAccountByEmail.isPresent()) {
            Account existingAccount = existingAccountByEmail.get();

            // Nếu tài khoản chưa được kích hoạt, gửi lại mã xác thực
            if (existingAccount.getBlockReason() != null) {
                sendVerificationEmail(existingAccount);
                return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), "Account exists but not activated. Verification code sent again. Please verify to activate your account.");
            } else {
                // Nếu tài khoản đã được kích hoạt, ném ra lỗi
                throw new ResourceAlreadyExistsException("Account", "email");
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
        newAccount.setBlockReason("unverified_account");
        newAccount.setCreateDt(ZonedDateTime.now());
        newAccount.setUpdateDt(ZonedDateTime.now());

        // Lưu tài khoản
        accountRepository.save(newAccount);

        // Tạo và lưu mối quan hệ AccountRole
        Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow(() -> new ResourceNotFoundException("Role", "name"));
        AccountRole accountRole = new AccountRole();
        accountRole.setAccount(newAccount);
        accountRole.setRole(userRole);
        accountRoleRepository.save(accountRole);

        // Gửi email xác thực cho tài khoản mới
        sendVerificationEmail(newAccount);

        return new APICustomize<>(ApiError.CREATED.getCode(), ApiError.CREATED.getMessage(), "Verification code sent. Please verify to activate account.");
    }

    @Override
    public APICustomize<AccountResponseDto> verifyEmail(String email, String code) {
        VerificationInfo verificationInfo = verificationMap.get(email);
        if (verificationInfo == null ||
                !verificationInfo.getVerificationCode().equals(code) ||
                Duration.between(verificationInfo.getSentTime(), LocalDateTime.now()).getSeconds() > 60) {
            throw new InValidVerifyEmailException();
        }

        // Cập nhật tài khoản đã tạo trước đó
        Account existingAccount = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "email"));

        // Kích hoạt tài khoản
        existingAccount.setBlockReason(null);
        existingAccount.setUpdateDt(ZonedDateTime.now());

        // Lưu tài khoản
        accountRepository.save(existingAccount);

        // Tạo danh sách vai trò từ AccountRole
        List<String> roles = existingAccount.getAccountRoles().stream()
                .map(accountRole -> accountRole.getRole().getName())
                .collect(Collectors.toList());

        // Tạo đối tượng AccountResponseDto
        AccountResponseDto accountResponse = new AccountResponseDto(
                existingAccount.getId(),
                existingAccount.getFullName(),
                existingAccount.getUsername(),
                existingAccount.getEmail(),
                existingAccount.getAvatar(),
                existingAccount.getPhone(),
                roles
        );

        // Xóa thông tin xác thực sau khi xác thực thành công
        verificationMap.remove(email);
        return new APICustomize<>(ApiError.CREATED.getCode(), ApiError.CREATED.getMessage(), accountResponse);
    }

    @Override
    public APICustomize<String> sendVerificationForPasswordReset(String emailOrUsername) {
        
        String email;
        if (emailOrUsername.contains("@gmail.com")) {
            email = emailOrUsername;
        } else {
            Optional<Account> accountOptional = accountRepository.findByUsername(emailOrUsername);
            if (accountOptional.isPresent()) {
                email = accountOptional.get().getEmail();
            } else {
                throw new ResourceNotFoundException("Account", "username");
            }
        }

        // Gửi email xác thực
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "email"));

        sendVerificationEmail(account);
        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), "Verification code sent to your email. Please verify to reset your password.");
    }

    @Override
    public APICustomize<String> resetPassword(ResetPasswordRequest request) {
        
        // Xác thực mã
        VerificationInfo verificationInfo = verificationMap.get(request.getEmail());
        if (verificationInfo == null ||
                !verificationInfo.getVerificationCode().equals(request.getCode()) ||
                Duration.between(verificationInfo.getSentTime(), LocalDateTime.now()).getSeconds() > 60) {
            throw new InValidVerifyEmailException();
        }

        // Kiểm tra password và rePassword
        if (!request.getNewPassword().equals(request.getRePassword())) {
            throw new NotMatchPasswordException();
        }

        // Cập nhật mật khẩu
        Account account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "email"));

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        account.setPassword(encodedPassword);
        accountRepository.save(account);

        // Xóa thông tin xác thực
        verificationMap.remove(request.getEmail());

        return new APICustomize<>(ApiError.NO_CONTENT.getCode(), ApiError.NO_CONTENT.getMessage(), "Password has been reset successfully.");
    }

    @Override
    public APICustomize<String> UnBlockAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id"));
        
        account.setBlockReason(null);
        accountRepository.save(account);

        return new APICustomize<>(ApiError.NO_CONTENT.getCode(), ApiError.NO_CONTENT.getMessage(), "Account has been unblocked successfully.");
    }

    @Override
    public APICustomize<AccountResponseDto> updateAccountInfo(UpdateInfoRequest request, String token) {
        // Lấy accountId bằng token
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id"));

        // Cập nhật fullName
        if (request.getFullName() != null) {
            account.setFullName(request.getFullName());
        }

        // Cập nhật phone
        if (request.getPhone() != null) {
            account.setPhone(request.getPhone());
        }

        // Cập nhật avatar
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            String avatarUrl = cloudinaryService.uploadImage(request.getAvatar());
            account.setAvatar(avatarUrl);
        }

        // Lưu tài khoản
        Account savedAccount = accountRepository.save(account);

        // Tạo danh sách vai trò từ AccountRole
        List<String> roles = savedAccount.getAccountRoles().stream()
                .map(accountRole -> accountRole.getRole().getName())
                .toList();

        // Tạo đối tượng AccountResponseDto để trả về
        AccountResponseDto responseDto = new AccountResponseDto(
                account.getId(),
                account.getFullName(),
                account.getUsername(),
                account.getEmail(),
                account.getAvatar(),
                account.getPhone(),
                roles
        );

        return new APICustomize<>(ApiError.NO_CONTENT.getCode(), ApiError.NO_CONTENT.getMessage(), responseDto);
    }

    @Override
    public APICustomize<AccountResponseDto> getAccountByToken(String token) {
        // Lấy userId bằng token
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);

        // Tìm tài khoản bằng userId
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id"));

        // Tạo danh sách vai trò từ AccountRole
        List<String> roles = account.getAccountRoles().stream()
                .map(accountRole -> accountRole.getRole().getName())
                .toList();
        
        // Tạo đối tượng AccountResponseDto để trả về
        AccountResponseDto responseDto = new AccountResponseDto(
                account.getId(),
                account.getFullName(),
                account.getUsername(),
                account.getEmail(),
                account.getAvatar(),
                account.getPhone(),
                roles
        );

        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), responseDto);
    }

    @Override
    public APICustomize<String> deleteAccountById(Long accountId) {
        // Tìm tài khoản bằng accountId
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id"));

        // Xóa tài khoản
        accountRepository.delete(account);
        
        return new APICustomize<>(ApiError.NO_CONTENT.getCode(), ApiError.NO_CONTENT.getMessage(), "");
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?") // Chạy mỗi ngày vào lúc 0:00
//    @Scheduled(cron = "*/30 * * * * ?") // Chạy mỗi 30 giây
    public void blockInactiveAccounts() {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(30);
        List<Account> inactiveAccounts = accountRepository.findByLatestLoginBefore(thresholdDate);

        for (Account account : inactiveAccounts) {
            account.setBlockReason("Long time no login");
            accountRepository.save(account);
        }
    }
}
