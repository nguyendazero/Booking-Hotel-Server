package com.vinova.booking_hotel.repository;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.property.model.District;
import com.vinova.booking_hotel.property.model.Hotel;
import com.vinova.booking_hotel.property.model.WishList;
import com.vinova.booking_hotel.property.repository.WishListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class WishListRepositoryTest {

    @Autowired
    private WishListRepository wishListRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Account testAccount1;
    private Account testAccount2;
    private Hotel testHotel1;
    private Hotel testHotel2;

    @BeforeEach
    void setUp() {
        wishListRepository.deleteAll();
        entityManager.clear();

        testAccount1 = new Account();
        testAccount1.setUsername("user1");
        testAccount1.setPassword("pass1");
        testAccount1.setEmail("user1@example.com");
        testAccount1.setFullName("User One");
        // Các trường khác của Account (nếu cần)
        entityManager.persist(testAccount1);

        testAccount2 = new Account();
        testAccount2.setUsername("user2");
        testAccount2.setPassword("pass2");
        testAccount2.setEmail("user2@example.com");
        testAccount2.setFullName("User Two");
        // Các trường khác của Account (nếu cần)
        entityManager.persist(testAccount2);

        District district1 = new District();
        district1.setName("District 1");
        entityManager.persist(district1);

        District district2 = new District();
        district2.setName("District 2");
        entityManager.persist(district2);

        testHotel1 = new Hotel();
        testHotel1.setName("Hotel A");
        testHotel1.setPricePerDay(BigDecimal.TEN);
        testHotel1.setDistrict(district1);
        testHotel1.setAccount(testAccount1);
        // Các trường khác của Hotel (nếu cần)
        entityManager.persist(testHotel1);

        testHotel2 = new Hotel();
        testHotel2.setName("Hotel B");
        testHotel2.setPricePerDay(BigDecimal.ONE);
        testHotel2.setDistrict(district2);
        testHotel2.setAccount(testAccount2);
        // Các trường khác của Hotel (nếu cần)
        entityManager.persist(testHotel2);

        WishList wishList1 = new WishList();
        wishList1.setAccount(testAccount1);
        wishList1.setHotel(testHotel1);
        entityManager.persist(wishList1);

        WishList wishList2 = new WishList();
        wishList2.setAccount(testAccount1);
        wishList2.setHotel(testHotel2);
        entityManager.persist(wishList2);

        WishList wishList3 = new WishList();
        wishList3.setAccount(testAccount2);
        wishList3.setHotel(testHotel1);
        entityManager.persist(wishList3);

        entityManager.flush();
    }

    @Test
    void findByAccountAndHotel_shouldReturnWishListEntry_whenExists() {
        // Act
        WishList foundWishList = wishListRepository.findByAccountAndHotel(testAccount1, testHotel1);

        // Assert
        assertThat(foundWishList).isNotNull();
        assertThat(foundWishList.getAccount()).isEqualTo(testAccount1);
        assertThat(foundWishList.getHotel()).isEqualTo(testHotel1);
    }

    @Test
    void findByAccountAndHotel_shouldReturnNull_whenNotExists() {
        // Act
        WishList foundWishList = wishListRepository.findByAccountAndHotel(testAccount2, testHotel2);

        // Assert
        assertThat(foundWishList).isNull();
    }

    @Test
    @Transactional
    void deleteWishListsByHotelId_shouldDeleteAllWishListsForGivenHotelId() {
        // Act
        wishListRepository.deleteWishListsByHotelId(testHotel1.getId());
        entityManager.flush();
        entityManager.clear();

        // Assert
        List<WishList> wishListsForHotel1 = findAllByHotelId(testHotel1.getId());
        assertThat(wishListsForHotel1).isEmpty();

        WishList wishListForAccount2Hotel2 = wishListRepository.findByAccountAndHotel(testAccount2, testHotel2);
        assertThat(wishListForAccount2Hotel2).isNull(); // Phải là null vì Hotel B không có trong wishlist của Account 2
    }

    @Test
    @Transactional
    void deleteWishListsByHotelId_shouldNotAffectWishListsForOtherHotels() {
        // Act
        wishListRepository.deleteWishListsByHotelId(testHotel1.getId());
        entityManager.flush();
        entityManager.clear();

        // Assert
        WishList wishListForAccount1Hotel2 = wishListRepository.findByAccountAndHotel(testAccount1, testHotel2);
        assertThat(wishListForAccount1Hotel2).isNotNull();
    }

    @Test
    @Transactional
    void deleteWishListsByHotelId_shouldNotDeleteAnything_whenHotelIdDoesNotExist() {
        // Act
        wishListRepository.deleteWishListsByHotelId(999L);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertThat(wishListRepository.count()).isEqualTo(3); // Ensure no wishlists were deleted
    }

    // Helper method to find all wishlists by hotel ID (not in the original repository)
    private List<WishList> findAllByHotelId(Long hotelId) {
        return entityManager.getEntityManager()
                .createQuery("SELECT wl FROM WishList wl WHERE wl.hotel.id = :hotelId", WishList.class)
                .setParameter("hotelId", hotelId)
                .getResultList();
    }
}