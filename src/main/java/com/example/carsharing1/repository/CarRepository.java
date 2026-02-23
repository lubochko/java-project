package com.example.carsharing1.repository;

import com.example.carsharing1.entity.Car;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class CarRepository {

    private final List<Car> cars = new ArrayList<>();

    public CarRepository() {
        cars.add(new Car(1, "Toyota", "Corolla", 0.5, "AVAILABLE"));
        cars.add(new Car(2, "BMW", "X5", 1.2, "BUSY"));
        cars.add(new Car(3, "Audi", "A4", 0.9, "AVAILABLE"));
        cars.add(new Car(4, "Tesla", "Model 3", 1.5, "BUSY"));
        cars.add(new Car(5, "Kia", "Rio", 0.4, "AVAILABLE"));
    }

    public List<Car> findAll() {
        return cars;
    }
}

