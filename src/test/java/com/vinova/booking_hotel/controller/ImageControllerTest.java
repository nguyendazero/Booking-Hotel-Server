package com.vinova.booking_hotel.controller;

import com.vinova.booking_hotel.property.controller.ImageController;
import com.vinova.booking_hotel.property.dto.response.ImageResponseDto;
import com.vinova.booking_hotel.property.service.ImageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageControllerTest {

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ImageController imageController;

    @Test
    void images_shouldReturnOkAndListOfImageResponseDto_whenServiceReturnsImages() {
        // Arrange
        Long hotelId = 1L;
        List<ImageResponseDto> mockResponseDtos = Arrays.asList(
                new ImageResponseDto(1L, "url1"),
                new ImageResponseDto(2L, "url2")
        );
        when(imageService.images(hotelId)).thenReturn(mockResponseDtos);

        // Act
        ResponseEntity<List<ImageResponseDto>> responseEntity = imageController.images(hotelId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(2, Objects.requireNonNull(responseEntity.getBody()).size());
        assertEquals(1L, responseEntity.getBody().get(0).getId());
        assertEquals("url1", responseEntity.getBody().get(0).getImageUrl());
        assertEquals(2L, responseEntity.getBody().get(1).getId());
        assertEquals("url2", responseEntity.getBody().get(1).getImageUrl());
    }

    @Test
    void images_shouldReturnOkAndEmptyList_whenServiceReturnsEmptyList() {
        // Arrange
        Long hotelId = 2L;
        when(imageService.images(hotelId)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<ImageResponseDto>> responseEntity = imageController.images(hotelId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(0, Objects.requireNonNull(responseEntity.getBody()).size());
    }

    @Test
    void images_shouldReturnOkAndEmptyList_whenHotelIdDoesNotExist() {
        // Arrange
        Long hotelId = 3L;
        when(imageService.images(hotelId)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<ImageResponseDto>> responseEntity = imageController.images(hotelId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(0, Objects.requireNonNull(responseEntity.getBody()).size());
    }
}