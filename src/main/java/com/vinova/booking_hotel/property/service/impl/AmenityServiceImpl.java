package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.common.enums.ApiError;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddAmenityRequestDto;
import com.vinova.booking_hotel.property.dto.response.AmenityResponseDto;
import com.vinova.booking_hotel.property.model.Amenity;
import com.vinova.booking_hotel.property.repository.AmenityRepository;
import com.vinova.booking_hotel.property.service.AmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AmenityServiceImpl implements AmenityService {

    private final AmenityRepository amenityRepository;

    @Override
    public APICustomize<List<AmenityResponseDto>> amenities() {
        List<Amenity> amenities = amenityRepository.findAll();
        List<AmenityResponseDto> response = amenities.stream()
                .map(amenity -> new AmenityResponseDto(amenity.getId(), amenity.getName()))
                .toList();

        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), response);
    }

    @Override
    public APICustomize<AmenityResponseDto> amenity(Long id) {
        Amenity amenity = amenityRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        AmenityResponseDto response = new AmenityResponseDto(amenity.getId(), amenity.getName());

        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), response);
    }

    @Override
    public APICustomize<AmenityResponseDto> create(AddAmenityRequestDto requestDto) {
        Amenity amenity = new Amenity();
        amenity.setName(requestDto.getName());
        amenityRepository.save(amenity);

        return new APICustomize<>(ApiError.CREATED.getCode(), ApiError.CREATED.getMessage(), new AmenityResponseDto(amenity.getId(), amenity.getName()));
    }

    @Override
    public APICustomize<AmenityResponseDto> update(Long id, AddAmenityRequestDto requestDto) {
        Amenity amenity = amenityRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        if (requestDto.getName() != null) {
            amenity.setName(requestDto.getName());
        }
        amenityRepository.save(amenity);

        return new APICustomize<>(ApiError.NO_CONTENT.getCode(), ApiError.NO_CONTENT.getMessage(), new AmenityResponseDto(amenity.getId(), amenity.getName()));
    }

    @Override
    public APICustomize<Void> delete(Long id) {
        Amenity amenity = amenityRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        amenityRepository.delete(amenity);

        return new APICustomize<>(ApiError.NO_CONTENT.getCode(), ApiError.NO_CONTENT.getMessage(), null);
    }
}