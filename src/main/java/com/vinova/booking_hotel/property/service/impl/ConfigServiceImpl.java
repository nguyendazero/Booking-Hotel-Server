package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
import com.vinova.booking_hotel.common.enums.ApiError;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddConfigRequestDto;
import com.vinova.booking_hotel.property.dto.response.ConfigResponseDto;
import com.vinova.booking_hotel.property.model.Config;
import com.vinova.booking_hotel.property.repository.ConfigRepository;
import com.vinova.booking_hotel.property.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final ConfigRepository configRepository;
    private final AccountRepository accountRepository;
    private final JwtUtils jwtUtils;

    @Override
    public APICustomize<List<ConfigResponseDto>> configs() {
        List<Config> configs = configRepository.findAll();
        List<ConfigResponseDto> response = configs.stream()
                .map(config -> new ConfigResponseDto(config.getId(), config.getKey(), config.getValue()))
                .toList();

        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), response);
    }

    @Override
    public APICustomize<List<ConfigResponseDto>> configsByToken(String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(ResourceNotFoundException::new);

        List<Config> configs = configRepository.findByAccount(account);
        List<ConfigResponseDto> response = configs.stream()
                .map(config -> new ConfigResponseDto(config.getId(), config.getKey(), config.getValue()))
                .toList();

        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), response);
    }

    @Override
    public APICustomize<List<ConfigResponseDto>> configsByAccountId(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(ResourceNotFoundException::new);

        List<Config> configs = configRepository.findByAccount(account);
        List<ConfigResponseDto> response = configs.stream()
                .map(config -> new ConfigResponseDto(config.getId(), config.getKey(), config.getValue()))
                .toList();

        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), response);
    }

    @Override
    public APICustomize<ConfigResponseDto> config(Long id) {
        Config config = configRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        ConfigResponseDto response = new ConfigResponseDto(config.getId(), config.getKey(), config.getValue());

        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), response);
    }

    @Override
    public APICustomize<ConfigResponseDto> create(AddConfigRequestDto requestDto, String token) {
        Config config = new Config();
        config.setKey(requestDto.getKey());
        config.setValue(requestDto.getValue());
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(ResourceNotFoundException::new);
        config.setAccount(account);
        configRepository.save(config);

        return new APICustomize<>(ApiError.CREATED.getCode(), ApiError.CREATED.getMessage(), new ConfigResponseDto(config.getId(), config.getKey(), config.getValue()));
    }

    @Override
    public APICustomize<ConfigResponseDto> update(Long id, AddConfigRequestDto requestDto, String token) {
        Config config = configRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId).orElseThrow(ResourceNotFoundException::new);
        if (Objects.equals(account.getId(), config.getAccount().getId())) {
            if (requestDto.getKey() != null) {
                config.setKey(requestDto.getKey());
            }
            if (requestDto.getValue() != null) {
                config.setValue(requestDto.getValue());
            }
        }else{
            throw new RuntimeException("You not have permission to update this config");
        }
        configRepository.save(config);

        return new APICustomize<>(ApiError.NO_CONTENT.getCode(), ApiError.NO_CONTENT.getMessage(), new ConfigResponseDto(config.getId(), config.getKey(), config.getValue()));
    }

    @Override
    public APICustomize<Void> delete(Long id, String token) {
        Config config = configRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId).orElseThrow(ResourceNotFoundException::new);
        if (Objects.equals(account.getId(), config.getAccount().getId())) {
            configRepository.delete(config);
        }else{
            throw new RuntimeException("You not have permission to delete this config");
        }
        
        return new APICustomize<>(ApiError.NO_CONTENT.getCode(), ApiError.NO_CONTENT.getMessage(), null);
    }
}