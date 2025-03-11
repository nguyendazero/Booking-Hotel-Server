package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.common.enums.ApiError;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.response.ImageResponseDto;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.Image;
import com.vinova.booking_hotel.property.repository.HotelRepository;
import com.vinova.booking_hotel.property.repository.ImageRepository;
import com.vinova.booking_hotel.property.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    
    private final ImageRepository imageRepository;
    private final HotelRepository hotelRepository;

    @Override
    public APICustomize<List<ImageResponseDto>> imagesByHotelId(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(ResourceNotFoundException::new);
        List<Image> images = imageRepository.findAllByHotel(hotel);
        List<ImageResponseDto> imageResponses = images.stream()
                .map(image -> new ImageResponseDto(
                        image.getId(), 
                        image.getImageUrl()))
                .toList();
        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), imageResponses);
    }
}
