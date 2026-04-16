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

    private static final String KEY_MODE = "mode";
    private static final String KEY_THREADS = "threads";
    private static final String KEY_ITERATIONS = "iterationsPerThread";
    private static final String KEY_EXPECTED = "expected";
    private static final String KEY_ACTUAL = "actual";

    private static final String MODE_UNSAFE = "unsafe";
    private static final String MODE_ATOMIC = "atomic";
    private static final String MODE_SYNCHRONIZED = "synchronized";

    @Operation(summary = "Демонстрация race condition",
            description = "Инкремент незащищенного счётчика в нескольких потоках")
    @GetMapping("/race-unsafe")
    public ResponseEntity<Map<String, Object>> raceUnsafe(
            @RequestParam(defaultValue = "50") int threads,
            @RequestParam(defaultValue = "10000") int iterationsPerThread) {
        int actual = counterDemoService.runRaceConditionDemo(threads, iterationsPerThread);
        int expected = threads * iterationsPerThread;
        return ResponseEntity.ok(Map.of(
                KEY_MODE, MODE_UNSAFE,
                KEY_THREADS, threads,
                KEY_ITERATIONS, iterationsPerThread,
                KEY_EXPECTED, expected,
                KEY_ACTUAL, actual
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
                KEY_MODE, MODE_ATOMIC,
                KEY_THREADS, threads,
                KEY_ITERATIONS, iterationsPerThread,
                KEY_EXPECTED, expected,
                KEY_ACTUAL, actual
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
                KEY_MODE, MODE_SYNCHRONIZED,
                KEY_THREADS, threads,
                KEY_ITERATIONS, iterationsPerThread,
                KEY_EXPECTED, expected,
                KEY_ACTUAL, actual
        ));
    }
}