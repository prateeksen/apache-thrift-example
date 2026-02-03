package com.example;

import UserService.*;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserServiceImpl
 */
@DisplayName("UserService Implementation Tests")
class UserServiceImplTest {

    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceImpl();
    }

    @Test
    @DisplayName("Should create user with auto-generated ID")
    void testCreateUser() throws TException {
        // Given
        String name = "John Doe";
        int age = 30;

        // When
        User user = service.createUser(name, age);

        // Then
        assertNotNull(user);
        assertEquals(1L, user.getId());
        assertEquals(name, user.getName());
        assertEquals(age, user.getAge());
    }

    @Test
    @DisplayName("Should create multiple users with incremental IDs")
    void testCreateMultipleUsers() throws TException {
        // When
        User user1 = service.createUser("Alice", 25);
        User user2 = service.createUser("Bob", 30);
        User user3 = service.createUser("Charlie", 35);

        // Then
        assertEquals(1L, user1.getId());
        assertEquals(2L, user2.getId());
        assertEquals(3L, user3.getId());
    }

    @Test
    @DisplayName("Should retrieve user by ID")
    void testGetUser() throws TException {
        // Given
        User created = service.createUser("Jane Doe", 28);

        // When
        User retrieved = service.getUser(created.getId());

        // Then
        assertNotNull(retrieved);
        assertEquals(created.getId(), retrieved.getId());
        assertEquals(created.getName(), retrieved.getName());
        assertEquals(created.getAge(), retrieved.getAge());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException for non-existent user")
    void testGetUserNotFound() {
        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            service.getUser(999L);
        });
    }

    @Test
    @DisplayName("Should update existing user")
    void testUpdateUser() throws TException {
        // Given
        User created = service.createUser("Original Name", 25);
        User updated = new User(created.getId(), "Updated Name", 30);

        // When
        User result = service.updateUser(updated);

        // Then
        assertEquals(updated.getName(), result.getName());
        assertEquals(updated.getAge(), result.getAge());

        // Verify the change persisted
        User retrieved = service.getUser(created.getId());
        assertEquals("Updated Name", retrieved.getName());
        assertEquals(30, retrieved.getAge());
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when updating non-existent user")
    void testUpdateUserNotFound() {
        // Given
        User nonExistent = new User(999L, "Ghost User", 100);

        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            service.updateUser(nonExistent);
        });
    }

    @Test
    @DisplayName("Should delete existing user")
    void testDeleteUser() throws TException {
        // Given
        User created = service.createUser("To Be Deleted", 40);

        // When
        service.deleteUser(created.getId());

        // Then
        assertThrows(UserNotFoundException.class, () -> {
            service.getUser(created.getId());
        });
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when deleting non-existent user")
    void testDeleteUserNotFound() {
        // When & Then
        assertThrows(UserNotFoundException.class, () -> {
            service.deleteUser(999L);
        });
    }

    @Test
    @DisplayName("Should retrieve all users")
    void testGetAllUsers() throws TException {
        // Given
        service.createUser("User1", 20);
        service.createUser("User2", 30);
        service.createUser("User3", 40);

        // When
        List<User> allUsers = service.getAllUsers();

        // Then
        assertEquals(3, allUsers.size());
    }

    @Test
    @DisplayName("Should return empty list when no users exist")
    void testGetAllUsersEmpty() throws TException {
        // When
        List<User> allUsers = service.getAllUsers();

        // Then
        assertNotNull(allUsers);
        assertTrue(allUsers.isEmpty());
    }

    @Test
    @DisplayName("Should validate correct user data")
    void testValidateUserDataValid() throws TException {
        // When
        String result = service.validateUserData("John Doe", 30, true);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("valid"));
        assertTrue(result.contains("John Doe"));
    }

    @Test
    @DisplayName("Should throw TApplicationException for null name")
    void testValidateUserDataNullName() {
        // When & Then
        TApplicationException exception = assertThrows(TApplicationException.class, () -> {
            service.validateUserData(null, 30, true);
        });
        assertTrue(exception.getMessage().contains("Name cannot be null"));
    }

    @Test
    @DisplayName("Should throw TApplicationException for empty name")
    void testValidateUserDataEmptyName() {
        // When & Then
        TApplicationException exception = assertThrows(TApplicationException.class, () -> {
            service.validateUserData("", 30, true);
        });
        assertTrue(exception.getMessage().contains("Name cannot be null"));
    }

    @Test
    @DisplayName("Should throw TApplicationException for negative age")
    void testValidateUserDataNegativeAge() {
        // When & Then
        TApplicationException exception = assertThrows(TApplicationException.class, () -> {
            service.validateUserData("John Doe", -1, true);
        });
        assertTrue(exception.getMessage().contains("Age must be between"));
    }

    @Test
    @DisplayName("Should throw TApplicationException for age too high")
    void testValidateUserDataAgeToHigh() {
        // When & Then
        TApplicationException exception = assertThrows(TApplicationException.class, () -> {
            service.validateUserData("John Doe", 200, true);
        });
        assertTrue(exception.getMessage().contains("Age must be between"));
    }

    @Test
    @DisplayName("Should accept boundary age values")
    void testValidateUserDataBoundaryAges() throws TException {
        // Test minimum age
        String result1 = service.validateUserData("Baby", 0, true);
        assertNotNull(result1);

        // Test maximum age
        String result2 = service.validateUserData("Elder", 150, true);
        assertNotNull(result2);
    }

    @Test
    @DisplayName("Should execute oneway call without blocking")
    void testLogUserActivity() throws TException {
        // Given
        long startTime = System.currentTimeMillis();

        // When
        service.logUserActivity("login", 1L, "2026-02-03T12:00:00Z");

        // Then - Should complete quickly (oneway calls don't wait for processing)
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 100, "Oneway call should return immediately");
    }

    @Test
    @DisplayName("Should handle complete CRUD lifecycle")
    void testCRUDLifecycle() throws TException {
        // Create
        User created = service.createUser("Lifecycle User", 25);
        assertNotNull(created);
        assertEquals(1L, created.getId());

        // Read
        User read = service.getUser(created.getId());
        assertEquals(created.getName(), read.getName());

        // Update
        User updated = new User(created.getId(), "Updated Lifecycle User", 26);
        service.updateUser(updated);
        User readAfterUpdate = service.getUser(created.getId());
        assertEquals("Updated Lifecycle User", readAfterUpdate.getName());
        assertEquals(26, readAfterUpdate.getAge());

        // Delete
        service.deleteUser(created.getId());
        assertThrows(UserNotFoundException.class, () -> {
            service.getUser(created.getId());
        });
    }

    @Test
    @DisplayName("Should maintain data integrity across multiple operations")
    void testDataIntegrity() throws TException {
        // Create multiple users
        service.createUser("User1", 20);
        service.createUser("User2", 30);
        User user3 = service.createUser("User3", 40);
        service.createUser("User4", 50);

        // Delete one user
        service.deleteUser(user3.getId());

        // Verify remaining users
        List<User> remaining = service.getAllUsers();
        assertEquals(3, remaining.size());
        assertFalse(remaining.stream().anyMatch(u -> u.getId() == user3.getId()));
    }
}
