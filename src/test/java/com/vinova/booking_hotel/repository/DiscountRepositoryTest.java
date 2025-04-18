package com.vinova.booking_hotel.repository;

import com.vinova.booking_hotel.property.model.Discount;
import com.vinova.booking_hotel.property.repository.DiscountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DiscountRepositoryTest {

    @Autowired
    private DiscountRepository discountRepository;

    @BeforeEach
    public void setUp() {
        discountRepository.deleteAll(); // Đảm bảo cơ sở dữ liệu sạch sẽ trước mỗi bài kiểm tra
    }

    @Test
    public void testFindByRate_WhenDiscountExists() {
        // Arrange
        Discount discount = new Discount();
        discount.setRate(new BigDecimal("10.00")); // Tỷ lệ giảm giá 10%
        discountRepository.save(discount);

        // Act
        Discount foundDiscount = discountRepository.findByRate(new BigDecimal("10.00"));

        // Assert
        assertThat(foundDiscount).isNotNull();
        assertThat(foundDiscount.getRate()).isEqualTo(new BigDecimal("10.00"));
    }

    @Test
    public void testFindByRate_WhenDiscountDoesNotExist() {
        // Act
        Discount foundDiscount = discountRepository.findByRate(new BigDecimal("20.00")); // Tỷ lệ không tồn tại

        // Assert
        assertThat(foundDiscount).isNull();
    }
}
