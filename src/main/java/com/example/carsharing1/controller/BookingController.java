package com.example.carsharing1.controller;

import com.example.carsharing1.dto.BookingDto;
import com.example.carsharing1.entity.Booking;
import com.example.carsharing1.enums.BookingStatus;
import com.example.carsharing1.mapper.BookingMapper;
import com.example.carsharing1.repository.BookingRepository;
import com.example.carsharing1.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Бронирования", description = "Управление бронированиями автомобилей")
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

    @Operation(summary = "Получить все бронирования", description = "Возвращает список всех бронирований")
    @GetMapping
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        log.info(LOG_GET_ALL_BOOKINGS);

        List<BookingDto> bookings = bookingRepository.findAll().stream()
                .map(BookingMapper::toDto)
                .toList();

        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "Получить бронирование по ID", description = "Возвращает бронирование по указанному ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Бронирование " +
            "найдено"), @ApiResponse(responseCode = "404", description = "Бронирование не найдено")
    })
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

    @Operation(summary = "Получить бронирования по машине", description = "Возвращает все бронирования " +
            "указанного автомобиля")
    @GetMapping("/by-car/{carId}")
    public ResponseEntity<List<BookingDto>> getBookingsByCarId(@PathVariable Long carId) {
        log.info(LOG_GET_BY_CAR, carId);

        List<BookingDto> bookings = bookingRepository.findByCarId(carId).stream()
                .map(BookingMapper::toDto)
                .toList();

        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "Получить бронирования по пользователю", description = "Возвращает все " +
            "бронирования указанного пользователя")
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<BookingDto>> getBookingsByUserId(@PathVariable Long userId) {
        log.info(LOG_GET_BY_USER, userId);

        List<BookingDto> bookings = bookingRepository.findByUserId(userId).stream()
                .map(BookingMapper::toDto)
                .toList();

        return ResponseEntity.ok(bookings);
    }

    @Operation(summary = "Создать бронирование", description = "Создает новое бронирование и связанный с ним платеж")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Бронирование " +
            "создано"), @ApiResponse(responseCode = "400", description = "Некорректные " +
            "данные"), @ApiResponse(responseCode = "500", description = "Ошибка " +
            "сервера или откат транзакции")
    })
    @PostMapping
    public ResponseEntity<Object> createBooking(
            @RequestParam @NotNull(message = "ID пользователя обязателен") @Positive(message = "ID пользователя " +
                    "должен быть положительным") Long userId,
            @RequestParam @NotNull(message = "ID автомобиля обязателен") @Positive(message = "ID автомобиля " +
                    "должен быть положительным") Long carId,
            @RequestParam @NotNull(message = "Время начала обязательно") @FutureOrPresent(message = "Время начала " +
                    "не может быть в прошлом") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime startTime,
            @RequestParam @NotNull(message = "Продолжительность" +
                    " " + "обязательна") @Positive(message = "Продолжительность должна быть положительной")
            Integer minutes) {

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

    @Operation(summary = "Завершить бронирование", description = "Переводит бронирование в статус COMPLETED")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Бронирование " +
            "завершено"), @ApiResponse(responseCode = "404", description = "Бронирование не найдено")
    })
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