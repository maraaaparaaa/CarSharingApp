package com.carsharing.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity  // says that this is a table in DB
@Table(name = "bookings")
//@Data // generates getter, setter, tostring, equals, hashcode
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id  // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // identity represents autoincrement in db , auto generated id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passenger_id", nullable = false) //many bookings correspond to one user
    private User passenger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false) // many bookings correspond to one ride
    private Ride ride;

    @Column(nullable = false)  // configures the column in sql table, not null
    private Integer seatsBooked;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING) //saves enum in DB
    @Column(nullable = false)
    private BookingStatus status = BookingStatus.PENDING;  // default value

    @CreationTimestamp // registration date and time
    @Column(updatable = false) // once set it cannot be updated
    private LocalDateTime createdAt;

    public enum BookingStatus {
        PENDING,
        CONFIRMED,
        CANCELLED,
        COMPLETED
    }
}