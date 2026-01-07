package com.example;

import UserService.UserService;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TTransportException;

public class Server {
    private static final int PORT = 9091;

    public static void main(String[] args) {
        try {
            // Create service implementation
            UserServiceImpl serviceImpl = new UserServiceImpl();
            UserService.Processor<UserServiceImpl> processor = 
                new UserService.Processor<>(serviceImpl);

            // Create server socket
            TServerSocket serverSocket = new TServerSocket(PORT);

            // Create server with thread pool
            TThreadPoolServer.Args serverArgs = new TThreadPoolServer.Args(serverSocket)
                .processor(processor)
                .protocolFactory(new TBinaryProtocol.Factory());

            TServer server = new TThreadPoolServer(serverArgs);

            System.out.println("Starting Apache Thrift User Service on port " + PORT);
            System.out.println("Server ready to accept connections...");

            // Add shutdown hook for graceful shutdown
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                server.stop();
            }));

            // Start the server
            server.serve();

        } catch (TTransportException e) {
            System.err.println("Failed to start server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}