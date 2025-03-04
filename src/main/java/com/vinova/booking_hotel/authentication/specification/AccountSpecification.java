package com.vinova.booking_hotel.authentication.specification;

import com.vinova.booking_hotel.authentication.model.Account;
import org.springframework.data.jpa.domain.Specification;

public class AccountSpecification {

    public static Specification<Account> hasFullName(String fullName) {
        return (root, query, criteriaBuilder) ->
                fullName == null ?
                        criteriaBuilder.conjunction() :
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("fullName")), "%" + fullName.toLowerCase() + "%");
    }

    public static Specification<Account> hasRole(String role) {
        return (root, query, criteriaBuilder) ->
                role == null ?
                        criteriaBuilder.conjunction() :
                        criteriaBuilder.equal(root.get("role"), role);
    }

    public static Specification<Account> isEnabled(Boolean enabled) {
        return (root, query, criteriaBuilder) ->
                enabled == null ?
                        criteriaBuilder.conjunction() :
                        criteriaBuilder.equal(root.get("enabled"), enabled);
    }
}
