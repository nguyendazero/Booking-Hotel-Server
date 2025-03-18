package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.authentication.service.impl.CloudinaryService;
import com.vinova.booking_hotel.common.enums.EntityType;
import com.vinova.booking_hotel.property.dto.request.AddImagesRequestDto;
import com.vinova.booking_hotel.property.dto.response.*;
import com.vinova.booking_hotel.property.model.*;
import com.vinova.booking_hotel.property.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import com.vinova.booking_hotel.common.exception.*;
import com.vinova.booking_hotel.property.dto.request.AddHotelRequestDto;
import com.vinova.booking_hotel.property.dto.response.HotelResponseDto;
import com.vinova.booking_hotel.property.service.HotelService;
import com.vinova.booking_hotel.property.repository.specification.HotelSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {
    
    private final HotelRepository hotelRepository;
    private final JwtUtils jwtUtils;
    private final AccountRepository accountRepository;
    private final DistrictRepository districtRepository;
    private final CloudinaryService cloudinaryService;
    private final RatingRepository ratingRepository;
    private final ImageRepository imageRepository;
    private final BookingRepository bookingRepository;

    @Override
    public List<HotelResponseDto> hotels(Long accountId, Long districtId, String name,
                                                       BigDecimal minPrice, BigDecimal maxPrice,
                                                       List<String> amenityNames,
                                                       ZonedDateTime startDate, ZonedDateTime endDate,
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
                .and(HotelSpecification.hasAmenityNames(amenityNames))
                .and(HotelSpecification.isAvailableBetween(startDate, endDate));

        // Sử dụng Pageable từ Spring Data
        Sort sort = Sort.by(sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        Pageable pageable = PageRequest.of(pageIndex, pageSize, sort);
        
        // Tìm danh sách khách sạn với Specification và phân trang
        Page<Hotel> hotelPage = hotelRepository.findAll(spec, pageable);

        // Lọc các khách sạn để đảm bảo có đủ số lượng tiện nghi
        List<Hotel> filteredHotels = hotelPage.getContent().stream()
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
        Map<Long, Long> reviewCounts = new HashMap<>();
        for (Hotel hotel : filteredHotels) {
            Double averageRating = hotelRepository.findAverageRatingByHotelId(hotel.getId());
            Long reviewCount = ratingRepository.countByHotel(hotel); // Đếm số lượng đánh giá
            averageRatings.put(hotel.getId(), averageRating);
            reviewCounts.put(hotel.getId(), reviewCount);
        }

        // Sắp xếp theo rating, pricePerDay hoặc id
        if (sortBy != null) {
            Sort.Direction direction = sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;

            if ("rating".equalsIgnoreCase(sortBy)) {
                filteredHotels.sort((h1, h2) -> {
                    Double rating1 = averageRatings.get(h1.getId());
                    Double rating2 = averageRatings.get(h2.getId());
                    return (rating1 == null ? 0 : rating1.compareTo(rating2)) * (direction == Sort.Direction.ASC ? 1 : -1);
                });
            } else if ("pricePerDay".equalsIgnoreCase(sortBy)) {
                filteredHotels.sort((h1, h2) -> {
                    int comparison = h1.getPricePerDay().compareTo(h2.getPricePerDay());
                    return direction == Sort.Direction.ASC ? comparison : -comparison;
                });
            } else if ("id".equalsIgnoreCase(sortBy)) {
                filteredHotels.sort((h1, h2) -> {
                    int comparison = h1.getId().compareTo(h2.getId());
                    return direction == Sort.Direction.ASC ? comparison : -comparison;
                });
            }
        }

        // Chuyển đổi danh sách khách sạn sang danh sách HotelResponseDto

        // Trả về kết quả
        return filteredHotels.stream()
                .map(hotel -> new HotelResponseDto(
                        hotel.getId(),
                        hotel.getName(),
                        hotel.getDescription(),
                        hotel.getPricePerDay(),
                        hotel.getHighLightImageUrl(),
                        hotel.getStreetAddress(),
                        hotel.getLatitude(),
                        hotel.getLongitude(),
                        averageRatings.get(hotel.getId()) != null ? averageRatings.get(hotel.getId()) : 0.0,
                        reviewCounts.get(hotel.getId()) != null ? reviewCounts.get(hotel.getId()) : 0L,
                        null,
                        null
                )).toList();
    }

    @Override
    public List<HotelResponseDto> wishlist(String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        // Lấy danh sách yêu thích liên kết với tài khoản
        List<WishList> wishList = account.getWishList();

        // Lấy các ID khách sạn duy nhất từ danh sách yêu thích
        List<Long> hotelIds = wishList.stream()
                .map(wish -> wish.getHotel().getId())
                .collect(Collectors.toList());

        // Lấy danh sách khách sạn dựa trên các ID đã lấy
        List<Hotel> hotels = hotelRepository.findAllById(hotelIds);

        // Chuẩn bị phản hồi bằng cách chuyển đổi khách sạn thành HotelResponseDto

        // Trả về phản hồi
        return hotels.stream()
                .map(hotel -> {
                    Double averageRating = ratingRepository.findAverageRatingByHotelId(hotel.getId());
                    Long reviewCount = ratingRepository.countByHotel(hotel);
                    return new HotelResponseDto(
                            hotel.getId(),
                            hotel.getName(),
                            hotel.getDescription(),
                            hotel.getPricePerDay(),
                            hotel.getHighLightImageUrl(),
                            hotel.getStreetAddress(),
                            hotel.getLatitude(),
                            hotel.getLongitude(),
                            averageRating != null ? averageRating : 0.0,
                            reviewCount,
                            null,
                            null
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    public HotelResponseDto hotel(Long id) {
        // Tìm khách sạn theo ID
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel"));

        // Lấy điểm đánh giá trung bình của khách sạn
        Double averageRating = ratingRepository.findAverageRatingByHotelId(id);

        // Lấy danh sách đặt phòng của khách sạn
        List<Booking> bookings = bookingRepository.findByHotelId(id);

        // Lấy thời điểm hiện tại
        ZonedDateTime now = ZonedDateTime.now();

        // Chuyển đổi các đặt phòng thành DateRangeResponseDto chỉ cho những booking chưa diễn ra
        List<DateRangeResponseDto> bookedDates = bookings.stream()
                .filter(booking -> booking.getStartDate().isAfter(now)) // Lọc các booking có ngày bắt đầu trong tương lai
                .map(booking -> new DateRangeResponseDto(
                        booking.getStartDate(),
                        booking.getEndDate()
                ))
                .collect(Collectors.toList());

        // Lấy số lượng đánh giá của khách sạn
        Long reviewCount = ratingRepository.countByHotel(hotel);

        // Lấy hình ảnh của khách sạn thông qua entityId và entityType
        List<Image> images = imageRepository.findByEntityIdAndEntityType(id, EntityType.HOTEL);
        List<ImageResponseDto> imageResponses = images.stream()
                .map(image -> new ImageResponseDto(
                        image.getId(),
                        image.getImageUrl()))
                .toList();

        // Chuyển đổi khách sạn thành HotelResponseDto

        // Trả về kết quả
        return new HotelResponseDto(
                hotel.getId(),
                hotel.getName(),
                hotel.getDescription(),
                hotel.getPricePerDay(),
                hotel.getHighLightImageUrl(),
                hotel.getStreetAddress(),
                hotel.getLatitude(),
                hotel.getLongitude(),
                averageRating != null ? averageRating : 0.0,
                reviewCount,
                imageResponses,
                bookedDates
        );
    }

    @Override
    public HotelResponseDto create(AddHotelRequestDto requestDto, String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));
        
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
        District district = districtRepository.findById(requestDto.getDistrictId())
                .orElseThrow(() -> new ResourceNotFoundException("District"));
        hotel.setDistrict(district);
        hotel.setAccount(account);
        
        Hotel savedHotel = hotelRepository.save(hotel);

        return new HotelResponseDto(
                savedHotel.getId(),
                savedHotel.getName(),
                savedHotel.getDescription(),
                savedHotel.getPricePerDay(),
                savedHotel.getHighLightImageUrl(),
                savedHotel.getStreetAddress(),
                savedHotel.getLatitude(),
                savedHotel.getLongitude(),
                null,
                0L,
                null,
                null
        );
    }

    @Override
    public Void update(Long id, AddHotelRequestDto requestDto, String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel"));
        if (Objects.equals(account.getId(), hotel.getAccount().getId())) {
            // Cập nhật thông tin khách sạn chỉ nếu có giá trị mới
            if (requestDto.getName() != null) {
                hotel.setName(requestDto.getName());
            }
            if (requestDto.getDescription() != null) {
                hotel.setDescription(requestDto.getDescription());
            }
            if (requestDto.getPricePerDay() != null) {
                hotel.setPricePerDay(requestDto.getPricePerDay());
            }
            if (requestDto.getStreetAddress() != null) {
                hotel.setStreetAddress(requestDto.getStreetAddress());
            }
            if (requestDto.getLatitude() != null) {
                hotel.setLatitude(requestDto.getLatitude());
            }
            if (requestDto.getLongitude() != null) {
                hotel.setLongitude(requestDto.getLongitude());
            }
            if (requestDto.getHighLightImageUrl() != null && !requestDto.getHighLightImageUrl().isEmpty()) {
                hotel.setHighLightImageUrl(cloudinaryService.uploadImage(requestDto.getHighLightImageUrl()));
            }
            if (requestDto.getDistrictId() != null) {
                hotel.setDistrict(districtRepository.findById(requestDto.getDistrictId())
                        .orElseThrow(() -> new ResourceNotFoundException("District")));
            }

            // Lưu cập nhật
            hotelRepository.save(hotel);
        } else {
            throw new RuntimeException("You do not have permission to update this hotel");
        }

        return null;
    }

    @Override
    public Void delete(Long id, String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel"));
        if (Objects.equals(account.getId(), hotel.getAccount().getId())) {
            hotelRepository.delete(hotel);
        }else{
            throw new RuntimeException("You not have permission to delete this hotel");
        }
        
        return  null;
    }

    @Override
    public List<ImageResponseDto> addImages(Long hotelId, AddImagesRequestDto requestDto, String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel"));

        // Kiểm tra quyền truy cập
        if (Objects.equals(account.getId(), hotel.getAccount().getId())) {
            List<ImageResponseDto> imageResponses = new ArrayList<>();

            for (MultipartFile file : requestDto.getImageUrls()) {
                if (file != null && !file.isEmpty()) {
                    // Tải hình ảnh lên Cloudinary
                    String imageUrl = cloudinaryService.uploadImage(file);

                    // Tạo đối tượng Image mới
                    Image image = new Image();
                    image.setImageUrl(imageUrl);
                    image.setEntityId(hotelId); 
                    image.setEntityType(EntityType.HOTEL);

                    // Lưu hình ảnh vào cơ sở dữ liệu
                    imageRepository.save(image);

                    // Thêm vào danh sách phản hồi
                    imageResponses.add(new ImageResponseDto(image.getId(), imageUrl));
                }
            }
            return imageResponses;
        } else {
            throw new RuntimeException("You do not have permission to add images to this hotel");
        }
    }

    @Override
    @Transactional
    public Void deleteImages(Long hotelId, List<Long> imageIds, String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        // Lấy thông tin khách sạn và kiểm tra quyền truy cập
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel"));

        // Kiểm tra xem tài khoản có phải là chủ sở hữu khách sạn không
        if (!Objects.equals(account.getId(), hotel.getAccount().getId())) {
            throw new RuntimeException("You do not have permission to delete images from this hotel");
        }

        // Lấy danh sách hình ảnh theo ID
        List<Image> images = imageRepository.findAllById(imageIds);

        // Kiểm tra xem tất cả hình ảnh có thuộc về khách sạn không
        for (Image image : images) {
            if (!Objects.equals(image.getEntityId(), hotelId) || !image.getEntityType().equals(EntityType.HOTEL)) {
                throw new RuntimeException("Image with ID " + image.getId() + " does not belong to this hotel");
            }
        }

        // Xóa tất cả hình ảnh khỏi cơ sở dữ liệu
        imageRepository.deleteAll(images);

        // Trả về phản hồi thành công
        return null;
    }
}
