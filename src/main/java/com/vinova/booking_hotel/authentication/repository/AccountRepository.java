package com.vinova.booking_hotel.authentication.repository;

import com.vinova.booking_hotel.authentication.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long>, JpaSpecificationExecutor<Account> {
    Optional<Account> findByUsername(String username);
    
    Optional<Account> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    List<Account> findByLatestLoginBefore(LocalDateTime dateTime);

    List<Account> findByAccountRoles_RoleName(String roleName);
    
}
    