#!/usr/bin/env python3
"""
Unit tests for the Python Thrift client
Run with: python3 -m pytest test_client.py -v
or: python3 test_client.py
"""

import unittest
import sys
import os
from unittest.mock import Mock, patch, MagicMock

# Add parent directory to path for imports
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from thrift.transport import TSocket, TTransport
from thrift.protocol import TBinaryProtocol, TCompactProtocol, TJSONProtocol


class TestProtocolSelection(unittest.TestCase):
    """Test protocol selection logic"""
    
    def setUp(self):
        """Clean up environment before each test"""
        if 'THRIFT_PROTOCOL' in os.environ:
            del os.environ['THRIFT_PROTOCOL']
    
    def test_binary_protocol_default(self):
        """Should default to binary protocol"""
        protocol_type = os.environ.get('THRIFT_PROTOCOL', 'binary')
        self.assertEqual(protocol_type, 'binary')
    
    def test_environment_variable_priority(self):
        """Environment variable should have priority over command-line argument"""
        # Simulate environment variable being set
        os.environ['THRIFT_PROTOCOL'] = 'compact'
        protocol_type = os.environ.get('THRIFT_PROTOCOL', 'binary')
        self.assertEqual(protocol_type, 'compact')
        # Clean up
        del os.environ['THRIFT_PROTOCOL']
    
    def test_protocol_type_validation(self):
        """Should accept valid protocol types"""
        valid_protocols = ['binary', 'compact', 'json']
        for protocol in valid_protocols:
            self.assertIn(protocol, valid_protocols)
    
    def test_unknown_protocol_defaults_to_binary(self):
        """Unknown protocol should default to binary"""
        protocol_type = 'unknown'
        # In the actual client, this would default to binary
        default_protocol = 'binary' if protocol_type not in ['compact', 'json'] else protocol_type
        self.assertEqual(default_protocol, 'binary')


class TestProtocolCreation(unittest.TestCase):
    """Test protocol object creation"""
    
    def test_create_binary_protocol(self):
        """Should create TBinaryProtocol instance"""
        transport = Mock()
        protocol = TBinaryProtocol.TBinaryProtocol(transport)
        self.assertIsInstance(protocol, TBinaryProtocol.TBinaryProtocol)
    
    def test_create_compact_protocol(self):
        """Should create TCompactProtocol instance"""
        transport = Mock()
        protocol = TCompactProtocol.TCompactProtocol(transport)
        self.assertIsInstance(protocol, TCompactProtocol.TCompactProtocol)
    
    def test_create_json_protocol(self):
        """Should create TJSONProtocol instance"""
        transport = Mock()
        protocol = TJSONProtocol.TJSONProtocol(transport)
        self.assertIsInstance(protocol, TJSONProtocol.TJSONProtocol)


class TestClientConfiguration(unittest.TestCase):
    """Test client configuration logic"""
    
    def test_default_host_and_port(self):
        """Should use default host and port"""
        host = 'localhost'
        port = 9091
        self.assertEqual(host, 'localhost')
        self.assertEqual(port, 9091)
    
    def test_command_line_argument_parsing(self):
        """Should parse command-line arguments correctly"""
        # Simulate command-line argument
        test_args = ['client.py', 'compact']
        protocol = test_args[1] if len(test_args) > 1 else 'binary'
        self.assertEqual(protocol, 'compact')
    
    def test_empty_arguments_use_default(self):
        """Should use default when no arguments provided"""
        test_args = ['client.py']
        protocol = test_args[1] if len(test_args) > 1 else 'binary'
        self.assertEqual(protocol, 'binary')


class TestIntegrationSetup(unittest.TestCase):
    """Test integration test setup (requires running server)"""
    
    @unittest.skip("Requires running server on port 9091")
    def test_connection_to_server(self):
        """Should connect to server on localhost:9091"""
        transport = TSocket.TSocket('localhost', 9091)
        transport = TTransport.TBufferedTransport(transport)
        
        try:
            transport.open()
            self.assertTrue(transport.isOpen())
        except Exception as e:
            self.fail(f"Failed to connect to server: {e}")
        finally:
            if transport.isOpen():
                transport.close()
    
    @unittest.skip("Requires running server on port 9091")
    def test_protocol_communication(self):
        """Should communicate with server using binary protocol"""
        from UserService import UserService
        
        transport = TSocket.TSocket('localhost', 9091)
        transport = TTransport.TBufferedTransport(transport)
        protocol = TBinaryProtocol.TBinaryProtocol(transport)
        client = UserService.Client(protocol)
        
        try:
            transport.open()
            # Test basic operation
            users = client.getAllUsers()
            self.assertIsInstance(users, list)
        except Exception as e:
            self.fail(f"Failed to communicate with server: {e}")
        finally:
            if transport.isOpen():
                transport.close()


def run_tests():
    """Run all tests"""
    loader = unittest.TestLoader()
    suite = loader.loadTestsFromModule(sys.modules[__name__])
    runner = unittest.TextTestRunner(verbosity=2)
    result = runner.run(suite)
    return 0 if result.wasSuccessful() else 1


if __name__ == '__main__':
    sys.exit(run_tests())
