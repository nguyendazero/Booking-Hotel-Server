package com.vinova.booking_hotel.property.repository;

import com.vinova.booking_hotel.authentication.model.Account;
import com.vinova.booking_hotel.common.enums.BookingStatus;
import com.vinova.booking_hotel.property.model.Booking;
import com.vinova.booking_hotel.property.model.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.hotel.id = :hotelId AND " +
            "(b.startDate < :endDate AND b.endDate > :startDate)")
    List<Booking> findByHotelIdAndDateRange(@Param("hotelId") Long hotelId,
                                            @Param("startDate") ZonedDateTime startDate,
                                            @Param("endDate") ZonedDateTime endDate);

    @Query("SELECT b FROM Booking b WHERE DATE(b.createDt) = :date")
    List<Booking> findByCreateDt(@Param("date") LocalDateTime date);

    List<Booking> findByHotelId(Long hotelId);

    Optional<Booking> findFirstByHotelAndAccount(Hotel hotel, Account account);

    List<Booking> findByStartDateBeforeAndStatus(ZonedDateTime startDate, BookingStatus status);
    List<Booking> findByEndDateBeforeAndStatus(ZonedDateTime endDate, BookingStatus status);

    List<Booking> findByAccount(Account account);

    List<Booking> findByHotel(Hotel hotel);

    @Modifying
    @Query("DELETE FROM Booking b WHERE b.hotel.id = :hotelId")
    void deleteBookingsByHotelId(@Param("hotelId") Long hotelId);
}
