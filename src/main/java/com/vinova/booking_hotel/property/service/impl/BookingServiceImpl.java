package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.authentication.dto.response.AccountResponseDto;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.common.enums.ApiError;
import com.vinova.booking_hotel.common.enums.BookingStatus;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddBookingRequestDto;
import com.vinova.booking_hotel.property.dto.response.BookingResponseDto;
import com.vinova.booking_hotel.property.dto.response.HotelResponseDto;
import com.vinova.booking_hotel.property.model.Booking;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.HotelDiscount;
import com.vinova.booking_hotel.property.repository.BookingRepository;
import com.vinova.booking_hotel.property.repository.HotelDiscountRepository;
import com.vinova.booking_hotel.property.repository.HotelRepository;
import com.vinova.booking_hotel.property.repository.RatingRepository;
import com.vinova.booking_hotel.property.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final AccountRepository accountRepository; 
    private final JwtUtils jwtUtils;
    private final HotelDiscountRepository hotelDiscountRepository;
    private final RatingRepository ratingRepository;

    @Override
    public APICustomize<BookingResponseDto> createBooking(AddBookingRequestDto requestDto, Long hotelId, String token) {
        // Lấy accountId từ token
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(ResourceNotFoundException::new);

        // Tìm khách sạn theo hotelId
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(ResourceNotFoundException::new);

        // Kiểm tra thời gian đặt phòng
        if (requestDto.getStartDate().isAfter(requestDto.getEndDate())) {
            throw new RuntimeException("Start date must be before end date");
        }
        
        // Kiểm tra xem có bất kỳ booking nào đã tồn tại cho khoảng thời gian này không
        List<Booking> existingBookings = bookingRepository.findByHotelIdAndDateRange(hotelId, requestDto.getStartDate(), requestDto.getEndDate());
        if (!existingBookings.isEmpty()) {
            throw new RuntimeException("This hotel is already booked for the selected dates");
        }

        // Tính tổng giá dựa trên thời gian và giảm giá
        BigDecimal totalPrice = calculateTotalPrice(requestDto, hotel);

        // Tạo booking mới
        Booking booking = new Booking();
        booking.setStartDate(requestDto.getStartDate());
        booking.setEndDate(requestDto.getEndDate());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING);
        booking.setHotel(hotel);
        booking.setAccount(account);

        Booking savedBooking = bookingRepository.save(booking);

        // Tính toán điểm số trung bình cho khách sạn
        Double averageRating = ratingRepository.findAverageRatingByHotelId(hotel.getId());

        // Tạo BookingResponseDto
        HotelResponseDto hotelResponse = new HotelResponseDto(
                hotel.getId(),
                hotel.getName(),
                hotel.getDescription(),
                hotel.getPricePerDay(),
                hotel.getHighLightImageUrl(),
                hotel.getStreetAddress(),
                hotel.getLatitude(),
                hotel.getLongitude(),
                null, //images, null
                averageRating
        );

        AccountResponseDto accountResponse = new AccountResponseDto(
                account.getId(),
                account.getFullName(),
                account.getUsername(),
                account.getEmail(),
                account.getAvatar(),
                account.getPhone(),
                null // list role null
        );

        BookingResponseDto response = new BookingResponseDto(
                savedBooking.getId(),
                savedBooking.getStartDate(),
                savedBooking.getEndDate(),
                savedBooking.getTotalPrice(),
                savedBooking.getStatus().toString(),
                savedBooking.getCreateDt(),
                hotelResponse,
                accountResponse
        );

        return new APICustomize<>(ApiError.CREATED.getCode(), ApiError.CREATED.getMessage(), response);
    }

    private BigDecimal calculateTotalPrice(AddBookingRequestDto requestDto, Hotel hotel) {
        ZonedDateTime startDate = requestDto.getStartDate();
        ZonedDateTime endDate = requestDto.getEndDate();
        BigDecimal totalPrice = BigDecimal.ZERO;

        // Lấy danh sách giảm giá của khách sạn
        List<HotelDiscount> hotelDiscounts = hotelDiscountRepository.findByHotelId(hotel.getId());
        ZonedDateTime currentStartDate = startDate;

        while (currentStartDate.isBefore(endDate)) {
            boolean discountApplied = false;

            // Kiểm tra từng giảm giá để tính toán
            for (HotelDiscount hotelDiscount : hotelDiscounts) {
                if (hotelDiscount.getStartDate().isBefore(endDate) && hotelDiscount.getEndDate().isAfter(currentStartDate)) {
                    // Tính toán thời gian đặt trong khoảng giảm giá
                    ZonedDateTime discountStartDate = currentStartDate.isBefore(hotelDiscount.getStartDate()) ? hotelDiscount.getStartDate() : currentStartDate;
                    ZonedDateTime discountEndDate = endDate.isAfter(hotelDiscount.getEndDate()) ? hotelDiscount.getEndDate() : endDate;

                    // Lấy tỷ lệ giảm giá và chuyển đổi thành tỷ lệ phần trăm
                    BigDecimal discountRate = hotelDiscount.getDiscount().getRate().divide(BigDecimal.valueOf(100));
                    BigDecimal discountedPrice = hotel.getPricePerDay().multiply(BigDecimal.ONE.subtract(discountRate));

                    long daysInDiscount = java.time.Duration.between(discountStartDate, discountEndDate).toDays();
                    totalPrice = totalPrice.add(discountedPrice.multiply(BigDecimal.valueOf(daysInDiscount)));

                    // Cập nhật currentStartDate
                    currentStartDate = discountEndDate;
                    discountApplied = true;
                    break; // Chỉ cần tìm giảm giá đầu tiên
                }
            }

            // Nếu không còn giảm giá, tính theo giá của khách sạn
            if (!discountApplied && currentStartDate.isBefore(endDate)) {
                long daysWithoutDiscount = java.time.Duration.between(currentStartDate, endDate).toDays();
                totalPrice = totalPrice.add(hotel.getPricePerDay().multiply(BigDecimal.valueOf(daysWithoutDiscount)));
                currentStartDate = endDate; // Kết thúc vòng lặp
            }
        }

        return totalPrice;
    }
    
}
