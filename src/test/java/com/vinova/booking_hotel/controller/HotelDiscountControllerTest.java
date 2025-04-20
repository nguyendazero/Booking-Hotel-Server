package com.vinova.booking_hotel.controller;

import com.vinova.booking_hotel.property.controller.HotelDiscountController;
import com.vinova.booking_hotel.property.dto.request.AddDiscountToHotelRequestDto;
import com.vinova.booking_hotel.property.dto.response.DiscountResponseDto;
import com.vinova.booking_hotel.property.dto.response.HotelDiscountResponseDto;
import com.vinova.booking_hotel.property.service.HotelDiscountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

public class HotelDiscountControllerTest {

    @Mock
    private HotelDiscountService hotelDiscountService;

    @InjectMocks
    private HotelDiscountController hotelDiscountController;

    private final String TEST_TOKEN = "Bearer test_token";
    private final Long TEST_HOTEL_ID = 1L;
    private final Long TEST_HOTEL_DISCOUNT_ID = 2L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addDiscountToHotel_shouldReturnCreatedStatusAndMessage() {
        // Arrange
        AddDiscountToHotelRequestDto requestDto = new AddDiscountToHotelRequestDto(TEST_HOTEL_ID, BigDecimal.valueOf(0.1), ZonedDateTime.now(), ZonedDateTime.now().plusDays(7));
        when(hotelDiscountService.addDiscountToHotel(any(AddDiscountToHotelRequestDto.class), anyString()))
                .thenReturn("Discount added successfully");

        // Act
        ResponseEntity<String> response = hotelDiscountController.addDiscountToHotel(requestDto, TEST_TOKEN);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Discount added successfully", response.getBody());
    }

    @Test
    void deleteHotelDiscount_shouldReturnNoContentStatus() {
        // Arrange
        when(hotelDiscountService.deleteHotelDiscount(anyLong(), anyString()))
                .thenReturn("Discount deleted successfully");

        // Act
        ResponseEntity<String> response = hotelDiscountController.deleteHotelDiscount(TEST_HOTEL_DISCOUNT_ID, TEST_TOKEN);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void getDiscountsByHotelId_shouldReturnOkStatusAndListOfDiscounts() {
        // Arrange
        DiscountResponseDto discountDto1 = new DiscountResponseDto(1L, BigDecimal.valueOf(0.1));
        HotelDiscountResponseDto hotelDiscountDto1 = new HotelDiscountResponseDto(101L, ZonedDateTime.now(), ZonedDateTime.now().plusDays(3), discountDto1);

        DiscountResponseDto discountDto2 = new DiscountResponseDto(2L, BigDecimal.valueOf(0.2));
        HotelDiscountResponseDto hotelDiscountDto2 = new HotelDiscountResponseDto(102L, ZonedDateTime.now().plusDays(5), ZonedDateTime.now().plusDays(10), discountDto2);

        List<HotelDiscountResponseDto> discounts = Arrays.asList(hotelDiscountDto1, hotelDiscountDto2);
        when(hotelDiscountService.getHotelDiscounts(TEST_HOTEL_ID)).thenReturn(discounts);

        // Act
        ResponseEntity<List<HotelDiscountResponseDto>> response = hotelDiscountController.getDiscountsByHotelId(TEST_HOTEL_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(discounts, response.getBody());
    }

    @Test
    void getDiscountsByHotelId_shouldReturnOkStatusAndEmptyList_whenNoDiscounts() {
        // Arrange
        when(hotelDiscountService.getHotelDiscounts(TEST_HOTEL_ID)).thenReturn(java.util.Collections.emptyList());

        // Act
        ResponseEntity<List<HotelDiscountResponseDto>> response = hotelDiscountController.getDiscountsByHotelId(TEST_HOTEL_ID);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(java.util.Collections.emptyList(), response.getBody());
    }
}