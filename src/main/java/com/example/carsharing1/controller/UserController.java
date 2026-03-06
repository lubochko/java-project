package com.example.carsharing1.controller;

import com.example.carsharing1.dto.UserDto;
import com.example.carsharing1.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("GET /api/users - запрос всех пользователей");
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.info("GET /api/users/{} - запрос пользователя по ID", id);
        UserDto user = userService.getUserById(id);

        if (user == null) {
            log.warn("Пользователь с ID {} не найден", id);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String email) {
        log.info("GET /api/users/email/{} - запрос пользователя по email", email);
        UserDto user = userService.getUserByEmail(email);

        if (user == null) {
            log.warn("Пользователь с email {} не найден", email);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping("/{id}/with-bookings")
    public ResponseEntity<UserDto> getUserWithBookings(@PathVariable Long id) {
        log.info("GET /api/users/{}/with-bookings - запрос пользователя с бронированиями", id);
        UserDto user = userService.getUserWithBookings(id);

        if (user == null) {
            log.warn("Пользователь с ID {} не найден", id);
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getUsersCount() {
        log.info("GET /api/users/count - запрос количества пользователей");
        long count = userService.getUsersCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/check-email")
    public ResponseEntity<Boolean> checkEmailExists(@RequestParam String email) {
        log.info("GET /api/users/check-email?email={} - проверка существования email", email);
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDto userDto) {
        log.info("POST /api/users - создание нового пользователя: {}", userDto.getEmail());

        UserDto createdUser = userService.createUser(userDto);

        if (createdUser == null) {
            log.warn("Не удалось создать пользователя: email {} уже занят", userDto.getEmail());
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Пользователь с таким email уже существует");
        }

        log.info("Пользователь создан с ID: {}", createdUser.getId());
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        log.info("PUT /api/users/{} - обновление пользователя", id);

        UserDto updatedUser = userService.updateUser(id, userDto);

        if (updatedUser == null) {
            UserDto existingUser = userService.getUserById(id);
            if (existingUser == null) {
                log.warn("Пользователь с ID {} не найден", id);
                return ResponseEntity.notFound().build();
            } else {
                log.warn("Не удалось обновить пользователя: email {} уже занят", userDto.getEmail());
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("Пользователь с таким email уже существует");
            }
        }

        log.info("Пользователь с ID {} успешно обновлен", id);
        return ResponseEntity.ok(updatedUser);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> patchUser(@PathVariable Long id, @RequestBody UserDto userDto) {
        log.info("PATCH /api/users/{} - частичное обновление пользователя", id);

        UserDto patchedUser = userService.patchUser(id, userDto);

        if (patchedUser == null) {
            UserDto existingUser = userService.getUserById(id);
            if (existingUser == null) {
                log.warn("Пользователь с ID {} не найден", id);
                return ResponseEntity.notFound().build();
            } else {
                log.warn("Не удалось частично обновить пользователя с ID {}", id);
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("Конфликт данных при обновлении");
            }
        }

        log.info("Пользователь с ID {} успешно частично обновлен", id);
        return ResponseEntity.ok(patchedUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{} - удаление пользователя", id);

        UserDto existingUser = userService.getUserById(id);
        if (existingUser == null) {
            log.warn("Пользователь с ID {} не найден", id);
            return ResponseEntity.notFound().build();
        }

        userService.deleteUser(id);
        log.info("Пользователь с ID {} успешно удален", id);
        return ResponseEntity.noContent().build();
    }
}