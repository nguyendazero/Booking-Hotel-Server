package com.vinova.booking_hotel.service;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.authentication.service.impl.CloudinaryService;
import com.vinova.booking_hotel.common.enums.EntityType;
import com.vinova.booking_hotel.common.exception.InvalidPageOrSizeException;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddHotelRequestDto;
import com.vinova.booking_hotel.property.dto.request.AddImagesRequestDto;
import com.vinova.booking_hotel.property.dto.response.HotelResponseDto;
import com.vinova.booking_hotel.property.dto.response.ImageResponseDto;
import com.vinova.booking_hotel.property.model.*;
import com.vinova.booking_hotel.property.repository.*;
import com.vinova.booking_hotel.property.service.impl.HotelServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private DistrictRepository districtRepository;
    @Mock
    private CloudinaryService cloudinaryService;
    @Mock
    private RatingRepository ratingRepository;
    @Mock
    private ImageRepository imageRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private HotelDiscountRepository hotelDiscountRepository;
    @Mock
    private HotelAmenityRepository hotelAmenityRepository;
    @Mock
    private WishListRepository wishListRepository;

    @InjectMocks
    private HotelServiceImpl hotelService;

    private Account testAccount;
    private District testDistrict;
    private Hotel testHotel1;
    private Hotel testHotel2;
    private String testToken = "test-token";
    private Long testAccountId = 1L;
    private Long testHotelId = 1L;
    private Long testDistrictId = 1L;

    @BeforeEach
    void setUp() {
        testAccount = new Account();
        testAccount.setId(testAccountId);
        testAccount.setUsername("testuser");

        testDistrict = new District();
        testDistrict.setId(testDistrictId);
        testDistrict.setName("Test District");

        testHotel1 = new Hotel();
        testHotel1.setId(testHotelId);
        testHotel1.setName("Test Hotel 1");
        testHotel1.setAccount(testAccount);
        testHotel1.setDistrict(testDistrict);

        testHotel2 = new Hotel();
        testHotel2.setId(2L);
        testHotel2.setName("Test Hotel 2");
        testHotel2.setAccount(testAccount);
        testHotel2.setDistrict(testDistrict);
    }

    @Test
    void hotels_withInvalidPagination_shouldThrowException() {
        assertThrows(InvalidPageOrSizeException.class, () ->
                hotelService.hotels(null, null, null, null, null, null, null, null, -1, 10, "name", "asc")
        );
        assertThrows(InvalidPageOrSizeException.class, () ->
                hotelService.hotels(null, null, null, null, null, null, null, null, 0, 0, "name", "asc")
        );
    }

    @Test
    void hotels_shouldReturnListOfHotelResponseDto() {
        List<Hotel> hotels = List.of(testHotel1, testHotel2);
        Page<Hotel> hotelPage = new PageImpl<>(hotels);
        when(hotelRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(hotelPage);
        when(hotelRepository.findAverageRatingByHotelId(anyLong())).thenReturn(4.5);
        when(ratingRepository.countByHotel(any(Hotel.class))).thenReturn(10L);
        when(imageRepository.findByEntityIdAndEntityType(anyLong(), eq(EntityType.HOTEL))).thenReturn(new ArrayList<>());
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(testAccount));

        List<HotelResponseDto> response = hotelService.hotels(null, null, null, null, null, null, null, null, 0, 10, "name", "asc");

        assertEquals(2, response.size());
        assertEquals(testHotel1.getId(), response.get(0).getId());
        assertEquals(testHotel2.getId(), response.get(1).getId());
    }

    @Test
    void wishlist_shouldReturnListOfHotelResponseDto() {
        when(jwtUtils.getUserIdFromJwtToken(testToken)).thenReturn(testAccountId);
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        WishList wish1 = new WishList();
        wish1.setHotel(testHotel1);
        WishList wish2 = new WishList();
        wish2.setHotel(testHotel2);
        testAccount.setWishList(List.of(wish1, wish2));
        when(hotelRepository.findAllById(anyList())).thenReturn(List.of(testHotel1, testHotel2));
        when(hotelRepository.findAverageRatingByHotelId(testHotelId)).thenReturn(4.0);
        when(hotelRepository.findAverageRatingByHotelId(2L)).thenReturn(3.5);
        when(ratingRepository.countByHotel(testHotel1)).thenReturn(5L);
        when(ratingRepository.countByHotel(testHotel2)).thenReturn(7L);
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));

        List<HotelResponseDto> response = hotelService.wishlist(testToken);

        assertEquals(2, response.size());
        assertEquals(testHotel1.getId(), response.get(0).getId());
        assertEquals(testHotel2.getId(), response.get(1).getId());
    }

    @Test
    void hotel_shouldReturnHotelResponseDtoWithDetails() {
        when(hotelRepository.findById(testHotelId)).thenReturn(Optional.of(testHotel1));
        when(hotelRepository.findAverageRatingByHotelId(testHotelId)).thenReturn(4.2);
        when(ratingRepository.countByHotel(testHotel1)).thenReturn(15L);
        when(bookingRepository.findByHotelId(testHotelId)).thenReturn(new ArrayList<>());
        when(imageRepository.findByEntityIdAndEntityType(testHotelId, EntityType.HOTEL)).thenReturn(new ArrayList<>());
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));

        HotelResponseDto response = hotelService.hotel(testHotelId);

        assertNotNull(response);
        assertEquals(testHotel1.getId(), response.getId());
    }

    @Test
    void hotel_withNonExistingId_shouldThrowException() {
        when(hotelRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> hotelService.hotel(999L));
    }

    @Test
    void create_shouldReturnCreatedHotelResponseDto() {
        AddHotelRequestDto requestDto = new AddHotelRequestDto();
        requestDto.setName("New Hotel");
        requestDto.setDistrictId(testDistrictId);

        when(jwtUtils.getUserIdFromJwtToken(testToken)).thenReturn(testAccountId);
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(districtRepository.findById(testDistrictId)).thenReturn(Optional.of(testDistrict));
        when(cloudinaryService.uploadImage(any())).thenReturn("image-url");

        // Tạo một Hotel mới để trả về khi save được gọi
        Hotel savedHotel = new Hotel();
        savedHotel.setId(3L); // Một ID mới
        savedHotel.setName(requestDto.getName());
        savedHotel.setAccount(testAccount);
        savedHotel.setDistrict(testDistrict);
        savedHotel.setHighLightImageUrl("image-url"); // Giả định image url đã được tải lên

        when(hotelRepository.save(any(Hotel.class))).thenReturn(savedHotel);
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount)); // Cần thiết cho việc tạo AccountResponseDto

        HotelResponseDto response = hotelService.create(requestDto, testToken);

        assertNotNull(response);
        assertEquals(savedHotel.getId(), response.getId());
        assertEquals(requestDto.getName(), response.getName());
    }

    @Test
    void update_shouldUpdateHotelSuccessfully() {
        AddHotelRequestDto requestDto = new AddHotelRequestDto();
        requestDto.setName("Updated Hotel");
        when(jwtUtils.getUserIdFromJwtToken(testToken)).thenReturn(testAccountId);
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(hotelRepository.findById(testHotelId)).thenReturn(Optional.of(testHotel1));
        when(hotelRepository.save(any(Hotel.class))).thenReturn(testHotel1);

        hotelService.update(testHotelId, requestDto, testToken);

        verify(hotelRepository, times(1)).save(any(Hotel.class));
        assertEquals("Updated Hotel", testHotel1.getName());
    }

    @Test
    void update_withNonExistingHotel_shouldThrowException() {
        AddHotelRequestDto requestDto = new AddHotelRequestDto();
        when(jwtUtils.getUserIdFromJwtToken(testToken)).thenReturn(testAccountId);
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(hotelRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> hotelService.update(999L, requestDto, testToken));
    }

    @Test
    void update_withUnauthorizedAccount_shouldThrowException() {
        Account otherAccount = new Account();
        otherAccount.setId(99L);
        AddHotelRequestDto requestDto = new AddHotelRequestDto();
        when(jwtUtils.getUserIdFromJwtToken(testToken)).thenReturn(testAccountId);
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(otherAccount));
        when(hotelRepository.findById(testHotelId)).thenReturn(Optional.of(testHotel1));

        assertThrows(RuntimeException.class, () -> hotelService.update(testHotelId, requestDto, testToken));
    }

    @Test
    void delete_shouldDeleteHotelAndRelatedEntitiesSuccessfully() {
        when(jwtUtils.getUserIdFromJwtToken(testToken)).thenReturn(testAccountId);
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(hotelRepository.findById(testHotelId)).thenReturn(Optional.of(testHotel1));

        hotelService.delete(testHotelId, testToken);

        verify(bookingRepository, times(1)).deleteBookingsByHotelId(testHotelId);
        verify(hotelDiscountRepository, times(1)).deleteDiscountsByHotelId(testHotelId);
        verify(wishListRepository, times(1)).deleteWishListsByHotelId(testHotelId);
        verify(hotelAmenityRepository, times(1)).deleteAmenitiesByHotelId(testHotelId);
        verify(ratingRepository, times(1)).deleteRatingsByHotelId(testHotelId);
        verify(hotelRepository, times(1)).deleteHotelById(testHotelId);
    }

    @Test
    void delete_withNonExistingHotel_shouldThrowException() {
        when(jwtUtils.getUserIdFromJwtToken(testToken)).thenReturn(testAccountId);
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(hotelRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> hotelService.delete(999L, testToken));
    }

    @Test
    void delete_withUnauthorizedAccount_shouldThrowException() {
        Account otherAccount = new Account();
        otherAccount.setId(99L);
        when(jwtUtils.getUserIdFromJwtToken(testToken)).thenReturn(testAccountId);
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(otherAccount));
        when(hotelRepository.findById(testHotelId)).thenReturn(Optional.of(testHotel1));

        assertThrows(RuntimeException.class, () -> hotelService.delete(testHotelId, testToken));
    }

    @Test
    void addImages_shouldAddImagesToHotel() {
        when(jwtUtils.getUserIdFromJwtToken(testToken)).thenReturn(testAccountId);
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(hotelRepository.findById(testHotelId)).thenReturn(Optional.of(testHotel1));
        when(cloudinaryService.uploadImage(any(MultipartFile.class))).thenReturn("image-url");
        when(imageRepository.save(any(Image.class))).thenReturn(new Image());

        AddImagesRequestDto requestDto = new AddImagesRequestDto();
        requestDto.setImageUrls(List.of(mock(MultipartFile.class), mock(MultipartFile.class)));

        List<ImageResponseDto> response = hotelService.addImages(testHotelId, requestDto, testToken);

        assertEquals(2, response.size());
        verify(imageRepository, times(2)).save(any(Image.class));
    }

    @Test
    void addImages_withNonExistingHotel_shouldThrowException() {
        when(jwtUtils.getUserIdFromJwtToken(testToken)).thenReturn(testAccountId);
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(hotelRepository.findById(anyLong())).thenReturn(Optional.empty());
        AddImagesRequestDto requestDto = new AddImagesRequestDto();
        requestDto.setImageUrls(List.of(mock(MultipartFile.class)));

        assertThrows(ResourceNotFoundException.class, () -> hotelService.addImages(999L, requestDto, testToken));
    }

    @Test
    void addImages_withUnauthorizedAccount_shouldThrowException() {
        Account otherAccount = new Account();
        otherAccount.setId(99L);
        when(jwtUtils.getUserIdFromJwtToken(testToken)).thenReturn(testAccountId);
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(otherAccount));
        when(hotelRepository.findById(testHotelId)).thenReturn(Optional.of(testHotel1));
        AddImagesRequestDto requestDto = new AddImagesRequestDto();
        requestDto.setImageUrls(List.of(mock(MultipartFile.class)));

        assertThrows(RuntimeException.class, () -> hotelService.addImages(testHotelId, requestDto, testToken));
    }

    @Test
    void deleteImages_shouldDeleteImagesSuccessfully() {
        when(jwtUtils.getUserIdFromJwtToken(testToken)).thenReturn(testAccountId);
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(hotelRepository.findById(testHotelId)).thenReturn(Optional.of(testHotel1));
        Image image1 = new Image();
        image1.setId(101L);
        image1.setEntityId(testHotelId);
        image1.setEntityType(EntityType.HOTEL);
        Image image2 = new Image();
        image2.setId(102L);
        image2.setEntityId(testHotelId);
        image2.setEntityType(EntityType.HOTEL);
        List<Image> imagesToDelete = List.of(image1, image2);
        when(imageRepository.findAllById(List.of(101L, 102L))).thenReturn(imagesToDelete);

        hotelService.deleteImages(testHotelId, List.of(101L, 102L), testToken);

        verify(imageRepository, times(1)).deleteAll(imagesToDelete);
    }

    @Test
    void deleteImages_withNonExistingHotel_shouldThrowException() {
        when(jwtUtils.getUserIdFromJwtToken(testToken)).thenReturn(testAccountId);
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(hotelRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> hotelService.deleteImages(999L, List.of(101L), testToken));
    }

    @Test
    void deleteImages_withUnauthorizedAccount_shouldThrowException() {
        Account otherAccount = new Account();
        otherAccount.setId(99L);
        when(jwtUtils.getUserIdFromJwtToken(testToken)).thenReturn(testAccountId);
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(otherAccount));
        when(hotelRepository.findById(testHotelId)).thenReturn(Optional.of(testHotel1));

        assertThrows(RuntimeException.class, () -> hotelService.deleteImages(testHotelId, List.of(101L), testToken));
    }

    @Test
    void deleteImages_withImageNotBelongingToHotel_shouldThrowException() {
        when(jwtUtils.getUserIdFromJwtToken(testToken)).thenReturn(testAccountId);
        when(accountRepository.findById(testAccountId)).thenReturn(Optional.of(testAccount));
        when(hotelRepository.findById(testHotelId)).thenReturn(Optional.of(testHotel1));
        Image image = new Image();
        image.setId(101L);
        image.setEntityId(999L); // Different hotel ID
        image.setEntityType(EntityType.HOTEL);
        when(imageRepository.findAllById(List.of(101L))).thenReturn(List.of(image));

        assertThrows(RuntimeException.class, () -> hotelService.deleteImages(testHotelId, List.of(101L), testToken));
    }
}
