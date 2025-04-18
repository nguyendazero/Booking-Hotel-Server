package com.vinova.booking_hotel.repository;

import com.vinova.booking_hotel.property.model.District;
import com.vinova.booking_hotel.property.repository.DistrictRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DistrictRepositoryTest {

    @Autowired
    private DistrictRepository districtRepository;

    @BeforeEach
    public void setUp() {
        districtRepository.deleteAll(); // Đảm bảo rằng cơ sở dữ liệu được sạch sẽ trước mỗi bài kiểm tra
    }

    @Test
    public void testExistsByName_WhenNameExists() {
        // Sắp xếp
        District district = new District();
        district.setName("Test District"); // Sử dụng setter tự động tạo ra bởi Lombok
        districtRepository.save(district); // Lưu một quận mới vào cơ sở dữ liệu

        // Thực hiện
        boolean exists = districtRepository.existsByName("Test District");

        // Xác nhận
        assertThat(exists).isTrue();
    }

    @Test
    public void testExistsByName_WhenNameDoesNotExist() {
        // Sắp xếp
        District district = new District();
        district.setName("Existing District"); // Sử dụng setter tự động tạo ra bởi Lombok
        districtRepository.save(district); // Lưu một quận khác vào cơ sở dữ liệu

        // Thực hiện
        boolean exists = districtRepository.existsByName("Nonexistent District");

        // Xác nhận
        assertThat(exists).isFalse();
    }
}