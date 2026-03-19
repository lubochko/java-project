package com.example.carsharing1.cache;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.data.domain.Pageable;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class CarSearchKey {
    private final String email;
    private final String featureName;
    private final int page;
    private final int size;
    private final String sortBy;
    private final String sortDirection;

    public static CarSearchKey fromParams(String email, String featureName, Pageable pageable) {
        String sortBy = pageable.getSort().stream()
                .findFirst()
                .map(order -> order.getProperty())
                .orElse("id");

        String sortDirection = pageable.getSort().stream()
                .findFirst()
                .map(order -> order.getDirection().name())
                .orElse("ASC");

        return new CarSearchKey(
                email,
                featureName,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sortBy,
                sortDirection
        );
    }
}