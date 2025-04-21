package com.vinova.booking_hotel.controller;

import com.vinova.booking_hotel.property.controller.RatingController;
import com.vinova.booking_hotel.property.dto.request.AddRatingRequestDto;
import com.vinova.booking_hotel.property.dto.response.RatingResponseDto;
import com.vinova.booking_hotel.property.service.RatingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RatingControllerTest {

    @Mock
    private RatingService ratingService;

    @InjectMocks
    private RatingController ratingController;

    private final Long TEST_HOTEL_ID = 1L;
    private final Long TEST_RATING_ID = 2L;
    private final String TEST_TOKEN = "Bearer test_token";
    private final String TEST_ACCESS_TOKEN = "test_token";

    @Test
    void ratingsByHotelId_shouldReturnOkAndListOfRatingResponseDto() {
        // Arrange
        List<RatingResponseDto> mockResponse = Arrays.asList(
                new RatingResponseDto(1L, 5, "Excellent", ZonedDateTime.now(), Collections.emptyList(), null),
                new RatingResponseDto(2L, 4, "Good", ZonedDateTime.now(), Collections.emptyList(), null)
        );
        when(ratingService.ratingsByHotelId(TEST_HOTEL_ID)).thenReturn(mockResponse);

        // Act
        ResponseEntity<List<RatingResponseDto>> responseEntity = ratingController.ratingsByHotelId(TEST_HOTEL_ID);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(2, Objects.requireNonNull(responseEntity.getBody()).size());
        assertEquals(1L, responseEntity.getBody().getFirst().getId());
        assertEquals(5, responseEntity.getBody().getFirst().getStars());
    }

    @Test
    void ratingsByHotelId_shouldReturnOkAndEmptyList_whenNoRatingsFound() {
        // Arrange
        when(ratingService.ratingsByHotelId(TEST_HOTEL_ID)).thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<RatingResponseDto>> responseEntity = ratingController.ratingsByHotelId(TEST_HOTEL_ID);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(0, Objects.requireNonNull(responseEntity.getBody()).size());
    }

    @Test
    void rating_shouldReturnOkAndRatingResponseDto_whenRatingFound() {
        // Arrange
        RatingResponseDto mockResponse = new RatingResponseDto(TEST_RATING_ID, 4, "Nice", ZonedDateTime.now(), Collections.emptyList(), null);
        when(ratingService.rating(TEST_RATING_ID)).thenReturn(mockResponse);

        // Act
        ResponseEntity<RatingResponseDto> responseEntity = ratingController.rating(TEST_RATING_ID);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(TEST_RATING_ID, Objects.requireNonNull(responseEntity.getBody()).getId());
        assertEquals(4, responseEntity.getBody().getStars());
    }

    @Test
    void create_shouldReturnCreatedAndRatingResponseDto_whenCreationSuccessful() {
        // Arrange
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        AddRatingRequestDto requestDto = new AddRatingRequestDto(TEST_HOTEL_ID, 5, "Superb!", Collections.singletonList(new MockMultipartFile("image", "test.jpg", "image/jpeg", "content".getBytes())));
        RatingResponseDto mockResponse = new RatingResponseDto(3L, 5, "Superb!", ZonedDateTime.now(), Collections.emptyList(), null);
        when(ratingService.create(requestDto, TEST_ACCESS_TOKEN)).thenReturn(mockResponse);

        // Act
        ResponseEntity<RatingResponseDto> responseEntity = ratingController.create(requestDto, TEST_TOKEN);

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(3L, Objects.requireNonNull(responseEntity.getBody()).getId());
        assertEquals(5, responseEntity.getBody().getStars());
    }

    @Test
    void delete_shouldReturnNoContent_whenDeletionSuccessful() {
        // Arrange
        doNothing().when(ratingService).delete(TEST_RATING_ID, TEST_ACCESS_TOKEN);

        // Act
        ResponseEntity<Void> responseEntity = ratingController.delete(TEST_RATING_ID, TEST_TOKEN);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(ratingService, times(1)).delete(TEST_RATING_ID, TEST_ACCESS_TOKEN);
    }
}
