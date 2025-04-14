package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.property.dto.request.AddHotelRequestDto;
import com.vinova.booking_hotel.property.dto.request.AddImagesRequestDto;
import com.vinova.booking_hotel.property.dto.response.HotelResponseDto;
import com.vinova.booking_hotel.property.dto.response.ImageResponseDto;
import com.vinova.booking_hotel.property.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class HotelController {
    
    private final HotelService hotelService;

    @GetMapping("/public/hotels")
    public ResponseEntity<List<HotelResponseDto>> hotels(
                                            @RequestParam(required = false) Long accountId,
                                            @RequestParam(required = false) Long districtId,
                                            @RequestParam(required = false) String name,
                                            @RequestParam(required = false) BigDecimal minPrice,
                                            @RequestParam(required = false) BigDecimal maxPrice,
                                            @RequestParam(required = false) List<String> amenityNames,
                                            @RequestParam(required = false) ZonedDateTime startDate,
                                            @RequestParam(required = false) ZonedDateTime endDate,
                                            @RequestParam(defaultValue = "0") int pageIndex,
                                            @RequestParam(defaultValue = "8") int pageSize,
                                            @RequestParam(defaultValue = "id") String sortBy,
                                            @RequestParam(defaultValue = "asc") String sortOrder) {

        List<HotelResponseDto> response = hotelService.hotels(accountId, districtId, name, minPrice, maxPrice, amenityNames, startDate, endDate, pageIndex, pageSize, sortBy, sortOrder);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/user/hotel/wishlist")
    public ResponseEntity<List<HotelResponseDto>> create(@RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        List<HotelResponseDto> response = hotelService.wishlist(accessToken);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/owner/hotel")
    public ResponseEntity<HotelResponseDto> create(
                                            @ModelAttribute AddHotelRequestDto requestDto,
                                            @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        HotelResponseDto response = hotelService.create(requestDto, accessToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/owner/hotel/{id}")
    public ResponseEntity<Void> updateHotel(
                                            @PathVariable Long id,
                                            @ModelAttribute AddHotelRequestDto requestDto,
                                            @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        Void response = hotelService.update(id, requestDto, accessToken);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/public/hotel/{id}")
    public ResponseEntity<HotelResponseDto> getHotelById(@PathVariable Long id) {
        HotelResponseDto response = hotelService.hotel(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @DeleteMapping("/owner/hotel/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, 
                                       @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        Void response = hotelService.delete(id, accessToken);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/owner/hotel/{hotelId}/images")
    public ResponseEntity<List<ImageResponseDto>> addImages(
                                            @PathVariable Long hotelId, 
                                            @ModelAttribute AddImagesRequestDto requestDto, 
                                            @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        List<ImageResponseDto> response = hotelService.addImages(hotelId, requestDto, accessToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/owner/hotel/{hotelId}/images")
    public ResponseEntity<Void> deleteImages(
                                            @PathVariable Long hotelId,
                                            @RequestParam List<Long> imageIds,
                                            @RequestHeader("Authorization") String token) {

        String accessToken = token.substring(7);
        Void response = hotelService.deleteImages(hotelId, imageIds, accessToken);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
}
