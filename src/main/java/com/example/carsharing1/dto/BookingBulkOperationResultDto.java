package com.example.carsharing1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingBulkOperationResultDto {
    private List<BookingDto> bookings;
    private Integer createdCount;
    private boolean transactional;
}
