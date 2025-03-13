package com.vinova.booking_hotel.property.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddRatingRequestDto {
    private Long hotelId;
    private Integer stars;
    private String content;
    private List<MultipartFile> images = new ArrayList<>();
}
