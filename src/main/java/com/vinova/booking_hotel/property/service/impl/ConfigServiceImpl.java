package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddConfigRequestDto;
import com.vinova.booking_hotel.property.dto.response.ConfigResponseDto;
import com.vinova.booking_hotel.property.model.Config;
import com.vinova.booking_hotel.property.repository.ConfigRepository;
import com.vinova.booking_hotel.property.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final ConfigRepository configRepository;

    @Override
    public List<ConfigResponseDto> configs() {
        List<Config> configs = configRepository.findAll();

        return configs.stream()
                .map(config -> new ConfigResponseDto(config.getId(), config.getKey(), config.getValue()))
                .toList();
    }

    @Override
    public ConfigResponseDto create(AddConfigRequestDto requestDto) {
        Config config = new Config();
        config.setKey(requestDto.getKey());
        config.setValue(requestDto.getValue());
        configRepository.save(config);

        return new ConfigResponseDto(config.getId(), config.getKey(), config.getValue());
    }

    @Override
    public ConfigResponseDto update(Long id, AddConfigRequestDto requestDto) {
        Config config = configRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration"));

        if (requestDto.getKey() != null) {
            config.setKey(requestDto.getKey());
        }
        if (requestDto.getValue() != null) {
            config.setValue(requestDto.getValue());
        }
        configRepository.save(config);

        return new ConfigResponseDto(config.getId(), config.getKey(), config.getValue());
    }

    @Override
    public Void delete(Long id) {
        Config config = configRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Configuration"));
        configRepository.delete(config);

        return null;
    }


}