package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.authentication.dto.response.AccountResponseDto;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.common.enums.ApiError;
import com.vinova.booking_hotel.common.enums.BookingStatus;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddRatingRequestDto;
import com.vinova.booking_hotel.property.dto.response.RatingResponseDto;
import com.vinova.booking_hotel.property.model.Booking;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.Rating;
import com.vinova.booking_hotel.property.repository.BookingRepository;
import com.vinova.booking_hotel.property.repository.HotelRepository;
import com.vinova.booking_hotel.property.repository.RatingRepository;
import com.vinova.booking_hotel.property.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {
    
    private final RatingRepository ratingRepository;
    private final AccountRepository accountRepository;
    private final JwtUtils jwtUtils;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;

    @Override
    public APICustomize<List<RatingResponseDto>> ratingsByHotelId(Long hotelId) {
        List<Rating> ratings = ratingRepository.findByHotelId(hotelId);
        List<RatingResponseDto> responseDtos = ratings.stream()
                .map(rating -> new RatingResponseDto(
                        rating.getId(),
                        rating.getStars(),
                        rating.getContent(),
                        rating.getCreateDt(),
                        new AccountResponseDto(
                                rating.getAccount().getId(),
                                rating.getAccount().getFullName(),
                                rating.getAccount().getUsername(),
                                rating.getAccount().getEmail(),
                                rating.getAccount().getAvatar(),
                                rating.getAccount().getPhone(),
                                null
                        )
                ))
                .collect(Collectors.toList());

        // Tạo và trả về APICustomize chứa danh sách RatingResponseDto
        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), responseDtos);
    }

    @Override
    public APICustomize<RatingResponseDto> rating(Long id) {
        Rating rating = ratingRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        RatingResponseDto responseDto = new RatingResponseDto(
                rating.getId(),
                rating.getStars(),
                rating.getContent(),
                rating.getCreateDt(),
                new AccountResponseDto(
                        rating.getAccount().getId(),
                        rating.getAccount().getFullName(),
                        rating.getAccount().getUsername(),
                        rating.getAccount().getEmail(),
                        rating.getAccount().getAvatar(),
                        rating.getAccount().getPhone(),
                        null
                )
        );

        // Tạo và trả về APICustomize chứa RatingResponseDto
        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), responseDto);
    }

    @Override
    public APICustomize<RatingResponseDto> create(AddRatingRequestDto requestDto, Long hotelId, String token) {
        // Lấy accountId từ token
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(ResourceNotFoundException::new);

        // Tìm khách sạn theo hotelId
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(ResourceNotFoundException::new);

        // Kiểm tra xem người dùng đã đặt phòng tại khách sạn này chưa
        Booking booking = bookingRepository.findFirstByHotelAndAccount(hotel, account)
                .orElseThrow(() -> new RuntimeException("You must book a room at this hotel before leaving a rating"));

        // Kiểm tra trạng thái của booking
        if (!booking.getStatus().equals(BookingStatus.CHECKOUT) && !booking.getStatus().equals(BookingStatus.CHECKIN)) {
            throw new RuntimeException("You can only leave a rating after checking in or checking out.");
        }

        // Tạo đối tượng Rating mới
        Rating rating = new Rating();
        rating.setStars(requestDto.getStars());
        rating.setContent(requestDto.getContent());
        rating.setHotel(hotel);
        rating.setAccount(account);

        // Lưu đánh giá vào cơ sở dữ liệu
        Rating savedRating = ratingRepository.save(rating);

        // Chuyển đổi sang RatingResponseDto
        RatingResponseDto responseDto = new RatingResponseDto(
                savedRating.getId(),
                savedRating.getStars(),
                savedRating.getContent(),
                savedRating.getCreateDt(),
                new AccountResponseDto(
                        account.getId(),
                        account.getFullName(),
                        account.getUsername(),
                        account.getEmail(),
                        account.getAvatar(),
                        account.getPhone(),
                        null
                )
        );

        // Trả về APICustomize chứa RatingResponseDto
        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), responseDto);
    }

    @Override
    public APICustomize<Void> delete(Long id, String token) {
        // Lấy accountId từ token
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(ResourceNotFoundException::new);

        // Tìm đánh giá theo id
        Rating rating = ratingRepository.findById(id).orElseThrow(ResourceNotFoundException::new);

        // Kiểm tra xem tài khoản có quyền xóa đánh giá này không
        if (!rating.getAccount().getId().equals(account.getId())) {
            throw new RuntimeException("User does not have permission to delete this rating.");
        }

        // Xóa đánh giá
        ratingRepository.delete(rating);

        // Trả về kết quả thành công
        return new APICustomize<>(ApiError.NO_CONTENT.getCode(), ApiError.NO_CONTENT.getMessage(), null);
    }
}
