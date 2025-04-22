package com.vinova.booking_hotel.repository;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.config.TestPostgreSQLContainerConfig;
import com.vinova.booking_hotel.property.model.District;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.Rating;
import com.vinova.booking_hotel.property.repository.DistrictRepository;
import com.vinova.booking_hotel.property.repository.HotelRepository;
import com.vinova.booking_hotel.property.repository.RatingRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestPostgreSQLContainerConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class HotelRepositoryTest {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private EntityManager entityManager;

    private Hotel testHotel1;
    private Hotel testHotel2;
    private Account testAccount;
    private District testDistrict;
    private Account ratingAccount;

    @BeforeEach
    void setUp() {
        hotelRepository.deleteAll();
        ratingRepository.deleteAll();
        accountRepository.deleteAll();
        districtRepository.deleteAll();

        // Tạo và lưu Account
        testAccount = new Account();
        testAccount.setFullName("Test Account");
        testAccount.setEmail("test@example.com");
        testAccount.setUsername("testuser");
        accountRepository.save(testAccount);

        // Tạo và lưu District
        testDistrict = new District();
        testDistrict.setName("Test District");
        districtRepository.save(testDistrict);

        // Tạo và lưu Hotels
        testHotel1 = new Hotel();
        testHotel1.setName("Test Hotel 1");
        testHotel1.setAccount(testAccount);
        testHotel1.setDistrict(testDistrict);
        hotelRepository.save(testHotel1);

        testHotel2 = new Hotel();
        testHotel2.setName("Test Hotel 2");
        testHotel2.setAccount(testAccount);
        testHotel2.setDistrict(testDistrict);
        hotelRepository.save(testHotel2);

        // Tạo và lưu Account cho Rating
        ratingAccount = new Account();
        ratingAccount.setFullName("Rating Account");
        ratingAccount.setEmail("rating@example.com");
        ratingAccount.setUsername("ratinguser");
        accountRepository.save(ratingAccount);
    }

    @Test
    void testFindById_ShouldReturnHotelIfExists() {
        Optional<Hotel> foundHotel = hotelRepository.findById(testHotel1.getId());
        assertThat(foundHotel).isPresent();
        assertThat(foundHotel.get().getName()).isEqualTo("Test Hotel 1");
    }

    @Test
    void testFindById_ShouldReturnEmptyOptionalIfNotExists() {
        Optional<Hotel> foundHotel = hotelRepository.findById(999L);
        assertThat(foundHotel).isEmpty();
    }

    @Test
    void testFindAll_ShouldReturnAllHotels() {
        List<Hotel> allHotels = hotelRepository.findAll();
        assertThat(allHotels).hasSize(2);
        assertThat(allHotels).contains(testHotel1, testHotel2);
    }

    @Test
    void testSave_ShouldPersistNewHotel() {
        Hotel newHotel = new Hotel();
        newHotel.setName("New Hotel");
        newHotel.setAccount(testAccount);
        newHotel.setDistrict(testDistrict);
        Hotel savedHotel = hotelRepository.save(newHotel);
        assertThat(savedHotel.getId()).isNotNull();
        Optional<Hotel> retrievedHotel = hotelRepository.findById(savedHotel.getId());
        assertThat(retrievedHotel).isPresent();
        assertThat(retrievedHotel.get().getName()).isEqualTo("New Hotel");
    }

    @Test
    void testFindAverageRatingByHotelId_WhenRatingsExist() {
        // Tạo và lưu Ratings cho testHotel1
        Rating rating1 = new Rating();
        rating1.setHotel(testHotel1);
        rating1.setStars(4);
        rating1.setAccount(ratingAccount);
        rating1.setContent("Good");
        ratingRepository.save(rating1);

        Rating rating2 = new Rating();
        rating2.setHotel(testHotel1);
        rating2.setStars(5);
        rating2.setAccount(ratingAccount);
        rating2.setContent("Excellent");
        ratingRepository.save(rating2);

        // Tạo và lưu Rating cho testHotel2
        Rating rating3 = new Rating();
        rating3.setHotel(testHotel2);
        rating3.setStars(3);
        rating3.setAccount(ratingAccount);
        rating3.setContent("Average");
        ratingRepository.save(rating3);

        Double averageRating = hotelRepository.findAverageRatingByHotelId(testHotel1.getId());
        assertThat(averageRating).isEqualTo(4.5);
    }

    @Test
    void testFindAverageRatingByHotelId_WhenNoRatingsExist() {
        Double averageRating = hotelRepository.findAverageRatingByHotelId(testHotel1.getId());
        assertThat(averageRating).isEqualTo(0.0);
    }

    @Test
    @Transactional
    void testDeleteHotelById_ShouldDeleteHotelAndRelatedEntities() {
        // Lấy lại Hotel từ database để đảm bảo nó được quản lý trong transaction hiện tại
        Hotel managedHotel = entityManager.find(Hotel.class, testHotel1.getId());

        // Tạo và lưu một Rating liên quan đến managedHotel
        Rating rating = new Rating();
        rating.setHotel(managedHotel);
        rating.setStars(5);
        rating.setAccount(ratingAccount);
        rating.setContent("Superb");
        entityManager.persist(rating);
        entityManager.flush();

        long initialHotelCount = hotelRepository.count();
        long initialRatingCount = ratingRepository.count();

        // Xóa Rating trước
        entityManager.remove(rating);
        entityManager.flush();

        // Sau đó xóa Hotel
        entityManager.remove(managedHotel);
        entityManager.flush();

        assertThat(hotelRepository.count()).isEqualTo(initialHotelCount - 1);
        assertThat(hotelRepository.findById(testHotel1.getId())).isEmpty();
        assertThat(ratingRepository.count()).isEqualTo(initialRatingCount - 1);
    }

    @Test
    @Transactional
    void testDeleteHotelById_ShouldNotAffectOtherHotels() {
        long initialHotelCount = hotelRepository.count();
        hotelRepository.deleteHotelById(testHotel1.getId());
        assertThat(hotelRepository.count()).isEqualTo(initialHotelCount - 1);
        assertThat(hotelRepository.findById(testHotel2.getId())).isPresent();
    }
}