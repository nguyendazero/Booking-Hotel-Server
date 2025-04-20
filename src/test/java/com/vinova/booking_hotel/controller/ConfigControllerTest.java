package com.vinova.booking_hotel.controller;

import com.vinova.booking_hotel.property.controller.ConfigController;
import com.vinova.booking_hotel.property.dto.request.AddConfigRequestDto;
import com.vinova.booking_hotel.property.dto.response.ConfigResponseDto;
import com.vinova.booking_hotel.property.service.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class ConfigControllerTest {

    @Mock
    private ConfigService configService;

    @InjectMocks
    private ConfigController configController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testConfigs() {
        List<ConfigResponseDto> mockResponse = Collections.singletonList(new ConfigResponseDto(1L, "key1", "value1"));
        when(configService.configs()).thenReturn(mockResponse);

        ResponseEntity<List<ConfigResponseDto>> response = configController.configs();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testCreate() {
        AddConfigRequestDto requestDto = new AddConfigRequestDto("newKey", "newValue");
        ConfigResponseDto mockResponse = new ConfigResponseDto(1L, "newKey", "newValue");
        when(configService.create(requestDto)).thenReturn(mockResponse);

        ResponseEntity<ConfigResponseDto> response = configController.create(requestDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(mockResponse, response.getBody());
    }

    @Test
    public void testUpdate() {
        Long configId = 1L;
        AddConfigRequestDto requestDto = new AddConfigRequestDto("updatedKey", "updatedValue");
        ConfigResponseDto mockResponse = new ConfigResponseDto(configId, "updatedKey", "updatedValue");
        when(configService.update(anyLong(), any(AddConfigRequestDto.class))).thenReturn(mockResponse);

        ResponseEntity<ConfigResponseDto> response = configController.update(configId, requestDto);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    public void testDelete() {
        Long configId = 1L;
        doNothing().when(configService).delete(configId);

        ResponseEntity<Void> response = configController.delete(configId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}