package com.vinova.booking_hotel.service;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.common.enums.BookingStatus;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.payment.dto.PaymentRequestDto;
import com.vinova.booking_hotel.payment.dto.StripeResponseDto;
import com.vinova.booking_hotel.payment.service.StripeService;
import com.vinova.booking_hotel.property.dto.request.AddBookingRequestDto;
import com.vinova.booking_hotel.property.dto.response.BookingResponseDto;
import com.vinova.booking_hotel.property.model.Booking;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.repository.BookingRepository;
import com.vinova.booking_hotel.property.repository.HotelDiscountRepository;
import com.vinova.booking_hotel.property.repository.HotelRepository;
import com.vinova.booking_hotel.property.service.impl.BookingServiceImpl; // Import implementation class
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private HotelRepository hotelRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private HotelDiscountRepository hotelDiscountRepository;
    @Mock
    private StripeService stripeService;

    @InjectMocks
    private BookingServiceImpl bookingService; // Thay đổi kiểu ở đây

    private final Long TEST_ACCOUNT_ID = 1L;
    private final Long TEST_HOTEL_ID = 2L;
    private final Long TEST_BOOKING_ID = 3L;
    private final String TEST_TOKEN = "test_token";

    private Account createTestAccount() {
        Account account = new Account();
        account.setId(TEST_ACCOUNT_ID);
        return account;
    }

    private Hotel createTestHotel() {
        Hotel hotel = new Hotel();
        hotel.setId(TEST_HOTEL_ID);
        hotel.setName("Test Hotel");
        hotel.setPricePerDay(BigDecimal.valueOf(50));
        return hotel;
    }

    private Booking createTestBooking() {
        Booking booking = new Booking();
        booking.setId(TEST_BOOKING_ID);
        booking.setStartDate(ZonedDateTime.now().plusDays(1));
        booking.setEndDate(ZonedDateTime.now().plusDays(3));
        booking.setTotalPrice(BigDecimal.valueOf(100));
        booking.setStatus(BookingStatus.PENDING);
        booking.setHotel(createTestHotel());
        booking.setAccount(createTestAccount());
        return booking;
    }

    private AddBookingRequestDto createAddBookingRequestDto() {
        AddBookingRequestDto requestDto = new AddBookingRequestDto();
        requestDto.setHotelId(TEST_HOTEL_ID);
        requestDto.setStartDate(ZonedDateTime.now().plusDays(1));
        requestDto.setEndDate(ZonedDateTime.now().plusDays(3));
        return requestDto;
    }

    @Test
    void createBooking_success() {
        // Arrange
        AddBookingRequestDto requestDto = createAddBookingRequestDto();
        Account account = createTestAccount();
        Hotel hotel = createTestHotel();
        Booking savedBooking = createTestBooking();
        StripeResponseDto stripeResponseDto = new StripeResponseDto();
        stripeResponseDto.setSessionId("session_id");
        stripeResponseDto.setSessionUrl("session_url");

        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(hotel));
        when(bookingRepository.findByHotelIdAndDateRange(eq(TEST_HOTEL_ID), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(List.of());
        when(hotelDiscountRepository.findByHotelId(TEST_HOTEL_ID)).thenReturn(List.of());
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(stripeService.checkoutBooking(any(PaymentRequestDto.class), eq(TEST_BOOKING_ID))).thenReturn(stripeResponseDto);

        // Act
        StripeResponseDto response = bookingService.createBooking(requestDto, TEST_TOKEN);

        // Assert
        assertNotNull(response);
        assertNotNull(response.getBooking());
        assertEquals(savedBooking.getId(), response.getBooking().getId());
        assertEquals("session_id", response.getSessionId());
        assertEquals("session_url", response.getSessionUrl());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(stripeService, times(1)).checkoutBooking(any(PaymentRequestDto.class), eq(TEST_BOOKING_ID));
    }

    @Test
    void createBooking_accountNotFound() {
        // Arrange
        AddBookingRequestDto requestDto = createAddBookingRequestDto();
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(requestDto, TEST_TOKEN));
        verify(bookingRepository, times(0)).save(any(Booking.class));
        verify(stripeService, times(0)).checkoutBooking(any(PaymentRequestDto.class), anyLong());
    }

    @Test
    void createBooking_hotelNotFound() {
        // Arrange
        AddBookingRequestDto requestDto = createAddBookingRequestDto();
        Account account = createTestAccount();

        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bookingService.createBooking(requestDto, TEST_TOKEN));
        verify(bookingRepository, times(0)).save(any(Booking.class));
        verify(stripeService, times(0)).checkoutBooking(any(PaymentRequestDto.class), anyLong());
    }

    @Test
    void createBooking_startDateAfterEndDate() {
        // Arrange
        AddBookingRequestDto requestDto = createAddBookingRequestDto();
        requestDto.setStartDate(ZonedDateTime.now().plusDays(3));
        requestDto.setEndDate(ZonedDateTime.now().plusDays(1));
        Account account = createTestAccount();
        Hotel hotel = createTestHotel();

        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(hotel));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> bookingService.createBooking(requestDto, TEST_TOKEN));
        verify(bookingRepository, times(0)).save(any(Booking.class));
        verify(stripeService, times(0)).checkoutBooking(any(PaymentRequestDto.class), anyLong());
    }

    @Test
    void createBooking_bookingDatesInThePast() {
        // Arrange
        AddBookingRequestDto requestDto = createAddBookingRequestDto();
        requestDto.setStartDate(ZonedDateTime.now().minusDays(1));
        requestDto.setEndDate(ZonedDateTime.now().plusDays(1));
        Account account = createTestAccount();
        Hotel hotel = createTestHotel();

        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(hotel));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> bookingService.createBooking(requestDto, TEST_TOKEN));
        verify(bookingRepository, times(0)).save(any(Booking.class));
        verify(stripeService, times(0)).checkoutBooking(any(PaymentRequestDto.class), anyLong());
    }

    @Test
    void createBooking_hotelAlreadyBooked() {
        // Arrange
        AddBookingRequestDto requestDto = createAddBookingRequestDto();
        Account account = createTestAccount();
        Hotel hotel = createTestHotel();
        Booking existingBooking = createTestBooking();
        existingBooking.setStatus(BookingStatus.CONFIRMED);

        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(hotel));
        when(bookingRepository.findByHotelIdAndDateRange(eq(TEST_HOTEL_ID), any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(List.of(existingBooking));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> bookingService.createBooking(requestDto, TEST_TOKEN));
        verify(bookingRepository, times(0)).save(any(Booking.class));
        verify(stripeService, times(0)).checkoutBooking(any(PaymentRequestDto.class), anyLong());
    }

    @Test
    void cancelBooking_success() {
        // Arrange
        Account account = createTestAccount();
        Booking booking = createTestBooking();

        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(bookingRepository.findById(TEST_BOOKING_ID)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        // Act
        bookingService.cancelBooking(TEST_BOOKING_ID, TEST_TOKEN);

        // Assert
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void cancelBooking_accountNotFound() {
        // Arrange
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bookingService.cancelBooking(TEST_BOOKING_ID, TEST_TOKEN));
        verify(bookingRepository, times(0)).save(any(Booking.class));
    }

    @Test
    void cancelBooking_bookingNotFound() {
        // Arrange
        Account account = createTestAccount();
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(bookingRepository.findById(TEST_BOOKING_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bookingService.cancelBooking(TEST_BOOKING_ID, TEST_TOKEN));
        verify(bookingRepository, times(0)).save(any(Booking.class));
    }

    @Test
    void cancelBooking_permissionDenied() {
        // Arrange
        Account ownerAccount = createTestAccount();
        Account otherAccount = new Account();
        otherAccount.setId(99L);
        Booking booking = createTestBooking();
        booking.setAccount(ownerAccount);

        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(otherAccount.getId());
        when(accountRepository.findById(otherAccount.getId())).thenReturn(Optional.of(otherAccount));
        when(bookingRepository.findById(TEST_BOOKING_ID)).thenReturn(Optional.of(booking));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> bookingService.cancelBooking(TEST_BOOKING_ID, TEST_TOKEN));
        verify(bookingRepository, times(0)).save(any(Booking.class));
    }

    @Test
    void cancelBooking_invalidStatus() {
        // Arrange
        Account account = createTestAccount();
        Booking booking = createTestBooking();
        booking.setStatus(BookingStatus.CANCELLED);

        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(bookingRepository.findById(TEST_BOOKING_ID)).thenReturn(Optional.of(booking));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> bookingService.cancelBooking(TEST_BOOKING_ID, TEST_TOKEN));
        verify(bookingRepository, times(0)).save(any(Booking.class));
    }

    @Test
    void confirmBooking_success() {
        // Arrange
        Account hotelOwnerAccount = createTestAccount();
        Hotel hotel = createTestHotel();
        hotel.setAccount(hotelOwnerAccount);
        Booking booking = createTestBooking();
        booking.setStatus(BookingStatus.PENDING);
        booking.setHotel(hotel);

        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(hotelOwnerAccount));
        when(bookingRepository.findById(TEST_BOOKING_ID)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        // Act
        bookingService.confirmBooking(TEST_BOOKING_ID, TEST_TOKEN);

        // Assert
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void getBookingsByToken_success() {
        // Arrange
        Account account = createTestAccount();
        Booking booking1 = createTestBooking();
        Booking booking2 = createTestBooking();
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(bookingRepository.findByAccount(account)).thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> response = bookingService.getBookingsByToken(TEST_TOKEN);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(booking1.getId(), response.get(0).getId());
        assertEquals(booking2.getId(), response.get(1).getId());
    }

    @Test
    void getBookingsByToken_accountNotFound() {
        // Arrange
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bookingService.getBookingsByToken(TEST_TOKEN));
    }

    @Test
    void getReservations_success() {
        // Arrange
        Account account = createTestAccount();
        Booking futureBooking1 = createTestBooking();
        futureBooking1.setStartDate(ZonedDateTime.now().plusDays(5));
        Booking pastBooking = createTestBooking();
        pastBooking.setStartDate(ZonedDateTime.now().minusDays(1));
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(account));
        when(bookingRepository.findByAccount(account)).
                thenReturn(List.of(futureBooking1, pastBooking));

        // Act
        List<BookingResponseDto> response = bookingService.getReservations(TEST_TOKEN);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(futureBooking1.getId(), response.getFirst().getId());
    }

    @Test
    void getReservations_accountNotFound() {
        // Arrange
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bookingService.getReservations(TEST_TOKEN));
    }

    @Test
    void getBookingsByHotelId_success() {
        // Arrange
        Account ownerAccount = createTestAccount();
        Hotel hotel = createTestHotel();
        hotel.setAccount(ownerAccount);
        Booking booking1 = createTestBooking();
        Booking booking2 = createTestBooking();
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(ownerAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(hotel));
        when(bookingRepository.findByHotel(hotel)).thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> response = bookingService.getBookingsByHotelId(TEST_HOTEL_ID, TEST_TOKEN);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(booking1.getId(), response.get(0).getId());
        assertEquals(booking2.getId(), response.get(1).getId());
    }

    @Test
    void getBookingsByHotelId_accountNotFound() {
        // Arrange
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bookingService.getBookingsByHotelId(TEST_HOTEL_ID, TEST_TOKEN));
    }

    @Test
    void getBookingsByHotelId_hotelNotFound() {
        // Arrange
        Account ownerAccount = createTestAccount();
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(ownerAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bookingService.getBookingsByHotelId(TEST_HOTEL_ID, TEST_TOKEN));
    }

    @Test
    void getBookingsByHotelId_permissionDenied() {
        // Arrange
        Account ownerAccount = createTestAccount();
        Account otherAccount = new Account();
        otherAccount.setId(99L);
        Hotel hotel = createTestHotel();
        hotel.setAccount(ownerAccount);

        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(otherAccount.getId());
        when(accountRepository.findById(otherAccount.getId())).thenReturn(Optional.of(otherAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(hotel));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> bookingService.getBookingsByHotelId(TEST_HOTEL_ID, TEST_TOKEN));
    }

    @Test
    void getReservationsByHotelId_success() {
        // Arrange
        Account ownerAccount = createTestAccount();
        Hotel hotel = createTestHotel();
        hotel.setAccount(ownerAccount);
        Booking futureBooking1 = createTestBooking();
        futureBooking1.setStartDate(ZonedDateTime.now().plusDays(5));
        Booking pastBooking = createTestBooking();
        pastBooking.setStartDate(ZonedDateTime.now().minusDays(1));
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(ownerAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(hotel));
        when(bookingRepository.findByHotel(hotel)).thenReturn(List.of(futureBooking1, pastBooking));

        // Act
        List<BookingResponseDto> response = bookingService.getReservationsByHotelId(TEST_HOTEL_ID, TEST_TOKEN);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(futureBooking1.getId(), response.getFirst().getId());
    }

    @Test
    void getReservationsByHotelId_accountNotFound() {
        // Arrange
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bookingService.getReservationsByHotelId(TEST_HOTEL_ID, TEST_TOKEN));
    }

    @Test
    void getReservationsByHotelId_hotelNotFound() {
        // Arrange
        Account ownerAccount = createTestAccount();
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.of(ownerAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bookingService.getReservationsByHotelId(TEST_HOTEL_ID, TEST_TOKEN));
    }

    @Test
    void getReservationsByHotelId_permissionDenied() {
        // Arrange
        Account ownerAccount = createTestAccount();
        Account otherAccount = new Account();
        otherAccount.setId(99L);
        Hotel hotel = createTestHotel();
        hotel.setAccount(ownerAccount);

        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(otherAccount.getId());
        when(accountRepository.findById(otherAccount.getId())).thenReturn(Optional.of(otherAccount));
        when(hotelRepository.findById(TEST_HOTEL_ID)).thenReturn(Optional.of(hotel));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> bookingService.getReservationsByHotelId(TEST_HOTEL_ID, TEST_TOKEN));
    }

    @Test
    void getAllBooking_success() {
        // Arrange
        Booking booking1 = createTestBooking();
        Booking booking2 = createTestBooking();
        when(bookingRepository.findAll()).thenReturn(List.of(booking1, booking2));

        // Act
        List<BookingResponseDto> response = bookingService.getAllBooking();

        // Assert
        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals(booking1.getId(), response.get(0).getId());
        assertEquals(booking2.getId(), response.get(1).getId());
    }

    @Test
    void getStatisticForOwner_accountNotFound() {
        // Arrange
        when(jwtUtils.getUserIdFromJwtToken(TEST_TOKEN)).thenReturn(TEST_ACCOUNT_ID);
        when(accountRepository.findById(TEST_ACCOUNT_ID)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> bookingService.getStatisticForOwner(TEST_TOKEN));
    }
}