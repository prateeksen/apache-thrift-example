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
      # Create connection
      transport = Thrift::BufferedTransport.new(Thrift::Socket.new(@host, @port))
      protocol = Thrift::BinaryProtocol.new(transport)
      client = UserService::UserService::Client.new(protocol)

      # Open connection
      transport.open
      puts "Connected to Thrift server at #{@host}:#{@port}"

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