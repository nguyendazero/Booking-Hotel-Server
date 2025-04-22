package com.vinova.booking_hotel.repository;

import com.vinova.booking_hotel.common.enums.EntityType;
import com.vinova.booking_hotel.config.TestPostgreSQLContainerConfig;
import com.vinova.booking_hotel.property.model.Image;
import com.vinova.booking_hotel.property.repository.ImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestPostgreSQLContainerConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ImageRepositoryTest {

    @Autowired
    private ImageRepository imageRepository;

    @Autowired
    private TestEntityManager entityManager;

    @BeforeEach
    public void setUp() {
        imageRepository.deleteAll(); // Đảm bảo bảng image sạch trước mỗi test
    }

    @Test
    void findByEntityIdAndEntityType_shouldReturnImagesForGivenHotelId() {
        // Arrange
        Image image1 = new Image();
        image1.setEntityId(100L);
        image1.setEntityType(EntityType.HOTEL);
        image1.setImageUrl("url_hotel_1");
        entityManager.persist(image1);

        Image image2 = new Image();
        image2.setEntityId(100L);
        image2.setEntityType(EntityType.HOTEL);
        image2.setImageUrl("url_hotel_2");
        entityManager.persist(image2);

        Image image3 = new Image();
        image3.setEntityId(200L);
        image3.setEntityType(EntityType.HOTEL);
        image3.setImageUrl("url_other_hotel");
        entityManager.persist(image3);

        Image imageForReview = new Image();
        imageForReview.setEntityId(100L);
        imageForReview.setEntityType(EntityType.REVIEW);
        imageForReview.setImageUrl("url_review");
        entityManager.persist(imageForReview);

        entityManager.flush();

        // Act
        List<Image> foundImages = imageRepository.findByEntityIdAndEntityType(100L, EntityType.HOTEL);

        // Assert
        assertThat(foundImages).hasSize(2);
        assertThat(foundImages.get(0).getImageUrl()).isEqualTo("url_hotel_1");
        assertThat(foundImages.get(1).getImageUrl()).isEqualTo("url_hotel_2");
    }

    @Test
    void findByEntityIdAndEntityType_shouldReturnImagesForGivenReviewId() {
        // Arrange
        Image image1 = new Image();
        image1.setEntityId(300L);
        image1.setEntityType(EntityType.REVIEW);
        image1.setImageUrl("url_review_1");
        entityManager.persist(image1);

        Image image2 = new Image();
        image2.setEntityId(300L);
        image2.setEntityType(EntityType.REVIEW);
        image2.setImageUrl("url_review_2");
        entityManager.persist(image2);

        Image imageForHotel = new Image();
        imageForHotel.setEntityId(300L);
        imageForHotel.setEntityType(EntityType.HOTEL);
        imageForHotel.setImageUrl("url_hotel");
        entityManager.persist(imageForHotel);

        entityManager.flush();

        // Act
        List<Image> foundImages = imageRepository.findByEntityIdAndEntityType(300L, EntityType.REVIEW);

        // Assert
        assertThat(foundImages).hasSize(2);
        assertThat(foundImages.get(0).getImageUrl()).isEqualTo("url_review_1");
        assertThat(foundImages.get(1).getImageUrl()).isEqualTo("url_review_2");
    }

    @Test
    void findByEntityIdAndEntityType_shouldReturnEmptyListWhenNoImagesMatch() {
        // Arrange
        Image imageHotel = new Image();
        imageHotel.setEntityId(400L);
        imageHotel.setEntityType(EntityType.HOTEL);
        imageHotel.setImageUrl("hotel_url");
        entityManager.persist(imageHotel);

        Image imageReview = new Image();
        imageReview.setEntityId(500L);
        imageReview.setEntityType(EntityType.REVIEW);
        imageReview.setImageUrl("review_url");
        entityManager.persist(imageReview);

        entityManager.flush();

        // Act
        List<Image> foundImagesForNonExistingHotel = imageRepository.findByEntityIdAndEntityType(999L, EntityType.HOTEL);
        List<Image> foundImagesForNonExistingReview = imageRepository.findByEntityIdAndEntityType(999L, EntityType.REVIEW);
        List<Image> foundImagesWithWrongType = imageRepository.findByEntityIdAndEntityType(400L, EntityType.REVIEW);

        // Assert
        assertThat(foundImagesForNonExistingHotel).isEmpty();
        assertThat(foundImagesForNonExistingReview).isEmpty();
        assertThat(foundImagesWithWrongType).isEmpty();
    }
}