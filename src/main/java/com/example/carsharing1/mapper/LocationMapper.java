package com.example.carsharing1.mapper;

import com.example.carsharing1.dto.LocationDto;
import com.example.carsharing1.entity.Location;
import java.util.List;

public class LocationMapper {

    private LocationMapper() { }

    public static LocationDto toDto(Location location) {
        if (location == null) {
            return null;
        }

        LocationDto dto = new LocationDto();
        dto.setId(location.getId());
        dto.setCity(location.getCity());
        dto.setAddress(location.getAddress());
        dto.setLatitude(location.getLatitude());
        dto.setLongitude(location.getLongitude());
        dto.setCapacity(location.getCapacity());

        return dto;
    }

    public static List<LocationDto> toDtoList(List<Location> locations) {
        return locations.stream()
                .map(LocationMapper::toDto)
                .toList();
    }

    public static Location toEntity(LocationDto dto) {
        if (dto == null) {
            return null;
        }

        Location location = new Location();
        location.setId(dto.getId());
        location.setCity(dto.getCity());
        location.setAddress(dto.getAddress());
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());
        location.setCapacity(dto.getCapacity());

        return location;
    }
}