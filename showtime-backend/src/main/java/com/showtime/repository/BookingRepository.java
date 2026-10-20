package com.showtime.repository;

import com.showtime.entity.Booking;
import com.showtime.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /** HQL/JPQL query #2: booking history of one user, newest show first. */
    @Query("select b from Booking b where b.user.id = :userId order by b.showTime desc")
    List<Booking> findHistory(@Param("userId") Long userId);

    /** Pending bookings whose seat-hold time has run out. */
    @Query("select b from Booking b where b.status = :status and b.expiresAt < :now")
    List<Booking> findExpired(@Param("status") BookingStatus status, @Param("now") LocalDateTime now);
}
