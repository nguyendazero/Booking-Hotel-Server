package com.vinova.booking_hotel.property.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "amenities")
public class Amenity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    
    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "amenity", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HotelAmenity> hotelAmenities = new ArrayList<>();
    
}
