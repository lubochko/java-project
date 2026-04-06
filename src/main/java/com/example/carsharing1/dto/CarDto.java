package com.example.carsharing1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO автомобиля")
public class CarDto {

    @Schema(description = "ID автомобиля", example = "1")
    private Long id;

    @NotBlank(message = "Марка не может быть пустой")
    @Size(min = 2, max = 50, message = "Марка должна содержать от 2 до 50 символов")
    @Schema(description = "Марка автомобиля", example = "Toyota", required = true)
    private String brand;

    @NotBlank(message = "Модель не может быть пустой")
    @Size(min = 1, max = 50, message = "Модель должна содержать от 1 до 50 символов")
    @Schema(description = "Модель автомобиля", example = "Camry", required = true)
    private String model;

    @NotNull(message = "Цена за минуту обязательна")
    @DecimalMin(value = "0.1", message = "Цена за минуту должна быть не менее 0.1")
    @DecimalMax(value = "10.0", message = "Цена за минуту должна быть не более 10.0")
    @Schema(description = "Цена за минуту", example = "0.8", required = true)
    private Double pricePerMinute;

    @NotBlank(message = "Номерной знак обязателен")
    @Pattern(regexp = "^[A-Z0-9-]{5,15}$", message = "Некорректный формат номерного знака")
    @Schema(description = "Номерной знак", example = "1234AB-5", required = true)
    private String licensePlate;

    @Min(value = 1990, message = "Год выпуска должен быть не ранее 1990")
    @Max(value = 2026, message = "Год выпуска не может быть в будущем")
    @Schema(description = "Год выпуска", example = "2022")
    private Integer year;

    @DecimalMin(value = "0", message = "Уровень топлива не может быть отрицательным")
    @DecimalMax(value = "100", message = "Уровень топлива не может превышать 100")
    @Schema(description = "Уровень топлива (0-100)", example = "95.0")
    private Double fuelLevel;

    @NotNull(message = "Статус активности обязателен")
    @Schema(description = "Активна ли машина", example = "true")
    private boolean active;

    @Schema(description = "Доступна ли машина (вычисляемое поле)")
    private Boolean available;

    @Schema(description = "Город расположения")
    private String locationCity;

    @Schema(description = "Адрес расположения")
    private String locationAddress;

    @Schema(description = "Список особенностей")
    private Set<String> features;
}