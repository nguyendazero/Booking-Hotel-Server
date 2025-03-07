package com.vinova.booking_hotel.authentication.repository;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.authentication.model.OwnerRegistration;
import com.vinova.booking_hotel.common.enums.OwnerRegistrationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OwnerRegistrationRepository extends JpaRepository<OwnerRegistration, Long> {
    Optional<OwnerRegistration> findByAccountAndStatus(Account account, OwnerRegistrationStatus status);
}
