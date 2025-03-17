package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.property.dto.request.AddHotelRequestDto;
import com.vinova.booking_hotel.property.dto.request.AddImagesRequestDto;
import com.vinova.booking_hotel.property.dto.response.HotelResponseDto;
import com.vinova.booking_hotel.property.dto.response.ImageResponseDto;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public interface HotelService {

    //Loc theo districtId, AccountId, 
    List<HotelResponseDto> hotels(Long accountId, Long districtId, String name, BigDecimal minPrice, BigDecimal maxPrice, List<String> amenityNames, ZonedDateTime startDate, ZonedDateTime endDate, int pageIndex, int pageSize, String sortBy, String sortOrder);

    List<HotelResponseDto> wishlist(String token);
    
    HotelResponseDto hotel(Long id);
    
   HotelResponseDto create(AddHotelRequestDto requestDto, String token);

   Void update(Long id, AddHotelRequestDto requestDto, String token);

   Void delete(Long id, String token);

   List<ImageResponseDto> addImages(Long hotelId, AddImagesRequestDto requestDto, String token);

   Void deleteImages(Long hotelId, List<Long> imageIds, String token);
    
}
