package com.vinova.booking_hotel.property.model;

import com.vinova.booking_hotel.authentication.model.Account;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "hotels")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "high_light_image_url")
    private String highLightImageUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_per_day")
    private BigDecimal pricePerDay;

    @Column(name = "street_address", columnDefinition = "TEXT")
    private String streetAddress;

    @Column(name = "latitude")
    private String latitude;
    
    @Column(name = "longitude")
    private String longitude;

    @ManyToOne
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HotelAmenity> hotelAmenities = new ArrayList<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rating> ratings = new ArrayList<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HotelDiscount> hotelDiscounts = new ArrayList<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WishList> wishList = new ArrayList<>();

    @Column(name = "create_dt")
    @CreationTimestamp
    private ZonedDateTime createDt;

    @Column(name = "update_dt")
    @UpdateTimestamp
    private ZonedDateTime updateDt;
    
}
