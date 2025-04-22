package com.vinova.booking_hotel.repository;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.config.TestPostgreSQLContainerConfig;
import com.vinova.booking_hotel.property.model.Amenity;
import com.vinova.booking_hotel.property.model.District;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.HotelAmenity;
import com.vinova.booking_hotel.property.repository.AmenityRepository;
import com.vinova.booking_hotel.property.repository.DistrictRepository;
import com.vinova.booking_hotel.property.repository.HotelAmenityRepository;
import com.vinova.booking_hotel.property.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestPostgreSQLContainerConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class HotelAmenityRepositoryTest {

    @Autowired
    private HotelAmenityRepository hotelAmenityRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private AmenityRepository amenityRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DistrictRepository districtRepository;

    private Hotel testHotel;
    private Amenity testAmenity1;
    private Amenity testAmenity2;

    @BeforeEach
    public void setUp() {
        hotelAmenityRepository.deleteAll();
        hotelRepository.deleteAll();
        amenityRepository.deleteAll();
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

        // Tạo và lưu Hotel
        testHotel = new Hotel();
        testHotel.setName("Test Hotel");
        testHotel.setAccount(account);
        testHotel.setDistrict(district);
        hotelRepository.save(testHotel);

        // Tạo và lưu Amenities
        testAmenity1 = new Amenity();
        testAmenity1.setName("Free WiFi");
        amenityRepository.save(testAmenity1);

        testAmenity2 = new Amenity();
        testAmenity2.setName("Pool");
        amenityRepository.save(testAmenity2);
    }

    @Test
    public void testFindByHotelAndAmenity_WhenRelationExists() {
        // Tạo và lưu HotelAmenity
        HotelAmenity hotelAmenity = new HotelAmenity();
        hotelAmenity.setHotel(testHotel);
        hotelAmenity.setAmenity(testAmenity1);
        hotelAmenityRepository.save(hotelAmenity);

        // Tìm kiếm
        Optional<HotelAmenity> foundRelation = hotelAmenityRepository.findByHotelAndAmenity(testHotel, testAmenity1);

        // Assert
        assertThat(foundRelation).isPresent();
        assertThat(foundRelation.get().getHotel()).isEqualTo(testHotel);
        assertThat(foundRelation.get().getAmenity()).isEqualTo(testAmenity1);
    }

    @Test
    public void testFindByHotelAndAmenity_WhenRelationDoesNotExist() {
        // Tìm kiếm khi không có relation nào được tạo
        Optional<HotelAmenity> foundRelation = hotelAmenityRepository.findByHotelAndAmenity(testHotel, testAmenity2);

        // Assert
        assertThat(foundRelation).isEmpty();
    }

    @Test
    @Transactional
    public void testDeleteAmenitiesByHotelId_WhenRelationsExist() {
        // Tạo và lưu các HotelAmenity cho testHotel
        HotelAmenity hotelAmenity1 = new HotelAmenity();
        hotelAmenity1.setHotel(testHotel);
        hotelAmenity1.setAmenity(testAmenity1);
        hotelAmenityRepository.save(hotelAmenity1);

        HotelAmenity hotelAmenity2 = new HotelAmenity();
        hotelAmenity2.setHotel(testHotel);
        hotelAmenity2.setAmenity(testAmenity2);
        hotelAmenityRepository.save(hotelAmenity2);

        // Tạo và lưu HotelAmenity cho một hotel khác
        Hotel anotherHotel = new Hotel();
        anotherHotel.setName("Another Hotel");
        anotherHotel.setAccount(testHotel.getAccount());
        anotherHotel.setDistrict(testHotel.getDistrict());
        hotelRepository.save(anotherHotel);
        HotelAmenity hotelAmenityForAnotherHotel = new HotelAmenity();
        hotelAmenityForAnotherHotel.setHotel(anotherHotel);
        hotelAmenityForAnotherHotel.setAmenity(testAmenity1);
        hotelAmenityRepository.save(hotelAmenityForAnotherHotel);

        // Gọi phương thức deleteAmenitiesByHotelId
        hotelAmenityRepository.deleteAmenitiesByHotelId(testHotel.getId());

        // Kiểm tra xem các relation của testHotel đã bị xóa
        Optional<HotelAmenity> foundRelation1 = hotelAmenityRepository.findByHotelAndAmenity(testHotel, testAmenity1);
        Optional<HotelAmenity> foundRelation2 = hotelAmenityRepository.findByHotelAndAmenity(testHotel, testAmenity2);
        assertThat(foundRelation1).isEmpty();
        assertThat(foundRelation2).isEmpty();

        // Kiểm tra xem relation của hotel khác vẫn tồn tại
        Optional<HotelAmenity> foundRelationAnotherHotel = hotelAmenityRepository.findByHotelAndAmenity(anotherHotel, testAmenity1);
        assertThat(foundRelationAnotherHotel).isPresent();
    }

    @Test
    @Transactional
    public void testDeleteAmenitiesByHotelId_WhenNoRelationsExist() {
        // Gọi phương thức deleteAmenitiesByHotelId khi không có relation nào cho testHotel
        hotelAmenityRepository.deleteAmenitiesByHotelId(testHotel.getId());

        // Kiểm tra xem không có lỗi xảy ra và không có relation nào bị xóa (vì không có)
        Optional<HotelAmenity> foundRelation = hotelAmenityRepository.findByHotelAndAmenity(testHotel, testAmenity1);
        assertThat(foundRelation).isEmpty();
    }
}