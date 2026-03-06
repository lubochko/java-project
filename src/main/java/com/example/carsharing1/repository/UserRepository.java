package com.example.carsharing1.repository;

import com.example.carsharing1.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByDriverLicense(String driverLicense);

    @EntityGraph(attributePaths = {"bookings", "bookings.car"})
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithBookings(@Param("id") Long id);

    @Query("SELECT DISTINCT u FROM User u " +
            "LEFT JOIN FETCH u.bookings b " +
            "LEFT JOIN FETCH b.car " +
            "WHERE u.id = :id")
    Optional<User> findByIdWithBookingsFetch(@Param("id") Long id);
}