package com.vinova.booking_hotel.controller;

import com.vinova.booking_hotel.property.controller.DistrictController;
import com.vinova.booking_hotel.property.dto.request.AddDistrictRequestDto;
import com.vinova.booking_hotel.property.dto.response.DistrictResponseDto;
import com.vinova.booking_hotel.property.service.DistrictService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;

public class DistrictControllerTest {

    private DistrictService districtService;
    private DistrictController districtController;

    @BeforeEach
    public void setUp() {
        districtService = Mockito.mock(DistrictService.class);
        districtController = new DistrictController(districtService);
    }

    @Test
    public void testDistricts() {
        // Arrange
        List<DistrictResponseDto> mockResponse = Collections.singletonList(new DistrictResponseDto());
        Mockito.when(districtService.districts()).thenReturn(mockResponse);

        // Act
        ResponseEntity<List<DistrictResponseDto>> response = districtController.districts();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testDistrict() {
        // Arrange
        Long districtId = 1L;
        DistrictResponseDto mockResponse = new DistrictResponseDto();
        Mockito.when(districtService.district(districtId)).thenReturn(mockResponse);

        // Act
        ResponseEntity<DistrictResponseDto> response = districtController.district(districtId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testCreate() {
        // Arrange
        AddDistrictRequestDto requestDto = new AddDistrictRequestDto();
        DistrictResponseDto mockResponse = new DistrictResponseDto();
        Mockito.when(districtService.create(requestDto)).thenReturn(mockResponse);

        // Act
        ResponseEntity<DistrictResponseDto> response = districtController.create(requestDto);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testUpdate() {
        // Arrange
        Long districtId = 1L;
        AddDistrictRequestDto requestDto = new AddDistrictRequestDto();
        Mockito.when(districtService.update(anyLong(), any(AddDistrictRequestDto.class))).thenReturn(new DistrictResponseDto());

        // Act
        ResponseEntity<DistrictResponseDto> response = districtController.update(districtId, requestDto);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testDelete() {
        // Arrange
        Long districtId = 1L;
        Mockito.doNothing().when(districtService).delete(districtId);

        // Act
        ResponseEntity<Void> response = districtController.delete(districtId);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}