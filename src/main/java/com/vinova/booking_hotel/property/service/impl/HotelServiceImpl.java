package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.authentication.service.impl.CloudinaryService;
import com.vinova.booking_hotel.common.enums.ApiError;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddHotelRequestDto;
import com.vinova.booking_hotel.property.dto.response.HotelResponseDto;
import com.vinova.booking_hotel.property.model.District;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.repository.DistrictRepository;
import com.vinova.booking_hotel.property.repository.HotelRepository;
import com.vinova.booking_hotel.property.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {
    
    private final HotelRepository hotelRepository;
    private final JwtUtils jwtUtils;
    private final AccountRepository accountRepository;
    private final DistrictRepository districtRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public APICustomize<List<HotelResponseDto>> hotels(Long accountId, Long districtId, String name, BigDecimal minPrice, BigDecimal maxPrice, int pageIndex, int pageSize, String sortBy, String sortOrder) {
        return null;
    }

    @Override
    public APICustomize<HotelResponseDto> hotel(Long id) {
        return null;
    }

    @Override
    public APICustomize<HotelResponseDto> create(AddHotelRequestDto requestDto, String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId).orElseThrow(ResourceNotFoundException::new);
        
        Hotel hotel = new Hotel();
        hotel.setName(requestDto.getName());
        hotel.setDescription(requestDto.getDescription());
        hotel.setPricePerDay(requestDto.getPricePerDay());
        hotel.setStreetAddress(requestDto.getStreetAddress());
        hotel.setLatitude(requestDto.getLatitude());
        hotel.setLongitude(requestDto.getLongitude());
        
        //Xu ly imageHighLight
        String highLightImageUrl = cloudinaryService.uploadImage(requestDto.getHighLightImageUrl());
        hotel.setHighLightImageUrl(highLightImageUrl);
        //Xu ly district
        District district = districtRepository.findById(requestDto.getDistrictId()).orElseThrow(ResourceNotFoundException::new);
        hotel.setDistrict(district);
        hotel.setAccount(account);
        
        Hotel savedHotel = hotelRepository.save(hotel);
        
        HotelResponseDto response = new HotelResponseDto(
                savedHotel.getId(),
                savedHotel.getName(),
                savedHotel.getDescription(),
                savedHotel.getPricePerDay(),
                savedHotel.getHighLightImageUrl(),
                savedHotel.getStreetAddress(),
                savedHotel.getLatitude(),
                savedHotel.getLongitude(),
                null
        );
        
        return new APICustomize<>(ApiError.CREATED.getCode(), ApiError.CREATED.getMessage(), response);
    }

    @Override
    public APICustomize<Void> update(Long id, AddHotelRequestDto requestDto, String token) {
        return null;
    }

    @Override
    public APICustomize<Void> delete(Long id, String token) {
        return null;
    }
}
