package com.example.carsharing1.service;

import com.example.carsharing1.dto.BookingDto;
import com.example.carsharing1.dto.BookingBulkOperationResultDto;
import com.example.carsharing1.dto.BookingCreateRequestDto;
import com.example.carsharing1.entity.Booking;
import com.example.carsharing1.entity.Car;
import com.example.carsharing1.entity.Payment;
import com.example.carsharing1.entity.User;
import com.example.carsharing1.enums.BookingStatus;
import com.example.carsharing1.enums.PaymentStatus;
import com.example.carsharing1.exception.BookingException;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final PaymentRepository paymentRepository;
    private static final Comparator<BookingDto> BY_ID = Comparator.comparing(BookingDto::getId);

    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(BookingMapper::toDto)
                .sorted(BY_ID)
                .toList();
    }

    public List<BookingDto> getBookingsByCarId(Long carId) {
        return bookingRepository.findByCarId(carId).stream()
                .map(BookingMapper::toDto)
                .sorted(BY_ID)
                .toList();
    }

    public List<BookingDto> getBookingsByUserId(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(BookingMapper::toDto)
                .sorted(BY_ID)
                .toList();
    }

    public Optional<BookingDto> getBookingById(Long id) {
        return bookingRepository.findById(id).map(BookingMapper::toDto);
    }

    @Transactional
    public BookingDto createBookingWithPayment(Long userId, Long carId, LocalDateTime startTime, Integer minutes) {
        log.info("СОЗДАНИЕ БРОНИРОВАНИЯ С ПЛАТЕЖОМ");
        Booking savedBooking = saveBookingAndPayment(userId, carId, startTime, minutes);
        if (minutes > 120) {
            throw new BookingException("ОШИБКА! Бронирование более 2 часов невозможно. Транзакция будет откачена");
        }
        return BookingMapper.toDto(savedBooking);
    }

    public BookingBulkOperationResultDto createBookingsBulkWithoutTransaction(List<BookingCreateRequestDto> requests) {
        List<BookingDto> created = requests.stream()
                .map(this::createSingleForBulk)
                .toList();
        return new BookingBulkOperationResultDto(created, created.size(), false);
    }

    @Transactional
    public BookingBulkOperationResultDto createBookingsBulkWithTransaction(List<BookingCreateRequestDto> requests) {
        List<BookingDto> created = requests.stream()
                .map(this::createSingleForBulk)
                .toList();
        return new BookingBulkOperationResultDto(created, created.size(), true);
    }

    private BookingDto createSingleForBulk(BookingCreateRequestDto request) {
        Booking booking = saveBookingAndPayment(
                request.getUserId(),
                request.getCarId(),
                request.getStartTime(),
                request.getMinutes()
        );
        if (request.getMinutes() > 120) {
            throw new BookingException("ОШИБКА! Бронирование более 2 часов невозможно. "
                    + "Упали на carId=" + request.getCarId());
        }
        return BookingMapper.toDto(booking);
    }

    private Booking saveBookingAndPayment(Long userId, Long carId, LocalDateTime startTime, Integer minutes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BookingException("Пользователь с ID " + userId + " не найден"));
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new BookingException("Машина с ID " + carId + " не найдена"));

        validateCarAvailability(car, carId);

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setCar(car);
        booking.setStartTime(startTime);
        booking.setEndTime(startTime.plusMinutes(minutes));
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setTotalCost(car.getPricePerMinute() * minutes);

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Бронирование создано с ID: {}", savedBooking.getId());

        Payment payment = buildPayment(savedBooking);
        paymentRepository.save(payment);
        log.info("Платеж создан для бронирования ID: {}", savedBooking.getId());

        return savedBooking;
    }

    private void validateCarAvailability(Car car, Long carId) {
        if (!car.isActive()) {
            throw new BookingException("Машина с ID " + carId + " неактивна");
        }
        if (!car.isAvailable()) {
            throw new BookingException("Машина с ID " + carId + " занята");
        }
    }

    private Payment buildPayment(Booking savedBooking) {
        Payment payment = new Payment();
        payment.setBooking(savedBooking);
        payment.setAmount(savedBooking.getTotalCost());
        payment.setTime(LocalDateTime.now());
        payment.setStatus(PaymentStatus.PAID);
        payment.setMethod("CARD");
        payment.setTransactionId(UUID.randomUUID().toString());
        savedBooking.setPayment(payment);
        return payment;
    }
}