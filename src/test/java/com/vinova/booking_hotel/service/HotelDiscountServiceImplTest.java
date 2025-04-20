package com.vinova.booking_hotel.service;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddDiscountToHotelRequestDto;
import com.vinova.booking_hotel.property.dto.response.HotelDiscountResponseDto;
import com.vinova.booking_hotel.property.model.Discount;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.HotelDiscount;
import com.vinova.booking_hotel.property.repository.DiscountRepository;
import com.vinova.booking_hotel.property.repository.HotelDiscountRepository;
import com.vinova.booking_hotel.property.repository.HotelRepository;
import com.vinova.booking_hotel.property.service.impl.HotelDiscountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HotelDiscountServiceImplTest {

    @Mock
    private HotelDiscountRepository hotelDiscountRepository;
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private DiscountRepository discountRepository;

    @InjectMocks
    private HotelDiscountServiceImpl hotelDiscountService;

    private final String TEST_TOKEN = "test_token";
    private final Long TEST_HOTEL_ID = 1L;
    private final Long TEST_ACCOUNT_ID = 2L;
    private final Long TEST_DISCOUNT_ID = 3L;
    private final Long TEST_HOTEL_DISCOUNT_ID = 4L;
    private Hotel testHotel;
    private Account testAccount;
    private Discount existingDiscount;
    private Discount newDiscount;
    private ZonedDateTime now;
    private ZonedDateTime future;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setId(TEST_ACCOUNT_ID);

        testHotel = new Hotel();
        testHotel.setId(TEST_HOTEL_ID);
        testHotel.setAccount(testAccount);

        existingDiscount = new Discount();
        existingDiscount.setId(TEST_DISCOUNT_ID);
        existingDiscount.setRate(BigDecimal.valueOf(0.1));

        newDiscount = new Discount();
        newDiscount.setId(TEST_DISCOUNT_ID + 1);
        newDiscount.setRate(BigDecimal.valueOf(0.2));

        now = ZonedDateTime.now();
        future = now.plusDays(7);
    }

    @Test
    void addDiscountToHotel_shouldAddExistingDiscount() {
        // Arrange
        AddDiscountToHotelRequestDto requestDto = new AddDiscountToHotelRequestDto(TEST_HOTEL_ID, BigDecimal.valueOf(0.1), now, future);
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(testAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(testHotel));
        when(discountRepository.findByRate(BigDecimal.valueOf(0.1))).thenReturn(existingDiscount);
        when(hotelDiscountRepository.findByHotelIdAndDateRange(TEST_HOTEL_ID, now, future)).thenReturn(Collections.emptyList());
        when(hotelDiscountRepository.save(any(HotelDiscount.class))).thenReturn(new HotelDiscount());

        // Act
        String response = hotelDiscountService.addDiscountToHotel(requestDto, TEST_TOKEN);

        // Assert
        assertEquals("Discount added to hotel", response);
        verify(hotelRepository, times(1)).findById(TEST_HOTEL_ID);
        verify(accountRepository, times(1)).findById(TEST_ACCOUNT_ID);
        verify(discountRepository, times(1)).findByRate(BigDecimal.valueOf(0.1));
        verify(discountRepository, never()).save(any(Discount.class));
        verify(hotelDiscountRepository, times(1)).save(any(HotelDiscount.class));
        verify(hotelDiscountRepository, times(1)).findByHotelIdAndDateRange(TEST_HOTEL_ID, now, future);
    }

    @Test
    void addDiscountToHotel_shouldAddNewDiscount() {
        // Arrange
        AddDiscountToHotelRequestDto requestDto = new AddDiscountToHotelRequestDto(TEST_HOTEL_ID, BigDecimal.valueOf(0.2), now, future);
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(testAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(testHotel));
        when(discountRepository.findByRate(BigDecimal.valueOf(0.2))).thenReturn(null);
        when(discountRepository.save(any(Discount.class))).thenReturn(newDiscount);
        when(hotelDiscountRepository.findByHotelIdAndDateRange(TEST_HOTEL_ID, now, future)).thenReturn(Collections.emptyList());
        when(hotelDiscountRepository.save(any(HotelDiscount.class))).thenReturn(new HotelDiscount());

        // Act
        String response = hotelDiscountService.addDiscountToHotel(requestDto, TEST_TOKEN);

        // Assert
        assertEquals("Discount added to hotel", response);
        verify(hotelRepository, times(1)).findById(TEST_HOTEL_ID);
        verify(accountRepository, times(1)).findById(TEST_ACCOUNT_ID);
        verify(discountRepository, times(1)).findByRate(BigDecimal.valueOf(0.2));
        verify(discountRepository, times(1)).save(any(Discount.class));
        verify(hotelDiscountRepository, times(1)).save(any(HotelDiscount.class));
        verify(hotelDiscountRepository, times(1)).findByHotelIdAndDateRange(TEST_HOTEL_ID, now, future);
    }

    @Test
    void addDiscountToHotel_shouldThrowException_whenDateRangeInvalid() {
        // Arrange
        AddDiscountToHotelRequestDto requestDtoInvalid = new AddDiscountToHotelRequestDto(TEST_HOTEL_ID, BigDecimal.valueOf(0.1), future, now);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> hotelDiscountService.addDiscountToHotel(requestDtoInvalid, TEST_TOKEN));
        verifyNoInteractions(jwtUtils, accountRepository, hotelRepository, discountRepository, hotelDiscountRepository);

        AddDiscountToHotelRequestDto requestDtoNullStart = new AddDiscountToHotelRequestDto(TEST_HOTEL_ID, BigDecimal.valueOf(0.1), null, future);
        assertThrows(RuntimeException.class, () -> hotelDiscountService.addDiscountToHotel(requestDtoNullStart, TEST_TOKEN));
        verifyNoMoreInteractions(jwtUtils, accountRepository, hotelRepository, discountRepository, hotelDiscountRepository);

        AddDiscountToHotelRequestDto requestDtoNullEnd = new AddDiscountToHotelRequestDto(TEST_HOTEL_ID, BigDecimal.valueOf(0.1), now, null);
        assertThrows(RuntimeException.class, () -> hotelDiscountService.addDiscountToHotel(requestDtoNullEnd, TEST_TOKEN));
        verifyNoMoreInteractions(jwtUtils, accountRepository, hotelRepository, discountRepository, hotelDiscountRepository);
    }

    @Test
    void addDiscountToHotel_shouldThrowResourceNotFoundException_whenHotelNotFound() {
        // Arrange
        AddDiscountToHotelRequestDto requestDto = new AddDiscountToHotelRequestDto(TEST_HOTEL_ID, BigDecimal.valueOf(0.1), now, future);
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> hotelDiscountService.addDiscountToHotel(requestDto, TEST_TOKEN));
        verify(hotelRepository, times(1)).findById(TEST_HOTEL_ID);
        verifyNoInteractions(jwtUtils, accountRepository, discountRepository, hotelDiscountRepository);
    }

    @Test
    void addDiscountToHotel_shouldThrowResourceNotFoundException_whenAccountNotFound() {
        // Arrange
        AddDiscountToHotelRequestDto requestDto = new AddDiscountToHotelRequestDto(TEST_HOTEL_ID, BigDecimal.valueOf(0.1), now, future);
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.empty());
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(testHotel));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> hotelDiscountService.addDiscountToHotel(requestDto, TEST_TOKEN));
        verify(jwtUtils, times(1)).getUserIdFromJwtToken(TEST_TOKEN);
        verify(accountRepository, times(1)).findById(TEST_ACCOUNT_ID);
        verify(hotelRepository, times(1)).findById(TEST_HOTEL_ID);
        verifyNoInteractions(discountRepository, hotelDiscountRepository);
    }

    @Test
    void addDiscountToHotel_shouldThrowRuntimeException_whenNoPermission() {
        // Arrange
        Account anotherAccount = new Account();
        anotherAccount.setId(TEST_ACCOUNT_ID + 1);
        AddDiscountToHotelRequestDto requestDto = new AddDiscountToHotelRequestDto(TEST_HOTEL_ID, BigDecimal.valueOf(0.1), now, future);
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(anotherAccount.getId());
        when(accountRepository.findById(anotherAccount.getId())).thenReturn(Optional.of(anotherAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(testHotel));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> hotelDiscountService.addDiscountToHotel(requestDto, TEST_TOKEN));
        verify(jwtUtils, times(1)).getUserIdFromJwtToken(TEST_TOKEN);
        verify(accountRepository, times(1)).findById(anotherAccount.getId());
        verify(hotelRepository, times(1)).findById(TEST_HOTEL_ID);
        verifyNoInteractions(discountRepository, hotelDiscountRepository);
    }

    @Test
    void addDiscountToHotel_shouldThrowRuntimeException_whenDiscountAlreadyExists() {
        // Arrange
        AddDiscountToHotelRequestDto requestDto = new AddDiscountToHotelRequestDto(TEST_HOTEL_ID, BigDecimal.valueOf(0.1), now, future);
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(testAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(testHotel));
        when(hotelDiscountRepository.findByHotelIdAndDateRange(TEST_HOTEL_ID, now, future)).thenReturn(Collections.singletonList(new HotelDiscount()));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> hotelDiscountService.addDiscountToHotel(requestDto, TEST_TOKEN));
        verify(hotelRepository, times(1)).findById(TEST_HOTEL_ID);
        verify(accountRepository, times(1)).findById(TEST_ACCOUNT_ID);
        verify(hotelDiscountRepository, times(1)).findByHotelIdAndDateRange(TEST_HOTEL_ID, now, future);
        verifyNoInteractions(discountRepository);
        verify(hotelDiscountRepository, never()).save(any());
    }

    @Test
    void deleteHotelDiscount_shouldDeleteDiscountSuccessfully() {
        // Arrange
        HotelDiscount hotelDiscountToDelete = new HotelDiscount();
        hotelDiscountToDelete.setId(TEST_HOTEL_DISCOUNT_ID);
        hotelDiscountToDelete.setHotel(testHotel);

        when(hotelDiscountRepository.findById(TEST_HOTEL_DISCOUNT_ID)).thenReturn(Optional.of(hotelDiscountToDelete));
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(testAccount));
        doNothing().when(hotelDiscountRepository).delete(hotelDiscountToDelete);

        // Act
        String response = hotelDiscountService.deleteHotelDiscount(TEST_HOTEL_DISCOUNT_ID, TEST_TOKEN);

        // Assert
        assertEquals("Discount deleted successfully", response);
        verify(hotelDiscountRepository, times(1)).findById(TEST_HOTEL_DISCOUNT_ID);
        verify(jwtUtils, times(1)).getUserIdFromJwtToken(TEST_TOKEN);
        verify(accountRepository, times(1)).findById(TEST_ACCOUNT_ID);
        verify(hotelDiscountRepository, times(1)).delete(hotelDiscountToDelete);
    }

    @Test
    void deleteHotelDiscount_shouldThrowRuntimeException_whenDiscountNotFound() {
        // Arrange
        when(hotelDiscountRepository.findById(TEST_HOTEL_DISCOUNT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> hotelDiscountService.deleteHotelDiscount(TEST_HOTEL_DISCOUNT_ID, TEST_TOKEN));
        verify(hotelDiscountRepository, times(1)).findById(TEST_HOTEL_DISCOUNT_ID);
        verifyNoInteractions(jwtUtils, accountRepository, hotelRepository);
        verify(hotelDiscountRepository, never()).delete(any());
    }

    @Test
    void deleteHotelDiscount_shouldThrowResourceNotFoundException_whenAccountNotFound() {
        // Arrange
        HotelDiscount hotelDiscountToDelete = new HotelDiscount();
        hotelDiscountToDelete.setId(TEST_HOTEL_DISCOUNT_ID);
        hotelDiscountToDelete.setHotel(testHotel);

        when(hotelDiscountRepository.findById(TEST_HOTEL_DISCOUNT_ID)).thenReturn(Optional.of(hotelDiscountToDelete));
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> hotelDiscountService.deleteHotelDiscount(TEST_HOTEL_DISCOUNT_ID, TEST_TOKEN));
        verify(hotelDiscountRepository, times(1)).findById(TEST_HOTEL_DISCOUNT_ID);
        verify(jwtUtils, times(1)).getUserIdFromJwtToken(TEST_TOKEN);
        verify(accountRepository, times(1)).findById(TEST_ACCOUNT_ID);
        verify(hotelDiscountRepository, never()).delete(any());
    }

    @Test
    void deleteHotelDiscount_shouldThrowRuntimeException_whenNoPermission() {
        // Arrange
        Account anotherAccount = new Account();
        anotherAccount.setId(TEST_ACCOUNT_ID + 1);
        HotelDiscount hotelDiscountToDelete = new HotelDiscount();
        hotelDiscountToDelete.setId(TEST_HOTEL_DISCOUNT_ID);
        hotelDiscountToDelete.setHotel(testHotel);

        when(hotelDiscountRepository.findById(TEST_HOTEL_DISCOUNT_ID)).thenReturn(Optional.of(hotelDiscountToDelete));
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(anotherAccount.getId());
        when(accountRepository.findById(anotherAccount.getId())).thenReturn(Optional.of(anotherAccount));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> hotelDiscountService.deleteHotelDiscount(TEST_HOTEL_DISCOUNT_ID, TEST_TOKEN));
        verify(hotelDiscountRepository, times(1)).findById(TEST_HOTEL_DISCOUNT_ID);
        verify(jwtUtils, times(1)).getUserIdFromJwtToken(TEST_TOKEN);
        verify(accountRepository, times(1)).findById(anotherAccount.getId());
        verify(hotelDiscountRepository, never()).delete(any());
    }

    @Test
    void getHotelDiscounts_shouldReturnListOfHotelDiscountResponseDto() {
        // Arrange
        Discount discount1 = new Discount();
        discount1.setId(101L);
        discount1.setRate(BigDecimal.valueOf(0.15));

        HotelDiscount hotelDiscount1 = new HotelDiscount();
        hotelDiscount1.setId(1L);
        hotelDiscount1.setStartDate(now);
        hotelDiscount1.setEndDate(future);
        hotelDiscount1.setDiscount(discount1);
        
        Discount discount2 = new Discount();
        discount2.setId(102L);
        discount2.setRate(BigDecimal.valueOf(0.20));

        HotelDiscount hotelDiscount2 = new HotelDiscount();
        hotelDiscount2.setId(2L);
        hotelDiscount2.setStartDate(now.minusDays(2));
        hotelDiscount2.setEndDate(future.plusDays(2));
        hotelDiscount2.setDiscount(discount2);
        hotelDiscount2.setHotel(testHotel);

        testHotel.setHotelDiscounts(List.of(hotelDiscount1, hotelDiscount2));

        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(testHotel));

        // Act
        List<HotelDiscountResponseDto> responseDtos = hotelDiscountService.getHotelDiscounts(TEST_HOTEL_ID);

        // Assert
        assertNotNull(responseDtos);
        assertEquals(2, responseDtos.size());

        HotelDiscountResponseDto dto1 = responseDtos.getFirst();
        assertEquals(1L, dto1.getId());
        assertEquals(now, dto1.getStartDate());
        assertEquals(future, dto1.getEndDate());
        assertEquals(101L, dto1.getDiscount().getId());
        assertEquals(BigDecimal.valueOf(0.15), dto1.getDiscount().getRate());

        HotelDiscountResponseDto dto2 = responseDtos.get(1);
        assertEquals(2L, dto2.getId());
        assertEquals(now.minusDays(2), dto2.getStartDate());
        assertEquals(future.plusDays(2), dto2.getEndDate());
        assertEquals(102L, dto2.getDiscount().getId());
        assertEquals(BigDecimal.valueOf(0.20), dto2.getDiscount().getRate());
    }

    @Test
    void getHotelDiscounts_shouldThrowResourceNotFoundException_whenHotelNotFound() {
        // Arrange
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> hotelDiscountService.getHotelDiscounts(TEST_HOTEL_ID));
        verify(hotelRepository, times(1)).findById(TEST_HOTEL_ID);
    }

    @Test
    void getHotelDiscounts_shouldReturnEmptyList_whenNoDiscountsForHotel() {
        // Arrange
        testHotel.setHotelDiscounts(Collections.emptyList());
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(testHotel));

        // Act
        List<HotelDiscountResponseDto> responseDtos = hotelDiscountService.getHotelDiscounts(TEST_HOTEL_ID);

        // Assert
        assertNotNull(responseDtos);
        assertTrue(responseDtos.isEmpty());
        verify(hotelRepository, times(1)).findById(TEST_HOTEL_ID);
    }
}