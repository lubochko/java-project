package com.example.carsharing1.repository;

import com.example.carsharing1.entity.Car;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> findByBrand(String brand);

    List<Car> findByActiveTrue();

    @Query("SELECT c FROM Car c WHERE c.active = true")
    List<Car> findAllActive();

    @EntityGraph(attributePaths = {"location", "features"})
    @Query("SELECT c FROM Car c WHERE c.id = :id")
    Optional<Car> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM Car c " +
            "LEFT JOIN FETCH c.location " +
            "LEFT JOIN FETCH c.features " +
            "LEFT JOIN FETCH c.bookings b " +
            "LEFT JOIN FETCH b.payment " +
            "WHERE c.active = true")
    List<Car> findAllActiveWithDetails();

    @Query("SELECT c FROM Car c WHERE c.active = true AND c.id NOT IN " +
            "(SELECT DISTINCT b.car.id FROM Booking b WHERE b.status = 'ACTIVE')")
    List<Car> findAvailableCars();

    boolean existsByLicensePlate(String licensePlate);


    @Query("SELECT DISTINCT c FROM Car c " +
            "LEFT JOIN FETCH c.location " +
            "LEFT JOIN FETCH c.features " +
            "LEFT JOIN FETCH c.bookings b " +
            "LEFT JOIN FETCH b.payment " +
            "LEFT JOIN b.user u " +
            "WHERE (:email IS NULL OR u.email = :email) " +
            "AND (:featureName IS NULL OR EXISTS (SELECT f FROM c.features f WHERE f.name = :featureName))")
    List<Car> findCarsByComplexCriteria(@Param("email") String email,
                                        @Param("featureName") String featureName);

    @Query(value = "SELECT c FROM Car c " +
            "WHERE (:email IS NULL OR EXISTS (" +
            "SELECT b FROM Booking b JOIN b.user u WHERE b.car = c AND u.email = :email)) " +
            "AND (:featureName IS NULL OR EXISTS (SELECT f FROM c.features f WHERE f.name = :featureName)) " +
            "AND (:availableOnly = false OR (c.active = true AND NOT EXISTS " +
            "(SELECT activeBooking FROM Booking activeBooking WHERE activeBooking.car = c " +
            "AND activeBooking.status = 'ACTIVE')))",
            countQuery = "SELECT COUNT(c) FROM Car c " +
                    "WHERE (:email IS NULL OR EXISTS (" +
                    "SELECT b FROM Booking b JOIN b.user u WHERE b.car = c AND u.email = :email)) " +
                    "AND (:featureName IS NULL OR EXISTS (SELECT f FROM c.features f WHERE f.name = :featureName)) " +
                    "AND (:availableOnly = false OR (c.active = true AND NOT EXISTS " +
                    "(SELECT activeBooking FROM Booking activeBooking WHERE activeBooking.car = c " +
                    "AND activeBooking.status = 'ACTIVE')))")
    Page<Car> findCarsByComplexCriteriaPaged(@Param("email") String email,
                                             @Param("featureName") String featureName,
                                             @Param("availableOnly") boolean availableOnly,
                                             Pageable pageable);

    @Query(value = "SELECT DISTINCT c.* FROM cars c " +
            "LEFT JOIN bookings b ON c.id = b.car_id " +
            "LEFT JOIN users u ON b.user_id = u.id " +
            "LEFT JOIN car_features cf ON c.id = cf.car_id " +
            "LEFT JOIN features f ON cf.feature_id = f.id " +
            "WHERE (:email IS NULL OR u.email = :email) " +
            "AND (:featureName IS NULL OR f.name = :featureName)",
            nativeQuery = true)
    List<Car> findCarsByComplexCriteriaNative(@Param("email") String email,
                                              @Param("featureName") String featureName);

    @Query(value = "SELECT DISTINCT c.* FROM cars c " +
            "LEFT JOIN bookings b ON c.id = b.car_id " +
            "LEFT JOIN users u ON b.user_id = u.id " +
            "LEFT JOIN car_features cf ON c.id = cf.car_id " +
            "LEFT JOIN features f ON cf.feature_id = f.id " +
            "WHERE (:email IS NULL OR u.email = :email) " +
            "AND (:featureName IS NULL OR f.name = :featureName) " +
            "ORDER BY c.id LIMIT :limit OFFSET :offset",
            nativeQuery = true)
    List<Car> findCarsByComplexCriteriaNativePaged(@Param("email") String email,
                                                   @Param("featureName") String featureName,
                                                   @Param("limit") int limit,
                                                   @Param("offset") int offset);

    List<Car> findByBrandAndModel(String brand, String model);
}