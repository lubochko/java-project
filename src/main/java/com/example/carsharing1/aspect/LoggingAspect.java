package com.example.carsharing1.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Slf4j
@Aspect
@Component
public class LoggingAspect {
    @Pointcut("execution(* com.example.carsharing1.service.*.*(..))")
    public void serviceMethods() { }

    @Pointcut("execution(* com.example.carsharing1.controller.*.*(..))")
    public void controllerMethods() { }

    @Around("serviceMethods()")
    public Object logServiceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("НАЧАЛО выполнения метода: {}.{}", className, methodName);

        stopWatch.start();
        Object result = joinPoint.proceed();
        stopWatch.stop();

        long executionTime = stopWatch.getTotalTimeMillis();

        if (executionTime > 1000) {
            log.warn("Медленный метод {}.{} - время выполнения: {} мс", className, methodName, executionTime);
        } else {
            log.info("Метод {}.{} выполнен за {} мс", className, methodName, executionTime);
        }

        return result;
    }

    @Around("controllerMethods()")
    public Object logControllerExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        log.info("Вызов контроллера: {}.{} (аргументов: {})", className, methodName, args == null ? 0 : args.length);

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;

        log.info("Контроллер {}.{} выполнен за {} мс", className, methodName, executionTime);

        return result;
    }
}