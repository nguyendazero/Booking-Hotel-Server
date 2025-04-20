package com.vinova.booking_hotel.controller;

import com.vinova.booking_hotel.payment.dto.StripeResponseDto;
import com.vinova.booking_hotel.property.controller.BookingController;
import com.vinova.booking_hotel.property.dto.request.AddBookingRequestDto;
import com.vinova.booking_hotel.property.dto.response.BookingResponseDto;
import com.vinova.booking_hotel.property.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @InjectMocks
    private BookingController bookingController;

    private final String TEST_TOKEN = "Bearer test_token";
    private final String ACCESS_TOKEN = "test_token";
    private final Long TEST_BOOKING_ID = 1L;
    private final Long TEST_HOTEL_ID = 2L;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testBookHotel() {
        AddBookingRequestDto requestDto = new AddBookingRequestDto();
        StripeResponseDto mockResponse = new StripeResponseDto();
        when(bookingService.createBooking(any(AddBookingRequestDto.class), eq(ACCESS_TOKEN))).thenReturn(mockResponse);

        ResponseEntity<StripeResponseDto> response = bookingController.bookHotel(requestDto, TEST_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testCancelBooking() {
        Mockito.doNothing().when(bookingService).cancelBooking(eq(TEST_BOOKING_ID), eq(ACCESS_TOKEN));

        ResponseEntity<Void> response = bookingController.cancelBooking(TEST_BOOKING_ID, TEST_TOKEN);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testConfirmBooking() {
        Mockito.doNothing().when(bookingService).confirmBooking(eq(TEST_BOOKING_ID), eq(ACCESS_TOKEN));

        ResponseEntity<Void> response = bookingController.confirmBooking(TEST_BOOKING_ID, TEST_TOKEN);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    public void testGetBookingsByToken() {
        List<BookingResponseDto> mockResponse = Collections.singletonList(new BookingResponseDto());
        when(bookingService.getBookingsByToken(eq(ACCESS_TOKEN))).thenReturn(mockResponse);

        ResponseEntity<List<BookingResponseDto>> response = bookingController.getBookingsByToken(TEST_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testGetReservations() {
        List<BookingResponseDto> mockResponse = Collections.singletonList(new BookingResponseDto());
        when(bookingService.getReservations(eq(ACCESS_TOKEN))).thenReturn(mockResponse);

        ResponseEntity<List<BookingResponseDto>> response = bookingController.getReservations(TEST_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testGetBookingsByHotelId() {
        List<BookingResponseDto> mockResponse = Collections.singletonList(new BookingResponseDto());
        when(bookingService.getBookingsByHotelId(eq(TEST_HOTEL_ID), eq(ACCESS_TOKEN))).thenReturn(mockResponse);

        ResponseEntity<List<BookingResponseDto>> response = bookingController.getBookingsByHotelId(TEST_HOTEL_ID, TEST_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testGetReservationsByHotelId() {
        List<BookingResponseDto> mockResponse = Collections.singletonList(new BookingResponseDto());
        when(bookingService.getReservationsByHotelId(eq(TEST_HOTEL_ID), eq(ACCESS_TOKEN))).thenReturn(mockResponse);

        ResponseEntity<List<BookingResponseDto>> response = bookingController.getReservationsByHotelId(TEST_HOTEL_ID, TEST_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testGetAllBooking() {
        List<BookingResponseDto> mockResponse = Collections.singletonList(new BookingResponseDto());
        when(bookingService.getAllBooking()).thenReturn(mockResponse);

        ResponseEntity<List<BookingResponseDto>> response = bookingController.getAllBooking();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testGetStatistic() {
        List<BookingResponseDto> mockResponse = Collections.singletonList(new BookingResponseDto());
        when(bookingService.getStatisticForOwner(eq(ACCESS_TOKEN))).thenReturn(mockResponse);

        ResponseEntity<List<BookingResponseDto>> response = bookingController.getStatistic(TEST_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }
}