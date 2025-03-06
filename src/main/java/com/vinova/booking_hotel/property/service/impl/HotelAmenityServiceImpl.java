package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.common.enums.ApiError;
import com.vinova.booking_hotel.common.exception.ResourceAlreadyExistsException;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.model.Amenity;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.HotelAmenity;
import com.vinova.booking_hotel.property.repository.AmenityRepository;
import com.vinova.booking_hotel.property.repository.HotelAmenityRepository;
import com.vinova.booking_hotel.property.repository.HotelRepository;
import com.vinova.booking_hotel.property.service.HotelAmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelAmenityServiceImpl implements HotelAmenityService {
    
    private final HotelAmenityRepository hotelAmenityRepository;
    private final AmenityRepository amenityRepository;;
    private final HotelRepository hotelRepository;
    private final JwtUtils jwtUtils;
    private final AccountRepository accountRepository;

    @Override
    public APICustomize<String> addAmenityToHotel(String nameAmenity, Long hotelId, String token) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(ResourceNotFoundException::new);
        
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(ResourceNotFoundException::new);
        
        if(!hotel.getAccount().getId().equals(account.getId())) {
            throw new RuntimeException("You not have permission to add amenity to this hotel");
        }

        List<Amenity> amenities = amenityRepository.findAll();
        for (Amenity amenity : amenities) {
            if (amenity.getName().equals(nameAmenity)) {
                throw new ResourceAlreadyExistsException(nameAmenity);
            }
        }
        Amenity amenity = new Amenity();
        amenity.setName(nameAmenity);
        Amenity amenitySaved = amenityRepository.save(amenity);
        
        HotelAmenity hotelAmenity = new HotelAmenity();
        hotelAmenity.setAmenity(amenitySaved);
        hotelAmenity.setHotel(hotel);
        hotelAmenityRepository.save(hotelAmenity);
        
        return new APICustomize<>(ApiError.CREATED.getCode(), ApiError.CREATED.getMessage(), "Amenity added to hotel");
    }

    @Override
    public APICustomize<String> removeAmenityFromHotel(Long amenityId, Long hotelId, String token) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(ResourceNotFoundException::new);

        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(ResourceNotFoundException::new);

        if (!hotel.getAccount().getId().equals(account.getId())) {
            throw new RuntimeException("You do not have permission to remove amenities from this hotel");
        }
        
        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(ResourceNotFoundException::new);
        // Tìm kiếm Amenity trong hotelAmenity
        HotelAmenity hotelAmenity = hotelAmenityRepository.findByHotelAndAmenity(hotel, amenity)
                .orElseThrow(ResourceNotFoundException::new);
        hotelAmenityRepository.delete(hotelAmenity);

        return new APICustomize<>(ApiError.NO_CONTENT.getCode(), ApiError.NO_CONTENT.getMessage(), "Amenity removed from hotel");
    }


}
