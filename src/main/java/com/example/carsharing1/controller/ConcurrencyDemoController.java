package com.example.carsharing1.controller;

import com.example.carsharing1.service.CounterDemoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/concurrency")
@Tag(name = "Демонстрация многопоточности", description = "Race condition и потокобезопасные счетчики")
public class ConcurrencyDemoController {

    private final CounterDemoService counterDemoService;

    @Operation(summary = "Демонстрация race condition",
            description = "Инкремент незащищенного счётчика в нескольких потоках")
    @GetMapping("/race-unsafe")
    public ResponseEntity<Map<String, Object>> raceUnsafe(
            @RequestParam(defaultValue = "50") int threads,
            @RequestParam(defaultValue = "10000") int iterationsPerThread) {
        int actual = counterDemoService.runRaceConditionDemo(threads, iterationsPerThread);
        int expected = threads * iterationsPerThread;
        return ResponseEntity.ok(Map.of(
                "mode", "unsafe",
                "threads", threads,
                "iterationsPerThread", iterationsPerThread,
                "expected", expected,
                "actual", actual
        ));
    }

    @Operation(summary = "Потокобезопасный счетчик на AtomicInteger")
    @GetMapping("/race-atomic")
    public ResponseEntity<Map<String, Object>> raceAtomic(
            @RequestParam(defaultValue = "50") int threads,
            @RequestParam(defaultValue = "10000") int iterationsPerThread) {
        int actual = counterDemoService.runAtomicCounterDemo(threads, iterationsPerThread);
        int expected = threads * iterationsPerThread;
        return ResponseEntity.ok(Map.of(
                "mode", "atomic",
                "threads", threads,
                "iterationsPerThread", iterationsPerThread,
                "expected", expected,
                "actual", actual
        ));
    }

    @Operation(summary = "Потокобезопасный счетчик с synchronized")
    @GetMapping("/race-synchronized")
    public ResponseEntity<Map<String, Object>> raceSynchronized(
            @RequestParam(defaultValue = "50") int threads,
            @RequestParam(defaultValue = "10000") int iterationsPerThread) {
        int actual = counterDemoService.runSynchronizedCounterDemo(threads, iterationsPerThread);
        int expected = threads * iterationsPerThread;
        return ResponseEntity.ok(Map.of(
                "mode", "synchronized",
                "threads", threads,
                "iterationsPerThread", iterationsPerThread,
                "expected", expected,
                "actual", actual
        ));
    }
}

