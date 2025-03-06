package com.vinova.booking_hotel.property.service.impl;

import com.vinova.booking_hotel.property.repository.WishListRepository;
import com.vinova.booking_hotel.property.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WishListServiceImpl implements WishListService {
    
    private final WishListRepository wishListRepository;
    
}
