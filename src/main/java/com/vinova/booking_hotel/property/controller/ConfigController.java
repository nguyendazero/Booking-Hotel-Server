package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.dto.request.AddConfigRequestDto;
import com.vinova.booking_hotel.property.dto.response.ConfigResponseDto;
import com.vinova.booking_hotel.property.service.ConfigService;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<APICustomize<List<ConfigResponseDto>>> configs() {
        APICustomize<List<ConfigResponseDto>> response = configService.configs();
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @GetMapping("/public/configs/by-account-id/{accountId}")
    public ResponseEntity<APICustomize<List<ConfigResponseDto>>> configsByAccountId(@PathVariable Long accountId) {
        APICustomize<List<ConfigResponseDto>> response = configService.configsByAccountId(accountId);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @GetMapping("/public/config/{id}")
    public ResponseEntity<APICustomize<ConfigResponseDto>> config(@PathVariable Long id) {
        APICustomize<ConfigResponseDto> response = configService.config(id);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @GetMapping("/user/configs/by-token")
    public ResponseEntity<APICustomize<List<ConfigResponseDto>>> configsByToken(@RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<List<ConfigResponseDto>> response = configService.configsByToken(accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PostMapping("/user/config")
    public ResponseEntity<APICustomize<ConfigResponseDto>> create(@RequestBody AddConfigRequestDto requestDto,
                                            @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<ConfigResponseDto> response = configService.create(requestDto, accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @PutMapping("/user/config/{id}")
    public ResponseEntity<APICustomize<ConfigResponseDto>> update(@PathVariable Long id, @RequestBody AddConfigRequestDto requestDto
                                                                , @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<ConfigResponseDto> response = configService.update(id, requestDto, accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @DeleteMapping("/user/config/{id}")
    public ResponseEntity<APICustomize<Void>> delete(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<Void> response = configService.delete(id, accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }
}