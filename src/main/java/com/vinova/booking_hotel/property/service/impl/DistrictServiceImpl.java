package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.common.enums.ApiError;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddDistrictRequestDto;
import com.vinova.booking_hotel.property.dto.response.DistrictResponseDto;
import com.vinova.booking_hotel.property.model.District;
import com.vinova.booking_hotel.property.repository.DistrictRepository;
import com.vinova.booking_hotel.property.service.DistrictService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DistrictServiceImpl implements DistrictService {
    
    private final DistrictRepository districtRepository;

    @Override
    public APICustomize<List<DistrictResponseDto>> districts() {
        List<District> districts = districtRepository.findAll();
        List<DistrictResponseDto> response = districts.stream()
                .map(district -> new DistrictResponseDto(
                        district.getId(), 
                        district.getName()))
                .toList();
       
        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), response);
    }

    @Override
    public APICustomize<DistrictResponseDto> district(Long id) {
        District district = districtRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        DistrictResponseDto response = new DistrictResponseDto(
                district.getId(), 
                district.getName()
        );

        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), response);
    }

    @Override
    public APICustomize<DistrictResponseDto> create(AddDistrictRequestDto requestDto) {
        District district = new District();
        district.setName(requestDto.getName());
        districtRepository.save(district);

        return new APICustomize<>(ApiError.CREATED.getCode(), ApiError.CREATED.getMessage(), new DistrictResponseDto(district.getId(), district.getName()));
    }

    @Override
    public APICustomize<DistrictResponseDto> update(Long id, AddDistrictRequestDto requestDto) {
        District district = districtRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        if(requestDto.getName() != null){
            district.setName(requestDto.getName());
        }
        districtRepository.save(district);
        
        return new APICustomize<>(ApiError.NO_CONTENT.getCode(), ApiError.NO_CONTENT.getMessage(), new DistrictResponseDto(district.getId(), district.getName()));
    }

    @Override
    public APICustomize<Void> delete(Long id) {
        District district = districtRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        districtRepository.delete(district);
        
        return new APICustomize<>(ApiError.NO_CONTENT.getCode(), ApiError.NO_CONTENT.getMessage(), null);
    }
}
