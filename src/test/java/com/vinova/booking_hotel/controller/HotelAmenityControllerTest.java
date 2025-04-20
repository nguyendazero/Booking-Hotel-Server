package com.vinova.booking_hotel.controller;

import com.vinova.booking_hotel.property.controller.HotelAmenityController;
import com.vinova.booking_hotel.property.dto.request.AddAmenityToHotelRequestDto;
import com.vinova.booking_hotel.property.dto.request.DeleteAmenityFromHotelRequestDto;
import com.vinova.booking_hotel.property.service.HotelAmenityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class HotelAmenityControllerTest {

    @Mock
    private HotelAmenityService hotelAmenityService;

    @InjectMocks
    private HotelAmenityController hotelAmenityController;

    private final String TEST_TOKEN = "Bearer test_token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addAmenityToHotel_shouldReturnCreatedStatusAndMessage() {
        // Arrange
        AddAmenityToHotelRequestDto requestDto = new AddAmenityToHotelRequestDto(1L, "Swimming Pool");
        when(hotelAmenityService.addAmenityToHotel(any(AddAmenityToHotelRequestDto.class), anyString()))
                .thenReturn("Amenity added successfully");

        // Act
        ResponseEntity<String> response = hotelAmenityController.addAmenityToHotel(requestDto, TEST_TOKEN);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Amenity added successfully", response.getBody());
    }

    @Test
    void removeAmenityFromHotel_shouldReturnNoContentStatus() {
        // Arrange
        DeleteAmenityFromHotelRequestDto requestDto = new DeleteAmenityFromHotelRequestDto(1L, 2L);
        when(hotelAmenityService.removeAmenityFromHotel(any(DeleteAmenityFromHotelRequestDto.class), anyString()))
                .thenReturn("Amenity removed successfully");

        // Act
        ResponseEntity<String> response = hotelAmenityController.removeAmenityFromHotel(requestDto, TEST_TOKEN);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}