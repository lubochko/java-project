package com.example.carsharing1.dto;

import com.example.carsharing1.dto.BookingBulkOperationResultDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsyncBookingTaskStatusDto {

    private String taskId;

    private String status;

    private String errorMessage;

    private BookingBulkOperationResultDto result;
}

