package com.example.carsharing1.repository;

import com.example.carsharing1.entity.Car;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class CarRepository {

    private static final String STATUS_AVAILABLE = "AVAILABLE";
    private static final String STATUS_BUSY = "BUSY";

    private final List<Car> cars = new ArrayList<>();

    public CarRepository() {
        cars.add(new Car(1, "Toyota", "Corolla", 0.5, STATUS_AVAILABLE));
        cars.add(new Car(2, "BMW", "X5", 1.2, STATUS_BUSY));
        cars.add(new Car(3, "Audi", "A4", 0.9, STATUS_AVAILABLE));
        cars.add(new Car(4, "Tesla", "Model 3", 1.5, STATUS_BUSY));
        cars.add(new Car(5, "Kia", "Rio", 0.4, STATUS_AVAILABLE));
    }

    public List<Car> findAll() {
        return cars;
    }
}

