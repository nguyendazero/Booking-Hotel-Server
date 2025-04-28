package com.vinova.booking_hotel.authentication.service.impl;

import com.vinova.booking_hotel.authentication.dto.request.*;
import com.vinova.booking_hotel.authentication.dto.response.*;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.model.AccountRole;
import com.vinova.booking_hotel.authentication.model.Role;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.repository.AccountRoleRepository;
import com.vinova.booking_hotel.authentication.repository.RoleRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.authentication.service.AccountService;
import com.vinova.booking_hotel.authentication.service.EmailService;
import com.vinova.booking_hotel.authentication.repository.specification.AccountSpecification;
import com.vinova.booking_hotel.common.exception.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ConcurrentHashMap;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

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
    private final EmailService emailService;
    private final Map<String, VerificationInfo> verificationMap = new HashMap<>();

    //Login
    private final ConcurrentHashMap<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private static final int MAX_FAILED_ATTEMPTS = 5;

    private final RestTemplate restTemplate = new RestTemplate();

    // Google
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientIdGoogle;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecretGoogle;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.registration.google.token-uri}")
    private String tokenUriGoogle;

    @Value("${spring.security.oauth2.client.registration.google.user-info-uri}")
    private String userInfoUriGoogle;
    
    // Github
    @Value("${spring.security.oauth2.client.registration.github.client-id}")
    private String clientIdGithub;

    @Value("${spring.security.oauth2.client.registration.github.client-secret}")
    private String clientSecretGithub;

    @Value("${spring.security.oauth2.client.provider.github.authorization-uri}")
    private String authorizationUri;

    @Value("${spring.security.oauth2.client.registration.github.scope}")
    private String scope;

    @Value("${spring.security.oauth2.client.provider.github.token-uri}")
    private String tokenUriGithub;

    @Value("${spring.security.oauth2.client.provider.github.user-info-uri}")
    private String userInfoUriGithub;


    @Override
    public List<AccountResponseDto> accounts(String fullName, String role, Boolean isBlocked, int pageIndex, int pageSize, String sortBy, String sortOrder) {
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
                    account.getBlockReason(),
                    roles
            );

            accountResponses.add(accountResponse);
        }

        // Trả về kết quả
        return accountResponses;
    }

    @Override
    public SignInResponseDto signIn(SignInRequestDto request, HttpServletResponse httpServletResponse) {
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
            String accountIdKey = String.valueOf(account.getId());
            failedAttempts.merge(accountIdKey, 1, Integer::sum);
            attempts = failedAttempts.get(accountIdKey);

            // Kiểm tra nếu đã vượt quá số lần cho phép
            if (attempts >= MAX_FAILED_ATTEMPTS) {
                account.setBlockReason("too_many_failed_login_attempts");
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

        // Tạo refresh token mới
        String refreshToken = (currentRefreshToken == null || (refreshExpiresAt != null && LocalDateTime.now().isAfter(refreshExpiresAt)))
                ? jwtUtils.generateRefreshTokenFromUserDetails(userDetails)
                : currentRefreshToken;

        // Cập nhật refreshToken và thời gian hết hạn nếu cần
        if (currentRefreshToken == null || (refreshExpiresAt != null && LocalDateTime.now().isAfter(refreshExpiresAt))) {
            account.setRefreshToken(refreshToken);
            account.setRefreshExpiresAt(LocalDateTime.now().plusDays(30)); // 30 ngày
        }

        // Cập nhật tài khoản
        account.setLatestLogin(LocalDateTime.now());
        accountRepository.save(account);

        /// Thêm token vào cookie
        Cookie jwtCookie = new Cookie("token", jwtToken);
        jwtCookie.setHttpOnly(true); // Bảo vệ cookies khỏi JavaScript
        jwtCookie.setSecure(true); // Chỉ gửi cookies qua HTTPS
        jwtCookie.setPath("/"); // Áp dụng cookies trên toàn bộ domain
        jwtCookie.setMaxAge(60 * 60 * 24 * 7); // 7 ngày
        jwtCookie.setAttribute("SameSite", "None"); // Cho phép cross-domain cookies

        httpServletResponse.addCookie(jwtCookie);

        // Thêm refresh token vào cookie
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7);
        refreshTokenCookie.setAttribute("SameSite", "None");

        httpServletResponse.addCookie(refreshTokenCookie);

        // Tạo response với đầy đủ các trường cần thiết
        return new SignInResponseDto(
                jwtToken,
                refreshToken
        );
    }

    @Async
    @Override
    public String signUp(SignUpRequestDto request) {

        // Kiểm tra xem password và rePassword có khớp nhau không
        if (!request.getPassword().equals(request.getRePassword())) {
            throw new NotMatchPasswordException();
        }

        // Kiểm tra xem username đã tồn tại chưa
        Optional<Account> existingAccountByUsername = accountRepository.findByUsername(request.getUsername());
        if (existingAccountByUsername.isPresent() && existingAccountByUsername.get().getBlockReason() == null) {
            throw new ResourceAlreadyExistsException("username");
        }

        // Kiểm tra xem email đã tồn tại chưa
        Optional<Account> existingAccountByEmail = accountRepository.findByEmail(request.getEmail());
        if (existingAccountByEmail.isPresent()) {
            Account existingAccount = existingAccountByEmail.get();

            // Nếu tài khoản chưa được kích hoạt, gửi lại mã xác thực
            if (existingAccount.getBlockReason() != null) {
                String verificationCode = String.format("%06d", new Random().nextInt(999999));
                verificationMap.put(existingAccount.getEmail(), new VerificationInfo(verificationCode, LocalDateTime.now(), request.getUsername(), request.getFullName()));
                emailService.sendAccountReactivationEmail(existingAccount.getEmail(), verificationCode);
                return "Account exists but not activated. Verification code sent again. Please verify to activate your account.";
            } else {
                throw new ResourceAlreadyExistsException("email");
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
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("role"));
        AccountRole accountRole = new AccountRole();
        accountRole.setAccount(newAccount);
        accountRole.setRole(userRole);
        accountRoleRepository.save(accountRole);

        // Gửi email xác thực cho tài khoản mới
        String verificationCode = String.format("%06d", new Random().nextInt(999999));
        verificationMap.put(newAccount.getEmail(), new VerificationInfo(verificationCode, LocalDateTime.now(), newAccount.getUsername(), newAccount.getFullName()));
        emailService.sendAccountVerificationEmail(newAccount.getEmail(), verificationCode);

        return "Account created successfully. Verification code sent. Please verify to activate your account.";
    }

    @Async
    public String resendVerificationCode(String email) {
        // Kiểm tra xem tài khoản có tồn tại không
        Account existingAccount = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("account"));

        // Kiểm tra xem tài khoản đã được kích hoạt chưa
        if (existingAccount.getBlockReason() == null) {
            throw new ResourceAlreadyExistsException("Account already activated.");
        }

        // Kiểm tra xem có thông tin xác thực nào không
        VerificationInfo verificationInfo = verificationMap.get(email);
        if (verificationInfo != null) {
            // Kiểm tra thời gian gửi mã xác thực
            long secondsSinceSent = Duration.between(verificationInfo.getSentTime(), LocalDateTime.now()).getSeconds();
            if (secondsSinceSent < 60) {
                throw new CodeVerifySentException();
            }
        }

        // Tạo mã xác thực mới
        String verificationCode = String.format("%06d", new Random().nextInt(999999));
        assert verificationInfo != null;
        VerificationInfo newVerificationInfo = new VerificationInfo(verificationCode, LocalDateTime.now(), verificationInfo.getUsername(), verificationInfo.getFullName());
        verificationMap.put(existingAccount.getEmail(), newVerificationInfo);

        // Gửi mã xác thực mới
        emailService.sendAccountVerificationEmail(existingAccount.getEmail(), verificationCode);

        return "New verification code sent. Please check your email.";
    }
    
    @Override
    public String verifyEmail(String email, String code) {
        VerificationInfo verificationInfo = verificationMap.get(email);
        if (verificationInfo == null ||
                !verificationInfo.getVerificationCode().equals(code) ||
                Duration.between(verificationInfo.getSentTime(), LocalDateTime.now()).getSeconds() > 60) {
            throw new InValidVerifyEmailException();
        }

        // Cập nhật tài khoản đã tạo trước đó
        Account existingAccount = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("account"));

        // Kích hoạt tài khoản
        existingAccount.setBlockReason(null);
        existingAccount.setFullName(verificationInfo.getFullName());
        existingAccount.setUsername(verificationInfo.getUsername());

        // Lưu tài khoản
        accountRepository.save(existingAccount);

        // Xóa thông tin xác thực sau khi xác thực thành công
        verificationMap.remove(email);

        return "Account activated successfully.";
    }

    @Override
    public String sendVerificationForPasswordReset(String emailOrUsername) {
        String email;

        // Kiểm tra xem đầu vào có phải là email không
        if (emailOrUsername.contains("@")) {
            email = emailOrUsername;
        } else {
            // Tìm kiếm tài khoản bằng username
            Optional<Account> accountOptional = accountRepository.findByUsername(emailOrUsername);
            if (accountOptional.isPresent()) {
                email = accountOptional.get().getEmail();
            } else {
                throw new ResourceNotFoundException("account");
            }
        }

        // Gửi email xác thực
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("account"));
        
        if (account.getBlockReason() != null) {
            throw new AccountIsBlockException(account.getBlockReason());
        }

        // Tạo mã xác thực cho việc reset password
        String verificationCode = String.format("%06d", new Random().nextInt(999999));
        verificationMap.put(account.getEmail(), new VerificationInfo(verificationCode, LocalDateTime.now(), account.getUsername(), account.getFullName()));

        // Gửi email xác thực cho reset password
        emailService.sendPasswordResetEmail(account.getEmail(), verificationCode);

        return "Verification code sent to your email. Please check your email to reset your password.";
    }

    @Override
    public String resetPassword(ResetPasswordRequestDto request) {
        
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
                .orElseThrow(() -> new ResourceNotFoundException("account"));

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        account.setPassword(encodedPassword);
        accountRepository.save(account);

        // Xóa thông tin xác thực
        verificationMap.remove(request.getEmail());

        return "Password has been reset successfully.";
    }

    @Override
    public String changePassword(ChangePasswordRequestDto request, String token) {
        // Lấy accountId bằng token
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);

        // Tìm tài khoản bằng accountId
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("account"));

        // Kiem tra password cũ
        if (!passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
            throw new OldPasswordNotMatch();
        }
        
        //Kiem tra newPassword va rePassword
        if(!request.getNewPassword().equals(request.getRePassword())) {
            throw new NotMatchPasswordException();
        }
        
        // Cập nhật password
        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        account.setPassword(encodedPassword);
        accountRepository.save(account);

        return "Password has been changed successfully.";
    }

    @Override
    public String blockAccount(Long id, BlockAccountRequestDto request) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("account"));
        
        account.setBlockReason(request.getReason());
        accountRepository.save(account);

        return "Account has been blocked successfully.";
    }

    @Override
    public String unBlockAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("account"));
        
        account.setBlockReason(null);
        accountRepository.save(account);

        return "Account has been unblocked successfully.";
    }

    @Override
    public AccountResponseDto updateAccountInfo(UpdateInfoRequestDto request, String token) {
        // Lấy accountId bằng token
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("account"));

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

        return new AccountResponseDto(
                account.getId(),
                account.getFullName(),
                account.getUsername(),
                account.getEmail(),
                account.getAvatar(),
                account.getPhone(),
                account.getBlockReason(),
                roles
        );
    }

    @Override
    public AccountResponseDto getAccountByToken(String token) {
        // Lấy userId bằng token
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);

        // Tìm tài khoản bằng userId
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("account"));

        // Tạo danh sách vai trò từ AccountRole
        List<String> roles = account.getAccountRoles().stream()
                .map(accountRole -> accountRole.getRole().getName())
                .toList();
        
        // Tạo đối tượng AccountResponseDto để trả về

        return new AccountResponseDto(
                account.getId(),
                account.getFullName(),
                account.getUsername(),
                account.getEmail(),
                account.getAvatar(),
                account.getPhone(),
                account.getBlockReason(),
                roles
        );
    }

    @Override
    public void deleteAccountById(Long accountId) {
        // Tìm tài khoản bằng accountId
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("account"));

        // Xóa tài khoản
        accountRepository.delete(account);
    }

    @Override
    public AccountResponseDto handleGithubOAuth(String code, HttpServletResponse httpServletResponse) {
        // Bước 1: Đổi mã xác thực lấy access token (giữ nguyên)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientIdGithub);
        body.add("client_secret", clientSecretGithub);
        body.add("code", code);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                tokenUriGithub, HttpMethod.POST, new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<>() {}
        );

        String accessToken = (String) Objects.requireNonNull(response.getBody()).get("access_token");

        // Bước 2: Lấy thông tin người dùng (giữ nguyên)
        headers.setBearerAuth(accessToken);
        ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                userInfoUriGithub, HttpMethod.GET, new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
        );

        String githubEmail = Objects.requireNonNull(userResponse.getBody()).get("email").toString();
        String githubName = userResponse.getBody().get("name").toString();
        String githubAvatar = userResponse.getBody().get("avatar_url").toString();
        String githubUsername = userResponse.getBody().get("login").toString();

        Account account;

        // Kiểm tra xem tài khoản đã tồn tại dựa trên email
        Optional<Account> existingAccount = accountRepository.findByEmail(githubEmail);

        if (existingAccount.isPresent()) {
            // Tài khoản đã tồn tại
            account = existingAccount.get();
        } else {
            // Tài khoản chưa tồn tại, tạo mới
            account = new Account();
            account.setEmail(githubEmail);
            account.setFullName(githubName);
            account.setAvatar(githubAvatar);
            account.setUsername(githubUsername); // Có thể cần xử lý trùng username

            AccountRole accountRole = new AccountRole();
            accountRole.setAccount(account);
            accountRole.setRole(roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new ResourceNotFoundException("role")));
            account.getAccountRoles().add(accountRole);

            account = accountRepository.save(account);
        }

        // Tạo JWT token
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                account.getUsername(),
                account.getPassword() != null ? account.getPassword() : "", // Mật khẩu có thể null nếu là lần đầu login qua OAuth
                account.getAccountRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getRole().getName()))
                        .collect(Collectors.toList())
        );
        String jwtToken = jwtUtils.generateTokenFromUserDetails(userDetails);

        // Tạo hoặc cập nhật refresh token (tương tự như hàm signIn)
        String currentRefreshToken = account.getRefreshToken();
        LocalDateTime refreshExpiresAt = account.getRefreshExpiresAt();
        String refreshToken = (currentRefreshToken == null || (refreshExpiresAt != null && LocalDateTime.now().isAfter(refreshExpiresAt)))
                ? jwtUtils.generateRefreshTokenFromUserDetails(userDetails)
                : currentRefreshToken;

        if (currentRefreshToken == null || (refreshExpiresAt != null && LocalDateTime.now().isAfter(refreshExpiresAt))) {
            account.setRefreshToken(refreshToken);
            account.setRefreshExpiresAt(LocalDateTime.now().plusDays(30));
        }

        account.setLatestLogin(LocalDateTime.now());
        accountRepository.save(account);

        // Thêm token vào cookie (tương tự như hàm signIn)
        Cookie jwtCookie = new Cookie("token", jwtToken);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(60 * 60 * 24 * 7);
        jwtCookie.setAttribute("SameSite", "None");
        httpServletResponse.addCookie(jwtCookie);

        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(60 * 60 * 24 * 7);
        refreshTokenCookie.setAttribute("SameSite", "None");
        httpServletResponse.addCookie(refreshTokenCookie);

        // Tạo response
        return new AccountResponseDto(
                account.getId(),
                account.getFullName(),
                account.getUsername(),
                account.getEmail(),
                account.getAvatar(),
                account.getPhone(),
                account.getBlockReason(),
                account.getAccountRoles().stream().map(role -> role.getRole().getName()).toList()
        );
    }

    @Override
    public AccountResponseDto handleGoogleOAuth(String code) {
        // Bước 1: Đổi mã xác thực lấy access token từ Google
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientIdGoogle);
        body.add("client_secret", clientSecretGoogle);
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");

        ResponseEntity<Map<String, Object>> tokenResponse = restTemplate.exchange(
                tokenUriGoogle, HttpMethod.POST, new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<>() {}
        );

        String accessToken = (String) Objects.requireNonNull(tokenResponse.getBody()).get("access_token");

        // Bước 2: Lấy thông tin người dùng từ Google
        headers.setBearerAuth(accessToken);
        ResponseEntity<Map<String, Object>> userResponse = restTemplate.exchange(
                userInfoUriGoogle, HttpMethod.GET, new HttpEntity<>(headers),
                new ParameterizedTypeReference<>() {}
        );

        // Tạo tài khoản mới
        Account account = new Account();
        account.setEmail(Objects.requireNonNull(userResponse.getBody()).get("email").toString());
        account.setFullName(userResponse.getBody().get("name").toString());
        account.setAvatar(userResponse.getBody().get("picture").toString());
        account.setUsername(userResponse.getBody().get("email").toString().split("@")[0]);

        AccountRole accountRole = new AccountRole();
        accountRole.setAccount(account);
        accountRole.setRole(roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ResourceNotFoundException("role")));
        account.getAccountRoles().add(accountRole);

        // Lưu tài khoản vào cơ sở dữ liệu
        Account newAccount = accountRepository.save(account);

        return new AccountResponseDto(
                newAccount.getId(),
                newAccount.getFullName(),
                newAccount.getUsername(),
                newAccount.getEmail(),
                newAccount.getAvatar(),
                newAccount.getPhone(),
                account.getBlockReason(),
                newAccount.getAccountRoles().stream().map(role -> role.getRole().getName()).toList()
        );
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?") // Chạy mỗi ngày vào lúc 0:00
//    @Scheduled(cron = "*/30 * * * * ?") // Chạy mỗi 30 giây
    public void blockInactiveAccounts() {
        LocalDateTime thresholdDate = LocalDateTime.now().minusDays(30);
        List<Account> inactiveAccounts = accountRepository.findByLatestLoginBefore(thresholdDate);

        for (Account account : inactiveAccounts) {
            account.setBlockReason("long_time_no_login");
            accountRepository.save(account);
        }
    }
}
