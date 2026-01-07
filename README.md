# Apache Thrift Multi-Language Example

This project demonstrates Apache Thrift's cross-language capabilities with a simple service implementation featuring:
- A Java server
- A Java client
- A Python client
- A Ruby client

## Overview

Apache Thrift is a software framework for scalable cross-language services development. It combines a software stack with a code generation engine to build services that work efficiently and seamlessly between different programming languages.

## Project Structure

```
apache-thrift-example/
├── README.md
├── thrift/
│   └── user_service.thrift          # Thrift IDL definition
├── java-server/
│   ├── pom.xml                      # Maven configuration
│   ├── src/main/java/
│   │   └── UserService/             # Generated classes location
│   │       ├── UserService.java    # Generated service interface
│   │       ├── User.java           # Generated User class
│   │       └── UserNotFoundException.java
│   │   └── com/example/
│   │       ├── UserServiceImpl.java # Service implementation
│   │       └── Server.java          # Server main class
│   └── target/
├── java-client/
│   ├── pom.xml                      # Maven configuration
│   ├── src/main/java/
│   │   └── UserService/             # Generated classes (shared)
│   │   └── com/example/
│   │       └── JavaClient.java     # Java client implementation
│   └── target/
├── python-client/
│   ├── requirements.txt             # Python dependencies
│   ├── client.py                    # Python client implementation
│   └── gen-py/
│       └── UserService/             # Generated Python modules
│           ├── __init__.py
│           ├── UserService.py
│           ├── ttypes.py            # User struct and exceptions
│           └── constants.py
└── ruby-client/
    ├── Gemfile                      # Ruby dependencies
    ├── client.rb                    # Ruby client implementation
    └── gen-rb/
        └── user_service_types.rb    # Generated Ruby classes
        └── user_service.rb          # Generated service interface
```

## Service Definition

The example will implement a simple User Service with the following operations:

### UserService.thrift
```thrift
namespace java UserService
namespace py UserService
namespace rb UserService

struct User {
    1: i64 id,
    2: string name,
    3: i32 age
}

exception UserNotFoundException {
    1: string message
}

service UserService {
    // Create a new user
    User createUser(1: string name, 2: i32 age),
    
    // Get user by ID
    User getUser(1: i64 userId) throws (1: UserNotFoundException ex),
    
    // Update user information
    User updateUser(1: User user) throws (1: UserNotFoundException ex),
    
    // Delete user
    void deleteUser(1: i64 userId) throws (1: UserNotFoundException ex),
    
    // List all users
    list<User> getAllUsers()
}
```

## Implementation Plan

### 1. Java Server (`java-server/`)

**Technology Stack:**
- Java 11 (OpenJDK 11.0.29)
- Apache Thrift Java library
- Maven 3.6.3 (installed)
- In-memory storage (HashMap) for simplicity

**Key Components:**
- `UserServiceImpl.java`: Implements the UserService interface with CRUD operations
- `Server.java`: Sets up and starts the Thrift server on port 9091
- Thread pool server for handling concurrent client requests

**Features:**
- Multi-threaded server capable of handling multiple client connections
- Basic error handling and logging
- Simple in-memory user storage
- Graceful server shutdown

### 2. Java Client (`java-client/`)

**Technology Stack:**
- Java 11 (OpenJDK 11.0.29)
- Apache Thrift Java library
- Maven 3.6.3 (installed)

**Key Components:**
- `JavaClient.java`: Demonstrates all CRUD operations
- Connection management and error handling
- Sample data for testing

**Features:**
- Connect to Thrift server
- Demonstrate all service methods (create, read, update, delete)
- Proper connection cleanup
- Exception handling for service calls

### 3. Python Client (`python-client/`)

**Technology Stack:**
- Python 3.10.12
- Apache Thrift Python library (already installed v0.22.0)
- Generated Python bindings

**Key Components:**
- `client.py`: Python implementation demonstrating CRUD operations
- Same functionality as Java client but in Python

**Features:**
- Cross-language compatibility demonstration
- Identical operations to Java client
- Python-idiomatic error handling
- JSON-like data handling

### 4. Ruby Client (`ruby-client/`)

**Technology Stack:**
- Ruby 3.0.2
- Apache Thrift Ruby gem v0.22.0 (installed)
- Bundler 2.5.23 (installed)

**Key Components:**
- `client.rb`: Ruby implementation demonstrating CRUD operations
- `Gemfile`: Ruby dependency specification
- Same functionality as Java and Python clients

**Features:**
- Cross-language compatibility demonstration
- Ruby-idiomatic code patterns
- Elegant error handling with Ruby exceptions
- Demonstrates Thrift's multi-language capabilities

## Development Workflow

### Step 1: Define Thrift IDL
- Create `user_service.thrift` with service definition
- Define data structures and service methods

### Step 2: Generate Language Bindings
```bash
# Generate Java classes (creates gen-java/ directory)
thrift --gen java thrift/user_service.thrift

# Generate Python classes (creates gen-py/ directory)
thrift --gen py thrift/user_service.thrift

# Generate Ruby classes (creates gen-rb/ directory)
thrift --gen rb thrift/user_service.thrift
```

### Step 3: Implement Java Server
- Set up Maven project with Thrift dependencies
- Implement service logic
- Create server startup class
- Add logging and error handling

### Step 4: Implement Java Client
- Create Maven project for client
- Implement client operations
- Add connection management
- Test all service methods

### Step 5: Implement Python Client
- Set up Python environment with Thrift library
- Implement equivalent client functionality
- Ensure cross-language compatibility

### Step 6: Implement Ruby Client
- Set up Ruby environment with Thrift gem
- Create Gemfile with dependencies
- Implement Ruby client with idiomatic patterns
- Test cross-language compatibility

### Step 7: Testing
- Start Java server
- Test with Java client
- Test with Python client
- Test with Ruby client
- Verify data consistency across clients

## Dependencies

### Java Projects
```xml
<dependency>
    <groupId>org.apache.thrift</groupId>
    <artifactId>libthrift</artifactId>
    <version>0.22.0</version>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-simple</artifactId>
    <version>1.7.36</version>
</dependency>
```

### Python Project
```
# Already installed: thrift==0.22.0
# No additional installation needed
```

### Ruby Project
```ruby
# Gemfile
source 'https://rubygems.org'

ruby '3.0.2'

gem 'thrift', '~> 0.22.0'
```

## Running the Example

### ✅ Prerequisites (All Installed)
```bash
# ✅ Maven installed: Apache Maven 3.6.3
# ✅ Ruby Thrift gem installed: thrift (0.22.0)
# ✅ Bundler installed: Bundler version 2.5.23
# ✅ Python Thrift installed: thrift 0.22.0
```

### Steps

1. **Generate Thrift bindings**
   ```bash
   thrift --gen java:private-members thrift/user_service.thrift
   thrift --gen py thrift/user_service.thrift
   thrift --gen rb thrift/user_service.thrift
   ```

2. **Build and start the Java server**
   ```bash
   cd java-server
   mvn compile
   mvn exec:java
   ```

3. **Run Java client to test operations**
   ```bash
   cd java-client
   mvn compile
   mvn exec:java
   ```

4. **Run Python client to demonstrate cross-language capabilities**
   ```bash
   cd python-client
   # thrift library already installed (v0.22.0)
   python3 client.py
   ```

5. **Run Ruby client to further validate multi-language support**
   ```bash
   cd ruby-client
   bundle install
   ruby client.rb
   ```

## Manual Testing Guide

### Testing the Complete Demo

1. **Start the Server (Terminal 1)**
   ```bash
   cd java-server
   mvn compile
   mvn exec:java
   ```
   Expected output:
   ```
   Starting Apache Thrift User Service on port 9091
   Server ready to accept connections...
   ```

2. **Test Java Client (Terminal 2)**
   ```bash
   cd java-client
   mvn compile
   mvn exec:java
   ```
   Expected: Creates users Alice, Bob, Carol and performs all CRUD operations

3. **Test Python Client (Terminal 3)**
   ```bash
   cd python-client
   python3 client.py
   ```
   Expected: Creates users David, Eva, Frank and shows existing users from Java client

4. **Test Ruby Client (Terminal 4)**
   ```bash
   cd ruby-client
   ruby client.rb
   ```
   Expected: Creates users Grace, Henry, Iris and shows all users from previous clients

### Verifying Cross-Language Communication

**Key Things to Observe:**
- Each client connects to the same server instance
- Each client can see users created by other language clients
- User IDs increment across all clients (demonstrating shared state)
- All CRUD operations work consistently across languages
- Exception handling works properly in all languages

**Sample Server Log Output:**
```
Created user: User(id:1, name:Alice Johnson, age:28)
Created user: User(id:2, name:Bob Smith, age:35)
Retrieved 2 users
Created user: User(id:3, name:David Wilson, age:31)
Updated user: User(id:1, name:Alice Johnson, age:29)
Deleted user: User(id:2, name:Bob Smith, age:35)
```

### Troubleshooting

**Port Already in Use:**
If you see "Address already in use" error:
```bash
# Check what's using port 9091
netstat -tuln | grep 9091
# Kill the process if needed
sudo kill -9 $(lsof -ti:9091)
```

**Ruby Import Issues:**
If Ruby client fails with import errors, ensure generated files are in ruby-client directory:
```bash
cd ruby-client
ls -la *.rb  # Should show user_service.rb, user_service_types.rb, etc.
```

### Expected Demo Results

After running all clients, the server should have approximately:
- **12+ users** created across all language clients
- **Multiple operations logged** showing cross-language interaction
- **No connection errors** between any client and server
- **Consistent data** visible to all clients

## Expected Outcomes

This example will demonstrate:
- Cross-language service communication (Java, Python, Ruby)
- Thrift IDL to multiple language bindings
- Client-server architecture with Thrift
- Data serialization/deserialization across languages
- Error handling in distributed systems
- Performance characteristics of Thrift protocol
- Language-specific idiomatic patterns for the same service interface

## Next Steps

After implementing this basic example, potential extensions include:
- Database integration instead of in-memory storage
- Authentication and authorization
- Asynchronous client implementations
- Additional language clients (Go, Node.js, etc.)
- Performance benchmarking
- Docker containerization
- Service discovery integration