package com.example.carsharing1.controller;

import com.example.carsharing1.dto.AsyncBookingTaskStatusDto;
import com.example.carsharing1.dto.BookingCreateRequestDto;
import com.example.carsharing1.service.AsyncBookingTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/async-bookings")
@Tag(name = "Асинхронные задачи бронирования", description = "Демонстрация @Async и CompletableFuture")
public class AsyncBookingController {

    private final AsyncBookingTaskService asyncBookingTaskService;

    @Operation(summary = "Запустить асинхронное bulk-бронирование",
            description = "Возвращает ID задачи, по которому можно проверять статус")
    @PostMapping("/tasks")
    public ResponseEntity<Map<String, String>> startAsyncBulkBooking(
            @RequestBody List<@Valid BookingCreateRequestDto> requests) {
        String taskId = asyncBookingTaskService.startAsyncBulkBooking(requests);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(Map.of("taskId", taskId));
    }

    @Operation(summary = "Получить статус асинхронной задачи")
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<AsyncBookingTaskStatusDto> getTaskStatus(@PathVariable String taskId) {
        return ResponseEntity.ok(asyncBookingTaskService.getTaskStatus(taskId));
    }
}

