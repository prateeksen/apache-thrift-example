# Quick Start Guide: Multi-Protocol Support

## Usage

### Start Server with Specific Protocol

```bash
cd apache-thrift-example/java-server

# Option 1: Using environment variable (recommended)
export THRIFT_PROTOCOL=compact
mvn exec:java -Dexec.mainClass="com.example.Server"

# Option 2: Using command-line argument
mvn exec:java -Dexec.mainClass="com.example.Server" -Dexec.args="json"

# Option 3: Direct java command
export THRIFT_PROTOCOL=binary
java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" com.example.Server
```

### Run Clients with Specific Protocol

#### Java Client
```bash
cd apache-thrift-example/java-client

# Set protocol
export THRIFT_PROTOCOL=compact
mvn exec:java -Dexec.mainClass="com.example.JavaClient"

# Or with command-line argument
mvn exec:java -Dexec.mainClass="com.example.JavaClient" -Dexec.args="json"
```

#### Python Client
```bash
cd apache-thrift-example/python-client

# Set protocol
export THRIFT_PROTOCOL=compact
python3 client.py

# Or with command-line argument
python3 client.py json
```

#### Ruby Client
```bash
cd apache-thrift-example/ruby-client

# Set protocol
export THRIFT_PROTOCOL=compact
ruby -I. client.rb

# Or with command-line argument
ruby -I. client.rb json

# Note: -I. flag is required to load generated Thrift files
```

## Available Protocols

| Protocol | Java | Python | Ruby | Use Case | Status |
|----------|------|--------|------|----------|--------|
| **binary** | ✅ | ✅ | ✅ | Best performance (default) | Fully Tested ✓ |
| **compact** | ✅ | ✅ | ✅ | Smaller message size | Fully Tested ✓ |
| **json** | ✅ | ✅ | ✅ | Human-readable, debugging | Fully Tested ✓ |
| **simplejson** | ⚠️ | ❌ | ❌ | Write-only (not for RPC) | Limited Support |

**Important Notes:**
- TSimpleJSONProtocol is write-only and not suitable for bidirectional RPC
- Ruby uses `JsonProtocol` (capital J, lowercase son) instead of `JSONProtocol`
- Ruby client requires `-I.` flag: `ruby -I. client.rb`

## Quick Tests

### Test Binary Protocol
```bash
# Terminal 1: Start server
export THRIFT_PROTOCOL=binary
cd java-server && mvn exec:java -Dexec.mainClass="com.example.Server"

# Terminal 2: Run client
export THRIFT_PROTOCOL=binary
cd java-client && mvn exec:java -Dexec.mainClass="com.example.JavaClient"
```

### Test Compact Protocol
```bash
# Terminal 1: Start server
export THRIFT_PROTOCOL=compact
cd java-server && mvn exec:java -Dexec.mainClass="com.example.Server"

# Terminal 2: Run Python client
export THRIFT_PROTOCOL=compact
cd python-client && python3 client.py
```

### Test JSON Protocol
```bash
# Terminal 1: Start server
export THRIFT_PROTOCOL=json
cd java-server && mvn exec:java -Dexec.mainClass="com.example.Server"

# Terminal 2: Run Ruby client
export THRIFT_PROTOCOL=json
cd ruby-client && ruby client.rb
```

## Automated Testing

### Run All Protocol Tests
```bash
cd apache-thrift-example
./test-protocols.sh
```

### Test Single Protocol
```bash
cd apache-thrift-example
./test-single-protocol.sh compact
```

### Test Protocol Mismatch Detection
```bash
cd apache-thrift-example
./test-protocol-mismatch.sh
```

## Important Notes

1. **Protocol Must Match**: Server and client must use the same protocol
2. **Environment Variable Priority**: `THRIFT_PROTOCOL` env var takes precedence over command-line argument
3. **Default Protocol**: If no protocol specified, defaults to `binary`
4. **Case Insensitive**: Protocol names are case-insensitive (e.g., `COMPACT`, `compact`, `Compact` all work)

## Troubleshooting

### Client Can't Connect
- Ensure server is running: `netstat -an | grep 9091`
- Check server logs for protocol: Should show "using protocol: [name]"
- Verify client and server use same protocol

### Build Errors
```bash
# Rebuild Java projects
cd java-server && mvn clean package
cd ../java-client && mvn clean package
```

### Protocol Mismatch
If client and server use different protocols, you'll see connection or deserialization errors. Ensure both use the same protocol setting.

## Examples

### Example 1: Production Setup (Binary Protocol)
```bash
# Server
export THRIFT_PROTOCOL=binary
cd java-server && mvn exec:java -Dexec.mainClass="com.example.Server"

# Clients
export THRIFT_PROTOCOL=binary
cd java-client && mvn exec:java -Dexec.mainClass="com.example.JavaClient"
cd python-client && python3 client.py
cd ruby-client && ruby client.rb
```

### Example 2: Bandwidth-Constrained Environment (Compact Protocol)
```bash
# Use compact protocol for smaller messages
export THRIFT_PROTOCOL=compact

# Start server and clients as above
```

### Example 3: Debugging (JSON Protocol)
```bash
# Use JSON for human-readable messages
export THRIFT_PROTOCOL=json

# Start server and clients
# You can inspect network traffic with tools like tcpdump or Wireshark
```
