package com.shopapp.shared.interfaces;

import com.shopapp.shared.domain.Role;

import java.util.Optional;
import java.util.Set;

/**
 * Contract for the User module - used by other modules to interact with user data.
 * This interface ensures loose coupling between modules.
 */
public interface UserModuleApi {
    
    /**
     * Find a user by their ID
     */
    Optional<UserDto> findById(String userId);
    
    /**
     * Find a user by their email
     */
    Optional<UserDto> findByEmail(String email);
    
    /**
     * Add a role to a user
     */
    void addRole(String userId, Role role);
    
    /**
     * Remove a role from a user
     */
    void removeRole(String userId, Role role);
    
    /**
     * Check if a user exists
     */
    boolean existsById(String userId);
    
    /**
     * DTO for user data exposed to other modules
     */
    record UserDto(
            String id,
            String email,
            String firstName,
            String lastName,
            Set<Role> roles,
            boolean enabled
    ) {}
}
