package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.dto.response.AccountResponseDto;
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
    private final HotelDiscountRepository hotelDiscountRepository;
    private final HotelAmenityRepository hotelAmenityRepository;
    private final WishListRepository wishListRepository;

    @Override
    public List<HotelResponseDto> hotels(Long accountId, Long districtId, String name,
                                         BigDecimal minPrice, BigDecimal maxPrice,
                                         List<String> amenityNames,
                                         ZonedDateTime startDate, ZonedDateTime endDate,
                                         int pageIndex, int pageSize, String sortBy, String sortOrder) {

        if (pageIndex < 0 || pageSize <= 0) {
            throw new InvalidPageOrSizeException();
        }

        Specification<Hotel> spec = Specification
                .where(HotelSpecification.hasAccountId(accountId))
                .and(HotelSpecification.hasDistrictId(districtId))
                .and(HotelSpecification.hasName(name))
                .and(HotelSpecification.hasMinPrice(minPrice))
                .and(HotelSpecification.hasMaxPrice(maxPrice))
                .and(HotelSpecification.hasAmenityNames(amenityNames))
                .and(HotelSpecification.isAvailableBetween(startDate, endDate));

        List<Hotel> filteredHotels;
        boolean sortByRatings = "ratings".equalsIgnoreCase(sortBy);

        if (sortByRatings) {
            filteredHotels = hotelRepository.findAll(spec); // Không phân trang
        } else {
            Sort sort = Sort.by(sortOrder.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
            Pageable pageable = PageRequest.of(pageIndex, pageSize, sort);
            Page<Hotel> hotelPage = hotelRepository.findAll(spec, pageable);
            filteredHotels = hotelPage.getContent();
        }

        // Lọc lại theo amenityNames (nếu có)
        filteredHotels = filteredHotels.stream()
                .filter(hotel -> {
                    if (amenityNames == null) return true;
                    long count = hotel.getHotelAmenities().stream()
                            .filter(hotelAmenity -> amenityNames.contains(hotelAmenity.getAmenity().getName()))
                            .count();
                    return count == amenityNames.size();
                })
                .collect(Collectors.toList());

        // Tính rating và số lượt đánh giá
        Map<Long, Double> averageRatings = new HashMap<>();
        Map<Long, Long> reviewCounts = new HashMap<>();

        for (Hotel hotel : filteredHotels) {
            Double averageRating = hotelRepository.findAverageRatingByHotelId(hotel.getId());
            Long reviewCount = ratingRepository.countByHotel(hotel);
            averageRatings.put(hotel.getId(), averageRating);
            reviewCounts.put(hotel.getId(), reviewCount);
        }

        // Nếu cần sort theo ratings, sort sau khi đã có ratings
        if (sortByRatings) {
            Comparator<Hotel> comparator = Comparator.comparing(
                    h -> Optional.ofNullable(averageRatings.get(h.getId())).orElse(0.0)
            );
            if ("desc".equalsIgnoreCase(sortOrder)) {
                comparator = comparator.reversed();
            }
            filteredHotels.sort(comparator);

            // Thực hiện phân trang thủ công
            int fromIndex = Math.min(pageIndex * pageSize, filteredHotels.size());
            int toIndex = Math.min(fromIndex + pageSize, filteredHotels.size());
            filteredHotels = filteredHotels.subList(fromIndex, toIndex);
        }

        // Mapping sang DTO
        return filteredHotels.stream().map(hotel -> {
            DiscountResponseDto discountResponseDto = hotel.getHotelDiscounts().stream()
                    .findFirst()
                    .map(hotelDiscount -> new DiscountResponseDto(
                            hotelDiscount.getDiscount().getId(),
                            hotelDiscount.getDiscount().getRate()))
                    .orElse(null);

            List<Image> images = imageRepository.findByEntityIdAndEntityType(hotel.getId(), EntityType.HOTEL);
            List<ImageResponseDto> imageResponses = images.stream()
                    .map(image -> new ImageResponseDto(image.getId(), image.getImageUrl()))
                    .toList();

            Account owner = accountRepository.findById(hotel.getAccount().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account"));

            AccountResponseDto accountResponseDto = new AccountResponseDto(
                    owner.getId(),
                    owner.getFullName(),
                    owner.getUsername(),
                    owner.getEmail(),
                    owner.getAvatar(),
                    owner.getPhone(),
                    owner.getBlockReason(),
                    null
            );

            return new HotelResponseDto(
                    hotel.getId(),
                    hotel.getName(),
                    hotel.getDescription(),
                    hotel.getPricePerDay(),
                    hotel.getHighLightImageUrl(),
                    hotel.getStreetAddress(),
                    hotel.getLatitude(),
                    hotel.getLongitude(),
                    Optional.ofNullable(averageRatings.get(hotel.getId())).orElse(0.0),
                    Optional.ofNullable(reviewCounts.get(hotel.getId())).orElse(0L),
                    discountResponseDto,
                    accountResponseDto,
                    imageResponses,
                    null
            );
        }).toList();
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

        Map<Long, Double> averageRatings = new HashMap<>();
        Map<Long, Long> reviewCounts = new HashMap<>();

        for (Hotel hotel : hotels) {
            Double averageRating = hotelRepository.findAverageRatingByHotelId(hotel.getId());
            System.out.println("Hotel ID: " + hotel.getId() + ", averageRating: " + averageRating);
            Long reviewCount = ratingRepository.countByHotel(hotel);
            averageRatings.put(hotel.getId(), averageRating);
            reviewCounts.put(hotel.getId(), reviewCount);
        }

        return hotels.stream().map(hotel -> {
            DiscountResponseDto discountResponseDto = hotel.getHotelDiscounts().stream()
                    .findFirst()
                    .map(hotelDiscount -> new DiscountResponseDto(hotelDiscount.getDiscount().getId(), hotelDiscount.getDiscount().getRate()))
                    .orElse(null);

            Account owner = accountRepository.findById(hotel.getAccount().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Account"));
            AccountResponseDto accountResponseDto = new AccountResponseDto(
                    owner.getId(),
                    owner.getFullName(),
                    owner.getUsername(),
                    owner.getEmail(),
                    owner.getAvatar(),
                    owner.getPhone(),
                    owner.getBlockReason(),
                    null
            );

            return new HotelResponseDto(
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
                    discountResponseDto,
                    accountResponseDto,
                    null,
                    null
            );
        }).toList();
    }


    @Override
    public HotelResponseDto hotel(Long id) {
        // Tìm khách sạn theo ID
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel"));

        // Lấy điểm đánh giá trung bình của khách sạn
        Double averageRating = hotelRepository.findAverageRatingByHotelId(id);
        Long reviewCount = ratingRepository.countByHotel(hotel);

        // Lấy danh sách đặt phòng của khách sạn
        List<Booking> bookings = bookingRepository.findByHotelId(id);
        ZonedDateTime now = ZonedDateTime.now();
        List<DateRangeResponseDto> bookedDates = bookings.stream()
                .filter(booking -> booking.getStartDate().isAfter(now))
                .map(booking -> new DateRangeResponseDto(booking.getStartDate(), booking.getEndDate()))
                .toList();

        // Lấy danh sách hình ảnh của khách sạn
        List<Image> images = imageRepository.findByEntityIdAndEntityType(id, EntityType.HOTEL);
        List<ImageResponseDto> imageResponses = images.stream()
                .map(image -> new ImageResponseDto(image.getId(), image.getImageUrl()))
                .toList();

        // Lấy thông tin giảm giá (nếu có)
        DiscountResponseDto discountResponseDto = hotel.getHotelDiscounts().stream()
                .findFirst()
                .map(hotelDiscount -> new DiscountResponseDto(hotelDiscount.getDiscount().getId(), hotelDiscount.getDiscount().getRate()))
                .orElse(null);

        Account owner = accountRepository.findById(hotel.getAccount().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Account"));
        AccountResponseDto accountResponseDto = new AccountResponseDto(
                owner.getId(),
                owner.getFullName(),
                owner.getUsername(),
                owner.getEmail(),
                owner.getAvatar(),
                owner.getPhone(),
                owner.getBlockReason(),
                null
        );

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
                discountResponseDto,
                accountResponseDto,
                imageResponses,
                bookedDates
        );
    }


    @Override
    public HotelResponseDto create(AddHotelRequestDto requestDto, String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        Account owner = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));
        AccountResponseDto accountResponseDto = new AccountResponseDto(
                owner.getId(),
                owner.getFullName(),
                owner.getUsername(),
                owner.getEmail(),
                owner.getAvatar(),
                owner.getPhone(),
                owner.getBlockReason(),
                null
        );
        
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
                accountResponseDto,
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
    @Transactional
    public Void delete(Long id, String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel"));

        // Kiểm tra quyền xóa
        if (!account.getId().equals(hotel.getAccount().getId())) {
            throw new RuntimeException("You do not have permission to delete this hotel");
        }

        // Xóa tất cả các thực thể liên quan
        bookingRepository.deleteBookingsByHotelId(id);
        hotelDiscountRepository.deleteDiscountsByHotelId(id);
        wishListRepository.deleteWishListsByHotelId(id);
        hotelAmenityRepository.deleteAmenitiesByHotelId(id);
        ratingRepository.deleteRatingsByHotelId(id);

        // Cuối cùng, xóa khách sạn
        hotelRepository.deleteHotelById(hotel.getId());

        return null;
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
