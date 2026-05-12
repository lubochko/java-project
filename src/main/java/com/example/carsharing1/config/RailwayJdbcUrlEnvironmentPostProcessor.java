package com.example.carsharing1.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Railway/Heroku expose {@code DATABASE_URL} as {@code postgresql://...} without the {@code jdbc:}
 * prefix. Hikari passes the value to the driver as-is; PostgreSQL JDBC requires
 * {@code jdbc:postgresql://...}.
 */
public class RailwayJdbcUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String DATASOURCE_URL = "spring.datasource.url";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String url = environment.getProperty(DATASOURCE_URL);
        if (url == null || url.isBlank()) {
            return;
        }
        String trimmed = url.trim();
        String jdbcUrl = toJdbcPostgresqlUrl(trimmed);
        if (jdbcUrl != null) {
            Map<String, Object> map = new HashMap<>();
            map.put(DATASOURCE_URL, jdbcUrl);
            environment.getPropertySources().addFirst(new MapPropertySource("railwayJdbcUrl", map));
        }
    }

    static String toJdbcPostgresqlUrl(String url) {
        if (url.startsWith("jdbc:postgresql:") || url.startsWith("jdbc:postgres:")) {
            return null;
        }
        if (url.startsWith("postgres://")) {
            return "jdbc:postgresql://" + url.substring("postgres://".length());
        }
        if (url.startsWith("postgresql://")) {
            return "jdbc:" + url;
        }
        return null;
    }
}
