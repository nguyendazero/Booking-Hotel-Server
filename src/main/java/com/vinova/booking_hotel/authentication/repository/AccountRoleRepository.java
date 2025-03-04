package com.vinova.booking_hotel.authentication.repository;

import com.vinova.booking_hotel.authentication.model.AccountRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRoleRepository extends JpaRepository<AccountRole, Long> {
}
