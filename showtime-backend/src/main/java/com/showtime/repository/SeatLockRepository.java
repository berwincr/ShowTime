package com.showtime.repository;

import com.showtime.entity.SeatLock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface SeatLockRepository extends JpaRepository<SeatLock, Long> {

    @Query("select s.seatLabel from SeatLock s where s.showId = :showId")
    List<String> findSeatLabels(@Param("showId") Long showId);

    long countByShowId(Long showId);

    boolean existsByShowIdAndSeatLabelIn(Long showId, Collection<String> seatLabels);

    @Modifying
    @Query("delete from SeatLock s where s.booking.id = :bookingId")
    void deleteByBookingId(@Param("bookingId") Long bookingId);
}
