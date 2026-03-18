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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    @PostMapping
    public ResponseEntity<Object> createBooking(@RequestParam Long userId,
                                                @RequestParam Long carId,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
                                                LocalDateTime startTime,
                                                @RequestParam Integer minutes) {

        try {
            BookingDto booking = bookingService.createBookingWithPayment(userId, carId, startTime, minutes);
            return new ResponseEntity<>(booking, HttpStatus.CREATED);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<Object> completeBooking(@PathVariable Long id) {
        log.info("PATCH /api/bookings/{}/complete - завершение бронирования", id);

        return bookingRepository.findById(id)
                .map(booking -> {
                    booking.setStatus(BookingStatus.COMPLETED);
                    booking.setEndTime(LocalDateTime.now());
                    Booking updatedBooking = bookingRepository.save(booking);
                    log.info("Бронирование с ID {} завершено", id);
                    return ResponseEntity.ok((Object) BookingMapper.toDto(updatedBooking));
                })
                .orElseGet(() -> {
                    log.warn("Бронирование с ID {} не найдено", id);
                    return ResponseEntity.notFound().build();
                });
    }
}