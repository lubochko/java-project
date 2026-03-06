package com.example.carsharing1.repository;

import com.example.carsharing1.entity.Booking;
import com.example.carsharing1.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserId(Long userId);

    List<Booking> findByCarId(Long carId);

    List<Booking> findByStatus(BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.startTime BETWEEN :start AND :end")
    List<Booking> findBookingsInPeriod(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.car.id = :carId " +
            "AND b.status = 'ACTIVE' " +
            "AND b.startTime < :endTime " +
            "AND (b.endTime IS NULL OR b.endTime > :startTime)")
    boolean isCarAvailable(@Param("carId") Long carId,
                           @Param("startTime") LocalDateTime startTime,
                           @Param("endTime") LocalDateTime endTime);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.user " +
            "JOIN FETCH b.car " +
            "WHERE b.id = :id")
    Booking findByIdWithDetails(@Param("id") Long id);
}