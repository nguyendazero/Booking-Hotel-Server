package com.vinova.booking_hotel.repository;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.property.model.Discount;
import com.vinova.booking_hotel.property.model.District;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.HotelDiscount;
import com.vinova.booking_hotel.property.repository.DiscountRepository;
import com.vinova.booking_hotel.property.repository.DistrictRepository;
import com.vinova.booking_hotel.property.repository.HotelDiscountRepository;
import com.vinova.booking_hotel.property.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class HotelDiscountRepositoryTest {

    @Autowired
    private HotelDiscountRepository hotelDiscountRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private DiscountRepository discountRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DistrictRepository districtRepository;

    private Hotel testHotel1;
    private Hotel testHotel2;
    private Discount testDiscount1;
    private Discount testDiscount2;
    private ZonedDateTime now;
    private ZonedDateTime future;
    private ZonedDateTime past;

    @BeforeEach
    void setUp() {
        hotelDiscountRepository.deleteAll();
        hotelRepository.deleteAll();
        discountRepository.deleteAll();
        accountRepository.deleteAll();
        districtRepository.deleteAll();

        // Tạo và lưu Account
        Account account = new Account();
        account.setFullName("Test Account");
        account.setEmail("test@example.com");
        account.setUsername("testuser");
        accountRepository.save(account);

        // Tạo và lưu District
        District district = new District();
        district.setName("Test District");
        districtRepository.save(district);

        // Tạo và lưu Hotels
        testHotel1 = new Hotel();
        testHotel1.setName("Test Hotel 1");
        testHotel1.setAccount(account);
        testHotel1.setDistrict(district);
        hotelRepository.save(testHotel1);

        testHotel2 = new Hotel();
        testHotel2.setName("Test Hotel 2");
        testHotel2.setAccount(account);
        testHotel2.setDistrict(district);
        hotelRepository.save(testHotel2);

        // Tạo và lưu Discounts
        testDiscount1 = new Discount();
        testDiscount1.setRate(BigDecimal.valueOf(0.1));
        discountRepository.save(testDiscount1);

        testDiscount2 = new Discount();
        testDiscount2.setRate(BigDecimal.valueOf(0.2));
        discountRepository.save(testDiscount2);

        now = ZonedDateTime.now();
        future = now.plusDays(7);
        past = now.minusDays(7);
    }

    @Test
    void testFindByHotelId_WhenHotelDiscountsExist() {
        // Tạo và lưu HotelDiscounts cho testHotel1
        HotelDiscount hotelDiscount1 = new HotelDiscount();
        hotelDiscount1.setHotel(testHotel1);
        hotelDiscount1.setDiscount(testDiscount1);
        hotelDiscount1.setStartDate(past);
        hotelDiscount1.setEndDate(future);
        hotelDiscountRepository.save(hotelDiscount1);

        HotelDiscount hotelDiscount2 = new HotelDiscount();
        hotelDiscount2.setHotel(testHotel1);
        hotelDiscount2.setDiscount(testDiscount2);
        hotelDiscount2.setStartDate(now);
        hotelDiscount2.setEndDate(future.plusDays(3));
        hotelDiscountRepository.save(hotelDiscount2);

        // Tạo và lưu HotelDiscount cho testHotel2
        HotelDiscount hotelDiscount3 = new HotelDiscount();
        hotelDiscount3.setHotel(testHotel2);
        hotelDiscount3.setDiscount(testDiscount1);
        hotelDiscount3.setStartDate(past.minusDays(2));
        hotelDiscount3.setEndDate(now.plusDays(5));
        hotelDiscountRepository.save(hotelDiscount3);

        // Tìm kiếm hotelDiscounts cho testHotel1
        List<HotelDiscount> hotelDiscountsForHotel1 = hotelDiscountRepository.findByHotelId(testHotel1.getId());

        // Assert
        assertThat(hotelDiscountsForHotel1).hasSize(2);
        assertThat(hotelDiscountsForHotel1).contains(hotelDiscount1, hotelDiscount2);
    }

    @Test
    void testFindByHotelId_WhenNoHotelDiscountsExist() {
        // Tìm kiếm hotelDiscounts cho testHotel1 khi chưa có discount nào liên kết
        List<HotelDiscount> hotelDiscountsForHotel1 = hotelDiscountRepository.findByHotelId(testHotel1.getId());

        // Assert
        assertThat(hotelDiscountsForHotel1).isEmpty();
    }

    @Test
    void testFindByHotelIdAndDateRange_WhenHotelDiscountsOverlap() {
        // Tạo và lưu HotelDiscounts cho testHotel1
        HotelDiscount hotelDiscount1 = new HotelDiscount();
        hotelDiscount1.setHotel(testHotel1);
        hotelDiscount1.setDiscount(testDiscount1);
        hotelDiscount1.setStartDate(past);
        hotelDiscount1.setEndDate(future);
        hotelDiscountRepository.save(hotelDiscount1);

        HotelDiscount hotelDiscount2 = new HotelDiscount();
        hotelDiscount2.setHotel(testHotel1);
        hotelDiscount2.setDiscount(testDiscount2);
        hotelDiscount2.setStartDate(now.minusDays(2));
        hotelDiscount2.setEndDate(now.plusDays(2));
        hotelDiscountRepository.save(hotelDiscount2);

        // Xác định khoảng thời gian tìm kiếm
        ZonedDateTime searchStart = now.minusDays(1);
        ZonedDateTime searchEnd = now.plusDays(1);

        // Tìm kiếm hotelDiscounts trong khoảng thời gian
        List<HotelDiscount> overlappingHotelDiscounts = hotelDiscountRepository.findByHotelIdAndDateRange(
                testHotel1.getId(), searchStart, searchEnd);

        // Assert
        assertThat(overlappingHotelDiscounts).hasSize(2);
        assertThat(overlappingHotelDiscounts).contains(hotelDiscount1, hotelDiscount2);
    }

    @Test
    void testFindByHotelIdAndDateRange_WhenNoHotelDiscountsOverlap() {
        // Tạo và lưu HotelDiscount cho testHotel1
        HotelDiscount hotelDiscount1 = new HotelDiscount();
        hotelDiscount1.setHotel(testHotel1);
        hotelDiscount1.setDiscount(testDiscount1);
        hotelDiscount1.setStartDate(past);
        hotelDiscount1.setEndDate(past.plusDays(3));
        hotelDiscountRepository.save(hotelDiscount1);

        // Xác định khoảng thời gian tìm kiếm không trùng lặp
        ZonedDateTime searchStart = now.plusDays(1);
        ZonedDateTime searchEnd = now.plusDays(5);

        // Tìm kiếm hotelDiscounts trong khoảng thời gian
        List<HotelDiscount> overlappingHotelDiscounts = hotelDiscountRepository.findByHotelIdAndDateRange(
                testHotel1.getId(), searchStart, searchEnd);

        // Assert
        assertThat(overlappingHotelDiscounts).isEmpty();
    }

    @Test
    @Transactional
    void testDeleteDiscountsByHotelId_WhenHotelDiscountsExist() {
        // Tạo và lưu HotelDiscounts cho testHotel1
        HotelDiscount hotelDiscount1 = new HotelDiscount();
        hotelDiscount1.setHotel(testHotel1);
        hotelDiscount1.setDiscount(testDiscount1);
        hotelDiscount1.setStartDate(past);
        hotelDiscount1.setEndDate(future);
        hotelDiscountRepository.save(hotelDiscount1);

        HotelDiscount hotelDiscount2 = new HotelDiscount();
        hotelDiscount2.setHotel(testHotel1);
        hotelDiscount2.setDiscount(testDiscount2);
        hotelDiscount2.setStartDate(now);
        hotelDiscount2.setEndDate(future.plusDays(3));
        hotelDiscountRepository.save(hotelDiscount2);

        // Tạo và lưu HotelDiscount cho testHotel2
        HotelDiscount hotelDiscount3 = new HotelDiscount();
        hotelDiscount3.setHotel(testHotel2);
        hotelDiscount3.setDiscount(testDiscount1);
        hotelDiscount3.setStartDate(past.minusDays(2));
        hotelDiscount3.setEndDate(now.plusDays(5));
        hotelDiscountRepository.save(hotelDiscount3);

        // Xóa hotelDiscounts cho testHotel1
        hotelDiscountRepository.deleteDiscountsByHotelId(testHotel1.getId());

        // Kiểm tra xem hotelDiscounts của testHotel1 đã bị xóa
        List<HotelDiscount> hotelDiscountsForHotel1 = hotelDiscountRepository.findByHotelId(testHotel1.getId());
        assertThat(hotelDiscountsForHotel1).isEmpty();

        // Kiểm tra xem hotelDiscount của testHotel2 vẫn tồn tại
        List<HotelDiscount> hotelDiscountsForHotel2 = hotelDiscountRepository.findByHotelId(testHotel2.getId());
        assertThat(hotelDiscountsForHotel2).hasSize(1);
        assertThat(hotelDiscountsForHotel2).contains(hotelDiscount3);
    }

    @Test
    @Transactional
    void testDeleteDiscountsByHotelId_WhenNoHotelDiscountsExist() {
        // Gọi phương thức xóa khi không có hotelDiscounts cho testHotel1
        hotelDiscountRepository.deleteDiscountsByHotelId(testHotel1.getId());

        // Kiểm tra xem không có lỗi xảy ra và danh sách vẫn rỗng
        List<HotelDiscount> hotelDiscountsForHotel1 = hotelDiscountRepository.findByHotelId(testHotel1.getId());
        assertThat(hotelDiscountsForHotel1).isEmpty();
    }
}