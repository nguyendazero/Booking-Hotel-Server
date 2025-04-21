package com.vinova.booking_hotel.controller;

import com.vinova.booking_hotel.property.controller.WishListController;
import com.vinova.booking_hotel.property.service.WishListService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WishListControllerTest {

    @Mock
    private WishListService wishListService;

    @InjectMocks
    private WishListController wishListController;

    private final Long TEST_HOTEL_ID = 2L;
    private final String TEST_TOKEN = "Bearer test_token";
    private final String TEST_ACCESS_TOKEN = "test_token";

    @Test
    void addToWishList_shouldReturnCreatedAndMessage_whenAddedSuccessfully() {
        // Arrange
        String mockResponse = "Added to wishlist";
        when(wishListService.addToWishList(TEST_HOTEL_ID, TEST_ACCESS_TOKEN)).thenReturn(mockResponse);

        // Act
        ResponseEntity<String> responseEntity = wishListController.addToWishList(TEST_HOTEL_ID, TEST_TOKEN);

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(wishListService, times(1)).addToWishList(TEST_HOTEL_ID, TEST_ACCESS_TOKEN);
    }

    @Test
    void addToWishList_shouldReturnCreatedAndMessage_whenAlreadyInWishList() {
        // Arrange
        String mockResponse = "Hotel already in wishlist";
        when(wishListService.addToWishList(TEST_HOTEL_ID, TEST_ACCESS_TOKEN)).thenReturn(mockResponse);

        // Act
        ResponseEntity<String> responseEntity = wishListController.addToWishList(TEST_HOTEL_ID, TEST_TOKEN);

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(mockResponse, responseEntity.getBody());
        verify(wishListService, times(1)).addToWishList(TEST_HOTEL_ID, TEST_ACCESS_TOKEN);
    }

    @Test
    void removeFromWishList_shouldReturnNoContent_whenRemovedSuccessfully() {
        // Arrange
        String mockResponse = "Removed from wishlist";
        when(wishListService.removeFromWishList(TEST_HOTEL_ID, TEST_ACCESS_TOKEN)).thenReturn(mockResponse);

        // Act
        ResponseEntity<String> responseEntity = wishListController.removeFromWishList(TEST_HOTEL_ID, TEST_TOKEN);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(wishListService, times(1)).removeFromWishList(TEST_HOTEL_ID, TEST_ACCESS_TOKEN);
    }

    @Test
    void removeFromWishList_shouldReturnNoContent_whenNotFoundInWishList() {
        // Arrange
        String mockResponse = "Hotel not found in wishlist";
        when(wishListService.removeFromWishList(TEST_HOTEL_ID, TEST_ACCESS_TOKEN)).thenReturn(mockResponse);

        // Act
        ResponseEntity<String> responseEntity = wishListController.removeFromWishList(TEST_HOTEL_ID, TEST_TOKEN);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, responseEntity.getStatusCode());
        verify(wishListService, times(1)).removeFromWishList(TEST_HOTEL_ID, TEST_ACCESS_TOKEN);
    }
}
