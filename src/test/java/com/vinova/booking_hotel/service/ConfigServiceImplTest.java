package com.vinova.booking_hotel.service;

import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddConfigRequestDto;
import com.vinova.booking_hotel.property.dto.response.ConfigResponseDto;
import com.vinova.booking_hotel.property.model.Config;
import com.vinova.booking_hotel.property.repository.ConfigRepository;
import com.vinova.booking_hotel.property.service.impl.ConfigServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConfigServiceImplTest {

    @Mock
    private ConfigRepository configRepository;

    @InjectMocks
    private ConfigServiceImpl configService;

    private final ZonedDateTime NOW = ZonedDateTime.now();

    @Test
    void configs_shouldReturnListOfConfigResponseDtos() {
        // Arrange
        List<Config> mockConfigs = Collections.singletonList(new Config(1L, "key1", "value1", NOW, NOW));
        when(configRepository.findAll()).thenReturn(mockConfigs);

        // Act
        List<ConfigResponseDto> responseDtos = configService.configs();

        // Assert
        assertEquals(1, responseDtos.size());
        assertEquals(mockConfigs.getFirst().getId(), responseDtos.getFirst().getId());
        assertEquals(mockConfigs.getFirst().getKey(), responseDtos.getFirst().getName()); // Corrected: Compare with getKey()
        assertEquals(mockConfigs.getFirst().getValue(), responseDtos.getFirst().getValue());
        verify(configRepository, times(1)).findAll();
    }

    @Test
    void configs_shouldReturnEmptyList_whenNoConfigsExist() {
        // Arrange
        when(configRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<ConfigResponseDto> responseDtos = configService.configs();

        // Assert
        assertTrue(responseDtos.isEmpty());
        verify(configRepository, times(1)).findAll();
    }

    @Test
    void create_shouldSaveNewConfigAndReturnResponseDto() {
        // Arrange
        AddConfigRequestDto requestDto = new AddConfigRequestDto("newKey", "newValue");
        Config savedConfig = new Config(1L, "newKey", "newValue", NOW, NOW);

        // Khi save được gọi với bất kỳ đối tượng Config nào, trả về savedConfig
        when(configRepository.save(any(Config.class))).thenReturn(savedConfig);

        // Act
        ConfigResponseDto responseDto = configService.create(requestDto);

        // Assert
        assertNotNull(responseDto);
        assertEquals(savedConfig.getId(), 1L);
        assertEquals(savedConfig.getKey(), responseDto.getName());
        assertEquals(savedConfig.getValue(), responseDto.getValue());
        verify(configRepository, times(1)).save(any(Config.class));
    }

    @Test
    void update_shouldUpdateExistingConfigAndReturnResponseDto() {
        // Arrange
        Long configId = 1L;
        AddConfigRequestDto requestDto = new AddConfigRequestDto("updatedKey", "updatedValue");
        Config existingConfig = new Config(configId, "oldKey", "oldValue", NOW, NOW);
        Config updatedConfig = new Config(configId, "updatedKey", "updatedValue", NOW, NOW);
        when(configRepository.findById(configId)).thenReturn(Optional.of(existingConfig));
        when(configRepository.save(existingConfig)).thenReturn(updatedConfig);

        // Act
        ConfigResponseDto responseDto = configService.update(configId, requestDto);

        // Assert
        assertNotNull(responseDto);
        assertEquals(updatedConfig.getId(), responseDto.getId());
        assertEquals(updatedConfig.getKey(), responseDto.getName()); // Corrected: Compare with getKey()
        assertEquals(updatedConfig.getValue(), responseDto.getValue());
        verify(configRepository, times(1)).findById(configId);
        verify(configRepository, times(1)).save(existingConfig);
    }

    @Test
    void update_shouldThrowResourceNotFoundException_whenConfigNotFound() {
        // Arrange
        Long configId = 1L;
        AddConfigRequestDto requestDto = new AddConfigRequestDto("updatedKey", "updatedValue");
        when(configRepository.findById(configId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> configService.update(configId, requestDto));
        verify(configRepository, times(1)).findById(configId);
        verify(configRepository, never()).save(any(Config.class));
    }

    @Test
    void update_shouldOnlyUpdateNonNullFields() {
        // Arrange
        Long configId = 1L;
        AddConfigRequestDto requestDtoWithNullValue = new AddConfigRequestDto("updatedKey", null);
        Config existingConfig = new Config(configId, "oldKey", "oldValue", NOW, NOW);
        Config updatedConfig = new Config(configId, "updatedKey", "oldValue", NOW, NOW);
        when(configRepository.findById(configId)).thenReturn(Optional.of(existingConfig));
        when(configRepository.save(existingConfig)).thenReturn(updatedConfig);

        // Act
        ConfigResponseDto responseDto = configService.update(configId, requestDtoWithNullValue);

        // Assert
        assertNotNull(responseDto);
        assertEquals(updatedConfig.getId(), responseDto.getId());
        assertEquals(updatedConfig.getKey(), responseDto.getName()); // Corrected: Compare with getKey()
        assertEquals(updatedConfig.getValue(), responseDto.getValue());
        verify(configRepository, times(1)).findById(configId);
        verify(configRepository, times(1)).save(existingConfig);

        // Arrange for updating only value
        AddConfigRequestDto requestDtoWithNullKey = new AddConfigRequestDto(null, "updatedValue");
        Config existingConfig2 = new Config(configId, "updatedKey", "oldValue", NOW, NOW);
        Config updatedConfig2 = new Config(configId, "updatedKey", "updatedValue", NOW, NOW);
        when(configRepository.findById(configId)).thenReturn(Optional.of(existingConfig2));
        when(configRepository.save(existingConfig2)).thenReturn(updatedConfig2);

        // Act for updating only value
        ConfigResponseDto responseDto2 = configService.update(configId, requestDtoWithNullKey);

        // Assert for updating only value
        assertNotNull(responseDto2);
        assertEquals(updatedConfig2.getId(), responseDto2.getId());
        assertEquals(updatedConfig2.getKey(), responseDto2.getName()); // Corrected: Compare with getKey()
        assertEquals(updatedConfig2.getValue(), responseDto2.getValue());
        verify(configRepository, times(2)).findById(configId);
        verify(configRepository, times(2)).save(any(Config.class));
    }

    @Test
    void delete_shouldDeleteExistingConfig() {
        // Arrange
        Long configId = 1L;
        Config existingConfig = new Config(configId, "keyToDelete", "valueToDelete", NOW, NOW);
        when(configRepository.findById(configId)).thenReturn(Optional.of(existingConfig));

        // Act
        configService.delete(configId);

        // Assert
        verify(configRepository, times(1)).findById(configId);
        verify(configRepository, times(1)).delete(existingConfig);
    }

    @Test
    void delete_shouldThrowResourceNotFoundException_whenConfigNotFound() {
        // Arrange
        Long configId = 1L;
        when(configRepository.findById(configId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> configService.delete(configId));
        verify(configRepository, times(1)).findById(configId);
        verify(configRepository, never()).delete(any(Config.class));
    }
}