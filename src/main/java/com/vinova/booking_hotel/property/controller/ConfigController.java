package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.property.dto.request.AddConfigRequestDto;
import com.vinova.booking_hotel.property.dto.response.ConfigResponseDto;
import com.vinova.booking_hotel.property.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigService configService;

    @GetMapping("/public/configs")
    public ResponseEntity<List<ConfigResponseDto>> configs() {
        List<ConfigResponseDto> response = configService.configs();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/admin/config")
    public ResponseEntity<ConfigResponseDto> create(@RequestBody AddConfigRequestDto requestDto) {
        ConfigResponseDto response = configService.create(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/admin/config/{id}")
    public ResponseEntity<ConfigResponseDto> update(@PathVariable Long id, @RequestBody AddConfigRequestDto requestDto) {
        ConfigResponseDto response = configService.update(id, requestDto);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/admin/config/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Void response = configService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}