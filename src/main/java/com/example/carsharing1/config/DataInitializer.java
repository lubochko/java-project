package com.example.carsharing1.config;

import com.example.carsharing1.entity.Location;
import com.example.carsharing1.entity.Feature;
import com.example.carsharing1.entity.User;
import com.example.carsharing1.entity.Car;
import com.example.carsharing1.entity.Booking;
import com.example.carsharing1.enums.CarStatus;
import com.example.carsharing1.repository.UserRepository;
import com.example.carsharing1.repository.CarRepository;
import com.example.carsharing1.repository.LocationRepository;
import com.example.carsharing1.repository.FeatureRepository;
import com.example.carsharing1.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final LocationRepository locationRepository;
    private final FeatureRepository featureRepository;
    private final BookingRepository bookingRepository;

    @Override
    @Transactional
    public void run(String @NonNull ... args) {

        if (featureRepository.count() > 0) {
            System.out.println("ДАННЫЕ УЖЕ СУЩЕСТВУЮТ, ПРОПУСКАЕМ ИНИЦИАЛИЗАЦИЮ");
            return;
        }

        Location loc1 = new Location(null, "Минск", "пр-т Независимости, 4", 53.9022, 27.5618, 10, null);
        Location loc2 = new Location(null, "Минск", "ул. Сурганова, 6", 53.9206, 27.6026, 15, null);
        Location savedLoc1 = locationRepository.save(loc1);
        Location savedLoc2 = locationRepository.save(loc2);


        Feature f1 = new Feature(null, "Кондиционер", "Климат-контроль", "ac", null);
        Feature f2 = new Feature(null, "Автомат", "Автоматическая коробка передач", "automatic", null);
        Feature f3 = new Feature(null, "USB", "USB зарядка", "usb", null);
        Feature f4 = new Feature(null, "Навигация", "GPS навигатор", "gps", null);
        Feature f5 = new Feature(null, "Детское кресло", "Детское автомобильное кресло", "child-seat", null);

        Feature savedF1 = featureRepository.save(f1);
        Feature savedF2 = featureRepository.save(f2);
        Feature savedF3 = featureRepository.save(f3);
        Feature savedF4 = featureRepository.save(f4);
        Feature savedF5 = featureRepository.save(f5);


        User user1 = new User(null, "Иван Иванов", "ivan@email.com",
                "+375291234567", "AB123456", LocalDateTime.now(), null);
        User user2 = new User(null, "Петр Петров", "petr@email.com",
                "+375297654321", "CD789012", LocalDateTime.now(), null);
        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);


        Car car1 = new Car(null, "Toyota", "Camry", 0.8,
                CarStatus.AVAILABLE, "1234AB-5", 2022, 95.0,
                savedLoc1, null, Set.of(savedF1, savedF2));
        Car car2 = new Car(null, "BMW", "X5", 1.5,
                CarStatus.AVAILABLE, "5678CD-5", 2023, 80.0,
                savedLoc1, null, Set.of(savedF1, savedF2, savedF4));
        Car car3 = new Car(null, "Audi", "A4", 1.0,
                CarStatus.BUSY, "9012EF-5", 2022, 45.0,
                savedLoc2, null, Set.of(savedF1, savedF3));
        Car car4 = new Car(null, "Tesla", "Model 3", 1.2,
                CarStatus.AVAILABLE, "3456GH-5", 2023, 90.0,
                savedLoc2, null, Set.of(savedF1, savedF2, savedF4));

        Car savedCar1 = carRepository.save(car1);
        Car savedCar2 = carRepository.save(car2);
        Car savedCar3 = carRepository.save(car3);
        Car savedCar4 = carRepository.save(car4);


        Booking booking1 = new Booking(null, savedUser1, savedCar3,
                LocalDateTime.now().minusHours(2), LocalDateTime.now().plusHours(1),
                com.example.carsharing1.enums.BookingStatus.ACTIVE, 180.0, null);

        bookingRepository.save(booking1);

        System.out.println("ТЕСТОВЫЕ ДАННЫЕ ЗАГРУЖЕНЫ");
    }
}