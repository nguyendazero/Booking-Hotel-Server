package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.common.exception.ResourceNotFoundException;
import com.vinova.booking_hotel.property.dto.request.AddDiscountRequestDto;
import com.vinova.booking_hotel.property.dto.response.DiscountResponseDto;
import com.vinova.booking_hotel.property.model.Discount;
import com.vinova.booking_hotel.property.repository.DiscountRepository;
import com.vinova.booking_hotel.property.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiscountServiceImpl implements DiscountService {

    private final DiscountRepository discountRepository;

    @Override
    public List<DiscountResponseDto> discounts() {
        List<Discount> discounts = discountRepository.findAll();

        return discounts.stream()
                .map(discount -> new DiscountResponseDto(discount.getId(), discount.getRate()))
                .toList();
    }

    @Override
    public DiscountResponseDto discount(Long id) {
        Discount discount = discountRepository.findById(id).orElseThrow(ResourceNotFoundException::new);

        return new DiscountResponseDto(discount.getId(), discount.getRate());
    }

    @Override
    public DiscountResponseDto create(AddDiscountRequestDto requestDto) {
        Discount discount = new Discount();
        discount.setRate(requestDto.getRate());
        discountRepository.save(discount);

        return new DiscountResponseDto(discount.getId(), discount.getRate());
    }

    @Override
    public DiscountResponseDto update(Long id, AddDiscountRequestDto requestDto) {
        Discount discount = discountRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        if (requestDto.getRate() != null) {
            discount.setRate(requestDto.getRate());
        }
        discountRepository.save(discount);

        return new DiscountResponseDto(discount.getId(), discount.getRate());
    }

    @Override
    public Void delete(Long id) {
        Discount discount = discountRepository.findById(id).orElseThrow(ResourceNotFoundException::new);
        discountRepository.delete(discount);

        return null;
    }
}