package com.example.carsharing1.mapper;

import com.example.carsharing1.dto.BookingAdminListItem;
import com.example.carsharing1.dto.BookingDto;
import com.example.carsharing1.dto.PaymentDto;
import com.example.carsharing1.entity.Booking;

public class BookingMapper {

    private BookingMapper() { }

    public static BookingDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());

        if (booking.getUser() != null) {
            dto.setUserId(booking.getUser().getId());
            dto.setUserName(booking.getUser().getName());
        }

        if (booking.getCar() != null) {
            dto.setCarId(booking.getCar().getId());
            dto.setCarBrand(booking.getCar().getBrand());
            dto.setCarModel(booking.getCar().getModel());
        }

        dto.setStartTime(booking.getStartTime());
        dto.setEndTime(booking.getEndTime());
        dto.setStatus(booking.getStatus());
        dto.setTotalCost(booking.getTotalCost());

        if (booking.getPayment() != null) {
            PaymentDto paymentDto = new PaymentDto();
            paymentDto.setId(booking.getPayment().getId());
            paymentDto.setAmount(booking.getPayment().getAmount());
            paymentDto.setTime(booking.getPayment().getTime());
            paymentDto.setStatus(booking.getPayment().getStatus());
            paymentDto.setMethod(booking.getPayment().getMethod());
            paymentDto.setTransactionId(booking.getPayment().getTransactionId());
            dto.setPayment(paymentDto);
        }

        return dto;
    }

    public static BookingDto fromAdminListItem(BookingAdminListItem row) {
        if (row == null) {
            return null;
        }
        BookingDto dto = new BookingDto();
        dto.setId(row.getId());
        dto.setUserId(row.getUserId());
        dto.setUserName(row.getUserName());
        dto.setCarId(row.getCarId());
        dto.setCarBrand(row.getCarBrand());
        dto.setCarModel(row.getCarModel());
        dto.setStartTime(row.getStartTime());
        dto.setEndTime(row.getEndTime());
        dto.setStatus(row.getStatus());
        dto.setTotalCost(row.getTotalCost());
        if (row.getPaymentId() != null) {
            PaymentDto paymentDto = new PaymentDto();
            paymentDto.setId(row.getPaymentId());
            paymentDto.setAmount(row.getPaymentAmount());
            paymentDto.setTime(row.getPaymentTime());
            paymentDto.setStatus(row.getPaymentStatus());
            paymentDto.setMethod(row.getPaymentMethod());
            paymentDto.setTransactionId(row.getPaymentTransactionId());
            dto.setPayment(paymentDto);
        }
        return dto;
    }
}