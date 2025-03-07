package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.request.AddHotelRequestDto;
import com.vinova.booking_hotel.property.dto.response.HotelResponseDto;
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
        return null;
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
