package com.example.carsharing1.config;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * Railway and similar platforms set {@code DATABASE_URL} as {@code postgresql://user:pass@host:port/db}.
 * Spring/Hikari need {@code jdbc:postgresql://host:port/db} plus optional username/password properties.
 * Also normalizes {@code SPRING_DATASOURCE_URL} when it uses a {@code postgres://} URL without {@code jdbc:}.
 * <p>Runs last so {@code application.properties} resolution cannot place a raw {@code postgresql://} URL
 * above this fix (Spring Boot config data uses {@code addFirst}).
 * <p>If only split variables exist ({@code PGHOST}, {@code PGDATABASE}, {@code PGUSER}, {@code PGPASSWORD}),
 * as when referencing the Railway Postgres service without {@code DATABASE_URL}, builds the JDBC URL here.
 */
public class RailwayJdbcUrlEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final String ENV_DATABASE_URL = "DATABASE_URL";
    private static final String ENV_SPRING_DATASOURCE_URL = "SPRING_DATASOURCE_URL";
    private static final String DATASOURCE_URL = "spring.datasource.url";
    private static final String DATASOURCE_USERNAME = "spring.datasource.username";
    private static final String DATASOURCE_PASSWORD = "spring.datasource.password";

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String databaseUrl = environment.getProperty(ENV_DATABASE_URL);
        if (databaseUrl != null && !databaseUrl.isBlank()) {
            applyParsedUrl(environment, databaseUrl.trim());
            return;
        }

        String springUrl = environment.getProperty(ENV_SPRING_DATASOURCE_URL);
        if (springUrl != null && !springUrl.isBlank()) {
            String trimmed = springUrl.trim();
            if (trimmed.startsWith("postgres://") || trimmed.startsWith("postgresql://")) {
                applyParsedUrl(environment, trimmed);
                return;
            }
            if (trimmed.startsWith("jdbc:")) {
                return;
            }
        }

        applyFromPgHostVariables(environment);
    }

    private static void applyFromPgHostVariables(ConfigurableEnvironment environment) {
        String pghost = environment.getProperty("PGHOST");
        if (pghost == null || pghost.isBlank()) {
            return;
        }
        String pgport = environment.getProperty("PGPORT");
        if (pgport == null || pgport.isBlank()) {
            pgport = "5432";
        }
        String database = firstNonBlank(environment.getProperty("POSTGRES_DB"), environment.getProperty("PGDATABASE"));
        if (database == null || database.isBlank()) {
            database = "postgres";
        }
        String jdbcUrl = "jdbc:postgresql://" + pghost + ":" + pgport + "/" + database;
        String sslmode = environment.getProperty("PGSSLMODE");
        if (sslmode != null && !sslmode.isBlank()) {
            jdbcUrl = jdbcUrl + "?sslmode=" + URLEncoder.encode(sslmode.trim(), StandardCharsets.UTF_8);
        }

        Map<String, Object> map = new HashMap<>();
        map.put(DATASOURCE_URL, jdbcUrl);
        String user = firstNonBlank(environment.getProperty("POSTGRES_USER"), environment.getProperty("PGUSER"));
        String pass = firstNonBlank(environment.getProperty("DB_PASSWORD"), environment.getProperty("PGPASSWORD"));
        if (user != null) {
            map.put(DATASOURCE_USERNAME, user);
        }
        if (pass != null) {
            map.put(DATASOURCE_PASSWORD, pass);
        }
        environment.getPropertySources().addFirst(new MapPropertySource("railwayPgHostVariables", map));
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return null;
    }

    private static void applyParsedUrl(ConfigurableEnvironment environment, String databaseUrl) {
        try {
            Parsed parsed = parsePostgresUrl(databaseUrl);
            Map<String, Object> map = new HashMap<>();
            map.put(DATASOURCE_URL, parsed.jdbcUrl());
            if (parsed.username() != null) {
                map.put(DATASOURCE_USERNAME, parsed.username());
            }
            if (parsed.password() != null) {
                map.put(DATASOURCE_PASSWORD, parsed.password());
            }
            environment.getPropertySources().addFirst(new MapPropertySource("railwayDatabaseUrl", map));
        } catch (RuntimeException ignored) {
            // Leave defaults; startup will surface a datasource error if misconfigured.
        }
    }

    private record Parsed(String jdbcUrl, String username, String password) { }

    private static Parsed parsePostgresUrl(String databaseUrl) {
        String forUri = toHttpFormForParsing(databaseUrl);
        URI uri = URI.create(forUri);
        String userInfo = uri.getRawUserInfo();
        String username = null;
        String password = null;
        if (userInfo != null) {
            int colon = userInfo.indexOf(':');
            if (colon >= 0) {
                username = urlDecode(userInfo.substring(0, colon));
                password = urlDecode(userInfo.substring(colon + 1));
            } else {
                username = urlDecode(userInfo);
            }
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("missing host in database URL");
        }
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        String path = uri.getRawPath();
        String database = "postgres";
        if (path != null && path.length() > 1) {
            database = urlDecode(path.substring(1));
        }
        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;
        String query = uri.getRawQuery();
        if (query != null && !query.isBlank()) {
            jdbcUrl = jdbcUrl + "?" + query;
        }
        return new Parsed(jdbcUrl, username, password);
    }

    private static String toHttpFormForParsing(String databaseUrl) {
        if (databaseUrl.startsWith("postgres://")) {
            return "http://" + databaseUrl.substring("postgres://".length());
        }
        if (databaseUrl.startsWith("postgresql://")) {
            return "http://" + databaseUrl.substring("postgresql://".length());
        }
        throw new IllegalArgumentException("unsupported database URL scheme");
    }

    private static String urlDecode(String raw) {
        return URLDecoder.decode(raw, StandardCharsets.UTF_8);
    }
}
