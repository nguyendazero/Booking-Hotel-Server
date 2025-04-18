package com.vinova.booking_hotel.service;

import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddDiscountRequestDto;
import com.vinova.booking_hotel.property.dto.response.DiscountResponseDto;
import com.vinova.booking_hotel.property.model.Discount;
import com.vinova.booking_hotel.property.repository.DiscountRepository;
import com.vinova.booking_hotel.property.service.impl.DiscountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class DiscountServiceTest {

    @Mock
    private DiscountRepository discountRepository;

    @InjectMocks
    private DiscountServiceImpl discountService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this); // Khởi tạo các mock
    }

    @Test
    public void testDiscounts() {
        // Arrange
        Discount mockDiscount1 = new Discount(1L, new BigDecimal("10.00"), null, null, null);
        Discount mockDiscount2 = new Discount(2L, new BigDecimal("20.00"), null, null, null);
        List<Discount> mockDiscounts = Arrays.asList(mockDiscount1, mockDiscount2);

        when(discountRepository.findAll()).thenReturn(mockDiscounts);

        // Act
        List<DiscountResponseDto> result = discountService.discounts();

        // Assert
        assertNotNull(result);
        assertEquals(mockDiscounts.size(), result.size());
        assertEquals(mockDiscount1.getRate(), result.get(0).getRate());
        assertEquals(mockDiscount2.getRate(), result.get(1).getRate());
    }

    @Test
    public void testDiscount_WhenExists_ReturnDiscount() {
        // Arrange
        Long discountId = 1L;
        Discount mockDiscount = new Discount(discountId, new BigDecimal("10.00"), null, null, null);
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(mockDiscount));

        // Act
        DiscountResponseDto result = discountService.discount(discountId);

        // Assert
        assertNotNull(result);
        assertEquals(mockDiscount.getRate(), result.getRate());
    }

    @Test
    public void testDiscount_WhenNotExists_ThrowException() {
        // Arrange
        Long discountId = 1L;
        when(discountRepository.findById(discountId)).thenReturn(Optional.empty());

        // Act và Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> discountService.discount(discountId),
                "Discount not found"
        );
    }

    @Test
    public void testCreateDiscount() {
        // Arrange
        AddDiscountRequestDto mockRequest = new AddDiscountRequestDto(new BigDecimal("15.00"));
        Discount mockDiscount = new Discount();
        mockDiscount.setId(1L);
        mockDiscount.setRate(mockRequest.getRate());

        when(discountRepository.save(any(Discount.class))).thenReturn(mockDiscount);

        // Act
        DiscountResponseDto savedDiscount = discountService.create(mockRequest);

        // Assert
        assertNotNull(savedDiscount);
        assertEquals(mockRequest.getRate(), savedDiscount.getRate());
    }

    @Test
    public void testUpdateDiscount_WhenExists_UpdateSuccessfully() {
        // Arrange
        Long discountId = 1L;
        AddDiscountRequestDto mockRequest = new AddDiscountRequestDto(new BigDecimal("25.00"));
        Discount existingDiscount = new Discount(discountId, new BigDecimal("10.00"), null, null, null);
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(existingDiscount));

        // Act
        DiscountResponseDto updatedDiscount = discountService.update(discountId, mockRequest);

        // Assert
        assertNotNull(updatedDiscount);
        assertEquals(mockRequest.getRate(), updatedDiscount.getRate());
    }

    @Test
    public void testUpdateDiscount_WhenNotExists_ThrowException() {
        // Arrange
        Long discountId = 1L;
        AddDiscountRequestDto mockRequest = new AddDiscountRequestDto(new BigDecimal("30.00"));
        when(discountRepository.findById(discountId)).thenReturn(Optional.empty());

        // Act và Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> discountService.update(discountId, mockRequest),
                "Discount not found"
        );
    }

    @Test
    public void testDeleteDiscount_WhenExists_DeleteSuccessfully() {
        // Arrange
        Long discountId = 1L;
        Discount mockDiscount = new Discount(discountId, new BigDecimal("10.00"), null, null, null);
        when(discountRepository.findById(discountId)).thenReturn(Optional.of(mockDiscount));

        // Act
        discountService.delete(discountId);

        // Assert
        // Xác minh rằng discount được xóa
        Mockito.verify(discountRepository).delete(mockDiscount);
    }

    @Test
    public void testDeleteDiscount_WhenNotExists_ThrowException() {
        // Arrange
        Long discountId = 1L;
        when(discountRepository.findById(discountId)).thenReturn(Optional.empty());

        // Act và Assert
        assertThrows(
                ResourceNotFoundException.class,
                () -> discountService.delete(discountId),
                "Discount not found"
        );
    }
}