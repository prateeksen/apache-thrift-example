package com.example;

import UserService.*;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for client protocol selection
 * Note: These tests require a running server on port 9091
 * Run the server separately: cd java-server && mvn exec:java
 */
@DisplayName("Client Protocol Integration Tests")
@Disabled("These tests require a running server. Enable manually when testing.")
class IntegrationTest {

    private static final String HOST = "localhost";
    private static final int PORT = 9091;

    @Test
    @DisplayName("Manual Integration: Should create and retrieve user")
    void testCreateAndRetrieveUser() throws TException {
        try (TTransport transport = new TSocket(HOST, PORT)) {
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            UserService.Client client = new UserService.Client(protocol);

            // Create user
            User created = client.createUser("Integration User", 35);
            assertNotNull(created);
            assertEquals("Integration User", created.getName());
            assertEquals(35, created.getAge());

            // Retrieve user
            User retrieved = client.getUser(created.getId());
            assertEquals(created.getId(), retrieved.getId());
            assertEquals(created.getName(), retrieved.getName());
        }
    }

    @Test
    @DisplayName("Manual Integration: Should handle full CRUD operations")
    void testFullCRUDOperations() throws TException {
        try (TTransport transport = new TSocket(HOST, PORT)) {
            transport.open();
            TProtocol protocol = new TBinaryProtocol(transport);
            UserService.Client client = new UserService.Client(protocol);

            // Create
            User user = client.createUser("CRUD Test User", 40);
            long userId = user.getId();

            // Read
            User read = client.getUser(userId);
            assertEquals("CRUD Test User", read.getName());

            // Update
            User updated = new User(userId, "Updated CRUD User", 41);
            client.updateUser(updated);
            User readAfterUpdate = client.getUser(userId);
            assertEquals("Updated CRUD User", readAfterUpdate.getName());
            assertEquals(41, readAfterUpdate.getAge());

            // Delete
            client.deleteUser(userId);
            assertThrows(UserNotFoundException.class, () -> client.getUser(userId));
        }
    }
}
