package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.dto.response.*;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.common.enums.*;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.payment.dto.*;
import com.vinova.booking_hotel.payment.service.StripeService;
import com.vinova.booking_hotel.property.dto.request.AddBookingRequestDto;
import com.vinova.booking_hotel.property.dto.response.*;
import com.vinova.booking_hotel.property.model.*;
import com.vinova.booking_hotel.property.repository.*;
import com.vinova.booking_hotel.property.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    
    private final BookingRepository bookingRepository;
    private final HotelRepository hotelRepository;
    private final AccountRepository accountRepository; 
    private final JwtUtils jwtUtils;
    private final HotelDiscountRepository hotelDiscountRepository;
    private final StripeService stripeService;

    @Override
    public StripeResponseDto createBooking(AddBookingRequestDto requestDto, String token) {
        // Lấy accountId từ token
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("account"));

        // Tìm khách sạn theo hotelId
        Hotel hotel = hotelRepository.findById(requestDto.getHotelId())
                .orElseThrow(() -> new ResourceNotFoundException("Hotel"));

        // Kiểm tra thời gian đặt phòng
        if (requestDto.getStartDate().isAfter(requestDto.getEndDate())) {
            throw new RuntimeException("Start date must be before end date");
        }

        // Kiểm tra xem startDate và endDate không nằm trong quá khứ
        ZonedDateTime now = ZonedDateTime.now();
        if (requestDto.getStartDate().isBefore(now) || requestDto.getEndDate().isBefore(now)) {
            throw new RuntimeException("Booking dates must not be in the past");
        }

        // Kiểm tra xem có bất kỳ booking nào đã tồn tại cho khoảng thời gian này không
        List<Booking> existingBookings = bookingRepository.findByHotelIdAndDateRange(requestDto.getHotelId(), requestDto.getStartDate(), requestDto.getEndDate());

        // Kiểm tra nếu có booking đang hoạt động (không phải CANCELLED)
        boolean hasActiveBooking = existingBookings.stream()
                .anyMatch(booking -> booking.getStatus() != BookingStatus.CANCELLED);

        if (hasActiveBooking) {
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

        // Tạo PaymentRequestDto
        PaymentRequestDto paymentRequestDto = new PaymentRequestDto(
                savedBooking.getTotalPrice().longValue() * 100,
                1L,
                hotel.getName(),
                "USD"
        );

        // Gọi dịch vụ thanh toán
        StripeResponseDto stripe = stripeService.checkoutBooking(paymentRequestDto, savedBooking.getId());

        // Tạo đối tượng phản hồi StripeResponseDto
        StripeResponseDto response = new StripeResponseDto();
        response.setBooking(new BookingResponseDto(
                savedBooking.getId(),
                savedBooking.getStartDate(),
                savedBooking.getEndDate(),
                savedBooking.getTotalPrice(),
                booking.getStatus().toString(),
                savedBooking.getCreateDt(),
                new HotelResponseDto(
                        hotel.getId(),
                        hotel.getName(),
                        hotel.getDescription(),
                        hotel.getPricePerDay(),
                        hotel.getHighLightImageUrl(),
                        hotel.getStreetAddress(),
                        hotel.getLatitude(),
                        hotel.getLongitude(),
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                new AccountResponseDto(
                        account.getId(),
                        account.getFullName(),
                        account.getUsername(),
                        account.getEmail(),
                        account.getAvatar(),
                        account.getPhone(),
                        null
                )
        ));

        // Thêm thông tin phiên thanh toán vào phản hồi
        response.setSessionId(stripe.getSessionId());
        response.setSessionUrl(stripe.getSessionUrl());

        return response;
    }

    private BigDecimal calculateTotalPrice(AddBookingRequestDto requestDto, Hotel hotel) {
        ZonedDateTime startDate = requestDto.getStartDate();
        ZonedDateTime endDate = requestDto.getEndDate();
        BigDecimal totalPrice = BigDecimal.ZERO;

        // Lấy danh sách giảm giá của khách sạn
        List<HotelDiscount> hotelDiscounts = hotelDiscountRepository.findByHotelId(hotel.getId());

        // Duyệt từ startDate đến endDate theo từng ngày
        ZonedDateTime currentDate = startDate;

        while (currentDate.isBefore(endDate)) {
            BigDecimal dailyPrice = hotel.getPricePerDay();

            // Kiểm tra từng giảm giá để tính toán
            for (HotelDiscount hotelDiscount : hotelDiscounts) {
                // Kiểm tra xem ngày hiện tại có nằm trong khoảng giảm giá không
                if (hotelDiscount.getStartDate().isBefore(currentDate.plusDays(1)) && hotelDiscount.getEndDate().isAfter(currentDate)) {
                    // Nếu có, tính toán giá sau giảm giá
                    BigDecimal discountRate = hotelDiscount.getDiscount().getRate().divide(BigDecimal.valueOf(100));
                    dailyPrice = dailyPrice.multiply(BigDecimal.ONE.subtract(discountRate));
                }
            }

            // Cộng dồn giá vào totalPrice
            totalPrice = totalPrice.add(dailyPrice);

            // Tiến đến ngày tiếp theo
            currentDate = currentDate.plusDays(1);
        }

        return totalPrice;
    }

    @Override
    public Void cancelBooking(Long bookingId, String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        // Tìm booking theo bookingId
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking"));

        // Kiểm tra xem account có phải là chủ sở hữu của booking không
        if (!booking.getAccount().getId().equals(account.getId())) {
            throw new RuntimeException("You do not have permission to cancel this booking");
        }

        // Kiểm tra trạng thái hiện tại của booking
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only pending bookings can be cancelled");
        }

        // Cập nhật trạng thái của booking
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        return null;
    }

    @Override
    public Void confirmBooking(Long bookingId, String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        // Tìm booking theo bookingId
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking"));

        // Kiểm tra xem account có phải là chủ sở hữu của hotel không
        if (!booking.getHotel().getAccount().getId().equals(account.getId())) {
            throw new RuntimeException("You do not have permission to confirm this booking");
        }

        // Kiểm tra trạng thái hiện tại của booking
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only pending bookings can be confirmed");
        }

        // Cập nhật trạng thái của booking
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        return null;
    }

    @Override
    public List<BookingResponseDto> getBookingsByToken(String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));
        
        List<Booking> bookings = bookingRepository.findByAccount(account);

        return bookings.stream()
                .map(booking -> new BookingResponseDto(
                        booking.getId(),
                        booking.getStartDate(),
                        booking.getEndDate(),
                        booking.getTotalPrice(),
                        booking.getStatus().toString(),
                        booking.getCreateDt(),
                        new HotelResponseDto(
                                booking.getHotel().getId(),
                                booking.getHotel().getName(),
                                booking.getHotel().getDescription(),
                                booking.getHotel().getPricePerDay(),
                                booking.getHotel().getHighLightImageUrl(),
                                booking.getHotel().getStreetAddress(),
                                booking.getHotel().getLatitude(),
                                booking.getHotel().getLongitude(),
                                null, null, null, null, null
                        ),
                        new AccountResponseDto(
                                account.getId(),
                                account.getFullName(),
                                account.getUsername(),
                                account.getEmail(),
                                account.getAvatar(),
                                account.getPhone(),
                                null
                        )
                )).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getReservations(String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        // Lấy danh sách booking của tài khoản
        List<Booking> bookings = bookingRepository.findByAccount(account);

        // Lọc những booking nằm trong tương lai
        ZonedDateTime now = ZonedDateTime.now();

        return bookings.stream()
                .filter(booking -> booking.getStartDate().isAfter(now))
                .map(booking -> new BookingResponseDto(
                        booking.getId(),
                        booking.getStartDate(),
                        booking.getEndDate(),
                        booking.getTotalPrice(),
                        booking.getStatus().toString(),
                        booking.getCreateDt(),
                        new HotelResponseDto(
                                booking.getHotel().getId(),
                                booking.getHotel().getName(),
                                booking.getHotel().getDescription(),
                                booking.getHotel().getPricePerDay(),
                                booking.getHotel().getHighLightImageUrl(),
                                booking.getHotel().getStreetAddress(),
                                booking.getHotel().getLatitude(),
                                booking.getHotel().getLongitude(),
                                null, null, null, null, null
                        ),
                        new AccountResponseDto(
                                account.getId(),
                                account.getFullName(),
                                account.getUsername(),
                                account.getEmail(),
                                account.getAvatar(),
                                account.getPhone(),
                                null
                        )
                )).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getBookingsByHotelId(Long hotelId, String token) {
        // Lấy accountId từ token
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        // Tìm khách sạn theo hotelId
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel"));

        // Kiểm tra xem tài khoản có phải là chủ sở hữu khách sạn không
        if (!hotel.getAccount().getId().equals(accountId)) {
            throw new RuntimeException("You do not have permission to view bookings for this hotel.");
        }

        // Lấy danh sách booking cho khách sạn
        List<Booking> bookings = bookingRepository.findByHotel(hotel);

        // Chuyển đổi sang BookingResponseDto

        return bookings.stream()
                .map(booking -> new BookingResponseDto(
                        booking.getId(),
                        booking.getStartDate(),
                        booking.getEndDate(),
                        booking.getTotalPrice(),
                        booking.getStatus().toString(),
                        booking.getCreateDt(),
                        new HotelResponseDto(
                                hotel.getId(),
                                hotel.getName(),
                                hotel.getDescription(),
                                hotel.getPricePerDay(),
                                hotel.getHighLightImageUrl(),
                                hotel.getStreetAddress(),
                                hotel.getLatitude(),
                                hotel.getLongitude(),
                                null, null, null, null, null
                        ),
                        new AccountResponseDto(
                                account.getId(),
                                account.getFullName(),
                                account.getUsername(),
                                account.getEmail(),
                                account.getAvatar(),
                                account.getPhone(),
                                null
                        )
                )).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getReservationsByHotelId(Long hotelId, String token) {
        // Lấy accountId từ token
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account"));

        // Tìm khách sạn theo hotelId
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel"));

        // Kiểm tra xem tài khoản có phải là chủ sở hữu khách sạn không
        if (!hotel.getAccount().getId().equals(accountId)) {
            throw new RuntimeException("You do not have permission to view reservations for this hotel.");
        }

        // Lấy danh sách booking cho khách sạn
        List<Booking> bookings = bookingRepository.findByHotel(hotel);

        // Lọc booking nằm trong tương lai
        ZonedDateTime now = ZonedDateTime.now();
        // Chỉ lấy booking có ngày bắt đầu lớn hơn thời gian hiện tại

        return bookings.stream()
                .filter(booking -> booking.getStartDate().isAfter(now)) // Chỉ lấy booking có ngày bắt đầu lớn hơn thời gian hiện tại
                .map(booking -> new BookingResponseDto(
                        booking.getId(),
                        booking.getStartDate(),
                        booking.getEndDate(),
                        booking.getTotalPrice(),
                        booking.getStatus().toString(),
                        booking.getCreateDt(),
                        new HotelResponseDto(
                                hotel.getId(),
                                hotel.getName(),
                                hotel.getDescription(),
                                hotel.getPricePerDay(),
                                hotel.getHighLightImageUrl(),
                                hotel.getStreetAddress(),
                                hotel.getLatitude(),
                                hotel.getLongitude(),
                                null, null, null, null, null
                        ),
                        new AccountResponseDto(
                                account.getId(),
                                account.getFullName(),
                                account.getUsername(),
                                account.getEmail(),
                                account.getAvatar(),
                                account.getPhone(),
                                null
                        )
                )).collect(Collectors.toList());
    }

    @Override
    public List<BookingResponseDto> getAllBooking() {
        // Lấy danh sách tất cả booking
        List<Booking> bookings = bookingRepository.findAll();

        // Chuyển đổi danh sách booking sang BookingResponseDto

        return bookings.stream()
                .map(booking -> new BookingResponseDto(
                        booking.getId(),
                        booking.getStartDate(),
                        booking.getEndDate(),
                        booking.getTotalPrice(),
                        booking.getStatus().toString(),
                        booking.getCreateDt(),
                        new HotelResponseDto(
                                booking.getHotel().getId(),
                                booking.getHotel().getName(),
                                booking.getHotel().getDescription(),
                                booking.getHotel().getPricePerDay(),
                                booking.getHotel().getHighLightImageUrl(),
                                booking.getHotel().getStreetAddress(),
                                booking.getHotel().getLatitude(),
                                booking.getHotel().getLongitude(),
                                null, null, null, null, null
                        ),
                        new AccountResponseDto(
                                booking.getAccount().getId(),
                                booking.getAccount().getFullName(),
                                booking.getAccount().getUsername(),
                                booking.getAccount().getEmail(),
                                booking.getAccount().getAvatar(),
                                booking.getAccount().getPhone(),
                                null
                        )
                )).collect(Collectors.toList());
    }


}
