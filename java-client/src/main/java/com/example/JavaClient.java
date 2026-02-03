package com.example;

import UserService.*;
import org.apache.thrift.TException;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
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

            // Protocol selection: env > arg > default
            String envProtocol = System.getenv("THRIFT_PROTOCOL");
            String argProtocol = (args.length > 0) ? args[0] : null;
            String protocolName = (envProtocol != null && !envProtocol.isEmpty()) ? envProtocol : (argProtocol != null ? argProtocol : "binary");
            protocolName = protocolName.trim().toLowerCase();

            transport = new TSocket(SERVER_HOST, SERVER_PORT);
            TProtocol protocol;
            switch (protocolName) {
                case "compact":
                    protocol = new TCompactProtocol(transport);
                    break;
                case "json":
                    protocol = new TJSONProtocol(transport);
                    break;
                case "simplejson":
                    protocol = new TSimpleJSONProtocol(transport);
                    break;
                case "binary":
                default:
                    protocol = new TBinaryProtocol(transport);
            }

            // Set env variable for child processes (optional, for demo)
            System.setProperty("THRIFT_PROTOCOL", protocolName);

            UserService.Client client = new UserService.Client(protocol);

            // Open transport
            transport.open();
            System.out.println("Connected to Thrift server at " + SERVER_HOST + ":" + SERVER_PORT + " using protocol: " + protocolName);

            // Demo operations
            performUserOperations(client);

        } catch (TTransportException e) {
            System.err.println("Transport error: " + e.getMessage());
        } catch (TException e) {
            System.err.println("Thrift error: " + e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("InterruptedException error: " + e.getMessage());
        } finally {
            if (transport != null) {
                transport.close();
                System.out.println("Connection closed.");
            }
        }
    }

    private static void performUserOperations(UserService.Client client) throws TException, InterruptedException {
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

        // Test TApplicationException (Protocol Exception)
        System.out.println("\n8. Testing TApplicationException (Protocol Exception)...");
        
        // Test 1: Valid call (should work)
        // try {
        //     String result = client.validateUserData("John Doe", 30, true);
        //     System.out.println("  ✓ Valid data accepted: " + result);
        // } catch (TApplicationException e) {
        //     System.out.println("  ✗ Unexpected TApplicationException: " + e.getMessage());
        // } catch (TException e) {
        //     System.out.println("  ✗ Unexpected TException: " + e.getMessage());
        // }
        
        // Test 2: Invalid data (should trigger TApplicationException)
        // try {
        //     System.out.println("  Attempting invalid call with null name...");
        //     String result = client.validateUserData(null, 25, true);
        //     System.out.println("  ✗ Should not reach here! Result: " + result);
        // } catch (TApplicationException e) {
        //     System.out.println("  ✓ Caught TApplicationException (EXCEPTION frame): " + e.getType() + " - " + e.getMessage());
        // } catch (TException e) {
        //     System.out.println("  ✗ Unexpected TException instead of TApplicationException: " + e.getMessage());
        // }
        
        // Test 3: Invalid age (should trigger TApplicationException)
        try {
            System.out.println("  Attempting invalid call with age = 200...");
            String result = client.validateUserData("Jane Doe", 200, false);
            System.out.println("  ✗ Should not reach here! Result: " + result);
        } catch (TApplicationException e) {
            System.out.println("  ✓ Caught TApplicationException (EXCEPTION frame): " + e.getType() + " - " + e.getMessage());
        } catch (TException e) {
            System.out.println("  ✗ Unexpected TException instead of TApplicationException: " + e.getMessage());
        }

        // Test Oneway calls (Fire-and-forget)
        System.out.println("\n9. Testing Oneway calls (Fire-and-forget)...");
        
        try {
            long currentTime = System.currentTimeMillis();
            String timestamp = java.time.Instant.ofEpochMilli(currentTime).toString();
            
            System.out.println("  Sending oneway call: logUserActivity('login', 1, '" + timestamp + "')");
            long startTime = System.nanoTime();
            
            // This call returns immediately - no response expected (ONEWAY frame)
            client.logUserActivity("login", 1L, timestamp);
            
            long endTime = System.nanoTime();
            long duration = endTime - startTime;
            
            System.out.println("  ✓ Oneway call completed in " + (duration / 1000) + " microseconds");
            System.out.println("  ✓ No response expected (fire-and-forget semantics)");
            
            // Send multiple oneway calls rapidly
            for (int i = 0; i < 3; i++) {
                String action = "action_" + i;
                long userId = i + 10;
                String ts = java.time.Instant.ofEpochMilli(System.currentTimeMillis()).toString();
                client.logUserActivity(action, userId, ts);
                System.out.println("  ✓ Sent oneway call " + (i + 1) + ": " + action);
            }
            
            System.out.println("  ✓ All oneway calls sent rapidly (no waiting for responses)");
            
        } catch (TException e) {
            System.out.println("  ✗ Unexpected error in oneway call: " + e.getMessage());
        }

        System.out.println("\n=== Java Client Demo Complete ===");
    }
}