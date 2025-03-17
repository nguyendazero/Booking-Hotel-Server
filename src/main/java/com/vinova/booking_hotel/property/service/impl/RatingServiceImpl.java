package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.dto.response.*;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.authentication.service.impl.CloudinaryService;
import com.vinova.booking_hotel.common.enums.*;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddRatingRequestDto;
import com.vinova.booking_hotel.property.dto.response.*;
import com.vinova.booking_hotel.property.model.*;
import com.vinova.booking_hotel.property.repository.*;
import com.vinova.booking_hotel.property.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RatingServiceImpl implements RatingService {
    
    private final RatingRepository ratingRepository;
    private final AccountRepository accountRepository;
    private final JwtUtils jwtUtils;
    private final CloudinaryService cloudinaryService;
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final ImageRepository imageRepository;

    @Override
    public List<RatingResponseDto> ratingsByHotelId(Long hotelId) {
        List<Rating> ratings = ratingRepository.findByHotelId(hotelId);

        // Tạo và trả về APICustomize chứa danh sách RatingResponseDto
        return ratings.stream()
                .map(rating -> {
                    List<Image> images = imageRepository.findByEntityIdAndEntityType(rating.getId(), EntityType.REVIEW);
                    List<ImageResponseDto> imageDtos = images.stream()
                            .map(image -> new ImageResponseDto(
                                    image.getId(),
                                    image.getImageUrl()
                            ))
                            .collect(Collectors.toList());

                    return new RatingResponseDto(
                            rating.getId(),
                            rating.getStars(),
                            rating.getContent(),
                            rating.getCreateDt(),
                            imageDtos,
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
                })
                .collect(Collectors.toList());
    }

    @Override
    public RatingResponseDto rating(Long id) {
        Rating rating = ratingRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        
        // Lấy danh sách hình ảnh liên quan đến đánh giá từ repository
        List<Image> images = imageRepository.findByEntityIdAndEntityType(rating.getId(), EntityType.REVIEW);
        List<ImageResponseDto> imageDtos = images.stream()
                .map(image -> new ImageResponseDto(
                        image.getId(),
                        image.getImageUrl()
                ))
                .collect(Collectors.toList());

        // Tạo và trả về APICustomize chứa RatingResponseDto
        return new RatingResponseDto(
                rating.getId(),
                rating.getStars(),
                rating.getContent(),
                rating.getCreateDt(),
                imageDtos,
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
    }

    @Override
    public RatingResponseDto create(AddRatingRequestDto requestDto, String token) {
        // Lấy accountId từ token
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(ResourceNotFoundException::new);

        // Tìm khách sạn theo hotelId
        Hotel hotel = hotelRepository.findById(requestDto.getHotelId())
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

        // Lưu hình ảnh vào Cloudinary và tạo danh sách hình ảnh
        List<ImageResponseDto> imageDtos = new ArrayList<>();
        if (requestDto.getImages() != null && !requestDto.getImages().isEmpty()) {
            for (MultipartFile imageFile : requestDto.getImages()) {
                // Tải hình ảnh lên Cloudinary
                String imageUrl = cloudinaryService.uploadImage(imageFile);

                // Tạo đối tượng Image mới
                Image image = new Image();
                image.setEntityId(savedRating.getId());
                image.setEntityType(EntityType.REVIEW);
                image.setImageUrl(imageUrl);

                // Lưu hình ảnh vào cơ sở dữ liệu
                imageRepository.save(image);

                // Thêm vào danh sách hình ảnh DTO
                imageDtos.add(new ImageResponseDto(image.getId(), imageUrl));
            }
        }

        // Chuyển đổi sang RatingResponseDto

        // Trả về APICustomize chứa RatingResponseDto
        return new RatingResponseDto(
                savedRating.getId(),
                savedRating.getStars(),
                savedRating.getContent(),
                savedRating.getCreateDt(),
                imageDtos,
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
    }

    @Override
    public Void delete(Long id, String token) {
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
        return  null;
    }
}
