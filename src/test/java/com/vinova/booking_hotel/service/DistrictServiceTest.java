package com.vinova.booking_hotel.service;

import com.vinova.booking_hotel.common.exception.ResourceAlreadyExistsException;
import com.vinova.booking_hotel.property.dto.request.AddDistrictRequestDto;
import com.vinova.booking_hotel.property.dto.response.DistrictResponseDto;
import com.vinova.booking_hotel.property.model.District;
import com.vinova.booking_hotel.property.repository.DistrictRepository;
import com.vinova.booking_hotel.property.service.impl.DistrictServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DistrictServiceTest {

    @Mock
    private DistrictRepository districtRepository;

    @InjectMocks
    private DistrictServiceImpl districtService;

    // Tạo mock AddDistrictRequestDto
    private AddDistrictRequestDto mockAddDistrictRequestDto() {
        return new AddDistrictRequestDto("Da");
    }

    // Tạo mock District
    private District mockDistrict() {
        return new District(1L, "Da", null, null, null);
    }

    @Test
    public void testCreate_WhenNameDistrictAlreadyExists_ThrowException() {
        // Arrange (Chuẩn bị dữ liệu đầu vào)
        AddDistrictRequestDto mockRequest = mockAddDistrictRequestDto();

        // Giả lập hành vi của repository: Tên đã tồn tại
        Mockito.when(districtRepository.existsByName(mockRequest.getName())).thenReturn(true);

        // Act và Assert (Hành động và kiểm tra lỗi ngoại lệ)
        Assertions.assertThrows(
                ResourceAlreadyExistsException.class,
                () -> districtService.create(mockRequest),
                "District name already exists"
        );

        // Đảm bảo repository được gọi một lần với phương thức existsByName
        Mockito.verify(districtRepository, Mockito.times(1)).existsByName(mockRequest.getName());
    }

    @Test
    public void testCreate_WhenNameDistrictDoesNotExist_SaveDistrict() {
        // Arrange (Chuẩn bị dữ liệu đầu vào)
        AddDistrictRequestDto mockRequest = mockAddDistrictRequestDto();
        District mockDistrict = new District();
        mockDistrict.setId(1L);
        mockDistrict.setName(mockRequest.getName());

        // Giả lập hành vi của repository: Tên không tồn tại
        Mockito.when(districtRepository.existsByName(mockRequest.getName())).thenReturn(false);
        Mockito.when(districtRepository.save(Mockito.any(District.class))).thenReturn(mockDistrict);

        // Act (Hành động)
        DistrictResponseDto savedDistrict = districtService.create(mockRequest);

        // Assert (Kiểm tra kết quả đầu ra)
        Assertions.assertNotNull(savedDistrict);
        Assertions.assertEquals(mockRequest.getName(), savedDistrict.getName());

        // Đảm bảo repository được gọi đúng cách
        Mockito.verify(districtRepository, Mockito.times(1)).existsByName(mockRequest.getName());
        Mockito.verify(districtRepository, Mockito.times(1)).save(Mockito.any(District.class));
    }
}