package com.example;

import UserService.*;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.util.List;

public class JavaClient {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 9091;

    public static void main(String[] args) {
        TTransport transport = null;
        try {
            // Create connection to server
            transport = new TSocket(SERVER_HOST, SERVER_PORT);
            TProtocol protocol = new TBinaryProtocol(transport);
            UserService.Client client = new UserService.Client(protocol);

            // Open transport
            transport.open();
            System.out.println("Connected to Thrift server at " + SERVER_HOST + ":" + SERVER_PORT);

            // Demo operations
            performUserOperations(client);

        } catch (TTransportException e) {
            System.err.println("Transport error: " + e.getMessage());
        } catch (TException e) {
            System.err.println("Thrift error: " + e.getMessage());
        } finally {
            if (transport != null) {
                transport.close();
                System.out.println("Connection closed.");
            }
        }
    }

    private static void performUserOperations(UserService.Client client) throws TException {
        System.out.println("\n=== Java Client Demo ===");

        // Create users
        System.out.println("\n1. Creating users...");
        User alice = client.createUser("Alice Johnson", 28);
        User bob = client.createUser("Bob Smith", 35);
        User carol = client.createUser("Carol Davis", 42);

        // List all users
        System.out.println("\n2. Listing all users...");
        List<User> allUsers = client.getAllUsers();
        for (User user : allUsers) {
            System.out.println("  User: " + user);
        }

        // Get specific user
        System.out.println("\n3. Getting specific user...");
        User retrievedUser = client.getUser(alice.getId());
        System.out.println("  Retrieved: " + retrievedUser);

        // Update user
        System.out.println("\n4. Updating user...");
        alice.setAge(29);
        User updatedUser = client.updateUser(alice);
        System.out.println("  Updated: " + updatedUser);

        // Try to get non-existent user
        System.out.println("\n5. Testing exception handling...");
        try {
            client.getUser(999);
        } catch (UserNotFoundException e) {
            System.out.println("  Expected exception: " + e.getMessage());
        }

        // Delete user
        System.out.println("\n6. Deleting user...");
        client.deleteUser(bob.getId());
        System.out.println("  Deleted user with ID: " + bob.getId());

        // Final list
        System.out.println("\n7. Final user list...");
        allUsers = client.getAllUsers();
        for (User user : allUsers) {
            System.out.println("  User: " + user);
        }

        System.out.println("\n=== Java Client Demo Complete ===");
    }
}