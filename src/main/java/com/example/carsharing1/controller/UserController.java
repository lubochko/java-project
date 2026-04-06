package com.example.carsharing1.controller;

import com.example.carsharing1.dto.UserDto;
import com.example.carsharing1.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "Управление пользователями системы")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Получить всех пользователей", description = "Возвращает список всех пользователей")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Получить пользователя по ID", description = "Возвращает пользователя по указанному ID")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Пользователь найден"), @ApiResponse(
            responseCode = "404", description = "Пользователь не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto user = userService.getUserById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Создать пользователя", description = "Создает нового пользователя")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Пользователь" +
            " создан"), @ApiResponse(responseCode = "400",
            description = "Некорректные " +
            "данные"), @ApiResponse(responseCode = "409", description = "Email уже занят")
    })
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody UserDto userDto) {
        UserDto createdUser = userService.createUser(userDto);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @Operation(summary = "Обновить пользователя", description = "Обновляет данные существующего пользователя")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        UserDto updatedUser = userService.updateUser(id, userDto);
        return updatedUser != null ? ResponseEntity.ok(updatedUser) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить количество пользователей", description = "Возвращает общее количество пользователей")
    @GetMapping("/count")
    public ResponseEntity<Long> getUsersCount() {
        return ResponseEntity.ok(userService.getUsersCount());
    }
}