package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.common.enums.ApiError;
import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddDiscountRequestDto;
import com.vinova.booking_hotel.property.dto.response.DiscountResponseDto;
import com.vinova.booking_hotel.property.model.Discount;
import com.vinova.booking_hotel.property.repository.DiscountRepository;
import com.vinova.booking_hotel.property.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;

    @Override
    public APICustomize<List<DiscountResponseDto>> discounts() {
        List<Discount> discounts = discountRepository.findAll();
        List<DiscountResponseDto> response = discounts.stream()
                .map(discount -> new DiscountResponseDto(discount.getId(), discount.getRate()))
                .toList();

        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), response);
    }

    @Override
    public APICustomize<DiscountResponseDto> discount(Long id) {
        Discount discount = discountRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        DiscountResponseDto response = new DiscountResponseDto(discount.getId(), discount.getRate());

        return new APICustomize<>(ApiError.OK.getCode(), ApiError.OK.getMessage(), response);
    }

    @Override
    public APICustomize<DiscountResponseDto> create(AddDiscountRequestDto requestDto) {
        Discount discount = new Discount();
        discount.setRate(requestDto.getRate());
        discountRepository.save(discount);

        return new APICustomize<>(ApiError.CREATED.getCode(), ApiError.CREATED.getMessage(), new DiscountResponseDto(discount.getId(), discount.getRate()));
    }

    @Override
    public APICustomize<DiscountResponseDto> update(Long id, AddDiscountRequestDto requestDto) {
        Discount discount = discountRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        if (requestDto.getRate() != null) {
            discount.setRate(requestDto.getRate());
        }
        discountRepository.save(discount);

        return new APICustomize<>(ApiError.NO_CONTENT.getCode(), ApiError.NO_CONTENT.getMessage(), new DiscountResponseDto(discount.getId(), discount.getRate()));
    }

    @Override
    public APICustomize<Void> delete(Long id) {
        Discount discount = discountRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        discountRepository.delete(discount);

        return new APICustomize<>(ApiError.NO_CONTENT.getCode(), ApiError.NO_CONTENT.getMessage(), null);
    }
}