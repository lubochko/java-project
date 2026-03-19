package com.example.carsharing1.controller;

import com.example.carsharing1.dto.BookingDto;
import com.example.carsharing1.entity.Booking;
import com.example.carsharing1.enums.BookingStatus;
import com.example.carsharing1.mapper.BookingMapper;
import com.example.carsharing1.repository.BookingRepository;
import com.example.carsharing1.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    
    private static final String LOG_GET_ALL_BOOKINGS = "GET /api/bookings - запрос всех бронирований";
    private static final String LOG_GET_BOOKING_BY_ID = "GET /api/bookings/{} - запрос бронирования по ID";
    private static final String LOG_GET_BY_CAR = "GET /api/bookings/by-car/{} - запрос бронирований по машине";
    private static final String LOG_GET_BY_USER = "GET /api/bookings/by-user/{} - запрос бронирований по пользователю";
    private static final String LOG_BOOKING_FOUND = "Бронирование с ID {} найдено";
    private static final String LOG_BOOKING_NOT_FOUND = "Бронирование с ID {} не найдено";
    private static final String LOG_BOOKING_COMPLETED = "Бронирование с ID {} завершено";
    private static final String LOG_CREATE_BOOKING = "POST /api/bookings - создание бронирования";
    private static final String LOG_COMPLETE_BOOKING = "PATCH /api/bookings/{}/complete - завершение бронирования";
    private static final String ERROR_CREATE_BOOKING = "Ошибка при создании бронирования: {}";

    @GetMapping
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        log.info(LOG_GET_ALL_BOOKINGS);

        List<BookingDto> bookings = bookingRepository.findAll().stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long id) {
        log.info(LOG_GET_BOOKING_BY_ID, id);

        return bookingRepository.findById(id)
                .map(booking -> {
                    log.info(LOG_BOOKING_FOUND, id);
                    return ResponseEntity.ok(BookingMapper.toDto(booking));
                })
                .orElseGet(() -> {
                    log.warn(LOG_BOOKING_NOT_FOUND, id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/by-car/{carId}")
    public ResponseEntity<List<BookingDto>> getBookingsByCarId(@PathVariable Long carId) {
        log.info(LOG_GET_BY_CAR, carId);

        List<BookingDto> bookings = bookingRepository.findByCarId(carId).stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<BookingDto>> getBookingsByUserId(@PathVariable Long userId) {
        log.info(LOG_GET_BY_USER, userId);

        List<BookingDto> bookings = bookingRepository.findByUserId(userId).stream()
                .map(BookingMapper::toDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(bookings);
    }

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestParam Long userId,
                                                @RequestParam Long carId,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                LocalDateTime startTime,
                                                @RequestParam Integer minutes) {

        log.info(LOG_CREATE_BOOKING);

        try {
            BookingDto booking = bookingService.createBookingWithPayment(userId, carId, startTime, minutes);
            return new ResponseEntity<>(booking, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error(ERROR_CREATE_BOOKING, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Object> completeBooking(@PathVariable Long id) {
        log.info(LOG_COMPLETE_BOOKING, id);

        return bookingRepository.findById(id)
                .map(booking -> {
                    booking.setStatus(BookingStatus.COMPLETED);
                    booking.setEndTime(LocalDateTime.now());
                    Booking updatedBooking = bookingRepository.save(booking);
                    log.info(LOG_BOOKING_COMPLETED, id);
                    return ResponseEntity.ok((Object) BookingMapper.toDto(updatedBooking));
                })
                .orElseGet(() -> {
                    log.warn(LOG_BOOKING_NOT_FOUND, id);
                    return ResponseEntity.notFound().build();
                });
    }
}