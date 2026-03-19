package com.example.carsharing1.config;

import com.example.carsharing1.entity.Location;
import com.example.carsharing1.entity.Feature;
import com.example.carsharing1.entity.User;
import com.example.carsharing1.entity.Car;
import com.example.carsharing1.entity.Booking;
import com.example.carsharing1.enums.BookingStatus;
import com.example.carsharing1.repository.UserRepository;
import com.example.carsharing1.repository.CarRepository;
import com.example.carsharing1.repository.LocationRepository;
import com.example.carsharing1.repository.FeatureRepository;
import com.example.carsharing1.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
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
            log.info("ДАННЫЕ УЖЕ СУЩЕСТВУЮТ, ПРОПУСКАЕМ ИНИЦИАЛИЗАЦИЮ");
            return;
        }

        Location loc1 = new Location(null, "Минск", "пр-т Независимости, 4", 53.9022,
                27.5618, 10, null);
        Location loc2 = new Location(null, "Минск", "ул. Сурганова, 6", 53.9206,
                27.6026, 15, null);
        Location savedLoc1 = locationRepository.save(loc1);
        Location savedLoc2 = locationRepository.save(loc2);

        log.info("Созданы локации: {} и {}", savedLoc1.getAddress(), savedLoc2.getAddress());

        Feature f1 = new Feature(null, "Кондиционер", "Климат-контроль", "ac", null);
        Feature f2 = new Feature(null, "Автомат", "Автоматическая коробка передач",
                "automatic", null);
        Feature f3 = new Feature(null, "USB", "USB зарядка", "usb", null);
        Feature f4 = new Feature(null, "Навигация", "GPS навигатор", "gps", null);
        Feature f5 = new Feature(null, "Детское кресло", "Детское автомобильное кресло",
                "child-seat", null);
        Feature f6 = new Feature(null, "Подогрев сидений", "Подогрев передних сидений",
                "heated-seats", null);
        Feature f7 = new Feature(null, "Люк", "Панорамная крыша", "sunroof", null);

        Feature savedF1 = featureRepository.save(f1);
        Feature savedF2 = featureRepository.save(f2);
        Feature savedF3 = featureRepository.save(f3);
        Feature savedF4 = featureRepository.save(f4);
        Feature savedF5 = featureRepository.save(f5);
        Feature savedF6 = featureRepository.save(f6);
        Feature savedF7 = featureRepository.save(f7);

        log.info("Создано {} особенностей", 7);

        User user1 = new User(null, "Иван Иванов", "ivan@email.com",
                "+375291234567", "AB123456", LocalDateTime.now(), null);
        User user2 = new User(null, "Петр Петров", "petr@email.com",
                "+375297654321", "CD789012", LocalDateTime.now(), null);
        User user3 = new User(null, "Мария Сидорова", "maria@email.com",
                "+375331234568", "EF345679", LocalDateTime.now(), null);
        User user4 = new User(null, "Алексей Козлов", "alexey@email.com",
                "+375334445566", "GH456780", LocalDateTime.now(), null);
        User user5 = new User(null, "Елена Новикова", "elena@email.com",
                "+375337778899", "IJ567891", LocalDateTime.now(), null);

        User savedUser1 = userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);
        User savedUser3 = userRepository.save(user3);
        User savedUser4 = userRepository.save(user4);
        User savedUser5 = userRepository.save(user5);

        log.info("Созданы пользователи: {}, {}, {}, {}, {}",
                savedUser1.getName(), savedUser2.getName(), savedUser3.getName(),
                savedUser4.getName(), savedUser5.getName());

        Car car1 = new Car(null, "Toyota", "Camry", 0.8,
                "1234AB-5", 2022, 95.0, true,
                savedLoc1, null, Set.of(savedF1, savedF2));

        Car car2 = new Car(null, "BMW", "X5", 1.5,
                "5678CD-5", 2023, 80.0, true,
                savedLoc1, null, Set.of(savedF1, savedF2, savedF4));

        Car car3 = new Car(null, "Audi", "A4", 1.0,
                "9012EF-5", 2022, 45.0, true,
                savedLoc2, null, Set.of(savedF1, savedF3));

        Car car4 = new Car(null, "Tesla", "Model 3", 1.2,
                "3456GH-5", 2023, 90.0, true,
                savedLoc2, null, Set.of(savedF1, savedF2, savedF4));

        Car car5 = new Car(null, "Honda", "Civic", 0.6,
                "4567IJ-6", 2021, 92.0, true,
                savedLoc1, null, Set.of(savedF1, savedF3));

        Car car6 = new Car(null, "Ford", "Focus", 0.55,
                "7890KL-7", 2020, 88.0, true,
                savedLoc2, null, Set.of(savedF2, savedF4));

        Car car7 = new Car(null, "Volkswagen", "Passat", 0.75,
                "1234MN-8", 2022, 94.0, true,
                savedLoc1, null, Set.of(savedF1, savedF2, savedF3));

        Car car8 = new Car(null, "Mercedes", "E-Class", 1.3,
                "5678OP-9", 2023, 97.0, true,
                savedLoc2, null, Set.of(savedF1, savedF2, savedF3, savedF4, savedF5, savedF6, savedF7));

        Car car9 = new Car(null, "Renault", "Logan", 0.4,
                "9012QR-0", 2020, 75.0, true,
                savedLoc1, null, Set.of(savedF1));

        Car car10 = new Car(null, "Kia", "Rio", 0.45,
                "3456ST-1", 2021, 82.0, true,
                savedLoc2, null, Set.of(savedF3));

        Car savedCar1 = carRepository.save(car1);
        Car savedCar2 = carRepository.save(car2);
        Car savedCar3 = carRepository.save(car3);
        Car savedCar4 = carRepository.save(car4);
        Car savedCar5 = carRepository.save(car5);
        Car savedCar6 = carRepository.save(car6);
        Car savedCar7 = carRepository.save(car7);
        Car savedCar8 = carRepository.save(car8);
        Car savedCar9 = carRepository.save(car9);
        Car savedCar10 = carRepository.save(car10);

        log.info("Создано {} автомобилей", 10);

        Booking booking1 = new Booking(null, savedUser1, savedCar3,
                LocalDateTime.now().minusHours(2), LocalDateTime.now().plusHours(1),
                BookingStatus.ACTIVE, 180.0, null);
        bookingRepository.save(booking1);

        Booking booking2 = new Booking(null, savedUser2, savedCar5,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(4),
                BookingStatus.COMPLETED, 150.0, null);
        bookingRepository.save(booking2);

        Booking booking3 = new Booking(null, savedUser3, savedCar6,
                LocalDateTime.now().minusDays(7), LocalDateTime.now().minusDays(6),
                BookingStatus.COMPLETED, 200.0, null);
        bookingRepository.save(booking3);

        Booking booking4 = new Booking(null, savedUser4, savedCar7,
                LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(2),
                BookingStatus.COMPLETED, 180.0, null);
        bookingRepository.save(booking4);

        Booking booking5 = new Booking(null, savedUser5, savedCar8,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusHours(2),
                BookingStatus.ACTIVE, 250.0, null);
        bookingRepository.save(booking5);

        Booking booking6 = new Booking(null, savedUser1, savedCar2,
                LocalDateTime.now().minusDays(10), LocalDateTime.now().minusDays(9),
                BookingStatus.COMPLETED, 300.0, null);
        bookingRepository.save(booking6);

        Booking booking7 = new Booking(null, savedUser2, savedCar8,
                LocalDateTime.now().minusDays(12), LocalDateTime.now().minusDays(11),
                BookingStatus.COMPLETED, 350.0, null);
        bookingRepository.save(booking7);

        Booking booking8 = new Booking(null, savedUser3, savedCar4,
                LocalDateTime.now().minusDays(15), LocalDateTime.now().minusDays(14),
                BookingStatus.COMPLETED, 280.0, null);
        bookingRepository.save(booking8);

        log.info("Создано {} бронирований", 8);
        log.info("ТЕСТОВЫЕ ДАННЫЕ УСПЕШНО ЗАГРУЖЕНЫ");
    }
}