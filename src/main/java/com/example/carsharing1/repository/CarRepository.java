package com.example.carsharing1.repository;

import com.example.carsharing1.entity.Car;
import com.example.carsharing1.enums.CarStatus;
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

    List<Car> findByStatus(CarStatus status);

    @EntityGraph(attributePaths = {"location", "features"})
    @Query("SELECT c FROM Car c WHERE c.id = :id")
    Optional<Car> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT DISTINCT c FROM Car c " +
            "LEFT JOIN FETCH c.location " +
            "LEFT JOIN FETCH c.features " +
            "WHERE c.status = :status")
    List<Car> findByStatusWithDetails(@Param("status") CarStatus status);

    List<Car> findByBrandAndModel(String brand, String model);

    boolean existsByLicensePlate(String licensePlate);
}