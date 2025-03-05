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
        return (root, query, criteriaBuilder) -> {
            if (role == null) {
                return criteriaBuilder.conjunction();
            }
            // Join với AccountRole để truy vấn
            var accountRoles = root.join("accountRoles");
            return criteriaBuilder.equal(accountRoles.get("role").get("name"), role);
        };
    }

    public static Specification<Account> isBlocked(Boolean isBlocked) {
        return (root, query, criteriaBuilder) -> {
            if (isBlocked == null) {
                return criteriaBuilder.conjunction();
            }
            return isBlocked ?
                    criteriaBuilder.isNotNull(root.get("blockReason")) :
                    criteriaBuilder.isNull(root.get("blockReason"));
        };
    }
}
