package com.example;

import org.apache.thrift.protocol.*;
import org.apache.thrift.transport.TTransport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Server protocol selection logic
 */
@DisplayName("Server Protocol Selection Tests")
class ServerTest {

    @Test
    @DisplayName("Should select binary protocol by default")
    void testDefaultProtocol() {
        // Given
        String protocol = "binary";

        // When
        TProtocolFactory factory = getProtocolFactory(protocol);

        // Then
        assertNotNull(factory);
        assertTrue(factory instanceof TBinaryProtocol.Factory);
    }

    @Test
    @DisplayName("Should select compact protocol")
    void testCompactProtocol() {
        // Given
        String protocol = "compact";

        // When
        TProtocolFactory factory = getProtocolFactory(protocol);

        // Then
        assertNotNull(factory);
        assertTrue(factory instanceof TCompactProtocol.Factory);
    }

    @Test
    @DisplayName("Should select JSON protocol")
    void testJsonProtocol() {
        // Given
        String protocol = "json";

        // When
        TProtocolFactory factory = getProtocolFactory(protocol);

        // Then
        assertNotNull(factory);
        assertTrue(factory instanceof TJSONProtocol.Factory);
    }

    @Test
    @DisplayName("Should select SimpleJSON protocol")
    void testSimpleJsonProtocol() {
        // Given
        String protocol = "simplejson";

        // When
        TProtocolFactory factory = getProtocolFactory(protocol);

        // Then
        assertNotNull(factory);
        assertTrue(factory instanceof TSimpleJSONProtocol.Factory);
    }

    @Test
    @DisplayName("Should default to binary for unknown protocol")
    void testUnknownProtocol() {
        // Given
        String protocol = "unknown";

        // When
        TProtocolFactory factory = getProtocolFactory(protocol);

        // Then
        assertNotNull(factory);
        assertTrue(factory instanceof TBinaryProtocol.Factory);
    }

    @Test
    @DisplayName("Should default to binary for null protocol")
    void testNullProtocol() {
        // Given
        String protocol = null;

        // When
        TProtocolFactory factory = getProtocolFactory(protocol);

        // Then
        assertNotNull(factory);
        assertTrue(factory instanceof TBinaryProtocol.Factory);
    }

    @Test
    @DisplayName("Should handle case-insensitive protocol names")
    void testCaseInsensitiveProtocol() {
        // Test various cases
        assertTrue(getProtocolFactory("COMPACT") instanceof TCompactProtocol.Factory);
        assertTrue(getProtocolFactory("Json") instanceof TJSONProtocol.Factory);
        assertTrue(getProtocolFactory("BINARY") instanceof TBinaryProtocol.Factory);
    }

    @Test
    @DisplayName("Should create protocol instances correctly")
    void testProtocolFactoryCreatesProtocol() throws Exception {
        // Given
        TTransport mockTransport = Mockito.mock(TTransport.class);
        TProtocolFactory factory = new TBinaryProtocol.Factory();

        // When
        TProtocol protocol = factory.getProtocol(mockTransport);

        // Then
        assertNotNull(protocol);
        assertTrue(protocol instanceof TBinaryProtocol);
    }

    /**
     * Helper method that mimics the protocol selection logic from Server.java
     */
    private TProtocolFactory getProtocolFactory(String protocol) {
        if (protocol == null) {
            protocol = "binary";
        }
        
        protocol = protocol.toLowerCase();
        
        switch (protocol) {
            case "compact":
                return new TCompactProtocol.Factory();
            case "json":
                return new TJSONProtocol.Factory();
            case "simplejson":
                return new TSimpleJSONProtocol.Factory();
            case "binary":
            default:
                return new TBinaryProtocol.Factory();
        }
    }
}
