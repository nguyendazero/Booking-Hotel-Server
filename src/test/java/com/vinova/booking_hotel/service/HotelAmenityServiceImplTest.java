package com.vinova.booking_hotel.service;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddAmenityToHotelRequestDto;
import com.vinova.booking_hotel.property.dto.request.DeleteAmenityFromHotelRequestDto;
import com.vinova.booking_hotel.property.model.Amenity;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.HotelAmenity;
import com.vinova.booking_hotel.property.repository.AmenityRepository;
import com.vinova.booking_hotel.property.repository.HotelAmenityRepository;
import com.vinova.booking_hotel.property.repository.HotelRepository;
import com.vinova.booking_hotel.property.service.impl.HotelAmenityServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HotelAmenityServiceImplTest {

    @Mock
    private HotelAmenityRepository hotelAmenityRepository;
    @Mock
    private AmenityRepository amenityRepository;
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private HotelAmenityServiceImpl hotelAmenityService;

    private final String TEST_TOKEN = "test_token";
    private final Long TEST_HOTEL_ID = 1L;
    private final Long TEST_ACCOUNT_ID = 2L;
    private final Long TEST_AMENITY_ID = 3L;
    private Hotel testHotel;
    private Account testAccount;
    private Amenity existingAmenity;
    private Amenity newAmenity;
    private HotelAmenity hotelAmenityRelation;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setId(TEST_ACCOUNT_ID);

        testHotel = new Hotel();
        testHotel.setId(TEST_HOTEL_ID);
        testHotel.setAccount(testAccount);

        existingAmenity = new Amenity();
        existingAmenity.setId(TEST_AMENITY_ID);
        existingAmenity.setName("Existing Amenity");

        newAmenity = new Amenity();
        newAmenity.setId(TEST_AMENITY_ID + 1);
        newAmenity.setName("New Amenity");

        hotelAmenityRelation = new HotelAmenity();
        hotelAmenityRelation.setHotel(testHotel);
        hotelAmenityRelation.setAmenity(existingAmenity);
    }

    @Test
    void addAmenityToHotel_shouldAddExistingAmenity() {
        // Arrange
        AddAmenityToHotelRequestDto requestDto = new AddAmenityToHotelRequestDto(TEST_HOTEL_ID, "Existing Amenity");
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(testAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(testHotel));
        when(amenityRepository.findByName("Existing Amenity")).thenReturn(existingAmenity);
        when(hotelAmenityRepository.save(any(HotelAmenity.class))).thenReturn(hotelAmenityRelation);

        // Act
        String response = hotelAmenityService.addAmenityToHotel(requestDto, TEST_TOKEN);

        // Assert
        assertEquals("Amenity added to hotel", response);
        verify(hotelRepository, times(1)).findById(TEST_HOTEL_ID);
        verify(accountRepository, times(1)).findById(TEST_ACCOUNT_ID);
        verify(amenityRepository, times(1)).findByName("Existing Amenity");
        verify(amenityRepository, never()).save(any(Amenity.class));
        verify(hotelAmenityRepository, times(1)).save(any(HotelAmenity.class));
    }

    @Test
    void addAmenityToHotel_shouldAddNewAmenity() {
        // Arrange
        AddAmenityToHotelRequestDto requestDto = new AddAmenityToHotelRequestDto(TEST_HOTEL_ID, "New Amenity");
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(testAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(testHotel));
        when(amenityRepository.findByName("New Amenity")).thenReturn(null);
        when(amenityRepository.save(any(Amenity.class))).thenReturn(newAmenity);
        when(hotelAmenityRepository.save(any(HotelAmenity.class))).thenReturn(hotelAmenityRelation);

        // Act
        String response = hotelAmenityService.addAmenityToHotel(requestDto, TEST_TOKEN);

        // Assert
        assertEquals("Amenity added to hotel", response);
        verify(hotelRepository, times(1)).findById(TEST_HOTEL_ID);
        verify(accountRepository, times(1)).findById(TEST_ACCOUNT_ID);
        verify(amenityRepository, times(1)).findByName("New Amenity");
        verify(amenityRepository, times(1)).save(any(Amenity.class));
        verify(hotelAmenityRepository, times(1)).save(any(HotelAmenity.class));
    }

    @Test
    void addAmenityToHotel_shouldThrowResourceNotFoundException_whenHotelNotFound() {
        // Arrange
        AddAmenityToHotelRequestDto requestDto = new AddAmenityToHotelRequestDto(TEST_HOTEL_ID, "Existing Amenity");
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> hotelAmenityService.addAmenityToHotel(requestDto, TEST_TOKEN));
        verify(jwtUtils, never()).getUserIdFromJwtToken(anyString());
        verify(accountRepository, never()).findById(anyLong());
        verify(amenityRepository, never()).findByName(anyString());
        verify(amenityRepository, never()).save(any(Amenity.class));
        verify(hotelAmenityRepository, never()).save(any(HotelAmenity.class));
    }

    @Test
    void addAmenityToHotel_shouldThrowResourceNotFoundException_whenAccountNotFound() {
        // Arrange
        AddAmenityToHotelRequestDto requestDto = new AddAmenityToHotelRequestDto(TEST_HOTEL_ID, "Existing Amenity");
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.empty());
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(testHotel));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> hotelAmenityService.addAmenityToHotel(requestDto, TEST_TOKEN));
        verify(hotelRepository, times(1)).findById(TEST_HOTEL_ID);
        verify(amenityRepository, never()).findByName(anyString());
        verify(amenityRepository, never()).save(any(Amenity.class));
        verify(hotelAmenityRepository, never()).save(any(HotelAmenity.class));
    }

    @Test
    void addAmenityToHotel_shouldThrowRuntimeException_whenNoPermission() {
        // Arrange
        Account anotherAccount = new Account();
        anotherAccount.setId(TEST_ACCOUNT_ID + 1);
        AddAmenityToHotelRequestDto requestDto = new AddAmenityToHotelRequestDto(TEST_HOTEL_ID, "Existing Amenity");
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(anotherAccount.getId());
        when(accountRepository.findById(anotherAccount.getId())).thenReturn(Optional.of(anotherAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(testHotel));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> hotelAmenityService.addAmenityToHotel(requestDto, TEST_TOKEN));
        verify(hotelRepository, times(1)).findById(TEST_HOTEL_ID);
        verify(amenityRepository, never()).findByName(anyString());
        verify(amenityRepository, never()).save(any(Amenity.class));
        verify(hotelAmenityRepository, never()).save(any(HotelAmenity.class));
    }

    @Test
    void removeAmenityFromHotel_shouldRemoveExistingAmenityRelation() {
        // Arrange
        DeleteAmenityFromHotelRequestDto requestDto = new DeleteAmenityFromHotelRequestDto(TEST_HOTEL_ID, TEST_AMENITY_ID);
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(testAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(testHotel));
        when(amenityRepository.findById(TEST_AMENITY_ID)).thenReturn(Optional.of(existingAmenity));
        when(hotelAmenityRepository.findByHotelAndAmenity(testHotel, existingAmenity)).thenReturn(Optional.of(hotelAmenityRelation));

        // Act
        String response = hotelAmenityService.removeAmenityFromHotel(requestDto, TEST_TOKEN);

        // Assert
        assertEquals("Amenity removed from hotel", response);
        verify(hotelRepository, times(1)).findById(TEST_HOTEL_ID);
        verify(accountRepository, times(1)).findById(TEST_ACCOUNT_ID);
        verify(amenityRepository, times(1)).findById(TEST_AMENITY_ID);
        verify(hotelAmenityRepository, times(1)).findByHotelAndAmenity(testHotel, existingAmenity);
        verify(hotelAmenityRepository, times(1)).delete(hotelAmenityRelation);
    }

    @Test
    void removeAmenityFromHotel_shouldThrowResourceNotFoundException_whenHotelNotFound() {
        // Arrange
        DeleteAmenityFromHotelRequestDto requestDto = new DeleteAmenityFromHotelRequestDto(TEST_HOTEL_ID, TEST_AMENITY_ID);
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> hotelAmenityService.removeAmenityFromHotel(requestDto, TEST_TOKEN));
        verify(jwtUtils, never()).getUserIdFromJwtToken(anyString());
        verify(accountRepository, never()).findById(anyLong());
        verify(amenityRepository, never()).findById(anyLong());
        verify(hotelAmenityRepository, never()).findByHotelAndAmenity(any(), any());
        verify(hotelAmenityRepository, never()).delete(any());
    }

    @Test
    void removeAmenityFromHotel_shouldThrowResourceNotFoundException_whenAccountNotFound() {
        // Arrange
        DeleteAmenityFromHotelRequestDto requestDto = new DeleteAmenityFromHotelRequestDto(TEST_HOTEL_ID, TEST_AMENITY_ID);
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.empty());
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(testHotel));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> hotelAmenityService.removeAmenityFromHotel(requestDto, TEST_TOKEN));
        verify(hotelRepository, times(1)).findById(TEST_HOTEL_ID);
        verify(amenityRepository, never()).findById(anyLong());
        verify(hotelAmenityRepository, never()).findByHotelAndAmenity(any(), any());
        verify(hotelAmenityRepository, never()).delete(any());
    }

    @Test
    void removeAmenityFromHotel_shouldThrowRuntimeException_whenNoPermission() {
        // Arrange
        Account anotherAccount = new Account();
        anotherAccount.setId(TEST_ACCOUNT_ID + 1);
        DeleteAmenityFromHotelRequestDto requestDto = new DeleteAmenityFromHotelRequestDto(TEST_HOTEL_ID, TEST_AMENITY_ID);
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(anotherAccount.getId());
        when(accountRepository.findById(anotherAccount.getId())).thenReturn(Optional.of(anotherAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(testHotel));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> hotelAmenityService.removeAmenityFromHotel(requestDto, TEST_TOKEN));
        verify(hotelRepository, times(1)).findById(TEST_HOTEL_ID);
        verify(amenityRepository, never()).findById(anyLong());
        verify(hotelAmenityRepository, never()).findByHotelAndAmenity(any(), any());
        verify(hotelAmenityRepository, never()).delete(any());
    }

    @Test
    void removeAmenityFromHotel_shouldThrowResourceNotFoundException_whenAmenityNotFound() {
        // Arrange
        DeleteAmenityFromHotelRequestDto requestDto = new DeleteAmenityFromHotelRequestDto(TEST_HOTEL_ID, TEST_AMENITY_ID);
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(testAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(testHotel));
        when(amenityRepository.findById(TEST_AMENITY_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> hotelAmenityService.removeAmenityFromHotel(requestDto, TEST_TOKEN));
        verify(hotelRepository, times(1)).findById(TEST_HOTEL_ID);
        verify(amenityRepository, times(1)).findById(TEST_AMENITY_ID);
        verify(hotelAmenityRepository, never()).findByHotelAndAmenity(any(), any());
        verify(hotelAmenityRepository, never()).delete(any());
    }

    @Test
    void removeAmenityFromHotel_shouldThrowResourceNotFoundException_whenHotelAmenityNotFound() {
        // Arrange
        DeleteAmenityFromHotelRequestDto requestDto = new DeleteAmenityFromHotelRequestDto(TEST_HOTEL_ID, TEST_AMENITY_ID);
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(testAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(testHotel));
        when(amenityRepository.findById(TEST_AMENITY_ID)).thenReturn(Optional.of(existingAmenity));
        when(hotelAmenityRepository.findByHotelAndAmenity(testHotel, existingAmenity)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> hotelAmenityService.removeAmenityFromHotel(requestDto, TEST_TOKEN));
        verify(hotelRepository, times(1)).findById(TEST_HOTEL_ID);
        verify(amenityRepository, times(1)).findById(TEST_AMENITY_ID);
        verify(hotelAmenityRepository, times(1)).findByHotelAndAmenity(testHotel, existingAmenity);
        verify(hotelAmenityRepository, never()).delete(any());
    }
}