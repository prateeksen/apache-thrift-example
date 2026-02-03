package com.example;

import UserService.UserService;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TJSONProtocol;
import org.apache.thrift.protocol.TSimpleJSONProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
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


            // Protocol selection: env > arg > default
            String envProtocol = System.getenv("THRIFT_PROTOCOL");
            String argProtocol = (args.length > 0) ? args[0] : null;
            String protocolName = (envProtocol != null && !envProtocol.isEmpty()) ? envProtocol : (argProtocol != null ? argProtocol : "binary");
            protocolName = protocolName.trim().toLowerCase();

            TProtocolFactory protocolFactory;
            switch (protocolName) {
                case "compact":
                    protocolFactory = new TCompactProtocol.Factory();
                    break;
                case "json":
                    protocolFactory = new TJSONProtocol.Factory();
                    break;
                case "simplejson":
                    protocolFactory = new TSimpleJSONProtocol.Factory();
                    break;
                case "binary":
                default:
                    protocolFactory = new TBinaryProtocol.Factory();
            }

            // Set env variable for child processes (optional, for demo)
            System.setProperty("THRIFT_PROTOCOL", protocolName);

            TThreadPoolServer.Args serverArgs = new TThreadPoolServer.Args(serverSocket)
                .processor(processor)
                .protocolFactory(protocolFactory);

            TServer server = new TThreadPoolServer(serverArgs);

            System.out.println("Starting Apache Thrift User Service on port " + PORT + " using protocol: " + protocolName);
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