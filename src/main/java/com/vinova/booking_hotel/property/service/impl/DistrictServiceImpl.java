package com.vinova.booking_hotel.property.service.impl;

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
    public List<DistrictResponseDto> districts() {
        List<District> districts = districtRepository.findAll();

        return districts.stream()
                .map(district -> new DistrictResponseDto(
                        district.getId(), 
                        district.getName()))
                .toList();
    }

    @Override
    public DistrictResponseDto district(Long id) {
        District district = districtRepository.findById(id).orElseThrow(ResourceNotFoundException::new);

        return new DistrictResponseDto(
                district.getId(), 
                district.getName()
        );
    }

    @Override
    public DistrictResponseDto create(AddDistrictRequestDto requestDto) {
        District district = new District();
        district.setName(requestDto.getName());
        districtRepository.save(district);

        return new DistrictResponseDto(district.getId(), district.getName());
    }

    @Override
    public DistrictResponseDto update(Long id, AddDistrictRequestDto requestDto) {
        District district = districtRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        if(requestDto.getName() != null){
            district.setName(requestDto.getName());
        }
        districtRepository.save(district);
        
        return new DistrictResponseDto(district.getId(), district.getName());
    }

    @Override
    public Void delete(Long id) {
        District district = districtRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        districtRepository.delete(district);
        
        return null;
    }
}
