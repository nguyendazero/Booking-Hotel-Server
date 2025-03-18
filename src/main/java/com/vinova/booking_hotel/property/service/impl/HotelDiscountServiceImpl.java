package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddDiscountToHotelRequestDto;
import com.vinova.booking_hotel.property.model.*;
import com.vinova.booking_hotel.property.repository.DiscountRepository;
import com.vinova.booking_hotel.property.repository.HotelDiscountRepository;
import com.vinova.booking_hotel.property.repository.HotelRepository;
import com.vinova.booking_hotel.property.service.HotelDiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HotelDiscountServiceImpl implements HotelDiscountService {
    
    private final HotelDiscountRepository hotelDiscountRepository;
    private final HotelRepository hotelRepository;
    private final AccountRepository accountRepository;
    private final JwtUtils jwtUtils;
    private final DiscountRepository discountRepository;

    @Override
    public String addDiscountToHotel(AddDiscountToHotelRequestDto requestDto, String token) {
        // Kiểm tra tính hợp lệ của startDate và endDate
        if (requestDto.getStartDate() == null || requestDto.getEndDate() == null) {
            throw new RuntimeException("Start date and end date must not be null");
        }

        // Kiểm tra xem startDate có lớn hơn endDate không
        if (requestDto.getStartDate().isAfter(requestDto.getEndDate())) {
            throw new RuntimeException("Start date must be before end date");
        }

        // Tìm khách sạn theo hotelId
        Hotel hotel = hotelRepository.findById(requestDto.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel"));

        // Lấy accountId từ token
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        // Kiểm tra quyền truy cập
        if (!hotel.getAccount().getId().equals(account.getId())) {
            throw new RuntimeException("You do not have permission to add discount to this hotel");
        }

        // Kiểm tra xem có bất kỳ discount nào đã tồn tại cho khoảng thời gian này không
        List<HotelDiscount> existingDiscounts = hotelDiscountRepository.findByHotelIdAndDateRange(requestDto.getHotelId(), requestDto.getStartDate(), requestDto.getEndDate());
        if (!existingDiscounts.isEmpty()) {
            throw new RuntimeException("A discount already exists for this date range");
        }

        Discount existingDiscount = discountRepository.findByRate(requestDto.getRate());
        if (existingDiscount != null) {
            // Nếu discount đã tồn tại, tạo liên kết giữa khách sạn và tiện nghi
            HotelDiscount hotelDiscount = new HotelDiscount();
            hotelDiscount.setStartDate(requestDto.getStartDate());
            hotelDiscount.setEndDate(requestDto.getEndDate());
            hotelDiscount.setDiscount(existingDiscount);
            hotelDiscount.setHotel(hotel);
            hotelDiscountRepository.save(hotelDiscount);
            return  "Discount added to hotel";
        }

        // Nếu discount chưa tồn tại, tạo mới
        Discount discount = new Discount();
        discount.setRate(requestDto.getRate());
        Discount savedDiscount = discountRepository.save(discount);

        // Tạo liên kết mới giữa khách sạn và discount mới
        HotelDiscount hotelDiscount = new HotelDiscount();
        hotelDiscount.setStartDate(requestDto.getStartDate());
        hotelDiscount.setEndDate(requestDto.getEndDate());
        hotelDiscount.setDiscount(savedDiscount);
        hotelDiscount.setHotel(hotel);
        hotelDiscountRepository.save(hotelDiscount);

        return "Discount added to hotel";
    }

    @Override
    public String deleteHotelDiscount(Long hotelDiscountId, String token) {
        // Tìm hotelDiscount theo hotelDiscountId
        HotelDiscount hotelDiscount = hotelDiscountRepository.findById(hotelDiscountId)
                .orElseThrow(() -> new RuntimeException("Discount not found"));

        // Tìm khách sạn từ hotelDiscount
        Hotel hotel = hotelDiscount.getHotel();

        // Lấy accountId từ token
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        // Kiểm tra quyền truy cập
        if (!hotel.getAccount().getId().equals(account.getId())) {
            throw new RuntimeException("You do not have permission to delete discount from this hotel");
        }

        // Xóa hotelDiscount
        hotelDiscountRepository.delete(hotelDiscount);

        return "Discount deleted successfully";
    }
}
