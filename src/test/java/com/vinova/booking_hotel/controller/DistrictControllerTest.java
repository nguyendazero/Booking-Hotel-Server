package com.vinova.booking_hotel.controller;

import com.vinova.booking_hotel.property.controller.DistrictController;
import com.vinova.booking_hotel.property.dto.request.AddDistrictRequestDto;
import com.vinova.booking_hotel.property.dto.response.DistrictResponseDto;
import com.vinova.booking_hotel.property.service.DistrictService;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class DistrictControllerTest {

    @Mock
    private DistrictService districtService;

    @InjectMocks
    private DistrictController districtController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Khởi tạo các mock
    }

    @Test
    public void testDistricts() {
        List<DistrictResponseDto> mockResponse = Collections.singletonList(new DistrictResponseDto());
        when(districtService.districts()).thenReturn(mockResponse);

        ResponseEntity<List<DistrictResponseDto>> response = districtController.districts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testDistrict() {
        Long districtId = 1L;
        DistrictResponseDto mockResponse = new DistrictResponseDto();
        when(districtService.district(districtId)).thenReturn(mockResponse);

        ResponseEntity<DistrictResponseDto> response = districtController.district(districtId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testCreate() {
        AddDistrictRequestDto requestDto = new AddDistrictRequestDto("Test District");
        DistrictResponseDto mockResponse = new DistrictResponseDto();
        when(districtService.create(requestDto)).thenReturn(mockResponse);

        ResponseEntity<DistrictResponseDto> response = districtController.create(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testUpdate() {
        Long districtId = 1L;
        AddDistrictRequestDto requestDto = new AddDistrictRequestDto("Updated District");
        when(districtService.update(anyLong(), any(AddDistrictRequestDto.class))).thenReturn(new DistrictResponseDto());

        ResponseEntity<DistrictResponseDto> response = districtController.update(districtId, requestDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testDelete() {
        Long districtId = 1L;
        // Giả lập hành vi của districtService để không có ngoại lệ xảy ra
        doNothing().when(districtService).delete(districtId);
        ResponseEntity<Void> response = districtController.delete(districtId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}