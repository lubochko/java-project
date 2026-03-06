package com.example.carsharing1.mapper;

import com.example.carsharing1.dto.UserDto;
//import com.example.carsharing1.dto.BookingDto;
import com.example.carsharing1.entity.User;
import java.util.stream.Collectors;

public class UserMapper {

    private UserMapper() { }

    public static UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setDriverLicense(user.getDriverLicense());
        dto.setRegistrationDate(user.getRegistrationDate());


        return dto;
    }

    public static UserDto toDtoWithBookings(User user) {
        UserDto dto = toDto(user);

        if (user.getBookings() != null && !user.getBookings().isEmpty()) {
            dto.setBookings(user.getBookings().stream()
                    .map(BookingMapper::toDto)
                    .collect(Collectors.toSet()));
        }

        return dto;
    }

    public static User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }

        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setDriverLicense(dto.getDriverLicense());
        user.setRegistrationDate(dto.getRegistrationDate());


        return user;
    }
}