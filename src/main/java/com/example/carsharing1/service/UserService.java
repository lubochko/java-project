package com.example.carsharing1.service;

import com.example.carsharing1.dto.UserDto;
import com.example.carsharing1.entity.User;
import com.example.carsharing1.mapper.UserMapper;
import com.example.carsharing1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        log.info("Получение списка всех пользователей");
        return userRepository.findAll().stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long id) {
        log.info("Получение пользователя с ID: {}", id);
        return userRepository.findById(id)
                .map(UserMapper::toDto)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public UserDto getUserByEmail(String email) {
        log.info("Получение пользователя с email: {}", email);
        return userRepository.findByEmail(email)
                .map(UserMapper::toDto)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public UserDto getUserWithBookings(Long id) {
        log.info("Получение пользователя с бронированиями, ID: {}", id);
        return userRepository.findByIdWithBookings(id)
                .map(UserMapper::toDtoWithBookings)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public long getUsersCount() {
        log.info("Получение количества пользователей");
        return userRepository.count();
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        log.info("Проверка существования email: {}", email);
        return userRepository.findByEmail(email).isPresent();
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.info("Создание нового пользователя с email: {}", userDto.getEmail());


        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            log.warn("Email {} уже занят", userDto.getEmail());
            return null;
        }


        if (userDto.getDriverLicense() != null && !userDto.getDriverLicense().isEmpty() &&
                userRepository.findByDriverLicense(userDto.getDriverLicense()).isPresent()) {
            log.warn("Водительское удостоверение {} уже зарегистрировано", userDto.getDriverLicense());
            return null;
        }

        User user = UserMapper.toEntity(userDto);
        user.setRegistrationDate(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        log.info("Пользователь создан с ID: {}", savedUser.getId());

        return UserMapper.toDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto userDto) {
        log.info("Обновление пользователя с ID: {}", id);

        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            log.warn("Пользователь с ID {} не найден", id);
            return null;
        }


        if (!existingUser.getEmail().equals(userDto.getEmail()) &&
                userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            log.warn("Email {} уже занят", userDto.getEmail());
            return null;
        }


        if (userDto.getDriverLicense() != null && !userDto.getDriverLicense().isEmpty() &&
                !userDto.getDriverLicense().equals(existingUser.getDriverLicense()) &&
                userRepository.findByDriverLicense(userDto.getDriverLicense()).isPresent()) {
            log.warn("Водительское удостоверение {} уже зарегистрировано", userDto.getDriverLicense());
            return null;
        }

        existingUser.setName(userDto.getName());
        existingUser.setEmail(userDto.getEmail());
        existingUser.setPhone(userDto.getPhone());
        existingUser.setDriverLicense(userDto.getDriverLicense());

        User updatedUser = userRepository.save(existingUser);
        log.info("Пользователь с ID {} обновлен", id);
        return UserMapper.toDto(updatedUser);
    }

    @Transactional
    public UserDto patchUser(Long id, UserDto userDto) {
        log.info("Частичное обновление пользователя с ID: {}", id);

        User existingUser = userRepository.findById(id).orElse(null);
        if (existingUser == null) {
            log.warn("Пользователь с ID {} не найден", id);
            return null;
        }


        if (userDto.getName() != null) {
            existingUser.setName(userDto.getName());
        }

        if (userDto.getEmail() != null) {

            if (!existingUser.getEmail().equals(userDto.getEmail()) &&
                    userRepository.findByEmail(userDto.getEmail()).isPresent()) {
                log.warn("Email {} уже занят", userDto.getEmail());
                return null;
            }
            existingUser.setEmail(userDto.getEmail());
        }

        if (userDto.getPhone() != null) {
            existingUser.setPhone(userDto.getPhone());
        }

        if (userDto.getDriverLicense() != null) {

            if (!userDto.getDriverLicense().equals(existingUser.getDriverLicense()) &&
                    userRepository.findByDriverLicense(userDto.getDriverLicense()).isPresent()) {
                log.warn("Водительское удостоверение {} уже зарегистрировано", userDto.getDriverLicense());
                return null;
            }
            existingUser.setDriverLicense(userDto.getDriverLicense());
        }

        User updatedUser = userRepository.save(existingUser);
        log.info("Пользователь с ID {} частично обновлен", id);
        return UserMapper.toDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Удаление пользователя с ID: {}", id);


        if (!userRepository.existsById(id)) {
            log.warn("Пользователь с ID {} не найден, удаление невозможно", id);
            return;
        }

        userRepository.deleteById(id);
        log.info("Пользователь с ID {} удален", id);
    }
}