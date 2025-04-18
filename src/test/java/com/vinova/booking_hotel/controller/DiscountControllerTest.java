package com.vinova.booking_hotel.controller;

import com.vinova.booking_hotel.property.controller.DiscountController;
import com.vinova.booking_hotel.property.dto.request.AddDiscountRequestDto;
import com.vinova.booking_hotel.property.dto.response.DiscountResponseDto;
import com.vinova.booking_hotel.property.service.DiscountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class DiscountControllerTest {

    @Mock
    private DiscountService discountService;

    @InjectMocks
    private DiscountController discountController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Khởi tạo các mock
    }

    @Test
    public void testDiscounts() {
        List<DiscountResponseDto> mockResponse = Collections.singletonList(new DiscountResponseDto(1L, new BigDecimal("10.00")));
        when(discountService.discounts()).thenReturn(mockResponse);

        ResponseEntity<List<DiscountResponseDto>> response = discountController.discounts();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testDiscount() {
        Long discountId = 1L;
        DiscountResponseDto mockResponse = new DiscountResponseDto(discountId, new BigDecimal("10.00"));
        when(discountService.discount(discountId)).thenReturn(mockResponse);

        ResponseEntity<DiscountResponseDto> response = discountController.discount(discountId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testCreate() {
        AddDiscountRequestDto requestDto = new AddDiscountRequestDto(new BigDecimal("15.00"));
        DiscountResponseDto mockResponse = new DiscountResponseDto(1L, new BigDecimal("15.00"));
        when(discountService.create(requestDto)).thenReturn(mockResponse);

        ResponseEntity<DiscountResponseDto> response = discountController.create(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testUpdate() {
        Long discountId = 1L;
        AddDiscountRequestDto requestDto = new AddDiscountRequestDto(new BigDecimal("20.00"));
        when(discountService.update(anyLong(), any(AddDiscountRequestDto.class))).thenReturn(new DiscountResponseDto(discountId, new BigDecimal("20.00")));

        ResponseEntity<DiscountResponseDto> response = discountController.update(discountId, requestDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testDelete() {
        Long discountId = 1L;
        // Giả lập hành vi của discountService để không có ngoại lệ xảy ra
        doNothing().when(discountService).delete(discountId);
        ResponseEntity<Void> response = discountController.delete(discountId);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}