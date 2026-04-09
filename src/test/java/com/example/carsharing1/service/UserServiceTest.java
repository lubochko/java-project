package com.example.carsharing1.service;

import com.example.carsharing1.dto.UserDto;
import com.example.carsharing1.entity.User;
import com.example.carsharing1.exception.DuplicateEmailException;
import com.example.carsharing1.exception.DuplicateLicenseException;
import com.example.carsharing1.exception.UserNotFoundException;
import com.example.carsharing1.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    @Test
    void getAllUsersSortsById() {
        when(userRepository.findAll()).thenReturn(List.of(user(2L), user(1L)));

        List<UserDto> users = userService.getAllUsers();

        assertEquals(List.of(1L, 2L), users.stream().map(UserDto::getId).toList());
    }

    @Test
    void getByIdAndEmailAndWithBookingsAndCountAndExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(userRepository.findByEmail("a@a.ru")).thenReturn(Optional.of(user(1L)));
        when(userRepository.findByIdWithBookings(1L)).thenReturn(Optional.of(user(1L)));
        when(userRepository.count()).thenReturn(5L);

        assertNotNull(userService.getUserById(1L));
        assertNotNull(userService.getUserByEmail("a@a.ru"));
        assertNotNull(userService.getUserWithBookings(1L));
        assertEquals(5L, userService.getUsersCount());
        assertTrue(userService.existsByEmail("a@a.ru"));
    }

    @Test
    void gettersReturnNullAndExistsFalseWhenNotFound() {
        when(userRepository.findById(55L)).thenReturn(Optional.empty());
        when(userRepository.findByEmail("none@x.ru")).thenReturn(Optional.empty());
        when(userRepository.findByIdWithBookings(55L)).thenReturn(Optional.empty());
        assertNull(userService.getUserById(55L));
        assertNull(userService.getUserByEmail("none@x.ru"));
        assertNull(userService.getUserWithBookings(55L));
        assertTrue(!userService.existsByEmail("none@x.ru"));
    }

    @Test
    void createUserSuccess() {
        UserDto dto = new UserDto();
        dto.setName("N");
        dto.setEmail("n@n.ru");
        dto.setDriverLicense("DL-1");
        when(userRepository.findByEmail("n@n.ru")).thenReturn(Optional.empty());
        when(userRepository.findByDriverLicense("DL-1")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        UserDto created = userService.createUser(dto);

        assertEquals(1L, created.getId());
        assertNotNull(created.getRegistrationDate());
    }

    @Test
    void createUserThrowsOnDuplicateEmailOrLicense() {
        UserDto dto = new UserDto();
        dto.setEmail("dup@x.ru");
        dto.setDriverLicense("L1");
        when(userRepository.findByEmail("dup@x.ru")).thenReturn(Optional.of(user(5L)));

        assertThrows(DuplicateEmailException.class, () -> userService.createUser(dto));

        dto.setEmail("ok@x.ru");
        when(userRepository.findByEmail("ok@x.ru")).thenReturn(Optional.empty());
        when(userRepository.findByDriverLicense("L1")).thenReturn(Optional.of(user(7L)));
        assertThrows(DuplicateLicenseException.class, () -> userService.createUser(dto));
    }

    @Test
    void createUserWithEmptyEmailAndLicenseSkipsDuplicateChecks() {
        UserDto dto = new UserDto();
        dto.setName("Empty");
        dto.setEmail("");
        dto.setDriverLicense("");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User saved = inv.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        UserDto created = userService.createUser(dto);

        assertEquals(10L, created.getId());
        verify(userRepository, never()).findByEmail("");
        verify(userRepository, never()).findByDriverLicense("");
    }

    @Test
    void createUserWithNullEmailAndLicenseSkipsDuplicateChecks() {
        UserDto dto = new UserDto();
        dto.setName("Nulls");
        dto.setEmail(null);
        dto.setDriverLicense(null);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User saved = inv.getArgument(0);
            saved.setId(11L);
            return saved;
        });

        UserDto created = userService.createUser(dto);

        assertEquals(11L, created.getId());
        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).findByDriverLicense(any());
    }

    @Test
    void updateUserAndPatchUserSuccess() {
        User existing = user(1L);
        existing.setEmail("old@x.ru");
        existing.setDriverLicense("OLD-L");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("new@x.ru")).thenReturn(Optional.empty());
        when(userRepository.findByDriverLicense("NEW-L")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto update = new UserDto();
        update.setName("New Name");
        update.setPhone("777");
        update.setEmail("new@x.ru");
        update.setDriverLicense("NEW-L");
        UserDto updated = userService.updateUser(1L, update);

        assertEquals("new@x.ru", updated.getEmail());
        assertEquals("NEW-L", updated.getDriverLicense());

        UserDto patch = new UserDto();
        patch.setName("Patch");
        patch.setPhone("999");
        UserDto patched = userService.patchUser(1L, patch);
        assertEquals("Patch", patched.getName());
        assertEquals("999", patched.getPhone());
    }

    @Test
    void updateUserDoesNotValidateWhenValuesUnchanged() {
        User existing = user(1L);
        existing.setEmail("same@x.ru");
        existing.setDriverLicense("SAME");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto update = new UserDto();
        update.setEmail("same@x.ru");
        update.setDriverLicense("SAME");
        update.setName("Name");
        UserDto updated = userService.updateUser(1L, update);

        assertEquals("same@x.ru", updated.getEmail());
        verify(userRepository, never()).findByEmail("same@x.ru");
        verify(userRepository, never()).findByDriverLicense("SAME");
    }

    @Test
    void updateUserWithNullEmailAndLicenseCoversNullBranch() {
        User existing = user(8L);
        existing.setEmail("old@x.ru");
        existing.setDriverLicense("OLD");
        when(userRepository.findById(8L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto update = new UserDto();
        update.setName("Only Name");
        update.setPhone("123");
        UserDto updated = userService.updateUser(8L, update);

        assertEquals("Only Name", updated.getName());
        assertEquals("123", updated.getPhone());
        assertEquals("old@x.ru", updated.getEmail());
        assertEquals("OLD", updated.getDriverLicense());
    }

    @Test
    void updateUserWithEmptyEmailAndLicenseCoversEmptyBranch() {
        User existing = user(9L);
        existing.setEmail("old9@x.ru");
        existing.setDriverLicense("OLD9");
        when(userRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto update = new UserDto();
        update.setEmail("");
        update.setDriverLicense("");
        UserDto updated = userService.updateUser(9L, update);

        assertEquals("", updated.getEmail());
        assertEquals("", updated.getDriverLicense());
        verify(userRepository, never()).findByEmail("");
        verify(userRepository, never()).findByDriverLicense("");
    }

    @Test
    void updateAndPatchThrowWhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        UserDto updateDto = new UserDto();
        UserDto patchDto = new UserDto();
        assertThrows(UserNotFoundException.class, () -> userService.updateUser(99L, updateDto));
        assertThrows(UserNotFoundException.class, () -> userService.patchUser(99L, patchDto));
    }

    @Test
    void patchThrowsOnDuplicateEmailOrLicense() {
        User existing = user(1L);
        existing.setEmail("a@a.ru");
        existing.setDriverLicense("DL-A");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));

        UserDto patchEmail = new UserDto();
        patchEmail.setEmail("dup@x.ru");
        when(userRepository.findByEmail("dup@x.ru")).thenReturn(Optional.of(user(2L)));
        assertThrows(DuplicateEmailException.class, () -> userService.patchUser(1L, patchEmail));

        UserDto patchLicense = new UserDto();
        patchLicense.setDriverLicense("dup-license");
        when(userRepository.findByDriverLicense("dup-license")).thenReturn(Optional.of(user(3L)));
        assertThrows(DuplicateLicenseException.class, () -> userService.patchUser(1L, patchLicense));
    }

    @Test
    void patchUserUpdatesEmailAndLicenseWhenProvided() {
        User existing = user(15L);
        existing.setEmail("old15@x.ru");
        existing.setDriverLicense("OLD-15");
        when(userRepository.findById(15L)).thenReturn(Optional.of(existing));
        when(userRepository.findByEmail("new15@x.ru")).thenReturn(Optional.empty());
        when(userRepository.findByDriverLicense("NEW-15")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto patch = new UserDto();
        patch.setEmail("new15@x.ru");
        patch.setDriverLicense("NEW-15");
        UserDto updated = userService.patchUser(15L, patch);

        assertEquals("new15@x.ru", updated.getEmail());
        assertEquals("NEW-15", updated.getDriverLicense());
    }

    @Test
    void patchUserWithNullEmailAndLicenseOnlyUpdatesNullableFields() {
        User existing = user(16L);
        existing.setEmail("old16@x.ru");
        existing.setDriverLicense("OLD-16");
        when(userRepository.findById(16L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto patch = new UserDto();
        patch.setName("Name16");
        patch.setPhone("1616");
        patch.setEmail(null);
        patch.setDriverLicense(null);
        UserDto updated = userService.patchUser(16L, patch);

        assertEquals("Name16", updated.getName());
        assertEquals("1616", updated.getPhone());
        assertEquals("old16@x.ru", updated.getEmail());
        assertEquals("OLD-16", updated.getDriverLicense());
    }

    @Test
    void deleteUserDeletesOnlyWhenExists() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);

        when(userRepository.existsById(2L)).thenReturn(false);
        userService.deleteUser(2L);
        verify(userRepository, never()).deleteById(2L);
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setName("User " + id);
        user.setEmail("u" + id + "@x.ru");
        user.setDriverLicense("DL-" + id);
        return user;
    }
}
