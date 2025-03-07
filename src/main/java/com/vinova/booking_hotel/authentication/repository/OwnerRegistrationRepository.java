package com.vinova.booking_hotel.authentication.repository;

import com.vinova.booking_hotel.authentication.model.OwnerRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OwnerRegistrationRepository extends JpaRepository<OwnerRegistration, Long> {
    List<OwnerRegistration> findByStatus(String status);
    OwnerRegistration findByAccountId(Long accountId);
}
