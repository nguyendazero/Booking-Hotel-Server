package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.authentication.service.impl.CloudinaryService;
import com.vinova.booking_hotel.common.enums.ApiError;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import com.vinova.booking_hotel.common.exception.InvalidPageOrSizeException;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddHotelRequestDto;
import com.vinova.booking_hotel.property.dto.response.HotelResponseDto;
import com.vinova.booking_hotel.property.model.District;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.repository.DistrictRepository;
import com.vinova.booking_hotel.property.repository.HotelRepository;
import com.vinova.booking_hotel.property.service.HotelService;
import com.vinova.booking_hotel.property.specification.HotelSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {
    
    private final HotelRepository hotelRepository;
    private final JwtUtils jwtUtils;
    private final AccountRepository accountRepository;
    private final DistrictRepository districtRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    public APICustomize<List<HotelResponseDto>> hotels(Long accountId, Long districtId, String name,
                                                       BigDecimal minPrice, BigDecimal maxPrice, List<String> amenityNames,
                                                       int pageIndex, int pageSize, String sortBy, String sortOrder) {
        // Kiểm tra hợp lệ cho pageIndex và pageSize
        if (pageIndex < 0 || pageSize <= 0) {
            throw new InvalidPageOrSizeException();
        }

        // Tạo Specification với các tiêu chí tìm kiếm
        Specification<Hotel> spec = Specification
                .where(HotelSpecification.hasAccountId(accountId))
                .and(HotelSpecification.hasDistrictId(districtId))
                .and(HotelSpecification.hasName(name))
                .and(HotelSpecification.hasMinPrice(minPrice))
                .and(HotelSpecification.hasMaxPrice(maxPrice))
                .and(HotelSpecification.hasAmenityNames(amenityNames));

        // Sử dụng Pageable từ Spring Data
        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        // Tìm danh sách khách sạn với Specification và phân trang
        List<Hotel> hotels = hotelRepository.findAll(spec, pageable).getContent();

        // Lọc các khách sạn để đảm bảo có đủ số lượng tiện nghi
        List<Hotel> filteredHotels = hotels.stream()
                .filter(hotel -> {
                    if (amenityNames == null) return true; // Nếu amenityNames là null, không lọc
                    long count = hotel.getHotelAmenities().stream()
                            .filter(hotelAmenity -> amenityNames.contains(hotelAmenity.getAmenity().getName()))
                            .count();
                    return count == amenityNames.size(); // Phải có đủ số lượng tiện nghi
                })
                .collect(Collectors.toList());

        // Tính toán điểm số trung bình cho từng khách sạn
        Map<Long, Double> averageRatings = new HashMap<>();
        for (Hotel hotel : filteredHotels) {
            Double averageRating = hotelRepository.findAverageRatingByHotelId(hotel.getId());
            averageRatings.put(hotel.getId(), averageRating);
        }

        // Sắp xếp theo rating hoặc pricePerDay
        if (sortBy != null) {
            Sort.Direction direction = sortOrder != null && sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

            if ("rating".equalsIgnoreCase(sortBy)) {
                filteredHotels.sort((h1, h2) -> {
                    Double rating1 = averageRatings.get(h1.getId());
                    Double rating2 = averageRatings.get(h2.getId());
                    // So sánh và xử lý null
                    return (rating1 == null ? 0 : rating1.compareTo(rating2)) * (direction == Sort.Direction.ASC ? 1 : -1);
                });
            } else if ("pricePerDay".equalsIgnoreCase(sortBy)) {
                filteredHotels.sort((h1, h2) -> {
                    int comparison = h1.getPricePerDay().compareTo(h2.getPricePerDay());
                    return direction == Sort.Direction.ASC ? comparison : -comparison;
                });
            }
        }

        // Chuyển đổi danh sách khách sạn sang danh sách HotelResponseDto
        List<HotelResponseDto> hotelResponses = filteredHotels.stream()
                .map(hotel -> new HotelResponseDto(
                        hotel.getId(),
                        hotel.getName(),
                        hotel.getDescription(),
                        hotel.getPricePerDay(),
                        hotel.getHighLightImageUrl(),
                        hotel.getStreetAddress(),
                        hotel.getLatitude(),
                        hotel.getLongitude(),
                        null,
                        averageRatings.get(hotel.getId())
                )).toList();

        // Trả về kết quả
        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), hotelResponses);
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
                null,
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
