package com.shopapp.user.service;

import com.shopapp.auth.service.UserRegistrationService;
import com.shopapp.shared.domain.Role;
import com.shopapp.shared.events.DomainEventPublisher;
import com.shopapp.shared.events.vendor.VendorApprovedEvent;
import com.shopapp.shared.exception.BadRequestException;
import com.shopapp.shared.exception.ResourceNotFoundException;
import com.shopapp.shared.interfaces.UserModuleApi;
import com.shopapp.user.domain.User;
import com.shopapp.user.dto.ChangePasswordRequest;
import com.shopapp.user.dto.UpdateProfileRequest;
import com.shopapp.user.dto.UserProfileResponse;
import com.shopapp.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserModuleApi, UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ===== UserModuleApi Implementation =====

    @Override
    public Optional<UserDto> findById(String userId) {
        return userRepository.findById(userId)
                .map(this::toUserDto);
    }

    @Override
    public Optional<UserDto> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::toUserDto);
    }

    @Override
    @Transactional
    public void addRole(String userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.addRole(role);
        userRepository.save(user);
        log.info("Added role {} to user {}", role, userId);
    }

    @Override
    @Transactional
    public void removeRole(String userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.removeRole(role);
        userRepository.save(user);
        log.info("Removed role {} from user {}", role, userId);
    }

    @Override
    public boolean existsById(String userId) {
        return userRepository.existsById(userId);
    }

    // ===== UserRegistrationService Implementation =====

    @Override
    @Transactional
    public String createUser(String email, String encodedPassword, String firstName, String lastName) {
        User user = User.builder()
                .email(email)
                .password(encodedPassword)
                .firstName(firstName)
                .lastName(lastName)
                .roles(Set.of(Role.USER))
                .enabled(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("Created new user with id: {}", savedUser.getId());
        return savedUser.getId();
    }

    @Override
    public boolean validatePassword(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .map(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
                .orElse(false);
    }

    // ===== User Profile Operations =====

    public UserProfileResponse getProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return toProfileResponse(user);
    }

    @Transactional
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        User updatedUser = userRepository.save(user);
        log.info("Updated profile for user: {}", userId);
        return toProfileResponse(updatedUser);
    }

    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", userId);
    }

    // ===== Admin Operations =====

    public Page<UserProfileResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toProfileResponse);
    }

    // ===== Event Handlers =====

    @EventListener
    public void handleVendorApproved(VendorApprovedEvent event) {
        log.info("Handling VendorApprovedEvent for user: {}", event.getUserId());
        addRole(event.getUserId(), Role.VENDOR);
    }

    // ===== Helper Methods =====

    private UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles(),
                user.isEnabled()
        );
    }

    private UserProfileResponse toProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles())
                .enabled(user.isEnabled())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
