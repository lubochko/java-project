package com.example.carsharing1.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Some proxies / healthcheck configs concatenate base URL ending with "/" and a path
 * starting with "/", producing "//actuator/health" which would 404. Collapse repeated
 * slashes in the request path only (URI path, not scheme).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CollapseDuplicateSlashesFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (uri == null || !uri.contains("//")) {
            chain.doFilter(request, response);
            return;
        }
        String normalized = uri.replaceAll("/+", "/");
        if (normalized.equals(uri)) {
            chain.doFilter(request, response);
            return;
        }
        String contextPath = request.getContextPath() == null ? "" : request.getContextPath();
        chain.doFilter(new HttpServletRequestWrapper(request) {
            @Override
            public String getRequestURI() {
                return normalized;
            }

            @Override
            public String getServletPath() {
                if (normalized.startsWith(contextPath)) {
                    String path = normalized.substring(contextPath.length());
                    return path.isEmpty() ? "/" : path;
                }
                return request.getServletPath();
            }

            @Override
            public String getPathInfo() {
                return null;
            }
        }, response);
    }
}
