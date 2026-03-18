package com.example.carsharing1.dto;

import com.example.carsharing1.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private Long id;
    private Double amount;
    private LocalDateTime Time;
    private PaymentStatus status;
    private String Method;
    private String transactionId;
}