package com.vinova.booking_hotel.service;

import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddAmenityRequestDto;
import com.vinova.booking_hotel.property.dto.response.AmenityResponseDto;
import com.vinova.booking_hotel.property.model.Amenity;
import com.vinova.booking_hotel.property.repository.AmenityRepository;
import com.vinova.booking_hotel.property.service.impl.AmenityServiceImpl;
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
public class AmenityServiceTest {

    @Mock
    private AmenityRepository amenityRepository;

    @InjectMocks
    private AmenityServiceImpl amenityService;

    // Tạo mock AddAmenityRequestDto
    private AddAmenityRequestDto mockAddAmenityRequestDto() {
        return new AddAmenityRequestDto("Free WiFi");
    }

    @Test
    public void testCreateAmenity() {
        // Arrange (Chuẩn bị dữ liệu đầu vào)
        AddAmenityRequestDto mockRequest = mockAddAmenityRequestDto();
        Amenity mockAmenity = new Amenity();
        mockAmenity.setId(1L);
        mockAmenity.setName(mockRequest.getName());

        // Giả lập hành vi của repository
        Mockito.when(amenityRepository.save(Mockito.any(Amenity.class))).thenReturn(mockAmenity);

        // Act (Hành động)
        AmenityResponseDto savedAmenity = amenityService.create(mockRequest);

        // Assert (Kiểm tra kết quả đầu ra)
        Assertions.assertNotNull(savedAmenity);
        Assertions.assertEquals(mockRequest.getName(), savedAmenity.getName());

        // Đảm bảo repository được gọi đúng cách
        Mockito.verify(amenityRepository, Mockito.times(1)).save(Mockito.any(Amenity.class));
    }

    @Test
    public void testGetAllAmenities() {
        // Arrange
        Amenity mockAmenity1 = new Amenity();
        mockAmenity1.setId(1L);
        mockAmenity1.setName("Free WiFi");

        Amenity mockAmenity2 = new Amenity();
        mockAmenity2.setId(2L);
        mockAmenity2.setName("Pool");

        List<Amenity> mockAmenities = Arrays.asList(mockAmenity1, mockAmenity2);

        // Giả lập hành vi của repository
        Mockito.when(amenityRepository.findAll()).thenReturn(mockAmenities);

        // Act
        List<AmenityResponseDto> result = amenityService.amenities();

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(mockAmenities.size(), result.size());
        Assertions.assertEquals(mockAmenity1.getName(), result.get(0).getName());
        Assertions.assertEquals(mockAmenity2.getName(), result.get(1).getName());

        // Đảm bảo repository được gọi đúng cách
        Mockito.verify(amenityRepository, Mockito.times(1)).findAll();
    }

    @Test
    public void testGetAmenityById_WhenExists_ReturnAmenity() {
        // Arrange
        Long amenityId = 1L;
        Amenity mockAmenity = new Amenity();
        mockAmenity.setId(amenityId);
        mockAmenity.setName("Free WiFi");
        Mockito.when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(mockAmenity));

        // Act
        AmenityResponseDto result = amenityService.amenity(amenityId);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(mockAmenity.getName(), result.getName());

        // Đảm bảo repository được gọi đúng cách
        Mockito.verify(amenityRepository, Mockito.times(1)).findById(amenityId);
    }

    @Test
    public void testGetAmenityById_WhenNotExists_ThrowException() {
        // Arrange
        Long amenityId = 1L;
        Mockito.when(amenityRepository.findById(amenityId)).thenReturn(Optional.empty());

        // Act và Assert
        Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> amenityService.amenity(amenityId),
                "amenity not found"
        );

        // Đảm bảo repository được gọi đúng cách
        Mockito.verify(amenityRepository, Mockito.times(1)).findById(amenityId);
    }

    @Test
    public void testUpdateAmenity_WhenExists_UpdateSuccessfully() {
        // Arrange
        Long amenityId = 1L;
        AddAmenityRequestDto mockRequest = new AddAmenityRequestDto("Updated Amenity");
        Amenity existingAmenity = new Amenity();
        existingAmenity.setId(amenityId);
        existingAmenity.setName("Old Amenity");
        Mockito.when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(existingAmenity));

        // Act
        AmenityResponseDto updatedAmenity = amenityService.update(amenityId, mockRequest);

        // Assert
        Assertions.assertNotNull(updatedAmenity);
        Assertions.assertEquals(mockRequest.getName(), updatedAmenity.getName());

        // Đảm bảo repository được gọi đúng cách
        Mockito.verify(amenityRepository, Mockito.times(1)).findById(amenityId);
        Mockito.verify(amenityRepository, Mockito.times(1)).save(Mockito.any(Amenity.class));
    }

    @Test
    public void testUpdateAmenity_WhenNotExists_ThrowException() {
        // Arrange
        Long amenityId = 1L;
        AddAmenityRequestDto mockRequest = new AddAmenityRequestDto("New Amenity");
        Mockito.when(amenityRepository.findById(amenityId)).thenReturn(Optional.empty());

        // Act và Assert
        Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> amenityService.update(amenityId, mockRequest),
                "amenity not found"
        );

        // Đảm bảo repository được gọi đúng cách
        Mockito.verify(amenityRepository, Mockito.times(1)).findById(amenityId);
    }

    @Test
    public void testDeleteAmenity_WhenExists_DeleteSuccessfully() {
        // Arrange
        Long amenityId = 1L;
        Amenity mockAmenity = new Amenity();
        mockAmenity.setId(amenityId);
        mockAmenity.setName("Existing Amenity");
        Mockito.when(amenityRepository.findById(amenityId)).thenReturn(Optional.of(mockAmenity));

        // Act
        amenityService.delete(amenityId);

        // Assert
        Mockito.verify(amenityRepository, Mockito.times(1)).delete(mockAmenity);
    }

    @Test
    public void testDeleteAmenity_WhenNotExists_ThrowException() {
        // Arrange
        Long amenityId = 1L;
        Mockito.when(amenityRepository.findById(amenityId)).thenReturn(Optional.empty());

        // Act và Assert
        Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> amenityService.delete(amenityId),
                "amenity not found"
        );

        // Đảm bảo repository được gọi đúng cách
        Mockito.verify(amenityRepository, Mockito.times(1)).findById(amenityId);
    }
}