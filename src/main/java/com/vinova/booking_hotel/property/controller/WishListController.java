package com.vinova.booking_hotel.property.controller;

import com.vinova.booking_hotel.authentication.dto.response.APICustomize;
import com.vinova.booking_hotel.property.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class WishListController {

    private final WishListService wishListService;

    @PostMapping("/user/wishlist/{hotelId}")
    public ResponseEntity<APICustomize<String>> addToWishList(@PathVariable Long hotelId,
                                                              @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<String> response = wishListService.addToWishList(hotelId, accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }

    @DeleteMapping("/user/wishlist/{hotelId}")
    public ResponseEntity<APICustomize<String>> removeFromWishList(@PathVariable Long hotelId,
                                                                   @RequestHeader("Authorization") String token) {
        String accessToken = token.substring(7);
        APICustomize<String> response = wishListService.removeFromWishList(hotelId, accessToken);
        return ResponseEntity.status(Integer.parseInt(response.getStatusCode())).body(response);
    }
}