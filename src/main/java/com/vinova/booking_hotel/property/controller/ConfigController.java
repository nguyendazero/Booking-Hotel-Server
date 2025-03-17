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

    @GetMapping("/public/configs/by-account-id/{accountId}")
    public ResponseEntity<List<ConfigResponseDto>> configsByAccountId(@PathVariable Long accountId) {
        List<ConfigResponseDto> response = configService.configsByAccountId(accountId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/public/config/{id}")
    public ResponseEntity<ConfigResponseDto> config(@PathVariable Long id) {
        ConfigResponseDto response = configService.config(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/user/configs/by-token")
    public ResponseEntity<List<ConfigResponseDto>> configsByToken(@RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        List<ConfigResponseDto> response = configService.configsByToken(accessToken);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/user/config")
    public ResponseEntity<ConfigResponseDto> create(@RequestBody AddConfigRequestDto requestDto,
                                            @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        ConfigResponseDto response = configService.create(requestDto, accessToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/user/config/{id}")
    public ResponseEntity<ConfigResponseDto> update(@PathVariable Long id, @RequestBody AddConfigRequestDto requestDto
                                                                , @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        ConfigResponseDto response = configService.update(id, requestDto, accessToken);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/user/config/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        Void response = configService.delete(id, accessToken);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}