package com.vinova.booking_hotel.repository;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.property.model.District;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.Rating;
import com.vinova.booking_hotel.property.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RatingRepositoryTest {

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Hotel testHotel1;
    private Hotel testHotel2;
    private Account testAccount2;

    @BeforeEach
    void setUp() {
        ratingRepository.deleteAll();
        entityManager.clear();

        Account testAccount1 = new Account();
        testAccount1.setFullName("Account 1");
        testAccount1.setEmail("account1@example.com");
        testAccount1.setUsername("user1");
        testAccount1.setPassword("pass1");
        entityManager.persist(testAccount1);

        testAccount2 = new Account();
        testAccount2.setFullName("Account 2");
        testAccount2.setEmail("account2@example.com");
        testAccount2.setUsername("user2");
        testAccount2.setPassword("pass2");
        entityManager.persist(testAccount2);

        // Tạo và persist các đối tượng District
        District district1 = new District();
        district1.setName("District 1");
        entityManager.persist(district1);

        District district2 = new District();
        district2.setName("District 2");
        entityManager.persist(district2);

        testHotel1 = new Hotel();
        testHotel1.setName("Hotel 1");
        testHotel1.setAccount(testAccount1);
        testHotel1.setDistrict(district1); // Gán District cho Hotel 1
        entityManager.persist(testHotel1);

        testHotel2 = new Hotel();
        testHotel2.setName("Hotel 2");
        testHotel2.setAccount(testAccount2);
        testHotel2.setDistrict(district2); // Gán District cho Hotel 2
        entityManager.persist(testHotel2);

        Rating rating1 = new Rating();
        rating1.setHotel(testHotel1);
        rating1.setAccount(testAccount1);
        rating1.setStars(5);
        rating1.setContent("Great stay!");
        entityManager.persist(rating1);

        Rating rating2 = new Rating();
        rating2.setHotel(testHotel1);
        rating2.setAccount(testAccount2);
        rating2.setStars(4);
        rating2.setContent("Good experience.");
        entityManager.persist(rating2);

        Rating rating3 = new Rating();
        rating3.setHotel(testHotel2);
        rating3.setAccount(testAccount1);
        rating3.setStars(3);
        rating3.setContent("Average.");
        entityManager.persist(rating3);

        entityManager.flush();
    }

    @Test
    void findByHotelId_shouldReturnRatingsForGivenHotelId() {
        // Act
        List<Rating> ratings = ratingRepository.findByHotelId(testHotel1.getId());

        // Assert
        assertThat(ratings).hasSize(2);
        assertThat(ratings.get(0).getStars()).isEqualTo(5);
        assertThat(ratings.get(1).getStars()).isEqualTo(4);
    }

    @Test
    void findByHotelId_shouldReturnEmptyListWhenNoRatingsForHotelId() {
        // Act
        List<Rating> ratings = ratingRepository.findByHotelId(999L); // Non-existent hotel ID

        // Assert
        assertThat(ratings).isEmpty();
    }

    @Test
    void countByHotel_shouldReturnCorrectCountForGivenHotel() {
        // Act
        Long countHotel1 = ratingRepository.countByHotel(testHotel1);
        Long countHotel2 = ratingRepository.countByHotel(testHotel2);

        // Assert
        assertThat(countHotel1).isEqualTo(2);
        assertThat(countHotel2).isEqualTo(1);
    }

    @Test
    @Transactional
    void deleteRatingsByHotelId_shouldDeleteAllRatingsForGivenHotelId() {
        // Act
        ratingRepository.deleteRatingsByHotelId(testHotel1.getId());
        entityManager.flush();
        entityManager.clear(); // Clear persistence context to see the changes

        // Assert
        List<Rating> remainingRatingsForHotel1 = ratingRepository.findByHotelId(testHotel1.getId());
        assertThat(remainingRatingsForHotel1).isEmpty();

        List<Rating> ratingsForHotel2 = ratingRepository.findByHotelId(testHotel2.getId());
        assertThat(ratingsForHotel2).hasSize(1); // Ensure ratings for other hotels are not deleted
    }

    @Test
    @Transactional
    void deleteRatingsByHotelId_shouldNotAffectOtherHotels() {
        // Act
        ratingRepository.deleteRatingsByHotelId(testHotel1.getId());
        entityManager.flush();
        entityManager.clear();

        // Assert
        Long countHotel2 = ratingRepository.countByHotel(testHotel2);
        assertThat(countHotel2).isEqualTo(1);
    }

    @Test
    @Transactional
    void deleteRatingsByHotelId_shouldNotDeleteAnythingForNonExistentHotelId() {
        // Act
        ratingRepository.deleteRatingsByHotelId(999L); // Non-existent hotel ID
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(ratingRepository.count()).isEqualTo(3); // Ensure no ratings were deleted
    }
}