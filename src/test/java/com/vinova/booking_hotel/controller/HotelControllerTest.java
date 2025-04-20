package com.vinova.booking_hotel.controller;

import com.vinova.booking_hotel.property.controller.HotelController;
import com.vinova.booking_hotel.property.dto.request.AddHotelRequestDto;
import com.vinova.booking_hotel.property.dto.request.AddImagesRequestDto;
import com.vinova.booking_hotel.property.dto.response.HotelResponseDto;
import com.vinova.booking_hotel.property.dto.response.ImageResponseDto;
import com.vinova.booking_hotel.property.service.HotelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class HotelControllerTest {

    @Mock
    private HotelService hotelService;

    @InjectMocks
    private HotelController hotelController;

    private final String AUTH_TOKEN = "Bearer test_token";
    private final String ACCESS_TOKEN = "test_token";
    private final Long TEST_HOTEL_ID = 1L;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetHotels() {
        List<HotelResponseDto> mockResponse = Arrays.asList(new HotelResponseDto(), new HotelResponseDto());
        when(hotelService.hotels(any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(mockResponse);

        ResponseEntity<List<HotelResponseDto>> response = hotelController.hotels(null, null, null, null, null, null, null, null, 0, 8, "id", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testGetWishlist() {
        List<HotelResponseDto> mockResponse = Collections.singletonList(new HotelResponseDto());
        when(hotelService.wishlist(eq(ACCESS_TOKEN))).thenReturn(mockResponse);

        ResponseEntity<List<HotelResponseDto>> response = hotelController.create(AUTH_TOKEN);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testCreateHotel() {
        AddHotelRequestDto requestDto = new AddHotelRequestDto();
        HotelResponseDto mockResponse = new HotelResponseDto();
        when(hotelService.create(any(AddHotelRequestDto.class), eq(ACCESS_TOKEN))).thenReturn(mockResponse);

        ResponseEntity<HotelResponseDto> response = hotelController.create(requestDto, AUTH_TOKEN);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testUpdateHotel() {
        AddHotelRequestDto requestDto = new AddHotelRequestDto();
        // Sử dụng doNothing() để chỉ định rằng phương thức void không làm gì khi được gọi
        Mockito.doNothing().when(hotelService).update(eq(TEST_HOTEL_ID), any(AddHotelRequestDto.class), eq(ACCESS_TOKEN));

        ResponseEntity<Void> response = hotelController.updateHotel(TEST_HOTEL_ID, requestDto, AUTH_TOKEN);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        // Bạn có thể thêm verify() để kiểm tra xem phương thức update có được gọi hay không
        Mockito.verify(hotelService, Mockito.times(1)).update(eq(TEST_HOTEL_ID), any(AddHotelRequestDto.class), eq(ACCESS_TOKEN));
    }

    @Test
    public void testGetHotelById() {
        HotelResponseDto mockResponse = new HotelResponseDto();
        when(hotelService.hotel(eq(TEST_HOTEL_ID))).thenReturn(mockResponse);

        ResponseEntity<HotelResponseDto> response = hotelController.getHotelById(TEST_HOTEL_ID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testDeleteHotel() {
        // Sử dụng doNothing() để chỉ định rằng phương thức void không làm gì khi được gọi
        Mockito.doNothing().when(hotelService).delete(eq(TEST_HOTEL_ID), eq(ACCESS_TOKEN));

        ResponseEntity<Void> response = hotelController.delete(TEST_HOTEL_ID, AUTH_TOKEN);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        // Thêm verify() để kiểm tra xem phương thức delete có được gọi hay không
        Mockito.verify(hotelService, Mockito.times(1)).delete(eq(TEST_HOTEL_ID), eq(ACCESS_TOKEN));
    }

    @Test
    public void testAddImages() {
        AddImagesRequestDto requestDto = new AddImagesRequestDto();
        List<ImageResponseDto> mockResponse = Collections.singletonList(new ImageResponseDto());
        when(hotelService.addImages(eq(TEST_HOTEL_ID), any(AddImagesRequestDto.class), eq(ACCESS_TOKEN))).thenReturn(mockResponse);

        ResponseEntity<List<ImageResponseDto>> response = hotelController.addImages(TEST_HOTEL_ID, requestDto, AUTH_TOKEN);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testDeleteImages() {
        List<Long> imageIds = Collections.singletonList(1L);
        // Sử dụng doNothing() cho phương thức void
        Mockito.doNothing().when(hotelService).deleteImages(eq(TEST_HOTEL_ID), eq(imageIds), eq(ACCESS_TOKEN));

        ResponseEntity<Void> response = hotelController.deleteImages(TEST_HOTEL_ID, imageIds, AUTH_TOKEN);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        // Thêm verify() để kiểm tra tương tác với service
        Mockito.verify(hotelService, Mockito.times(1)).deleteImages(eq(TEST_HOTEL_ID), eq(imageIds), eq(ACCESS_TOKEN));
    }
}