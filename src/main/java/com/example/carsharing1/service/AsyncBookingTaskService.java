package com.example.carsharing1.service;

import com.example.carsharing1.dto.AsyncBookingTaskStatusDto;
import com.example.carsharing1.dto.BookingBulkOperationResultDto;
import com.example.carsharing1.dto.BookingCreateRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncBookingTaskService {

    public enum TaskStatus {
        CREATED,
        RUNNING,
        SUCCESS,
        FAILED
    }

    private static class TaskInfo {
        private volatile TaskStatus status;
        private volatile String errorMessage;
        private volatile BookingBulkOperationResultDto result;

        public TaskInfo(TaskStatus status) {
            this.status = status;
        }
    }

    private final BookingService bookingService;

    // Самоинъекция через прокси
    @Lazy
    @Autowired
    private AsyncBookingTaskService self;

    private final Map<String, TaskInfo> tasks = new ConcurrentHashMap<>();

    public String startAsyncBulkBooking(List<BookingCreateRequestDto> requests) {
        String taskId = UUID.randomUUID().toString();
        tasks.put(taskId, new TaskInfo(TaskStatus.CREATED));
        // Вызов через self, а не через this
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

        taskInfo.status = TaskStatus.RUNNING;

        return CompletableFuture.runAsync(() -> {
            try {
                BookingBulkOperationResultDto result =
                        bookingService.createBookingsBulkWithTransaction(requests);
                taskInfo.result = result;
                taskInfo.status = TaskStatus.SUCCESS;
                log.info("Async booking task {} completed successfully", taskId);
            } catch (Exception ex) {
                log.error("Async booking task {} failed: {}", taskId, ex.getMessage(), ex);
                taskInfo.errorMessage = ex.getMessage();
                taskInfo.status = TaskStatus.FAILED;
            }
        });
    }

    public AsyncBookingTaskStatusDto getTaskStatus(String taskId) {
        TaskInfo info = tasks.get(taskId);
        if (info == null) {
            return new AsyncBookingTaskStatusDto(taskId, "NOT_FOUND", "Задача не найдена", null);
        }
        return new AsyncBookingTaskStatusDto(
                taskId,
                info.status.name(),
                info.errorMessage,
                info.result
        );
    }
}