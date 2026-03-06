package com.example.carsharing1.dto;

import com.example.carsharing1.enums.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {
    private Long id;
    private Long userId;
    private String userName;
    private Long carId;
    private String carBrand;
    private String carModel;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BookingStatus status;
    private Double totalCost;
    private PaymentDto payment;
}