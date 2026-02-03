package com.example;

import UserService.*;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JavaClient protocol selection and client operations
 */
@DisplayName("Java Client Tests")
class JavaClientTest {

    @Test
    @DisplayName("Should create binary protocol by default")
    void testDefaultProtocolSelection() {
        // Given
        TTransport mockTransport = mock(TTransport.class);
        String protocolType = "binary";

        // When
        TProtocol protocol = createProtocol(mockTransport, protocolType);

        // Then
        assertNotNull(protocol);
        assertTrue(protocol instanceof TBinaryProtocol);
    }

    @Test
    @DisplayName("Should create compact protocol")
    void testCompactProtocolSelection() {
        // Given
        TTransport mockTransport = mock(TTransport.class);
        String protocolType = "compact";

        // When
        TProtocol protocol = createProtocol(mockTransport, protocolType);

        // Then
        assertNotNull(protocol);
        assertTrue(protocol instanceof TCompactProtocol);
    }

    @Test
    @DisplayName("Should create JSON protocol")
    void testJsonProtocolSelection() {
        // Given
        TTransport mockTransport = mock(TTransport.class);
        String protocolType = "json";

        // When
        TProtocol protocol = createProtocol(mockTransport, protocolType);

        // Then
        assertNotNull(protocol);
        assertTrue(protocol instanceof TJSONProtocol);
    }

    @Test
    @DisplayName("Should create SimpleJSON protocol")
    void testSimpleJsonProtocolSelection() {
        // Given
        TTransport mockTransport = mock(TTransport.class);
        String protocolType = "simplejson";

        // When
        TProtocol protocol = createProtocol(mockTransport, protocolType);

        // Then
        assertNotNull(protocol);
        assertTrue(protocol instanceof TSimpleJSONProtocol);
    }

    @Test
    @DisplayName("Should default to binary for unknown protocol")
    void testUnknownProtocolDefaultsToBinary() {
        // Given
        TTransport mockTransport = mock(TTransport.class);
        String protocolType = "unknown";

        // When
        TProtocol protocol = createProtocol(mockTransport, protocolType);

        // Then
        assertNotNull(protocol);
        assertTrue(protocol instanceof TBinaryProtocol);
    }

    @Test
    @DisplayName("Should handle null protocol type")
    void testNullProtocolType() {
        // Given
        TTransport mockTransport = mock(TTransport.class);
        String protocolType = null;

        // When
        TProtocol protocol = createProtocol(mockTransport, protocolType);

        // Then
        assertNotNull(protocol);
        assertTrue(protocol instanceof TBinaryProtocol);
    }

    @Test
    @DisplayName("Should handle case-insensitive protocol names")
    void testCaseInsensitiveProtocolNames() {
        // Given
        TTransport mockTransport = mock(TTransport.class);

        // When & Then
        assertTrue(createProtocol(mockTransport, "COMPACT") instanceof TCompactProtocol);
        assertTrue(createProtocol(mockTransport, "Json") instanceof TJSONProtocol);
        assertTrue(createProtocol(mockTransport, "BINARY") instanceof TBinaryProtocol);
        assertTrue(createProtocol(mockTransport, "SimpleJson") instanceof TSimpleJSONProtocol);
    }

    @Test
    @DisplayName("Should parse environment variable correctly")
    void testEnvironmentVariableParsing() {
        // Given
        String envProtocol = "compact";

        // When
        String protocol = getProtocolFromEnvOrArg(envProtocol, null);

        // Then
        assertEquals("compact", protocol);
    }

    @Test
    @DisplayName("Should prioritize environment variable over argument")
    void testEnvironmentVariablePriority() {
        // Given
        String envProtocol = "compact";
        String argProtocol = "json";

        // When
        String protocol = getProtocolFromEnvOrArg(envProtocol, argProtocol);

        // Then
        assertEquals("compact", protocol);
    }

    @Test
    @DisplayName("Should use argument when environment variable is not set")
    void testArgumentFallback() {
        // Given
        String envProtocol = null;
        String argProtocol = "json";

        // When
        String protocol = getProtocolFromEnvOrArg(envProtocol, argProtocol);

        // Then
        assertEquals("json", protocol);
    }

    @Test
    @DisplayName("Should default to binary when both are null")
    void testDefaultWhenBothNull() {
        // Given
        String envProtocol = null;
        String argProtocol = null;

        // When
        String protocol = getProtocolFromEnvOrArg(envProtocol, argProtocol);

        // Then
        assertEquals("binary", protocol);
    }

    @Test
    @DisplayName("Should create client with binary protocol")
    void testClientCreationWithBinaryProtocol() {
        // Given
        TTransport mockTransport = mock(TTransport.class);
        TProtocol protocol = new TBinaryProtocol(mockTransport);

        // When
        UserService.Client client = new UserService.Client(protocol);

        // Then
        assertNotNull(client);
    }

    @Test
    @DisplayName("Should create client with compact protocol")
    void testClientCreationWithCompactProtocol() {
        // Given
        TTransport mockTransport = mock(TTransport.class);
        TProtocol protocol = new TCompactProtocol(mockTransport);

        // When
        UserService.Client client = new UserService.Client(protocol);

        // Then
        assertNotNull(client);
    }

    @Test
    @DisplayName("Should create client with JSON protocol")
    void testClientCreationWithJsonProtocol() {
        // Given
        TTransport mockTransport = mock(TTransport.class);
        TProtocol protocol = new TJSONProtocol(mockTransport);

        // When
        UserService.Client client = new UserService.Client(protocol);

        // Then
        assertNotNull(client);
    }

    /**
     * Helper method that mimics the protocol selection logic from JavaClient.java
     */
    private TProtocol createProtocol(TTransport transport, String protocolType) {
        if (protocolType == null) {
            protocolType = "binary";
        }
        
        protocolType = protocolType.toLowerCase();
        
        switch (protocolType) {
            case "compact":
                return new TCompactProtocol(transport);
            case "json":
                return new TJSONProtocol(transport);
            case "simplejson":
                return new TSimpleJSONProtocol(transport);
            case "binary":
            default:
                return new TBinaryProtocol(transport);
        }
    }

    /**
     * Helper method that mimics the env/arg priority logic
     */
    private String getProtocolFromEnvOrArg(String envProtocol, String argProtocol) {
        String protocol = envProtocol;
        if (protocol == null && argProtocol != null) {
            protocol = argProtocol;
        }
        if (protocol == null) {
            protocol = "binary";
        }
        return protocol;
    }
}
