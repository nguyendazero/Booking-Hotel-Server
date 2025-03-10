package com.vinova.booking_hotel.property.specification;

import com.vinova.booking_hotel.property.model.HotelAmenity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import com.vinova.booking_hotel.property.model.Hotel;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class HotelSpecification {

    public static Specification<Hotel> hasAccountId(Long accountId) {
        return (root, query, criteriaBuilder) -> accountId == null ?
                criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("account").get("id"), accountId);
    }

    public static Specification<Hotel> hasDistrictId(Long districtId) {
        return (root, query, criteriaBuilder) -> districtId == null ?
                criteriaBuilder.conjunction() : criteriaBuilder.equal(root.get("district").get("id"), districtId);
    }

    public static Specification<Hotel> hasName(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Hotel> hasMinPrice(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) -> minPrice == null ?
                criteriaBuilder.conjunction() : criteriaBuilder.greaterThanOrEqualTo(root.get("pricePerDay"), minPrice);
    }

    public static Specification<Hotel> hasMaxPrice(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> maxPrice == null ?
                criteriaBuilder.conjunction() : criteriaBuilder.lessThanOrEqualTo(root.get("pricePerDay"), maxPrice);
    }

    public static Specification<Hotel> hasAmenityNames(List<String> amenityNames) {
        return (root, query, criteriaBuilder) -> {
            if (amenityNames == null || amenityNames.isEmpty()) {
                return criteriaBuilder.conjunction(); // Không lọc nếu không có tên tiện nghi
            }

            // Tạo subquery để đếm số lượng tiện nghi
            assert query != null;
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<HotelAmenity> subRoot = subquery.from(HotelAmenity.class);

            // Các điều kiện cho subquery
            subquery.select(criteriaBuilder.count(subRoot))
                    .where(criteriaBuilder.and(
                            criteriaBuilder.equal(subRoot.get("hotel"), root),
                            subRoot.get("amenity").get("name").in(amenityNames)
                    ));

            // Đảm bảo khách sạn có đủ số lượng tiện nghi
            return criteriaBuilder.equal(subquery, (long) amenityNames.size());
        };
    }
    
}
