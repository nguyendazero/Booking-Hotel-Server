package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.property.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WishListController {

    private final WishListService wishListService;

    @PostMapping("/user/wishlist/{hotelId}")
    public ResponseEntity<String> addToWishList(@PathVariable Long hotelId,
                                                              @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        String response = wishListService.addToWishList(hotelId, accessToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/user/wishlist/{hotelId}")
    public ResponseEntity<String> removeFromWishList(@PathVariable Long hotelId,
                                                                   @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        String response = wishListService.removeFromWishList(hotelId, accessToken);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}