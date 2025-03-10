package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.request.AddHotelRequestDto;
import com.vinova.booking_hotel.property.dto.request.AddImagesRequestDto;
import com.vinova.booking_hotel.property.dto.response.HotelResponseDto;
import com.vinova.booking_hotel.property.dto.response.ImageResponseDto;

import java.math.BigDecimal;
import java.util.List;

public interface HotelService {

    //Loc theo districtId, AccountId, 
    APICustomize<List<HotelResponseDto>> hotels(Long accountId, Long districtId, String name, BigDecimal minPrice, BigDecimal maxPrice, List<String> amenityNames, int pageIndex, int pageSize, String sortBy, String sortOrder);
    
    APICustomize<HotelResponseDto> hotel(Long id);
    
    APICustomize<HotelResponseDto> create(AddHotelRequestDto requestDto, String token);

    APICustomize<Void> update(Long id, AddHotelRequestDto requestDto, String token);

    APICustomize<Void> delete(Long id, String token);

    APICustomize<List<ImageResponseDto>> addImages(Long hotelId, AddImagesRequestDto requestDto, String token);

    APICustomize<Void> deleteImages(Long hotelId, List<Long> imageIds, String token);
    
}
