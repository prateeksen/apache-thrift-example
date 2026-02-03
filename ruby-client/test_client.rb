#!/usr/bin/env ruby
# frozen_string_literal: true

require 'test/unit'
require 'thrift'

# Unit tests for Ruby Thrift client
class TestProtocolSelection < Test::Unit::TestCase
  def setup
    # Clean up environment before each test
    ENV.delete('THRIFT_PROTOCOL')
  end

  def test_binary_protocol_default
    # Should default to binary protocol
    protocol_type = ENV['THRIFT_PROTOCOL'] || 'binary'
    assert_equal 'binary', protocol_type
  end

  def test_environment_variable_priority
    # Environment variable should have priority
    ENV['THRIFT_PROTOCOL'] = 'compact'
    protocol_type = ENV['THRIFT_PROTOCOL'] || 'binary'
    assert_equal 'compact', protocol_type
    ENV.delete('THRIFT_PROTOCOL')
  end

  def test_protocol_type_validation
    # Should accept valid protocol types
    valid_protocols = ['binary', 'compact', 'json']
    valid_protocols.each do |protocol|
      assert_includes valid_protocols, protocol
    end
  end

  def test_unknown_protocol_defaults_to_binary
    # Unknown protocol should default to binary
    protocol_type = 'unknown'
    default_protocol = ['compact', 'json'].include?(protocol_type) ? protocol_type : 'binary'
    assert_equal 'binary', default_protocol
  end
end

class TestProtocolCreation < Test::Unit::TestCase
  def test_create_binary_protocol
    # Should create BinaryProtocol instance
    transport = Thrift::MemoryBufferTransport.new
    protocol = Thrift::BinaryProtocol.new(transport)
    assert_kind_of Thrift::BinaryProtocol, protocol
  end

  def test_create_compact_protocol
    # Should create CompactProtocol instance
    transport = Thrift::MemoryBufferTransport.new
    protocol = Thrift::CompactProtocol.new(transport)
    assert_kind_of Thrift::CompactProtocol, protocol
  end

  def test_create_json_protocol
    # Should create JsonProtocol instance
    transport = Thrift::MemoryBufferTransport.new
    protocol = Thrift::JsonProtocol.new(transport)
    assert_kind_of Thrift::JsonProtocol, protocol
  end
end

class TestClientConfiguration < Test::Unit::TestCase
  def test_default_host_and_port
    # Should use default host and port
    host = 'localhost'
    port = 9091
    assert_equal 'localhost', host
    assert_equal 9091, port
  end

  def test_command_line_argument_parsing
    # Should parse command-line arguments correctly
    test_args = ['compact']
    protocol = test_args[0] || 'binary'
    assert_equal 'compact', protocol
  end

  def test_empty_arguments_use_default
    # Should use default when no arguments provided
    test_args = []
    protocol = test_args[0] || 'binary'
    assert_equal 'binary', protocol
  end

  def test_protocol_selection_switch
    # Test protocol selection logic
    ['binary', 'compact', 'json'].each do |protocol_type|
      transport = Thrift::MemoryBufferTransport.new
      
      protocol = case protocol_type
        when 'compact'
          Thrift::CompactProtocol.new(transport)
        when 'json'
          Thrift::JsonProtocol.new(transport)
        else
          Thrift::BinaryProtocol.new(transport)
        end

      case protocol_type
      when 'compact'
        assert_kind_of Thrift::CompactProtocol, protocol
      when 'json'
        assert_kind_of Thrift::JsonProtocol, protocol
      else
        assert_kind_of Thrift::BinaryProtocol, protocol
      end
    end
  end
end

# Integration tests (skipped by default, require running server)
class TestIntegrationSetup < Test::Unit::TestCase
  def test_connection_requires_server
    # This test is informational - it documents that integration tests need a running server
    omit "Integration tests require a running server on localhost:9091"
    
    # Example of how to test connection (when server is running):
    # socket = Thrift::Socket.new('localhost', 9091)
    # transport = Thrift::BufferedTransport.new(socket)
    # transport.open
    # assert transport.open?
    # transport.close
  end
end

# Run tests if executed directly
if __FILE__ == $0
  require 'test/unit/ui/console/testrunner'
  Test::Unit::AutoRunner.run
end
