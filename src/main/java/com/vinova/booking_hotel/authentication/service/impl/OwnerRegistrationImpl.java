package com.vinova.booking_hotel.authentication.service.impl;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.authentication.dto.response.AccountResponseDto;
import com.vinova.booking_hotel.authentication.dto.response.OwnerRegistrationDto;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.model.AccountRole;
import com.vinova.booking_hotel.authentication.model.OwnerRegistration;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.repository.AccountRoleRepository;
import com.vinova.booking_hotel.authentication.repository.OwnerRegistrationRepository;
import com.vinova.booking_hotel.authentication.repository.RoleRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.authentication.service.OwnerRegistrationService;
import com.vinova.booking_hotel.common.enums.ApiError;
import com.vinova.booking_hotel.common.enums.OwnerRegistrationStatus;
import com.vinova.booking_hotel.common.exception.OwnerRegistrationException;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OwnerRegistrationImpl implements OwnerRegistrationService {
    
    private final OwnerRegistrationRepository ownerRegistrationRepository;
    private final JwtUtils jwtUtils;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final AccountRoleRepository accountRoleRepository;

    @Override
    public APICustomize<String> registerOwner(String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(ResourceNotFoundException::new);

        // Kiểm tra vai trò và yêu cầu
        if (account.getAccountRoles().stream().noneMatch(role -> role.getRole().getName().equals("ROLE_USER"))) {
            throw new OwnerRegistrationException("Only user can register");
        }

        // Kiểm tra xem tài khoản đã là OWNER chưa
        if (account.getAccountRoles().stream().anyMatch(role -> role.getRole().getName().equals("ROLE_OWNER"))) {
            throw new OwnerRegistrationException("You are already an owner.");
        }

        // Kiểm tra xem tài khoản đã gửi request chưa
        if (ownerRegistrationRepository.findByAccountAndStatus(account, OwnerRegistrationStatus.PENDING).isPresent()) {
            throw new OwnerRegistrationException("You have a pending request");
        }

        // Đăng ký tài khoản
        OwnerRegistration ownerRegistration = new OwnerRegistration();
        ownerRegistration.setAccount(account);
        ownerRegistration.setStatus(OwnerRegistrationStatus.PENDING);

        ownerRegistrationRepository.save(ownerRegistration);
        return new APICustomize<>(ApiError.CREATED.getCode(), ApiError.CREATED.getMessage(), "Success register");
    }

    @Override
    public APICustomize<List<OwnerRegistrationDto>> ownerRegistrations() {
        List<OwnerRegistration> ownerRegistrations = ownerRegistrationRepository.findAll();

        List<OwnerRegistrationDto> response = ownerRegistrations.stream()
                .map(ownerRegistration -> {
                    OwnerRegistrationDto dto = new OwnerRegistrationDto();
                    dto.setId(ownerRegistration.getId());
                    dto.setStatus(ownerRegistration.getStatus().name());

                    // Chuyển đổi Account thành AccountResponseDto
                    Account account = ownerRegistration.getAccount();
                    AccountResponseDto accountResponseDto = new AccountResponseDto();
                    accountResponseDto.setId(account.getId());
                    accountResponseDto.setFullName(account.getFullName());
                    accountResponseDto.setUsername(account.getUsername());
                    accountResponseDto.setEmail(account.getEmail());
                    accountResponseDto.setAvatar(account.getAvatar());
                    accountResponseDto.setPhone(account.getPhone());

                    // Lấy danh sách vai trò từ tài khoản
                    List<String> roles = account.getAccountRoles().stream()
                            .map(accountRole -> accountRole.getRole().getName())
                            .toList();
                    accountResponseDto.setRoles(roles);

                    dto.setAccount(accountResponseDto);
                    return dto;
                })
                .toList();

        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), response);
    }

    @Override
    public APICustomize<Void> acceptRegistration(Long ownerRegistrationId) {
        // Tìm yêu cầu đăng ký theo ID
        OwnerRegistration ownerRegistration = ownerRegistrationRepository.findById(ownerRegistrationId)
                .orElseThrow(ResourceNotFoundException::new);
        
        if(!ownerRegistration.getStatus().name().equals("PENDING")) {
            throw new OwnerRegistrationException("This request handled");
        }

        // Thay đổi trạng thái yêu cầu đăng ký thành ACCEPTED
        ownerRegistration.setStatus(OwnerRegistrationStatus.ACCEPTED);

        AccountRole accountRole = new AccountRole();
        accountRole.setAccount(ownerRegistration.getAccount());
        accountRole.setRole(roleRepository.findByName("ROLE_OWNER").orElseThrow(ResourceNotFoundException::new));
        accountRoleRepository.save(accountRole);
        
        // Lưu cập nhật vào repository
        ownerRegistrationRepository.save(ownerRegistration);
        return new APICustomize<>(ApiError.NO_CONTENT.getCode(), ApiError.NO_CONTENT.getMessage(), null);
    }

    @Override
    public APICustomize<Void> rejectRegistration(Long ownerRegistrationId) {
        // Tìm yêu cầu đăng ký theo ID
        OwnerRegistration ownerRegistration = ownerRegistrationRepository.findById(ownerRegistrationId)
                .orElseThrow(ResourceNotFoundException::new);

        if(!ownerRegistration.getStatus().name().equals("PENDING")) {
            throw new OwnerRegistrationException("This request handled");
        }

        // Thay đổi trạng thái yêu cầu đăng ký thành REJECTED
        ownerRegistration.setStatus(OwnerRegistrationStatus.REJECTED);

        // Lưu cập nhật vào repository
        ownerRegistrationRepository.save(ownerRegistration);
        return new APICustomize<>(ApiError.NO_CONTENT.getCode(), ApiError.NO_CONTENT.getMessage(), null);
    }
}
