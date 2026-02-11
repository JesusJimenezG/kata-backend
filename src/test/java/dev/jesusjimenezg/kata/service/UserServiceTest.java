package dev.jesusjimenezg.kata.service;

import dev.jesusjimenezg.kata.dto.UserRequest;
import dev.jesusjimenezg.kata.dto.UserResponse;
import dev.jesusjimenezg.kata.dto.UserUpdateRequest;
import dev.jesusjimenezg.kata.model.AppUser;
import dev.jesusjimenezg.kata.model.Role;
import dev.jesusjimenezg.kata.repository.AppUserRepository;
import dev.jesusjimenezg.kata.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private AppUser sampleUser;
    private Role userRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        userRole = new Role("USER");
        userRole.setId(1);

        adminRole = new Role("ADMIN");
        adminRole.setId(2);

        sampleUser = new AppUser();
        sampleUser.setId(UUID.randomUUID());
        sampleUser.setEmail("john@example.com");
        sampleUser.setPasswordHash("hashed");
        sampleUser.setFirstName("John");
        sampleUser.setLastName("Doe");
        sampleUser.setEnabled(true);
        sampleUser.setRoles(Set.of(userRole));
        sampleUser.setCreatedAt(LocalDateTime.now());
        sampleUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void findAll_returnsAllUsers() {
        when(appUserRepository.findAll()).thenReturn(List.of(sampleUser));

        List<UserResponse> result = userService.findAll();

        assertEquals(1, result.size());
        assertEquals("john@example.com", result.get(0).email());
        assertEquals("John", result.get(0).firstName());
        assertTrue(result.get(0).roles().contains("USER"));
    }

    @Test
    void findById_existingUser_returnsUser() {
        UUID id = sampleUser.getId();
        when(appUserRepository.findById(id)).thenReturn(Optional.of(sampleUser));

        UserResponse result = userService.findById(id);

        assertEquals(id, result.id());
        assertEquals("john@example.com", result.email());
    }

    @Test
    void findById_nonExistentUser_throwsException() {
        UUID id = UUID.randomUUID();
        when(appUserRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.findById(id));
    }

    @Test
    void create_validRequest_createsUser() {
        UserRequest request = new UserRequest("new@example.com", "password123", "Jane", "Doe", Set.of("USER"));
        when(appUserRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("password123")).thenReturn("encoded");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        UserResponse result = userService.create(request);

        assertNotNull(result.id());
        assertEquals("new@example.com", result.email());
        assertEquals("Jane", result.firstName());
        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void create_duplicateEmail_throwsException() {
        UserRequest request = new UserRequest("john@example.com", "password123", "John", "Doe", Set.of("USER"));
        when(appUserRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> userService.create(request));
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void create_invalidRole_throwsException() {
        UserRequest request = new UserRequest("new@example.com", "password123", "Jane", "Doe", Set.of("INVALID"));
        when(appUserRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findByName("INVALID")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.create(request));
    }

    @Test
    void create_noRoles_assignsDefaultRole() {
        UserRequest request = new UserRequest("new@example.com", "password123", "Jane", "Doe", null);
        when(appUserRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        UserResponse result = userService.create(request);

        assertTrue(result.roles().contains("USER"));
    }

    @Test
    void update_validRequest_updatesFields() {
        UUID id = sampleUser.getId();
        UserUpdateRequest request = new UserUpdateRequest("Updated", "Name", Set.of("ADMIN"), null);
        when(appUserRepository.findById(id)).thenReturn(Optional.of(sampleUser));
        when(roleRepository.findByName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(appUserRepository.save(any(AppUser.class))).thenReturn(sampleUser);

        UserResponse result = userService.update(id, request);

        assertNotNull(result);
        verify(appUserRepository).save(any(AppUser.class));
    }

    @Test
    void update_nonExistentUser_throwsException() {
        UUID id = UUID.randomUUID();
        UserUpdateRequest request = new UserUpdateRequest("First", "Last", null, null);
        when(appUserRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.update(id, request));
    }

    @Test
    void delete_existingUser_disablesUser() {
        UUID id = sampleUser.getId();
        when(appUserRepository.findById(id)).thenReturn(Optional.of(sampleUser));
        when(appUserRepository.save(any(AppUser.class))).thenReturn(sampleUser);

        userService.delete(id);

        assertFalse(sampleUser.isEnabled());
        verify(appUserRepository).save(sampleUser);
    }

    @Test
    void delete_nonExistentUser_throwsException() {
        UUID id = UUID.randomUUID();
        when(appUserRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.delete(id));
    }
}
