package com.example.carsharing1.dto;

import com.example.carsharing1.enums.BookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO бронирования")
public class BookingDto {

    @Schema(description = "ID бронирования", example = "1")
    private Long id;

    @NotNull(message = "ID пользователя обязателен")
    @Positive(message = "ID пользователя должен быть положительным")
    @Schema(description = "ID пользователя", example = "1", required = true)
    private Long userId;

    @Schema(description = "Имя пользователя")
    private String userName;

    @NotNull(message = "ID автомобиля обязателен")
    @Positive(message = "ID автомобиля должен быть положительным")
    @Schema(description = "ID автомобиля", example = "1", required = true)
    private Long carId;

    @Schema(description = "Марка автомобиля")
    private String carBrand;

    @Schema(description = "Модель автомобиля")
    private String carModel;

    @NotNull(message = "Время начала обязательно")
    @FutureOrPresent(message = "Время начала не может быть в прошлом")
    @Schema(description = "Время начала бронирования", example = "2026-04-01T15:00:00", required = true)
    private LocalDateTime startTime;

    @Schema(description = "Время окончания бронирования")
    private LocalDateTime endTime;

    @NotNull(message = "Статус бронирования обязателен")
    @Schema(description = "Статус бронирования", example = "ACTIVE", required = true)
    private BookingStatus status;

    @DecimalMin(value = "0", message = "Стоимость не может быть отрицательной")
    @Schema(description = "Общая стоимость", example = "180.0")
    private Double totalCost;

    @Schema(description = "Платеж")
    private PaymentDto payment;
}