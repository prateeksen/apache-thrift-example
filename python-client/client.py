#!/usr/bin/env python3

import sys
import os
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

from thrift.transport import TSocket
from thrift.transport import TTransport
from thrift.protocol import TBinaryProtocol
from thrift.server import TServer
from thrift.Thrift import TApplicationException

from UserService import UserService
from UserService.ttypes import *

def main():
    # Server connection details
    host = 'localhost'
    port = 9091
    
    try:
        # Create connection
        transport = TSocket.TSocket(host, port)
        transport = TTransport.TBufferedTransport(transport)
        protocol = TBinaryProtocol.TBinaryProtocol(transport)
        client = UserService.Client(protocol)
        
        # Open connection
        transport.open()
        print(f"Connected to Thrift server at {host}:{port}")
        
        # Perform operations
        perform_user_operations(client)
        
    except Exception as e:
        print(f"Error: {e}")
    finally:
        if 'transport' in locals():
            transport.close()
            print("Connection closed.")

def perform_user_operations(client):
    print("\n=== Python Client Demo ===")
    
    try:
        # Create users
        print("\n1. Creating users...")
        david = client.createUser("David Wilson", 31)
        eva = client.createUser("Eva Brown", 27)
        frank = client.createUser("Frank Miller", 45)
        print(f"  Created: {david}")
        print(f"  Created: {eva}")
        print(f"  Created: {frank}")
        
        # List all users
        print("\n2. Listing all users...")
        all_users = client.getAllUsers()
        for user in all_users:
            print(f"  User: ID={user.id}, Name={user.name}, Age={user.age}")
        
        # Get specific user
        print("\n3. Getting specific user...")
        retrieved_user = client.getUser(david.id)
        print(f"  Retrieved: ID={retrieved_user.id}, Name={retrieved_user.name}, Age={retrieved_user.age}")
        
        # Update user
        print("\n4. Updating user...")
        david.age = 32
        updated_user = client.updateUser(david)
        print(f"  Updated: ID={updated_user.id}, Name={updated_user.name}, Age={updated_user.age}")
        
        # Test exception handling
        print("\n5. Testing exception handling...")
        try:
            client.getUser(999)
        except UserNotFoundException as e:
            print(f"  Expected exception: {e.message}")
        
        # Delete user
        print("\n6. Deleting user...")
        client.deleteUser(eva.id)
        print(f"  Deleted user with ID: {eva.id}")
        
        # Final list
        print("\n7. Final user list...")
        all_users = client.getAllUsers()
        for user in all_users:
            print(f"  User: ID={user.id}, Name={user.name}, Age={user.age}")
        
        # Test TApplicationException (Protocol Exception)
        print("\n8. Testing TApplicationException (Protocol Exception)...")
        
        # Test 1: Valid call (should work)
        try:
            result = client.validateUserData("John Doe", 30, True)
            print(f"  ✓ Valid data accepted: {result}")
        except TApplicationException as e:
            print(f"  ✗ Unexpected TApplicationException: {e.type} - {e.message}")
        except Exception as e:
            print(f"  ✗ Unexpected Exception: {e}")
        
        # Test 2: Invalid data (should trigger TApplicationException)
        try:
            print("  Attempting invalid call with None name...")
            result = client.validateUserData(None, 25, True)
            print(f"  ✗ Should not reach here! Result: {result}")
        except TApplicationException as e:
            print(f"  ✓ Caught TApplicationException (EXCEPTION frame): {e.type} - {e.message}")
        except Exception as e:
            print(f"  ✗ Unexpected Exception instead of TApplicationException: {e}")
        
        # Test 3: Invalid age (should trigger TApplicationException)
        try:
            print("  Attempting invalid call with age = 200...")
            result = client.validateUserData("Jane Doe", 200, False)
            print(f"  ✗ Should not reach here! Result: {result}")
        except TApplicationException as e:
            print(f"  ✓ Caught TApplicationException (EXCEPTION frame): {e.type} - {e.message}")
        except Exception as e:
            print(f"  ✗ Unexpected Exception instead of TApplicationException: {e}")
        
        # Test Oneway calls (Fire-and-forget)
        print("\n9. Testing Oneway calls (Fire-and-forget)...")
        
        try:
            import time
            current_time = int(time.time() * 1000)
            timestamp = time.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3] + 'Z'
            
            print(f"  Sending oneway call: logUserActivity('login', 1, '{timestamp}')")
            start_time = time.time()
            
            # This call returns immediately - no response expected (ONEWAY frame)
            client.logUserActivity("login", 1, timestamp)
            
            end_time = time.time()
            duration = (end_time - start_time) * 1000000  # microseconds
            
            print(f"  ✓ Oneway call completed in {duration:.0f} microseconds")
            print("  ✓ No response expected (fire-and-forget semantics)")
            
            # Send multiple oneway calls rapidly
            for i in range(3):
                action = f"action_{i}"
                user_id = i + 10
                ts = time.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3] + 'Z'
                client.logUserActivity(action, user_id, ts)
                print(f"  ✓ Sent oneway call {i + 1}: {action}")
            
            print("  ✓ All oneway calls sent rapidly (no waiting for responses)")
            
        except Exception as e:
            print(f"  ✗ Unexpected error in oneway call: {e}")
            
        print("\n=== Python Client Demo Complete ===")
        
    except Exception as e:
        print(f"Operation error: {e}")

if __name__ == '__main__':
    main()