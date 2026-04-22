package com.example.carsharing1.service;

import com.example.carsharing1.dto.AsyncBookingTaskStatusDto;
import com.example.carsharing1.dto.BookingBulkOperationResultDto;
import com.example.carsharing1.dto.BookingCreateRequestDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class AsyncBookingTaskService {
    private static final long RECEIVED_STATUS_DELAY_MS = 5_000L;
    private static final long PROCESSING_STATUS_DELAY_MS = 7_000L;

    public enum TaskStatus {
        RECEIVED,
        PROCESSING,
        READY,
        FAILED
    }

    private static class TaskInfo {
        private volatile TaskStatus status;
        private volatile String detail;

        public TaskInfo(TaskStatus status) {
            this.status = status;
            this.detail = "Задача создана";
        }
    }

    private final BookingService bookingService;
    private final AsyncBookingTaskService self;
    private final Map<String, TaskInfo> tasks = new ConcurrentHashMap<>();
    private final AtomicLong taskIdSequence = new AtomicLong(0);

    public AsyncBookingTaskService(BookingService bookingService, @Lazy AsyncBookingTaskService self) {
        this.bookingService = bookingService;
        this.self = self;
    }

    public String startAsyncBulkBooking(List<BookingCreateRequestDto> requests) {
        String taskId = String.valueOf(taskIdSequence.incrementAndGet());
        tasks.put(taskId, new TaskInfo(TaskStatus.RECEIVED));
        self.executeTaskAsync(taskId, requests);
        log.info("Async booking task {} started", taskId);
        return taskId;
    }

    @Async
    public CompletableFuture<Void> executeTaskAsync(String taskId, List<BookingCreateRequestDto> requests) {
        TaskInfo taskInfo = tasks.get(taskId);
        if (taskInfo == null) {
            return CompletableFuture.completedFuture(null);
        }

        taskInfo.status = TaskStatus.RECEIVED;
        taskInfo.detail = "Запрос на пакетное бронирование получен";
        try {
            sleepForStatusTracking(RECEIVED_STATUS_DELAY_MS);
            taskInfo.status = TaskStatus.PROCESSING;
            taskInfo.detail = "Идет обработка заявок на бронирование";
            sleepForStatusTracking(PROCESSING_STATUS_DELAY_MS);
            BookingBulkOperationResultDto result =
                    bookingService.createBookingsBulkWithTransaction(requests);
            int successCount = result.getCreatedCount() == null ? 0 : result.getCreatedCount();
            int failureCount = Math.max(0, requests.size() - successCount);
            taskInfo.status = TaskStatus.READY;
            taskInfo.detail = "Готово. Успешно: " + successCount
                    + ", ошибок: " + failureCount;
            log.info("Async booking task {} completed successfully", taskId);
        } catch (Exception ex) {
            log.error("Async booking task {} failed: {}", taskId, ex.getMessage(), ex);
            taskInfo.status = TaskStatus.FAILED;
            taskInfo.detail = "Ошибка: " + ex.getMessage();
        }
        return CompletableFuture.completedFuture(null);
    }

    private void sleepForStatusTracking(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Асинхронная обработка была прервана", e);
        }
    }

    public AsyncBookingTaskStatusDto getTaskStatus(String taskId) {
        TaskInfo info = tasks.get(taskId);
        if (info == null) {
            return new AsyncBookingTaskStatusDto(taskId, "NOT_FOUND", "Задача не найдена");
        }
        return new AsyncBookingTaskStatusDto(taskId, mapStatusForApi(info.status), info.detail);
    }

    private String mapStatusForApi(TaskStatus status) {
        return switch (status) {
            case RECEIVED -> "ПОЛУЧЕНО";
            case PROCESSING -> "ОБРАБАТЫВАЕТСЯ";
            case READY -> "ГОТОВО";
            case FAILED -> "ОШИБКА";
        };
    }
}