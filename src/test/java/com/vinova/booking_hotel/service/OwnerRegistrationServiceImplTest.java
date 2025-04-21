package com.vinova.booking_hotel.service;

import com.vinova.booking_hotel.authentication.dto.response.OwnerRegistrationDto;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.model.AccountRole;
import com.vinova.booking_hotel.authentication.model.OwnerRegistration;
import com.vinova.booking_hotel.authentication.model.Role;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.repository.AccountRoleRepository;
import com.vinova.booking_hotel.authentication.repository.OwnerRegistrationRepository;
import com.vinova.booking_hotel.authentication.repository.RoleRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.authentication.service.impl.OwnerRegistrationServiceImpl;
import com.vinova.booking_hotel.common.enums.OwnerRegistrationStatus;
import com.vinova.booking_hotel.common.exception.OwnerRegistrationException;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OwnerRegistrationServiceImplTest {

    @Mock
    private OwnerRegistrationRepository ownerRegistrationRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AccountRoleRepository accountRoleRepository;

    @InjectMocks
    private OwnerRegistrationServiceImpl ownerRegistrationService;

    private static final String TEST_TOKEN = "test_jwt_token";
    private static final Long TEST_ACCOUNT_ID = 123L;
    private static final Long TEST_REGISTRATION_ID = 456L;
    private Account testAccount;
    private Role ownerRole;
    private AccountRole userAccountRole;
    private OwnerRegistration pendingRegistration;
    private OwnerRegistration acceptedRegistration;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setId(TEST_ACCOUNT_ID);
        testAccount.setUsername("testuser");
        testAccount.setFullName("Test User");
        testAccount.setEmail("test@example.com");
        testAccount.setAccountRoles(Collections.emptyList());

        Role userRole = new Role();
        userRole.setName("ROLE_USER");

        ownerRole = new Role();
        ownerRole.setName("ROLE_OWNER");

        userAccountRole = new AccountRole();
        userAccountRole.setRole(userRole);

        AccountRole ownerAccountRole = new AccountRole();
        ownerAccountRole.setRole(ownerRole);

        pendingRegistration = new OwnerRegistration();
        pendingRegistration.setId(TEST_REGISTRATION_ID);
        pendingRegistration.setAccount(testAccount);
        pendingRegistration.setStatus(OwnerRegistrationStatus.PENDING);

        acceptedRegistration = new OwnerRegistration();
        acceptedRegistration.setId(TEST_REGISTRATION_ID);
        acceptedRegistration.setAccount(testAccount);
        acceptedRegistration.setStatus(OwnerRegistrationStatus.ACCEPTED);
    }

    @Test
    void registerOwner_shouldSucceed_whenAccountIsUserAndNoPendingRequest() {
        // Arrange
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(testAccount));
        testAccount.setAccountRoles(Collections.singletonList(userAccountRole));
        when(ownerRegistrationRepository.findByAccountAndStatus(testAccount, OwnerRegistrationStatus.PENDING)).thenReturn(Optional.empty());
        when(ownerRegistrationRepository.save(any(OwnerRegistration.class))).thenReturn(pendingRegistration);

        // Act
        String result = ownerRegistrationService.registerOwner(TEST_TOKEN);

        // Assert
        assertEquals("Success register", result);
        verify(ownerRegistrationRepository, times(1)).save(any(OwnerRegistration.class));
    }

    @Test
    void ownerRegistrations_shouldReturnListOfOwnerRegistrationDtos() {
        // Arrange
        when(ownerRegistrationRepository.findAll()).thenReturn(Collections.singletonList(pendingRegistration));

        // Act
        List<OwnerRegistrationDto> dtos = ownerRegistrationService.ownerRegistrations();

        // Assert
        assertEquals(1, dtos.size());
        OwnerRegistrationDto dto = dtos.getFirst();
        assertEquals(pendingRegistration.getId(), dto.getId());
        assertEquals(pendingRegistration.getStatus().name(), dto.getStatus());
        assertNotNull(dto.getAccount());
        assertEquals(testAccount.getId(), dto.getAccount().getId());
        assertEquals(testAccount.getFullName(), dto.getAccount().getFullName());
        assertEquals(testAccount.getUsername(), dto.getAccount().getUsername());
        assertEquals(testAccount.getEmail(), dto.getAccount().getEmail());
        assertEquals(testAccount.getAccountRoles().stream().map(ar -> ar.getRole().getName()).toList(), dto.getAccount().getRoles());
        verify(ownerRegistrationRepository, times(1)).findAll();
        verify(jwtUtils, never()).getUserIdFromJwtToken(anyString()); // Không cần stub và verify ở đây
        verify(accountRepository, never()).findById(anyLong());     // Không cần stub và verify ở đây
    }

    @Test
    void acceptRegistration_shouldAcceptPendingRegistration() {
        // Arrange
        when(ownerRegistrationRepository.findById(TEST_REGISTRATION_ID)).thenReturn(Optional.of(pendingRegistration));
        when(roleRepository.findByName("ROLE_OWNER")).thenReturn(Optional.of(ownerRole));
        when(accountRoleRepository.save(any(AccountRole.class))).thenReturn(new AccountRole());
        when(ownerRegistrationRepository.save(any(OwnerRegistration.class))).thenReturn(acceptedRegistration);

        // Act
        ownerRegistrationService.acceptRegistration(TEST_REGISTRATION_ID);

        // Assert
        assertEquals(OwnerRegistrationStatus.ACCEPTED, pendingRegistration.getStatus());
        verify(ownerRegistrationRepository, times(1)).findById(TEST_REGISTRATION_ID);
        verify(roleRepository, times(1)).findByName("ROLE_OWNER");
        verify(accountRoleRepository, times(1)).save(any(AccountRole.class));
        verify(ownerRegistrationRepository, times(1)).save(pendingRegistration);
    }

    @Test
    void acceptRegistration_shouldThrowExceptions_forInvalidStates() {
        when(ownerRegistrationRepository.findById(TEST_REGISTRATION_ID)).thenReturn(Optional.of(acceptedRegistration));
        assertThrows(OwnerRegistrationException.class, () -> ownerRegistrationService.acceptRegistration(TEST_REGISTRATION_ID), "This request handled");

        when(ownerRegistrationRepository.findById(TEST_REGISTRATION_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> ownerRegistrationService.acceptRegistration(TEST_REGISTRATION_ID), "ownerRegistration not found");

        when(ownerRegistrationRepository.findById(TEST_REGISTRATION_ID)).thenReturn(Optional.of(pendingRegistration));
        when(roleRepository.findByName("ROLE_OWNER")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> ownerRegistrationService.acceptRegistration(TEST_REGISTRATION_ID), "role not found");
    }

    @Test
    void rejectRegistration_shouldRejectPendingRegistration() {
        // Arrange
        when(ownerRegistrationRepository.findById(TEST_REGISTRATION_ID)).thenReturn(Optional.of(pendingRegistration));
        OwnerRegistration rejectedRegistration = new OwnerRegistration();
        rejectedRegistration.setId(TEST_REGISTRATION_ID);
        rejectedRegistration.setAccount(testAccount);
        rejectedRegistration.setStatus(OwnerRegistrationStatus.REJECTED);
        when(ownerRegistrationRepository.save(any(OwnerRegistration.class))).thenReturn(rejectedRegistration);

        // Act
        ownerRegistrationService.rejectRegistration(TEST_REGISTRATION_ID);

        // Assert
        assertEquals(OwnerRegistrationStatus.REJECTED, pendingRegistration.getStatus());
        verify(ownerRegistrationRepository, times(1)).findById(TEST_REGISTRATION_ID);
        verify(ownerRegistrationRepository, times(1)).save(pendingRegistration);
    }

    @Test
    void rejectRegistration_shouldThrowExceptions_forInvalidStates() {
        when(ownerRegistrationRepository.findById(TEST_REGISTRATION_ID)).thenReturn(Optional.of(acceptedRegistration));
        assertThrows(OwnerRegistrationException.class, () -> ownerRegistrationService.rejectRegistration(TEST_REGISTRATION_ID), "This request handled");

        when(ownerRegistrationRepository.findById(TEST_REGISTRATION_ID)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> ownerRegistrationService.rejectRegistration(TEST_REGISTRATION_ID), "ownerRegistration not found");
    }
}