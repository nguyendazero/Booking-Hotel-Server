package com.vinova.booking_hotel.controller;

import com.vinova.booking_hotel.authentication.controller.OwnerRegistrationController;
import com.vinova.booking_hotel.authentication.dto.response.OwnerRegistrationDto;
import com.vinova.booking_hotel.authentication.service.OwnerRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class OwnerRegistrationControllerTest {

    @Mock
    private OwnerRegistrationService ownerRegistrationService;

    @InjectMocks
    private OwnerRegistrationController ownerRegistrationController;

    private static final String TEST_TOKEN = "Bearer test_token";
    private static final Long TEST_REGISTRATION_ID = 1L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registration_shouldReturnCreatedAndSuccessMessage() {
        String mockResponse = "Success register";
        when(ownerRegistrationService.registerOwner(anyString())).thenReturn(mockResponse);

        ResponseEntity<String> response = ownerRegistrationController.registration(TEST_TOKEN);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(ownerRegistrationService, times(1)).registerOwner(anyString());
    }

    @Test
    void registrations_shouldReturnOkAndListOfOwnerRegistrationDtos() {
        OwnerRegistrationDto dto = new OwnerRegistrationDto();
        dto.setId(TEST_REGISTRATION_ID);
        dto.setStatus("PENDING");
        List<OwnerRegistrationDto> mockResponse = Collections.singletonList(dto);
        when(ownerRegistrationService.ownerRegistrations()).thenReturn(mockResponse);

        ResponseEntity<List<OwnerRegistrationDto>> response = ownerRegistrationController.registrations();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
        verify(ownerRegistrationService, times(1)).ownerRegistrations();
    }

    @Test
    void acceptRegistration_shouldReturnNoContent() {
        doNothing().when(ownerRegistrationService).acceptRegistration(TEST_REGISTRATION_ID);

        ResponseEntity<Void> response = ownerRegistrationController.acceptRegistration(TEST_REGISTRATION_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(ownerRegistrationService, times(1)).acceptRegistration(TEST_REGISTRATION_ID);
    }

    @Test
    void rejectRegistration_shouldReturnNoContent() {
        doNothing().when(ownerRegistrationService).rejectRegistration(TEST_REGISTRATION_ID);

        ResponseEntity<Void> response = ownerRegistrationController.rejectRegistration(TEST_REGISTRATION_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(ownerRegistrationService, times(1)).rejectRegistration(TEST_REGISTRATION_ID);
    }
}