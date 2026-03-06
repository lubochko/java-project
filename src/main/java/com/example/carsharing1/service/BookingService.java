package com.example.carsharing1.service;

import com.example.carsharing1.dto.BookingDto;
import com.example.carsharing1.entity.Booking;
import com.example.carsharing1.entity.Car;
import com.example.carsharing1.entity.Payment;
import com.example.carsharing1.entity.User;
import com.example.carsharing1.enums.CarStatus;
import com.example.carsharing1.enums.BookingStatus;
import com.example.carsharing1.enums.PaymentStatus;
import com.example.carsharing1.mapper.BookingMapper;
import com.example.carsharing1.repository.BookingRepository;
import com.example.carsharing1.repository.CarRepository;
import com.example.carsharing1.repository.PaymentRepository;
import com.example.carsharing1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public BookingDto createBookingWithPayment(Long userId, Long carId, LocalDateTime startTime, Integer minutes) {
        log.info("СОЗДАНИЕ БРОНИРОВАНИЯ С ПЛАТЕЖОМ");

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.error("Пользователь с ID {} не найден", userId);
            return null;
        }

        Car car = carRepository.findById(carId).orElse(null);
        if (car == null) {
            log.error("Машина с ID {} не найдена", carId);
            return null;
        }


        if (!"AVAILABLE".equals(car.getStatus().name())) {
            log.error("Машина с ID {} недоступна (текущий статус: {})", carId, car.getStatus());
            return null;
        }


        Booking booking = new Booking();
        booking.setUser(user);
        booking.setCar(car);
        booking.setStartTime(startTime);
        booking.setEndTime(startTime.plusMinutes(minutes));
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setTotalCost(car.getPricePerMinute() * minutes);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Бронирование создано с ID: {}", savedBooking.getId());


        car.setStatus(CarStatus.valueOf("BUSY"));
        carRepository.save(car);


        Payment payment = new Payment();
        payment.setBooking(savedBooking);
        payment.setAmount(savedBooking.getTotalCost());
        payment.setPaymentTime(LocalDateTime.now());
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentMethod("CARD");
        payment.setTransactionId(UUID.randomUUID().toString());

        paymentRepository.save(payment);
        log.info("Платеж создан с ID: {}", payment.getId());

        if (minutes > 120) {
            throw new RuntimeException("ОШИБКА! Бронирование более 2 часов невозможно. Транзакция будет откачена");
        }

        return BookingMapper.toDto(savedBooking);
    }
}