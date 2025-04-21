package com.vinova.booking_hotel.service;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.authentication.service.impl.CloudinaryService;
import com.vinova.booking_hotel.common.enums.BookingStatus;
import com.vinova.booking_hotel.common.enums.EntityType;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddRatingRequestDto;
import com.vinova.booking_hotel.property.dto.response.RatingResponseDto;
import com.vinova.booking_hotel.property.model.*;
import com.vinova.booking_hotel.property.repository.BookingRepository;
import com.vinova.booking_hotel.property.repository.HotelRepository;
import com.vinova.booking_hotel.property.repository.ImageRepository;
import com.vinova.booking_hotel.property.repository.RatingRepository;
import com.vinova.booking_hotel.property.service.impl.RatingServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RatingServiceImplTest {

    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private RatingServiceImpl ratingService;

    private final String TEST_TOKEN = "test_token";
    private final Long TEST_ACCOUNT_ID = 1L;
    private final Long TEST_HOTEL_ID = 2L;
    private final Long TEST_RATING_ID = 3L;

    @Test
    void ratingsByHotelId_shouldReturnListOfRatingResponseDtoWithImagesAndAccountInfo() {
        // Arrange
        Account mockAccount = new Account(TEST_ACCOUNT_ID, "testuser", "password", "test@example.com", "Test User", null, "123456789", "avatar", LocalDateTime.now(), "refreshToken", LocalDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        District mockDistrict = new District(4L, "Test District", Collections.emptyList(), ZonedDateTime.now(), ZonedDateTime.now());
        Hotel mockHotel = new Hotel(TEST_HOTEL_ID, "Test Hotel", "highlight.jpg", "description", BigDecimal.valueOf(50.0), "123 Main St", "10.0", "20.0", mockDistrict, mockAccount, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), ZonedDateTime.now(), ZonedDateTime.now());
        Rating mockRating = new Rating(TEST_RATING_ID, "Great stay", 5, mockHotel, mockAccount, ZonedDateTime.now(), ZonedDateTime.now());
        Image mockImage = new Image(6L, TEST_RATING_ID, EntityType.REVIEW, "image_url", ZonedDateTime.now(), ZonedDateTime.now());

        when(ratingRepository.findByHotelId(TEST_HOTEL_ID)).thenReturn(Collections.singletonList(mockRating));
        when(imageRepository.findByEntityIdAndEntityType(TEST_RATING_ID, EntityType.REVIEW)).thenReturn(Collections.singletonList(mockImage));

        // Act
        List<RatingResponseDto> responseDtos = ratingService.ratingsByHotelId(TEST_HOTEL_ID);

        // Assert
        assertEquals(1, responseDtos.size());
        RatingResponseDto responseDto = responseDtos.getFirst();
        assertEquals(TEST_RATING_ID, responseDto.getId());
        assertEquals(5, responseDto.getStars());
        assertEquals("Great stay", responseDto.getContent());
        assertEquals(1, responseDto.getImages().size());
        assertEquals("image_url", responseDto.getImages().getFirst().getImageUrl());
        assertEquals(TEST_ACCOUNT_ID, responseDto.getAccount().getId());
        assertEquals("Test User", responseDto.getAccount().getFullName());
    }

    @Test
    void ratingsByHotelId_shouldReturnEmptyList_whenNoRatingsFoundForHotel() {
        // Arrange
        when(ratingRepository.findByHotelId(TEST_HOTEL_ID)).thenReturn(Collections.emptyList());

        // Act
        List<RatingResponseDto> responseDtos = ratingService.ratingsByHotelId(TEST_HOTEL_ID);

        // Assert
        assertTrue(responseDtos.isEmpty());
    }

    @Test
    void rating_shouldReturnRatingResponseDtoWithImagesAndAccountInfo_whenRatingFound() {
        // Arrange
        Account mockAccount = new Account(TEST_ACCOUNT_ID, "testuser", "password", "test@example.com", "Test User", null, "123456789", "avatar", LocalDateTime.now(), "refreshToken", LocalDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        District mockDistrict = new District(7L, "Another District", Collections.emptyList(), ZonedDateTime.now(), ZonedDateTime.now());
        Hotel mockHotel = new Hotel(TEST_HOTEL_ID, "Another Hotel", "other.jpg", "another description", BigDecimal.valueOf(75.0), "456 Oak Ave", "11.0", "21.0", mockDistrict, mockAccount, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), ZonedDateTime.now(), ZonedDateTime.now());
        Rating mockRating = new Rating(TEST_RATING_ID, "Good one", 4, mockHotel, mockAccount, ZonedDateTime.now(), ZonedDateTime.now());
        Image mockImage = new Image(8L, TEST_RATING_ID, EntityType.REVIEW, "review_image", ZonedDateTime.now(), ZonedDateTime.now());

        when(ratingRepository.findById(TEST_RATING_ID)).thenReturn(Optional.of(mockRating));
        when(imageRepository.findByEntityIdAndEntityType(TEST_RATING_ID, EntityType.REVIEW)).thenReturn(Collections.singletonList(mockImage));

        // Act
        RatingResponseDto responseDto = ratingService.rating(TEST_RATING_ID);

        // Assert
        assertEquals(TEST_RATING_ID, responseDto.getId());
        assertEquals(4, responseDto.getStars());
        assertEquals("Good one", responseDto.getContent());
        assertEquals(1, responseDto.getImages().size());
        assertEquals("review_image", responseDto.getImages().getFirst().getImageUrl());
        assertEquals(TEST_ACCOUNT_ID, responseDto.getAccount().getId());
        assertEquals("Test User", responseDto.getAccount().getFullName());
    }

    @Test
    void rating_shouldThrowResourceNotFoundException_whenRatingNotFound() {
        // Arrange
        when(ratingRepository.findById(TEST_RATING_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> ratingService.rating(TEST_RATING_ID));
    }

    @Test
    void create_shouldReturnRatingResponseDtoWithImages_whenBookingExistsAndValidStatus() {
        // Arrange
        AddRatingRequestDto requestDto = new AddRatingRequestDto(TEST_HOTEL_ID, 5, "Excellent!", Arrays.asList(
                new MockMultipartFile("image1", "image1.jpg", "image/jpeg", "content1".getBytes()),
                new MockMultipartFile("image2", "image2.png", "image/png", "content2".getBytes())
        ));
        Account mockAccount = new Account(TEST_ACCOUNT_ID, "testuser", "password", "test@example.com", "Test User", null, "123456789", "avatar", LocalDateTime.now(), "refreshToken", LocalDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        District mockDistrict = new District(9L, "Another District", Collections.emptyList(), ZonedDateTime.now(), ZonedDateTime.now());
        Hotel mockHotel = new Hotel(TEST_HOTEL_ID, "Another Hotel", "other.jpg", "another description", BigDecimal.valueOf(75.0), "456 Oak Ave", "11.0", "21.0", mockDistrict, mockAccount, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), ZonedDateTime.now(), ZonedDateTime.now());
        Booking mockBooking = new Booking(10L, ZonedDateTime.now().minusDays(5), ZonedDateTime.now().minusDays(2), BigDecimal.valueOf(150.0), BookingStatus.CHECKOUT, mockHotel, mockAccount, ZonedDateTime.now(), ZonedDateTime.now());
        Rating savedRating = new Rating(TEST_RATING_ID, "Excellent!", 5, mockHotel, mockAccount, ZonedDateTime.now(), ZonedDateTime.now());
        Image image1 = new Image(11L, TEST_RATING_ID, EntityType.REVIEW, "cloudinary_url1", ZonedDateTime.now(), ZonedDateTime.now());
        Image image2 = new Image(12L, TEST_RATING_ID, EntityType.REVIEW, "cloudinary_url2", ZonedDateTime.now(), ZonedDateTime.now());

        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(mockHotel));
        when(bookingRepository.findFirstByHotelAndAccount(mockHotel, mockAccount)).thenReturn(Optional.of(mockBooking));
        when(ratingRepository.save(any(Rating.class))).thenReturn(savedRating);
        when(cloudinaryService.uploadImage(any(MultipartFile.class))).thenReturn("cloudinary_url1", "cloudinary_url2");
        when(imageRepository.save(any(Image.class))).thenReturn(image1, image2);

        // Act
        RatingResponseDto responseDto = ratingService.create(requestDto, TEST_TOKEN);

        // Assert
        assertEquals(TEST_RATING_ID, responseDto.getId());
        assertEquals(5, responseDto.getStars());
        assertEquals("Excellent!", responseDto.getContent());
        assertEquals(2, responseDto.getImages().size());
        assertEquals("cloudinary_url1", responseDto.getImages().get(0).getImageUrl());
        assertEquals("cloudinary_url2", responseDto.getImages().get(1).getImageUrl());
        assertEquals(TEST_ACCOUNT_ID, responseDto.getAccount().getId());
        verify(ratingRepository, times(1)).save(any(Rating.class));
        verify(cloudinaryService, times(2)).uploadImage(any(MultipartFile.class));
        verify(imageRepository, times(2)).save(any(Image.class));
    }

    @Test
    void create_shouldThrowRuntimeException_whenUserHasNotBookedAtHotel() {
        // Arrange
        AddRatingRequestDto requestDto = new AddRatingRequestDto(TEST_HOTEL_ID, 4, "Nice place", Collections.emptyList());
        Account mockAccount = new Account(TEST_ACCOUNT_ID, "testuser", "password", "test@example.com", "Test User", null, "123456789", "avatar", LocalDateTime.now(), "refreshToken", LocalDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        District mockDistrict = new District(13L, "Another District", Collections.emptyList(), ZonedDateTime.now(), ZonedDateTime.now());
        Hotel mockHotel = new Hotel(TEST_HOTEL_ID, "Another Hotel", "other.jpg", "another description", BigDecimal.valueOf(75.0), "456 Oak Ave", "11.0", "21.0", mockDistrict, mockAccount, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), ZonedDateTime.now(), ZonedDateTime.now());

        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(mockHotel));
        when(bookingRepository.findFirstByHotelAndAccount(mockHotel, mockAccount)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> ratingService.create(requestDto, TEST_TOKEN));
        verify(ratingRepository, never()).save(any());
        verify(cloudinaryService, never()).uploadImage(any());
        verify(imageRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowRuntimeException_whenBookingStatusIsNotCheckoutOrCheckin() {
        // Arrange
        AddRatingRequestDto requestDto = new AddRatingRequestDto(TEST_HOTEL_ID, 3, "Okay", Collections.emptyList());
        Account mockAccount = new Account(TEST_ACCOUNT_ID, "testuser", "password", "test@example.com", "Test User", null, "123456789", "avatar", LocalDateTime.now(), "refreshToken", LocalDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        District mockDistrict = new District(14L, "Another District", Collections.emptyList(), ZonedDateTime.now(), ZonedDateTime.now());
        Hotel mockHotel = new Hotel(TEST_HOTEL_ID, "Another Hotel", "other.jpg", "another description", BigDecimal.valueOf(75.0), "456 Oak Ave", "11.0", "21.0", mockDistrict, mockAccount, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), ZonedDateTime.now(), ZonedDateTime.now());
        Booking mockBooking = new Booking(15L, ZonedDateTime.now().plusDays(1), ZonedDateTime.now().plusDays(3), BigDecimal.valueOf(150.0), BookingStatus.PENDING, mockHotel, mockAccount, ZonedDateTime.now(), ZonedDateTime.now());

        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(mockHotel));
        when(bookingRepository.findFirstByHotelAndAccount(mockHotel, mockAccount)).thenReturn(Optional.of(mockBooking));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> ratingService.create(requestDto, TEST_TOKEN));
        verify(ratingRepository, never()).save(any());
        verify(cloudinaryService, never()).uploadImage(any());
        verify(imageRepository, never()).save(any());
    }

    @Test
    void delete_shouldDeleteRating_whenUserIsOwner() {
        // Arrange
        Account mockAccount = new Account(TEST_ACCOUNT_ID, "testuser", "password", "test@example.com", "Test User", null, "123456789", "avatar", LocalDateTime.now(), "refreshToken", LocalDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        District mockDistrict = new District(16L, "Another District", Collections.emptyList(), ZonedDateTime.now(), ZonedDateTime.now());
        Hotel mockHotel = new Hotel(TEST_HOTEL_ID, "Another Hotel", "other.jpg", "another description", BigDecimal.valueOf(75.0), "456 Oak Ave", "11.0","21.0", mockDistrict, mockAccount, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), ZonedDateTime.now(), ZonedDateTime.now());
        Rating mockRating = new Rating(TEST_RATING_ID, "Okay", 3, mockHotel, mockAccount, ZonedDateTime.now(), ZonedDateTime.now());

        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));
        when(ratingRepository.findById(TEST_RATING_ID)).thenReturn(Optional.of(mockRating));

        // Act
        ratingService.delete(TEST_RATING_ID, TEST_TOKEN);

        // Assert
        verify(ratingRepository, times(1)).delete(mockRating);
    }

    @Test
    void delete_shouldThrowResourceNotFoundExceptionForAccount_whenAccountNotFound() {
        // Arrange
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> ratingService.delete(TEST_RATING_ID, TEST_TOKEN));
        verify(ratingRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowResourceNotFoundExceptionForRating_whenRatingNotFound() {
        // Arrange
        Account mockAccount = new Account(TEST_ACCOUNT_ID, "testuser", "password", "test@example.com", "Test User", null, "123456789", "avatar", LocalDateTime.now(), "refreshToken", LocalDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());

        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(mockAccount));
        when(ratingRepository.findById(TEST_RATING_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> ratingService.delete(TEST_RATING_ID, TEST_TOKEN));
        verify(ratingRepository, never()).delete(any());
    }

    @Test
    void delete_shouldThrowRuntimeException_whenUserIsNotOwner() {
        // Arrange
        Account ownerAccount = new Account(TEST_ACCOUNT_ID, "testuser", "password", "test@example.com", "Test User", null, "123456789", "avatar", LocalDateTime.now(), "refreshToken", LocalDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        Account otherAccount = new Account(99L, "otheruser", "otherpass", "other@example.com", "Other User", null, "987654321", "other_avatar", LocalDateTime.now(), "otherToken", LocalDateTime.now(), ZonedDateTime.now(), ZonedDateTime.now(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
        District mockDistrict = new District(17L, "Another District", Collections.emptyList(), ZonedDateTime.now(), ZonedDateTime.now());
        Hotel mockHotel = new Hotel(TEST_HOTEL_ID, "Another Hotel", "other.jpg", "another description", BigDecimal.valueOf(75.0), "456 Oak Ave", "11.0", "21.0", mockDistrict, ownerAccount, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), ZonedDateTime.now(), ZonedDateTime.now());
        Rating mockRating = new Rating(TEST_RATING_ID, "Okay", 3, mockHotel, ownerAccount, ZonedDateTime.now(), ZonedDateTime.now());

        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(otherAccount.getId());
        when(accountRepository.findById(otherAccount.getId())).thenReturn(Optional.of(otherAccount));
        when(ratingRepository.findById(TEST_RATING_ID)).thenReturn(Optional.of(mockRating));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> ratingService.delete(TEST_RATING_ID, TEST_TOKEN));
        verify(ratingRepository, never()).delete(any());
    }
}