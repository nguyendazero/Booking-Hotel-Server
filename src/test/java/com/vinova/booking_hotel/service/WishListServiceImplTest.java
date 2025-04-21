package com.vinova.booking_hotel.service;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.WishList;
import com.vinova.booking_hotel.property.repository.HotelRepository;
import com.vinova.booking_hotel.property.repository.WishListRepository;
import com.vinova.booking_hotel.property.service.impl.WishListServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class WishListServiceImplTest {

    @Mock
    private WishListRepository wishListRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private WishListServiceImpl wishListService;

    private final String TEST_TOKEN = "Bearer test_token";
    private final Long TEST_ACCOUNT_ID = 1L;
    private final Long TEST_HOTEL_ID = 2L;

    @Test
    void addToWishList_shouldAddHotel_whenHotelNotInWishList() {
        // Arrange
        Account mockAccount = new Account();
        Hotel mockHotel = new Hotel();
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(mockHotel));
        when(wishListRepository.findByAccountAndHotel(mockAccount, mockHotel)).thenReturn(null);

        // Act
        String result = wishListService.addToWishList(TEST_HOTEL_ID, TEST_TOKEN);

        // Assert
        assertEquals("Added to wishlist", result);
        verify(wishListRepository, times(1)).save(any(WishList.class));
    }

    @Test
    void addToWishList_shouldReturnAlreadyInWishList_whenHotelInWishList() {
        // Arrange
        Account mockAccount = new Account();
        Hotel mockHotel = new Hotel();
        WishList mockWishList = new WishList();
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(mockHotel));
        when(wishListRepository.findByAccountAndHotel(mockAccount, mockHotel)).thenReturn(mockWishList);

        // Act
        String result = wishListService.addToWishList(TEST_HOTEL_ID, TEST_TOKEN);

        // Assert
        assertEquals("Hotel already in wishlist", result);
        verify(wishListRepository, never()).save(any(WishList.class));
    }

    @Test
    void addToWishList_shouldThrowResourceNotFoundException_whenAccountNotFound() {
        // Arrange
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> wishListService.addToWishList(TEST_HOTEL_ID, TEST_TOKEN));
        verify(hotelRepository, never()).findById(anyLong());
        verify(wishListRepository, never()).findByAccountAndHotel(any(), any());
        verify(wishListRepository, never()).save(any(WishList.class));
    }

    @Test
    void addToWishList_shouldThrowResourceNotFoundException_whenHotelNotFound() {
        // Arrange
        Account mockAccount = new Account();
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> wishListService.addToWishList(TEST_HOTEL_ID, TEST_TOKEN));
        verify(wishListRepository, never()).findByAccountAndHotel(any(), any());
        verify(wishListRepository, never()).save(any(WishList.class));
    }

    @Test
    void removeFromWishList_shouldRemoveHotel_whenHotelInWishList() {
        // Arrange
        Account mockAccount = new Account();
        Hotel mockHotel = new Hotel();
        WishList mockWishList = new WishList();
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(mockHotel));
        when(wishListRepository.findByAccountAndHotel(mockAccount, mockHotel)).thenReturn(mockWishList);

        // Act
        String result = wishListService.removeFromWishList(TEST_HOTEL_ID, TEST_TOKEN);

        // Assert
        assertEquals("Removed from wishlist", result);
        verify(wishListRepository, times(1)).delete(mockWishList);
    }

    @Test
    void removeFromWishList_shouldReturnNotFoundInWishList_whenHotelNotInWishList() {
        // Arrange
        Account mockAccount = new Account();
        Hotel mockHotel = new Hotel();
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(mockHotel));
        when(wishListRepository.findByAccountAndHotel(mockAccount, mockHotel)).thenReturn(null);

        // Act
        String result = wishListService.removeFromWishList(TEST_HOTEL_ID, TEST_TOKEN);

        // Assert
        assertEquals("Hotel not found in wishlist", result);
        verify(wishListRepository, never()).delete(any(WishList.class));
    }

    @Test
    void removeFromWishList_shouldThrowResourceNotFoundException_whenAccountNotFound() {
        // Arrange
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> wishListService.removeFromWishList(TEST_HOTEL_ID, TEST_TOKEN));
        verify(hotelRepository, never()).findById(anyLong());
        verify(wishListRepository, never()).findByAccountAndHotel(any(), any());
        verify(wishListRepository, never()).delete(any(WishList.class));
    }

    @Test
    void removeFromWishList_shouldThrowResourceNotFoundException_whenHotelNotFound() {
        // Arrange
        Account mockAccount = new Account();
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> wishListService.removeFromWishList(TEST_HOTEL_ID, TEST_TOKEN));
        verify(wishListRepository, never()).findByAccountAndHotel(any(), any());
        verify(wishListRepository, never()).delete(any(WishList.class));
    }
}