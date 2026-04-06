package com.example.carsharing1.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO пользователя")
public class UserDto {

    @Schema(description = "ID пользователя", example = "1")
    private Long id;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 100, message = "Имя должно содержать от 2 до 100 символов")
    @Pattern(regexp = "^[а-яА-ЯёЁa-zA-Z\\s-]+$", message = "Имя может содержать только буквы, пробелы и дефисы")
    @Schema(description = "Имя пользователя", example = "Иван Иванов", required = true)
    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    @Schema(description = "Email пользователя", example = "ivan@email.com", required = true)
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\s-]{10,20}$", message = "Некорректный формат телефона")
    @Schema(description = "Телефон пользователя", example = "+375291234567")
    private String phone;

    @Pattern(regexp = "^[A-Z0-9]{5,20}$", message = "Некорректный формат водительского удостоверения")
    @Schema(description = "Водительское удостоверение", example = "AB123456")
    private String driverLicense;

    @PastOrPresent(message = "Дата регистрации не может быть в будущем")
    @Schema(description = "Дата регистрации", example = "2026-03-31T12:00:00")
    private LocalDateTime registrationDate;

    @Schema(description = "Бронирования пользователя")
    private Set<BookingDto> bookings;
}