#!/usr/bin/env ruby

require 'thrift'
require_relative 'user_service_types'
require_relative 'user_service_constants' 
require_relative 'user_service'

class RubyClient
  def initialize(host = 'localhost', port = 9091)
    @host = host
    @port = port
  end

  def run
    begin

      # Protocol selection: ENV > ARGV > default
      protocol_name = (ENV['THRIFT_PROTOCOL'] && !ENV['THRIFT_PROTOCOL'].empty?) ? ENV['THRIFT_PROTOCOL'] : (ARGV[0] ? ARGV[0] : 'binary')
      protocol_name = protocol_name.strip.downcase

      transport = Thrift::BufferedTransport.new(Thrift::Socket.new(@host, @port))
      protocol = case protocol_name
                 when 'compact'
                   Thrift::CompactProtocol.new(transport)
                 when 'json'
                   Thrift::JsonProtocol.new(transport)
                 else
                   Thrift::BinaryProtocol.new(transport)
                 end

      # Set ENV for child processes (optional, for demo)
      ENV['THRIFT_PROTOCOL'] = protocol_name

      client = UserService::UserService::Client.new(protocol)

      # Open connection
      transport.open
      puts "Connected to Thrift server at #{@host}:#{@port} using protocol: #{protocol_name}"

      # Perform operations
      perform_user_operations(client)

    rescue => e
      puts "Error: #{e.message}"
    ensure
      transport.close if transport
      puts "Connection closed."
    end
  end

  private

  def perform_user_operations(client)
    puts "\n=== Ruby Client Demo ==="

    begin
      # Create users
      puts "\n1. Creating users..."
      grace = client.createUser("Grace Lee", 29)
      henry = client.createUser("Henry Taylor", 38)
      iris = client.createUser("Iris Clark", 33)
      puts "  Created: #{format_user(grace)}"
      puts "  Created: #{format_user(henry)}"
      puts "  Created: #{format_user(iris)}"

      # List all users
      puts "\n2. Listing all users..."
      all_users = client.getAllUsers
      all_users.each do |user|
        puts "  User: #{format_user(user)}"
      end

      # Get specific user
      puts "\n3. Getting specific user..."
      retrieved_user = client.getUser(grace.id)
      puts "  Retrieved: #{format_user(retrieved_user)}"

      # Update user
      puts "\n4. Updating user..."
      grace.age = 30
      updated_user = client.updateUser(grace)
      puts "  Updated: #{format_user(updated_user)}"

      # Test exception handling
      puts "\n5. Testing exception handling..."
      begin
        client.getUser(999)
      rescue UserService::UserNotFoundException => e
        puts "  Expected exception: #{e.message}"
      end

      # Delete user
      puts "\n6. Deleting user..."
      client.deleteUser(henry.id)
      puts "  Deleted user with ID: #{henry.id}"

      # Final list
      puts "\n7. Final user list..."
      all_users = client.getAllUsers
      all_users.each do |user|
        puts "  User: #{format_user(user)}"
      end
      
      # Test TApplicationException (Protocol Exception)
      puts "\n8. Testing TApplicationException (Protocol Exception)..."
      
      # Test 1: Valid call (should work)
      begin
        result = client.validateUserData("John Doe", 30, true)
        puts "  ✓ Valid data accepted: #{result}"
      rescue Thrift::ApplicationException => e
        puts "  ✗ Unexpected TApplicationException: #{e.type} - #{e.message}"
      rescue => e
        puts "  ✗ Unexpected Exception: #{e}"
      end
      
      # Test 2: Invalid data (should trigger TApplicationException)
      begin
        puts "  Attempting invalid call with nil name..."
        result = client.validateUserData(nil, 25, true)
        puts "  ✗ Should not reach here! Result: #{result}"
      rescue Thrift::ApplicationException => e
        puts "  ✓ Caught TApplicationException (EXCEPTION frame): #{e.type} - #{e.message}"
      rescue => e
        puts "  ✗ Unexpected Exception instead of TApplicationException: #{e}"
      end
      
      # Test 3: Invalid age (should trigger TApplicationException)
      begin
        puts "  Attempting invalid call with age = 200..."
        result = client.validateUserData("Jane Doe", 200, false)
        puts "  ✗ Should not reach here! Result: #{result}"
      rescue Thrift::ApplicationException => e
        puts "  ✓ Caught TApplicationException (EXCEPTION frame): #{e.type} - #{e.message}"
      rescue => e
        puts "  ✗ Unexpected Exception instead of TApplicationException: #{e}"
      end
      
      # Test Oneway calls (Fire-and-forget)
      puts "\n9. Testing Oneway calls (Fire-and-forget)..."
      
      begin
        current_time = Time.now.to_f * 1000
        timestamp = Time.now.strftime('%Y-%m-%dT%H:%M:%S.%L') + 'Z'
        
        puts "  Sending oneway call: logUserActivity('login', 1, '#{timestamp}')"
        start_time = Process.clock_gettime(Process::CLOCK_MONOTONIC, :nanosecond)
        
        # This call returns immediately - no response expected (ONEWAY frame)
        client.logUserActivity("login", 1, timestamp)
        
        end_time = Process.clock_gettime(Process::CLOCK_MONOTONIC, :nanosecond)
        duration = (end_time - start_time) / 1000  # microseconds
        
        puts "  ✓ Oneway call completed in #{duration.to_i} microseconds"
        puts "  ✓ No response expected (fire-and-forget semantics)"
        
        # Send multiple oneway calls rapidly
        (0..2).each do |i|
          action = "action_#{i}"
          user_id = i + 10
          ts = Time.now.strftime('%Y-%m-%dT%H:%M:%S.%L') + 'Z'
          client.logUserActivity(action, user_id, ts)
          puts "  ✓ Sent oneway call #{i + 1}: #{action}"
        end
        
        puts "  ✓ All oneway calls sent rapidly (no waiting for responses)"
        
      rescue => e
        puts "  ✗ Unexpected error in oneway call: #{e}"
      end

      puts "\n=== Ruby Client Demo Complete ==="

    rescue => e
      puts "Operation error: #{e.message}"
    end
  end

  def format_user(user)
    "ID=#{user.id}, Name=#{user.name}, Age=#{user.age}"
  end
end

# Run the client
if __FILE__ == $0
  client = RubyClient.new
  client.run
end