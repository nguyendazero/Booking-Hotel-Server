package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddAmenityToHotelRequestDto;
import com.vinova.booking_hotel.property.dto.request.DeleteAmenityFromHotelRequestDto;
import com.vinova.booking_hotel.property.model.Amenity;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.HotelAmenity;
import com.vinova.booking_hotel.property.repository.AmenityRepository;
import com.vinova.booking_hotel.property.repository.HotelAmenityRepository;
import com.vinova.booking_hotel.property.repository.HotelRepository;
import com.vinova.booking_hotel.property.service.HotelAmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class HotelAmenityServiceImpl implements HotelAmenityService {
    
    private final HotelAmenityRepository hotelAmenityRepository;
    private final AmenityRepository amenityRepository;;
    private final HotelRepository hotelRepository;
    private final JwtUtils jwtUtils;
    private final AccountRepository accountRepository;

    @Override
    public String addAmenityToHotel(AddAmenityToHotelRequestDto requestDto, String token) {
        // Tìm khách sạn theo hotelId
        Hotel hotel = hotelRepository.findById(requestDto.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel"));

        // Lấy accountId từ token
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        // Kiểm tra quyền truy cập
        if (!hotel.getAccount().getId().equals(account.getId())) {
            throw new RuntimeException("You do not have permission to add amenity to this hotel");
        }

        // Tìm tiện nghi theo tên
        Amenity existingAmenity = amenityRepository.findByName(requestDto.getNameAmenity());
        if (existingAmenity != null) {
            // Nếu tiện nghi đã tồn tại, tạo liên kết giữa khách sạn và tiện nghi
            HotelAmenity hotelAmenity = new HotelAmenity();
            hotelAmenity.setAmenity(existingAmenity);
            hotelAmenity.setHotel(hotel);
            hotelAmenityRepository.save(hotelAmenity);
            return "Amenity added to hotel";
        }

        // Nếu tiện nghi chưa tồn tại, tạo mới
        Amenity amenity = new Amenity();
        amenity.setName(requestDto.getNameAmenity());
        Amenity amenitySaved = amenityRepository.save(amenity);

        // Tạo liên kết mới giữa khách sạn và tiện nghi mới
        HotelAmenity hotelAmenity = new HotelAmenity();
        hotelAmenity.setAmenity(amenitySaved);
        hotelAmenity.setHotel(hotel);
        hotelAmenityRepository.save(hotelAmenity);

        return "Amenity added to hotel";
    }

    @Override
    public String removeAmenityFromHotel(DeleteAmenityFromHotelRequestDto requestDto, String token) {
        Hotel hotel = hotelRepository.findById(requestDto.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel"));

        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        if (!hotel.getAccount().getId().equals(account.getId())) {
            throw new RuntimeException("You do not have permission to remove amenities from this hotel");
        }

        Amenity amenity = amenityRepository.findById(requestDto.getAmenityId())
                .orElseThrow(() -> new ResourceNotFoundException("Amenity"));
        // Tìm kiếm Amenity trong hotelAmenity
        HotelAmenity hotelAmenity = hotelAmenityRepository.findByHotelAndAmenity(hotel, amenity)
                .orElseThrow(() -> new ResourceNotFoundException("HotelAmenity"));
        hotelAmenityRepository.delete(hotelAmenity);

        return "Amenity removed from hotel";
    }


}
