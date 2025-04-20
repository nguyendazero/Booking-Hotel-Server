package com.vinova.booking_hotel.service;

import com.vinova.booking_hotel.common.enums.EntityType;
import com.vinova.booking_hotel.property.dto.response.ImageResponseDto;
import com.vinova.booking_hotel.property.model.Image;
import com.vinova.booking_hotel.property.repository.ImageRepository;
import com.vinova.booking_hotel.property.service.impl.ImageServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageServiceImplTest {

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ImageServiceImpl imageService;

    @Test
    void images_shouldReturnListOfImageResponseDto_whenImagesFoundForHotel() {
        // Arrange
        Long hotelId = 1L;
        List<Image> mockImages = Arrays.asList(
                new Image(1L, hotelId, EntityType.HOTEL, "url1", null, null),
                new Image(2L, hotelId, EntityType.HOTEL, "url2", null, null)
        );
        when(imageRepository.findByEntityIdAndEntityType(hotelId, EntityType.HOTEL)).thenReturn(mockImages);

        // Act
        List<ImageResponseDto> responseDtos = imageService.images(hotelId);

        // Assert
        assertEquals(2, responseDtos.size());
        assertEquals(1L, responseDtos.get(0).getId());
        assertEquals("url1", responseDtos.get(0).getImageUrl());
        assertEquals(2L, responseDtos.get(1).getId());
        assertEquals("url2", responseDtos.get(1).getImageUrl());
    }

    @Test
    void images_shouldReturnEmptyList_whenNoImagesFoundForHotel() {
        // Arrange
        Long hotelId = 2L;
        when(imageRepository.findByEntityIdAndEntityType(hotelId, EntityType.HOTEL)).thenReturn(Collections.emptyList());

        // Act
        List<ImageResponseDto> responseDtos = imageService.images(hotelId);

        // Assert
        assertTrue(responseDtos.isEmpty());
    }

    @Test
    void images_shouldReturnEmptyList_whenImagesFoundForDifferentEntityType() {
        // Arrange
        Long hotelId = 3L;
        List<Image> mockImagesForReview = Collections.singletonList(
                new Image(3L, hotelId, EntityType.REVIEW, "review_url", null, null)
        );
        when(imageRepository.findByEntityIdAndEntityType(hotelId, EntityType.HOTEL)).thenReturn(Collections.emptyList());

        // Act
        List<ImageResponseDto> responseDtos = imageService.images(hotelId);

        // Assert
        assertTrue(responseDtos.isEmpty());
    }
}