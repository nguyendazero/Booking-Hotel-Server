package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.repository.AccountRepository;
import com.vinova.booking_hotel.authentication.security.JwtUtils;
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
    public List<ConfigResponseDto> configs() {
        List<Config> configs = configRepository.findAll();

        return configs.stream()
                .map(config -> new ConfigResponseDto(config.getId(), config.getKey(), config.getValue()))
                .toList();
    }

    @Override
    public List<ConfigResponseDto> configsByToken(String token) {
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(ResourceNotFoundException::new);

        List<Config> configs = configRepository.findByAccount(account);

        return configs.stream()
                .map(config -> new ConfigResponseDto(config.getId(), config.getKey(), config.getValue()))
                .toList();
    }

    @Override
    public List<ConfigResponseDto> configsByAccountId(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(ResourceNotFoundException::new);

        List<Config> configs = configRepository.findByAccount(account);

        return configs.stream()
                .map(config -> new ConfigResponseDto(config.getId(), config.getKey(), config.getValue()))
                .toList();
    }

    @Override
    public ConfigResponseDto config(Long id) {
        Config config = configRepository.findById(id).orElseThrow(ResourceNotFoundException::new);

        return new ConfigResponseDto(config.getId(), config.getKey(), config.getValue());
    }

    @Override
    public ConfigResponseDto create(AddConfigRequestDto requestDto, String token) {
        Config config = new Config();
        config.setKey(requestDto.getKey());
        config.setValue(requestDto.getValue());
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(ResourceNotFoundException::new);
        config.setAccount(account);
        configRepository.save(config);

        return new ConfigResponseDto(config.getId(), config.getKey(), config.getValue());
    }

    @Override
    public ConfigResponseDto update(Long id, AddConfigRequestDto requestDto, String token) {
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

        return new ConfigResponseDto(config.getId(), config.getKey(), config.getValue());
    }

    @Override
    public Void delete(Long id, String token) {
        Config config = configRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        Long accountId = jwtUtils.getUserIdFromJwtToken(token);
        Account account = accountRepository.findById(accountId).orElseThrow(ResourceNotFoundException::new);
        if (Objects.equals(account.getId(), config.getAccount().getId())) {
            configRepository.delete(config);
        }else{
            throw new RuntimeException("You not have permission to delete this config");
        }
        
        return null;
    }
}