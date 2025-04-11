package com.vinova.booking_hotel.property.service;

import com.vinova.booking_hotel.property.dto.response.ImageResponseDto;

import java.util.List;

public interface ImageService {

    List<ImageResponseDto> images(Long hotelId);
}
