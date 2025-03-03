package com.vinova.booking_hotel.authentication.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) {
        try {
            Map<String, Object> data = this.cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            return (String) data.get("secure_url");
        } catch (IOException io) {
            throw new RuntimeException("Image upload failed", io);
        }
    }
}