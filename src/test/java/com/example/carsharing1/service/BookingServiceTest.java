package com.example.carsharing1.service;

import com.example.carsharing1.dto.BookingBulkOperationResultDto;
import com.example.carsharing1.dto.BookingCreateRequestDto;
import com.example.carsharing1.dto.BookingDto;
import com.example.carsharing1.entity.Booking;
import com.example.carsharing1.entity.Car;
import com.example.carsharing1.entity.Payment;
import com.example.carsharing1.entity.User;
import com.example.carsharing1.enums.BookingStatus;
import com.example.carsharing1.exception.BookingException;
import com.example.carsharing1.repository.BookingRepository;
import com.example.carsharing1.repository.CarRepository;
import com.example.carsharing1.repository.PaymentRepository;
import com.example.carsharing1.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CarRepository carRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @InjectMocks
    private BookingService bookingService;

    @Test
    void getAllBookingsSortsById() {
        Booking b2 = booking(2L, 40.0);
        Booking b1 = booking(1L, 20.0);
        when(bookingRepository.findAll()).thenReturn(List.of(b2, b1));

        List<BookingDto> result = bookingService.getAllBookings();

        assertEquals(List.of(1L, 2L), result.stream().map(BookingDto::getId).toList());
    }

    @Test
    void getBookingByIdReturnsOptional() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking(1L, 20.0)));
        assertTrue(bookingService.getBookingById(1L).isPresent());
        when(bookingRepository.findById(2L)).thenReturn(Optional.empty());
        assertTrue(bookingService.getBookingById(2L).isEmpty());
    }

    @Test
    void createBookingWithPaymentSuccess() {
        User user = user(10L);
        Car car = activeCar(11L, true);
        LocalDateTime start = LocalDateTime.now().plusHours(1);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(carRepository.findById(11L)).thenReturn(Optional.of(car));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(100L);
            return b;
        });
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingDto result = bookingService.createBookingWithPayment(10L, 11L, start, 60);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(90.0, result.getTotalCost());
        verify(bookingRepository, times(1)).save(any(Booking.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createBookingWithPaymentThrowsWhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        Long userId = 1L;
        Long carId = 2L;
        Integer minutes = 30;

        BookingException ex = assertThrows(
                BookingException.class,
                () -> bookingService.createBookingWithPayment(userId, carId, startTime, minutes)
        );

        assertTrue(ex.getMessage().contains("Пользователь"));
    }

    @Test
    void createBookingWithPaymentThrowsWhenCarInactive() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        Car car = activeCar(2L, true);
        car.setActive(false);
        when(carRepository.findById(2L)).thenReturn(Optional.of(car));
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        Long userId = 1L;
        Long carId = 2L;
        Integer minutes = 30;

        BookingException ex = assertThrows(
                BookingException.class,
                () -> bookingService.createBookingWithPayment(userId, carId, startTime, minutes)
        );

        assertTrue(ex.getMessage().contains("неактивна"));
    }

    @Test
    void createBookingWithPaymentThrowsWhenCarNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(carRepository.findById(2L)).thenReturn(Optional.empty());
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        Long userId = 1L;
        Long carId = 2L;
        Integer minutes = 30;

        BookingException ex = assertThrows(
                BookingException.class,
                () -> bookingService.createBookingWithPayment(userId, carId, startTime, minutes)
        );

        assertTrue(ex.getMessage().contains("не найдена"));
    }

    @Test
    void createBookingWithPaymentThrowsWhenCarUnavailable() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        Car car = activeCar(2L, false);
        when(carRepository.findById(2L)).thenReturn(Optional.of(car));
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        Long userId = 1L;
        Long carId = 2L;
        Integer minutes = 30;

        BookingException ex = assertThrows(
                BookingException.class,
                () -> bookingService.createBookingWithPayment(userId, carId, startTime, minutes)
        );

        assertTrue(ex.getMessage().contains("занята"));
    }

    @Test
    void createBookingWithPaymentThrowsWhenMoreThanTwoHours() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(carRepository.findById(2L)).thenReturn(Optional.of(activeCar(2L, true)));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        Long userId = 1L;
        Long carId = 2L;
        Integer minutes = 130;

        BookingException ex = assertThrows(
                BookingException.class,
                () -> bookingService.createBookingWithPayment(userId, carId, startTime, minutes)
        );

        assertTrue(ex.getMessage().contains("более 2 часов"));
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void createBookingsBulkWithoutTransactionCreatesAll() {
        prepareForBulk();
        List<BookingCreateRequestDto> requests = List.of(
                new BookingCreateRequestDto(1L, 10L, LocalDateTime.now().plusHours(1), 20),
                new BookingCreateRequestDto(1L, 10L, LocalDateTime.now().plusHours(2), 30)
        );

        BookingBulkOperationResultDto result = bookingService.createBookingsBulkWithoutTransaction(requests);

        assertEquals(2, result.getCreatedCount());
        assertFalse(result.isTransactional());
    }

    @Test
    void createBookingsBulkWithoutTransactionStopsOnError() {
        prepareForBulk();
        List<BookingCreateRequestDto> requests = List.of(
                new BookingCreateRequestDto(1L, 10L, LocalDateTime.now().plusHours(1), 20),
                new BookingCreateRequestDto(1L, 10L, LocalDateTime.now().plusHours(2), 130)
        );

        assertThrows(BookingException.class, () -> bookingService.createBookingsBulkWithoutTransaction(requests));
        verify(bookingRepository, times(2)).save(any(Booking.class));
    }

    @Test
    void createBookingsBulkWithTransactionReturnsFlag() {
        prepareForBulk();
        List<BookingCreateRequestDto> requests = List.of(
                new BookingCreateRequestDto(1L, 10L, LocalDateTime.now().plusHours(1), 20)
        );

        BookingBulkOperationResultDto result = bookingService.createBookingsBulkWithTransaction(requests);

        assertTrue(result.isTransactional());
        assertEquals(1, result.getCreatedCount());
    }

    @Test
    void createBookingsBulkWithTransactionThrowsOnError() {
        prepareForBulk();
        List<BookingCreateRequestDto> requests = List.of(
                new BookingCreateRequestDto(1L, 10L, LocalDateTime.now().plusHours(1), 20),
                new BookingCreateRequestDto(1L, 10L, LocalDateTime.now().plusHours(2), 130)
        );

        assertThrows(BookingException.class, () -> bookingService.createBookingsBulkWithTransaction(requests));
    }

    @Test
    void getBookingsByUserAndCarSorted() {
        Booking b2 = booking(2L, 20.0);
        Booking b1 = booking(1L, 10.0);
        when(bookingRepository.findByUserId(7L)).thenReturn(List.of(b2, b1));
        when(bookingRepository.findByCarId(8L)).thenReturn(List.of(b2, b1));

        assertEquals(List.of(1L, 2L), bookingService.getBookingsByUserId(7L).stream().map(BookingDto::getId).toList());
        assertEquals(List.of(1L, 2L), bookingService.getBookingsByCarId(8L).stream().map(BookingDto::getId).toList());
    }

    private void prepareForBulk() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(carRepository.findById(10L)).thenReturn(Optional.of(activeCar(10L, true)));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(99L);
            return booking;
        });
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private Booking booking(Long id, Double total) {
        Booking booking = new Booking();
        booking.setId(id);
        booking.setTotalCost(total);
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setStartTime(LocalDateTime.now().plusHours(1));
        booking.setEndTime(LocalDateTime.now().plusHours(2));
        booking.setUser(user(1L));
        booking.setCar(activeCar(2L, true));
        return booking;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("User" + id);
        return user;
    }

    private Car activeCar(Long id, boolean available) {
        Car car = new Car();
        car.setId(id);
        car.setBrand("B");
        car.setModel("M");
        car.setPricePerMinute(1.5);
        car.setActive(true);
        if (!available) {
            Booking active = new Booking();
            active.setStatus(BookingStatus.ACTIVE);
            car.getBookings().add(active);
        }
        return car;
    }
}
