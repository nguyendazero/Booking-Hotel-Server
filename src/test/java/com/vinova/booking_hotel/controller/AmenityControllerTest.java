package com.vinova.booking_hotel.controller;

import com.vinova.booking_hotel.property.controller.AmenityController;
import com.vinova.booking_hotel.property.dto.request.AddAmenityRequestDto;
import com.vinova.booking_hotel.property.dto.response.AmenityResponseDto;
import com.vinova.booking_hotel.property.service.AmenityService;
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

public class AmenityControllerTest {

    @Mock
    private AmenityService amenityService;

    @InjectMocks
    private AmenityController amenityController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Khởi tạo các mock
    }

    @Test
    public void testAmenities() {
        List<AmenityResponseDto> mockResponse = Collections.singletonList(new AmenityResponseDto(1L, "Free WiFi"));
        when(amenityService.amenities()).thenReturn(mockResponse);

        ResponseEntity<List<AmenityResponseDto>> response = amenityController.amenities();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testAmenitiesByHotelId() {
        Long hotelId = 1L;
        List<AmenityResponseDto> mockResponse = Collections.singletonList(new AmenityResponseDto(1L, "Pool"));
        when(amenityService.amenitiesByHotelId(hotelId)).thenReturn(mockResponse);

        ResponseEntity<List<AmenityResponseDto>> response = amenityController.amenitiesByHotelId(hotelId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testAmenity() {
        Long amenityId = 1L;
        AmenityResponseDto mockResponse = new AmenityResponseDto(amenityId, "Free WiFi");
        when(amenityService.amenity(amenityId)).thenReturn(mockResponse);

        ResponseEntity<AmenityResponseDto> response = amenityController.amenity(amenityId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testCreate() {
        AddAmenityRequestDto requestDto = new AddAmenityRequestDto("New Amenity");
        AmenityResponseDto mockResponse = new AmenityResponseDto(1L, "New Amenity");
        when(amenityService.create(requestDto)).thenReturn(mockResponse);

        ResponseEntity<AmenityResponseDto> response = amenityController.create(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testUpdate() {
        Long amenityId = 1L;
        AddAmenityRequestDto requestDto = new AddAmenityRequestDto("Updated Amenity");
        when(amenityService.update(anyLong(), any(AddAmenityRequestDto.class))).thenReturn(new AmenityResponseDto(amenityId, "Updated Amenity"));

        ResponseEntity<AmenityResponseDto> response = amenityController.update(amenityId, requestDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testDelete() {
        Long amenityId = 1L;
        // Giả lập hành vi của amenityService để không có ngoại lệ xảy ra
        doNothing().when(amenityService).delete(amenityId);
        ResponseEntity<Void> response = amenityController.delete(amenityId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}