package com.shopapp.user.service;

import com.shopapp.shared.domain.Role;
import com.shopapp.shared.events.vendor.VendorApprovedEvent;
import com.shopapp.shared.exception.BadRequestException;
import com.shopapp.shared.exception.ResourceNotFoundException;
import com.shopapp.shared.interfaces.UserModuleApi;
import com.shopapp.user.domain.User;
import com.shopapp.user.dto.ChangePasswordRequest;
import com.shopapp.user.dto.UpdateProfileRequest;
import com.shopapp.user.dto.UserProfileResponse;
import com.shopapp.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("User Creation (UserRegistrationService)")
    class UserCreation {

        @Test
        @DisplayName("Should create user with USER role")
        void shouldCreateUserWithUserRole() {
            User savedUser = User.builder()
                    .id("userId123")
                    .email("test@example.com")
                    .password("encodedPassword")
                    .firstName("John")
                    .lastName("Doe")
                    .roles(new HashSet<>(Set.of(Role.USER)))
                    .enabled(true)
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            String userId = userService.createUser("test@example.com", "encodedPassword", "John", "Doe");

            assertEquals("userId123", userId);
            
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            
            User capturedUser = userCaptor.getValue();
            assertEquals("test@example.com", capturedUser.getEmail());
            assertEquals("encodedPassword", capturedUser.getPassword());
            assertTrue(capturedUser.getRoles().contains(Role.USER));
            assertTrue(capturedUser.isEnabled());
        }

        @Test
        @DisplayName("Should set user as enabled by default")
        void shouldSetUserAsEnabledByDefault() {
            User savedUser = User.builder().id("userId123").enabled(true).build();
            when(userRepository.save(any(User.class))).thenReturn(savedUser);

            userService.createUser("test@example.com", "password", "John", "Doe");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            assertTrue(userCaptor.getValue().isEnabled());
        }
    }

    @Nested
    @DisplayName("UserModuleApi - Find Operations")
    class FindOperations {

        @Test
        @DisplayName("Should find user by ID")
        void shouldFindUserById() {
            User user = User.builder()
                    .id("userId123")
                    .email("test@example.com")
                    .firstName("John")
                    .lastName("Doe")
                    .roles(new HashSet<>(Set.of(Role.USER)))
                    .enabled(true)
                    .build();

            when(userRepository.findById("userId123")).thenReturn(Optional.of(user));

            Optional<UserModuleApi.UserDto> result = userService.findById("userId123");

            assertTrue(result.isPresent());
            assertEquals("test@example.com", result.get().email());
            assertEquals("John", result.get().firstName());
            assertEquals("Doe", result.get().lastName());
        }

        @Test
        @DisplayName("Should return empty Optional when user not found by ID")
        void shouldReturnEmptyWhenUserNotFoundById() {
            when(userRepository.findById("nonexistent")).thenReturn(Optional.empty());

            Optional<UserModuleApi.UserDto> result = userService.findById("nonexistent");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
            User user = User.builder()
                    .id("userId123")
                    .email("test@example.com")
                    .firstName("John")
                    .lastName("Doe")
                    .roles(new HashSet<>(Set.of(Role.USER)))
                    .enabled(true)
                    .build();

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

            Optional<UserModuleApi.UserDto> result = userService.findByEmail("test@example.com");

            assertTrue(result.isPresent());
            assertEquals("userId123", result.get().id());
        }

        @Test
        @DisplayName("Should return empty Optional when user not found by email")
        void shouldReturnEmptyWhenUserNotFoundByEmail() {
            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            Optional<UserModuleApi.UserDto> result = userService.findByEmail("nonexistent@example.com");

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Should check if user exists by ID")
        void shouldCheckIfUserExistsById() {
            when(userRepository.existsById("userId123")).thenReturn(true);
            when(userRepository.existsById("nonexistent")).thenReturn(false);

            assertTrue(userService.existsById("userId123"));
            assertFalse(userService.existsById("nonexistent"));
        }
    }

    @Nested
    @DisplayName("Role Management")
    class RoleManagement {

        @Test
        @DisplayName("Should add role to user")
        void shouldAddRoleToUser() {
            User user = User.builder()
                    .id("userId123")
                    .roles(new HashSet<>(Set.of(Role.USER)))
                    .build();

            when(userRepository.findById("userId123")).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            userService.addRole("userId123", Role.VENDOR);

            assertTrue(user.getRoles().contains(Role.VENDOR));
            assertTrue(user.getRoles().contains(Role.USER));
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when adding role to non-existent user")
        void shouldThrowExceptionWhenAddingRoleToNonExistentUser() {
            when(userRepository.findById("invalidId")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> userService.addRole("invalidId", Role.VENDOR));
        }

        @Test
        @DisplayName("Should remove role from user")
        void shouldRemoveRoleFromUser() {
            User user = User.builder()
                    .id("userId123")
                    .roles(new HashSet<>(Set.of(Role.USER, Role.VENDOR)))
                    .build();

            when(userRepository.findById("userId123")).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            userService.removeRole("userId123", Role.VENDOR);

            assertFalse(user.getRoles().contains(Role.VENDOR));
            assertTrue(user.getRoles().contains(Role.USER));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when removing role from non-existent user")
        void shouldThrowExceptionWhenRemovingRoleFromNonExistentUser() {
            when(userRepository.findById("invalidId")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> userService.removeRole("invalidId", Role.VENDOR));
        }
    }

    @Nested
    @DisplayName("Profile Management")
    class ProfileManagement {

        @Test
        @DisplayName("Should get user profile")
        void shouldGetUserProfile() {
            User user = User.builder()
                    .id("userId123")
                    .email("test@example.com")
                    .firstName("John")
                    .lastName("Doe")
                    .roles(new HashSet<>(Set.of(Role.USER)))
                    .enabled(true)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(userRepository.findById("userId123")).thenReturn(Optional.of(user));

            UserProfileResponse response = userService.getProfile("userId123");

            assertEquals("userId123", response.getId());
            assertEquals("test@example.com", response.getEmail());
            assertEquals("John", response.getFirstName());
            assertEquals("Doe", response.getLastName());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for non-existent user profile")
        void shouldThrowExceptionForNonExistentUserProfile() {
            when(userRepository.findById("invalidId")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, 
                    () -> userService.getProfile("invalidId"));
        }

        @Test
        @DisplayName("Should update user profile - first name only")
        void shouldUpdateFirstNameOnly() {
            User user = User.builder()
                    .id("userId123")
                    .email("test@example.com")
                    .firstName("John")
                    .lastName("Doe")
                    .roles(new HashSet<>(Set.of(Role.USER)))
                    .enabled(true)
                    .build();

            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .firstName("Jane")
                    .build();

            when(userRepository.findById("userId123")).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            UserProfileResponse response = userService.updateProfile("userId123", request);

            assertEquals("Jane", response.getFirstName());
            assertEquals("Doe", response.getLastName()); // Unchanged
        }

        @Test
        @DisplayName("Should update user profile - last name only")
        void shouldUpdateLastNameOnly() {
            User user = User.builder()
                    .id("userId123")
                    .firstName("John")
                    .lastName("Doe")
                    .roles(new HashSet<>(Set.of(Role.USER)))
                    .enabled(true)
                    .build();

            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .lastName("Smith")
                    .build();

            when(userRepository.findById("userId123")).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            UserProfileResponse response = userService.updateProfile("userId123", request);

            assertEquals("John", response.getFirstName()); // Unchanged
            assertEquals("Smith", response.getLastName());
        }

        @Test
        @DisplayName("Should update user profile - both names")
        void shouldUpdateBothNames() {
            User user = User.builder()
                    .id("userId123")
                    .firstName("John")
                    .lastName("Doe")
                    .roles(new HashSet<>(Set.of(Role.USER)))
                    .enabled(true)
                    .build();

            UpdateProfileRequest request = UpdateProfileRequest.builder()
                    .firstName("Jane")
                    .lastName("Smith")
                    .build();

            when(userRepository.findById("userId123")).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            UserProfileResponse response = userService.updateProfile("userId123", request);

            assertEquals("Jane", response.getFirstName());
            assertEquals("Smith", response.getLastName());
        }
    }

    @Nested
    @DisplayName("Password Management")
    class PasswordManagement {

        @Test
        @DisplayName("Should change password with correct current password")
        void shouldChangePasswordWithCorrectCurrentPassword() {
            User user = User.builder()
                    .id("userId123")
                    .password("currentEncodedPassword")
                    .build();

            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("currentPassword")
                    .newPassword("newPassword")
                    .build();

            when(userRepository.findById("userId123")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("currentPassword", "currentEncodedPassword")).thenReturn(true);
            when(passwordEncoder.encode("newPassword")).thenReturn("newEncodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);

            userService.changePassword("userId123", request);

            assertEquals("newEncodedPassword", user.getPassword());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("Should throw BadRequestException for wrong current password")
        void shouldThrowExceptionForWrongCurrentPassword() {
            User user = User.builder()
                    .id("userId123")
                    .password("currentEncodedPassword")
                    .build();

            ChangePasswordRequest request = ChangePasswordRequest.builder()
                    .currentPassword("wrongPassword")
                    .newPassword("newPassword")
                    .build();

            when(userRepository.findById("userId123")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongPassword", "currentEncodedPassword")).thenReturn(false);

            BadRequestException exception = assertThrows(BadRequestException.class, 
                    () -> userService.changePassword("userId123", request));
            
            assertEquals("Current password is incorrect", exception.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should validate password correctly")
        void shouldValidatePasswordCorrectly() {
            User user = User.builder()
                    .email("test@example.com")
                    .password("encodedPassword")
                    .build();

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("correctPassword", "encodedPassword")).thenReturn(true);
            when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

            assertTrue(userService.validatePassword("test@example.com", "correctPassword"));
            assertFalse(userService.validatePassword("test@example.com", "wrongPassword"));
        }

        @Test
        @DisplayName("Should return false when validating password for non-existent user")
        void shouldReturnFalseForNonExistentUser() {
            when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

            assertFalse(userService.validatePassword("nonexistent@example.com", "anyPassword"));
        }
    }

    @Nested
    @DisplayName("Admin Operations")
    class AdminOperations {

        @Test
        @DisplayName("Should get all users with pagination")
        void shouldGetAllUsersWithPagination() {
            User user1 = User.builder().id("1").email("user1@example.com")
                    .firstName("John").lastName("Doe")
                    .roles(new HashSet<>(Set.of(Role.USER))).enabled(true).build();
            User user2 = User.builder().id("2").email("user2@example.com")
                    .firstName("Jane").lastName("Doe")
                    .roles(new HashSet<>(Set.of(Role.USER))).enabled(true).build();

            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(user1, user2), pageable, 2);

            when(userRepository.findAll(pageable)).thenReturn(userPage);

            Page<UserProfileResponse> result = userService.getAllUsers(pageable);

            assertEquals(2, result.getContent().size());
            assertEquals("user1@example.com", result.getContent().get(0).getEmail());
            assertEquals("user2@example.com", result.getContent().get(1).getEmail());
        }
    }

    @Nested
    @DisplayName("Event Handlers")
    class EventHandlers {

        @Test
        @DisplayName("Should add VENDOR role when handling VendorApprovedEvent")
        void shouldAddVendorRoleOnVendorApproved() {
            User user = User.builder()
                    .id("userId123")
                    .roles(new HashSet<>(Set.of(Role.USER)))
                    .build();

            VendorApprovedEvent event = new VendorApprovedEvent("vendorId", "userId123");

            when(userRepository.findById("userId123")).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenReturn(user);

            userService.handleVendorApproved(event);

            assertTrue(user.getRoles().contains(Role.VENDOR));
            verify(userRepository).save(user);
        }
    }
}
