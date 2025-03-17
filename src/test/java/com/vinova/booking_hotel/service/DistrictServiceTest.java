package com.vinova.booking_hotel.service;

import com.vinova.booking_hotel.common.exception.ResourceAlreadyExistsException;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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

    @Test
    public void testGetAllDistrict() {
        // Arrange (Chuẩn bị dữ liệu đầu vào)
        District mockDistrict1 = new District(1L, "Da", null, null, null);
        District mockDistrict2 = new District(2L, "Hai", null, null, null);
        List<District> mockDistricts = Arrays.asList(mockDistrict1, mockDistrict2);

        // Giả lập hành vi của repository: trả về danh sách quận
        Mockito.when(districtRepository.findAll()).thenReturn(mockDistricts);

        // Act (Hành động)
        List<DistrictResponseDto> result = districtService.districts();

        // Assert (Kiểm tra kết quả đầu ra)
        Assertions.assertNotNull(result);
        Assertions.assertEquals(mockDistricts.size(), result.size());

        // Kiểm tra nội dung của danh sách kết quả
        Assertions.assertEquals(mockDistrict1.getName(), result.get(0).getName());
        Assertions.assertEquals(mockDistrict2.getName(), result.get(1).getName());

        // Đảm bảo repository được gọi đúng cách
        Mockito.verify(districtRepository, Mockito.times(1)).findAll();
    }

    @Test
    public void testGetDistrictById_WhenExists_ReturnDistrict() {
        // Arrange
        Long districtId = 1L;
        District mockDistrict = new District(districtId, "Da", null, null, null);
        Mockito.when(districtRepository.findById(districtId)).thenReturn(Optional.of(mockDistrict));

        // Act
        DistrictResponseDto result = districtService.district(districtId);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(mockDistrict.getName(), result.getName());

        // Đảm bảo repository được gọi đúng cách
        Mockito.verify(districtRepository, Mockito.times(1)).findById(districtId);
    }

    // Kiểm tra khi quận không tồn tại
    @Test
    public void testGetDistrictById_WhenNotExists_ThrowException() {
        // Arrange
        Long districtId = 1L;
        Mockito.when(districtRepository.findById(districtId)).thenReturn(Optional.empty());

        // Act và Assert
        Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> districtService.district(districtId),
                "Resource not found"
        );

        // Đảm bảo repository được gọi đúng cách
        Mockito.verify(districtRepository, Mockito.times(1)).findById(districtId);
    }

    // Kiểm tra khi xóa quận tồn tại
    @Test
    public void testDeleteDistrict_WhenExists_DeleteSuccessfully() {
        // Arrange
        Long districtId = 1L;
        District mockDistrict = new District(districtId, "Existing District", null, null, null);
        Mockito.when(districtRepository.findById(districtId)).thenReturn(Optional.of(mockDistrict));

        // Act
        districtService.delete(districtId);

        // Assert
        Mockito.verify(districtRepository, Mockito.times(1)).delete(mockDistrict);
    }

    @Test
    public void testDeleteDistrict_WhenNotExists_ThrowException() {
        // Arrange
        Long districtId = 1L;
        Mockito.when(districtRepository.findById(districtId)).thenReturn(Optional.empty());

        // Act và Assert
        Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> districtService.delete(districtId),
                "District not found"
        );

        // Đảm bảo repository được gọi đúng cách
        Mockito.verify(districtRepository, Mockito.times(1)).findById(districtId);
    }
    
    // Kiểm tra khi cập nhật quận tồn tại
    @Test
    public void testUpdateDistrict_WhenExists_UpdateSuccessfully() {
        // Arrange
        Long districtId = 1L;
        AddDistrictRequestDto mockRequest = new AddDistrictRequestDto("Updated District");
        District existingDistrict = new District(districtId, "Old District", null, null, null);
        Mockito.when(districtRepository.findById(districtId)).thenReturn(Optional.of(existingDistrict));

        // Act
        DistrictResponseDto updatedDistrict = districtService.update(districtId, mockRequest);

        // Assert
        Assertions.assertNotNull(updatedDistrict);
        Assertions.assertEquals(mockRequest.getName(), updatedDistrict.getName());

        // Đảm bảo repository được gọi đúng cách
        Mockito.verify(districtRepository, Mockito.times(1)).findById(districtId);
        Mockito.verify(districtRepository, Mockito.times(1)).save(Mockito.any(District.class));
    }

    // Kiểm tra khi cập nhật quận không tồn tại
    @Test
    public void testUpdateDistrict_WhenNotExists_ThrowException() {
        // Arrange
        Long districtId = 1L;
        AddDistrictRequestDto mockRequest = new AddDistrictRequestDto("New District");
        Mockito.when(districtRepository.findById(districtId)).thenReturn(Optional.empty());

        // Act và Assert
        Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> districtService.update(districtId, mockRequest),
                "District not found"
        );

        // Đảm bảo repository được gọi đúng cách
        Mockito.verify(districtRepository, Mockito.times(1)).findById(districtId);
    }
    
}