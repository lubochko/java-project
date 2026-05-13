package com.example.carsharing1.dto;

import com.example.carsharing1.enums.BookingStatus;
import com.example.carsharing1.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BookingAdminListItem {

    private final Long id;
    private final Long userId;
    private final String userName;
    private final Long carId;
    private final String carBrand;
    private final String carModel;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final BookingStatus status;
    private final Double totalCost;
    private final Long paymentId;
    private final Double paymentAmount;
    private final LocalDateTime paymentTime;
    private final PaymentStatus paymentStatus;
    private final String paymentMethod;
    private final String paymentTransactionId;
}
