package com.example.carsharing1.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarUpsertRequestDto {

    @Valid
    @NotNull
    private CarDto car;

    private Long locationId;

    private Set<Long> featureIds;
}
