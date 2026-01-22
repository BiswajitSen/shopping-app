package com.shopapp.auth.service;

/**
 * Interface for user registration operations.
 * This is implemented by the User module but used by the Auth module.
 * This allows the Auth module to create users without direct dependency on User module internals.
 */
public interface UserRegistrationService {
    
    /**
     * Create a new user with the given details
     * @return the created user's ID
     */
    String createUser(String email, String encodedPassword, String firstName, String lastName);
    
    /**
     * Validate a user's password
     */
    boolean validatePassword(String email, String rawPassword);
}
