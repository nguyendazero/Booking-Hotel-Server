package com.vinova.booking_hotel.repository;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.common.enums.BookingStatus;
import com.vinova.booking_hotel.config.TestPostgreSQLContainerConfig;
import com.vinova.booking_hotel.property.model.Booking;
import com.vinova.booking_hotel.property.model.District;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.repository.BookingRepository;
import com.vinova.booking_hotel.property.repository.DistrictRepository;
import com.vinova.booking_hotel.property.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestPostgreSQLContainerConfig.class)
public class BookingRepositoryTest {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private DistrictRepository districtRepository;

    private Hotel savedHotel;
    private Account savedAccount;

    @BeforeEach
    public void setUp() {

        District district = new District();
        district.setName("Test District");
        districtRepository.save(district);
        // Tạo và lưu Account
        Account account = new Account();
        account.setUsername("test_user");
        account.setEmail("test@example.com");
        account.setFullName("Test User");
        savedAccount = accountRepository.save(account);

        // Tạo và lưu Hotel, gán Account và District ID cho Hotel
        Hotel hotel = new Hotel();
        hotel.setName("Test Hotel");
        hotel.setAccount(savedAccount);
        hotel.setDistrict(district);
        savedHotel = hotelRepository.save(hotel);

        // Xóa tất cả các booking trước mỗi bài kiểm tra
        bookingRepository.deleteAll();
    }

    @Test
    public void testFindByHotelIdAndDateRange() {
        // Arrange
        Booking booking1 = new Booking();
        booking1.setHotel(savedHotel); // Sử dụng savedHotel
        booking1.setAccount(savedAccount); // Sử dụng savedAccount
        booking1.setStartDate(ZonedDateTime.now().plusDays(1));
        booking1.setEndDate(ZonedDateTime.now().plusDays(3));
        booking1.setTotalPrice(BigDecimal.valueOf(100));
        booking1.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking1);

        Booking booking2 = new Booking();
        booking2.setHotel(savedHotel); // Sử dụng savedHotel
        booking2.setAccount(savedAccount); // Sử dụng savedAccount
        booking2.setStartDate(ZonedDateTime.now().plusDays(4));
        booking2.setEndDate(ZonedDateTime.now().plusDays(5));
        booking2.setTotalPrice(BigDecimal.valueOf(200));
        booking2.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking2);

        // Act
        List<Booking> foundBookings = bookingRepository.findByHotelIdAndDateRange(savedHotel.getId(), // Sử dụng savedHotel.getId()
                ZonedDateTime.now(),
                ZonedDateTime.now().plusDays(2));

        // Assert
        assertThat(foundBookings).hasSize(1);
        assertThat(foundBookings.getFirst()).isEqualTo(booking1);
    }
    
    @Test
    public void testFindByCreateDt() {
        // Arrange
        LocalDateTime now = LocalDateTime.now();
        ZoneId systemZone = ZoneId.systemDefault();
        ZonedDateTime zonedDateTimeNow = ZonedDateTime.of(now, systemZone);

        Booking booking = new Booking();
        booking.setHotel(savedHotel);
        booking.setAccount(savedAccount);
        booking.setStartDate(ZonedDateTime.now().plusDays(1));
        booking.setEndDate(ZonedDateTime.now().plusDays(3));
        booking.setTotalPrice(BigDecimal.valueOf(100));
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCreateDt(zonedDateTimeNow);
        bookingRepository.save(booking);

        // Act
        List<Booking> foundBookings = bookingRepository.findByCreateDt(now);

        // Assert
        assertThat(foundBookings).hasSize(1);
        assertThat(foundBookings.getFirst()).isEqualTo(booking);
    }

    @Test
    public void testFindByHotelId() {
        // Arrange
        Booking booking = new Booking();
        booking.setHotel(savedHotel);
        booking.setAccount(savedAccount);
        booking.setStartDate(ZonedDateTime.now().plusDays(1));
        booking.setEndDate(ZonedDateTime.now().plusDays(3));
        booking.setTotalPrice(BigDecimal.valueOf(100));
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Act
        List<Booking> foundBookings = bookingRepository.findByHotelId(savedHotel.getId());

        // Assert
        assertThat(foundBookings).hasSize(1);
        assertThat(foundBookings.getFirst()).isEqualTo(booking);
    }

    @Test
    public void testFindFirstByHotelAndAccount() {
        // Arrange
        Booking booking = new Booking();
        booking.setHotel(savedHotel);
        booking.setAccount(savedAccount);
        booking.setStartDate(ZonedDateTime.now().plusDays(1));
        booking.setEndDate(ZonedDateTime.now().plusDays(3));
        booking.setTotalPrice(BigDecimal.valueOf(100));
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Act
        Booking foundBooking = bookingRepository.findFirstByHotelAndAccount(savedHotel, savedAccount).orElse(null);

        // Assert
        assertThat(foundBooking).isNotNull();
        assertThat(foundBooking).isEqualTo(booking);
    }

    @Test
    public void testDeleteBookingsByHotelId() {
        // Arrange
        Booking booking = new Booking();
        booking.setHotel(savedHotel);
        booking.setAccount(savedAccount);
        booking.setStartDate(ZonedDateTime.now().plusDays(1));
        booking.setEndDate(ZonedDateTime.now().plusDays(3));
        booking.setTotalPrice(BigDecimal.valueOf(100));
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        // Act
        bookingRepository.deleteBookingsByHotelId(savedHotel.getId());

        // Assert
        List<Booking> foundBookings = bookingRepository.findByHotelId(savedHotel.getId());
        assertThat(foundBookings).isEmpty();
    }
}