package com.vinova.booking_hotel.property.model;

import com.vinova.booking_hotel.authentication.model.Account;
import jakarta.persistence.*;
import lombok.*;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "configs")
public class Config extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "key", nullable = false)
    private String key;

    @Column(name = "value", nullable = false, columnDefinition = "TEXT")
    private String value;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
    
}
