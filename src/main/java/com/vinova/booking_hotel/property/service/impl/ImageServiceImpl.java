package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.common.enums.EntityType;
import com.vinova.booking_hotel.property.dto.response.ImageResponseDto;
import com.vinova.booking_hotel.property.model.Image;
import com.vinova.booking_hotel.property.repository.ImageRepository;
import com.vinova.booking_hotel.property.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {
    
    private final ImageRepository imageRepository;

    @Override
    public List<ImageResponseDto> images(Long hotelId) {
        List<Image> images = imageRepository.findByEntityIdAndEntityType(hotelId, EntityType.HOTEL);
        return images.stream()
                .map(image -> new ImageResponseDto(
                        image.getId(),
                        image.getImageUrl()))
                .toList();
    }
}
