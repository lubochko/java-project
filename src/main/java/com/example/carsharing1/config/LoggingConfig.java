package com.example.carsharing1.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

@Configuration
public class LoggingConfig {

    private static final String ROOT_LOGGER_NAME = org.slf4j.Logger.ROOT_LOGGER_NAME;

    private static final String LOG_PATTERN = "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level [%thread] %logger{36} - %msg%n";

    @PostConstruct
    public void init() {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        Logger rootLogger = context.getLogger(ROOT_LOGGER_NAME);

        rootLogger.detachAndStopAllAppenders();
        rootLogger.addAppender(createConsoleAppender(context));
        rootLogger.addAppender(createFileAppender(context));
        rootLogger.addAppender(createErrorFileAppender(context));
        rootLogger.setLevel(Level.INFO);

        Logger carsharingLogger = context.getLogger("com.example.carsharing1");
        carsharingLogger.setLevel(Level.DEBUG);

        Logger hibernateLogger = context.getLogger("org.hibernate.SQL");
        hibernateLogger.setLevel(Level.DEBUG);

        Logger hibernateTypeLogger = context.getLogger("org.hibernate.type.descriptor.sql");
        hibernateTypeLogger.setLevel(Level.TRACE);
    }

    private ConsoleAppender<ILoggingEvent> createConsoleAppender(LoggerContext context) {
        ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(context);
        appender.setName("CONSOLE");

        PatternLayoutEncoder encoder = createEncoder(context);

        appender.setEncoder(encoder);
        appender.start();

        return appender;
    }

    private RollingFileAppender<ILoggingEvent> createFileAppender(LoggerContext context) {
        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setName("FILE");
        appender.setFile("logs/carsharing.log");

        PatternLayoutEncoder encoder = createEncoder(context);
        appender.setEncoder(encoder);

        SizeAndTimeBasedRollingPolicy<ILoggingEvent> policy = new SizeAndTimeBasedRollingPolicy<>();
        policy.setContext(context);
        policy.setParent(appender);
        policy.setFileNamePattern("logs/carsharing-%d{yyyy-MM-dd}.%i.log");
        policy.setMaxFileSize(FileSize.valueOf("10MB"));
        policy.setMaxHistory(30);
        policy.setTotalSizeCap(FileSize.valueOf("1GB"));
        policy.start();

        appender.setRollingPolicy(policy);
        appender.start();

        return appender;
    }

    private RollingFileAppender<ILoggingEvent> createErrorFileAppender(LoggerContext context) {
        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<>();
        appender.setContext(context);
        appender.setName("ERROR_FILE");
        appender.setFile("logs/error.log");

        ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel("ERROR");
        filter.setContext(context);
        filter.start();
        appender.addFilter(filter);

        PatternLayoutEncoder encoder = createEncoder(context);
        appender.setEncoder(encoder);

        SizeAndTimeBasedRollingPolicy<ILoggingEvent> policy = new SizeAndTimeBasedRollingPolicy<>();
        policy.setContext(context);
        policy.setParent(appender);
        policy.setFileNamePattern("logs/error-%d{yyyy-MM-dd}.%i.log");
        policy.setMaxFileSize(FileSize.valueOf("10MB"));
        policy.setMaxHistory(30);
        policy.start();

        appender.setRollingPolicy(policy);
        appender.start();

        return appender;
    }

    private PatternLayoutEncoder createEncoder(LoggerContext context) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(context);
        encoder.setPattern(LOG_PATTERN);
        encoder.start();
        return encoder;
    }
}