package com.vinova.booking_hotel.property.service.impl;

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
    public List<AmenityResponseDto> amenities() {
        List<Amenity> amenities = amenityRepository.findAll();

        return amenities.stream()
                .map(amenity -> new AmenityResponseDto(amenity.getId(), amenity.getName()))
                .toList();
    }

    @Override
    public List<AmenityResponseDto> amenitiesByHotelId(Long hotelId) {
        List<Amenity> amenities = amenityRepository.findAmenitiesByHotelId(hotelId);

        return amenities.stream()
                .map(amenity -> new AmenityResponseDto(amenity.getId(), amenity.getName()))
                .toList();
    }

    @Override
    public AmenityResponseDto amenity(Long id) {
        Amenity amenity = amenityRepository.findById(id).orElseThrow(ResourceNotFoundException::new);

        return new AmenityResponseDto(amenity.getId(), amenity.getName());
    }

    @Override
    public AmenityResponseDto create(AddAmenityRequestDto requestDto) {
        Amenity amenity = new Amenity();
        amenity.setName(requestDto.getName());
        amenityRepository.save(amenity);

        return new AmenityResponseDto(amenity.getId(), amenity.getName());
    }

    @Override
    public AmenityResponseDto update(Long id, AddAmenityRequestDto requestDto) {
        Amenity amenity = amenityRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        if (requestDto.getName() != null) {
            amenity.setName(requestDto.getName());
        }
        amenityRepository.save(amenity);

        return new AmenityResponseDto(amenity.getId(), amenity.getName());
    }

    @Override
    public Void delete(Long id) {
        Amenity amenity = amenityRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        amenityRepository.delete(amenity);

        return null;
    }
}