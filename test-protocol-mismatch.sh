#!/bin/bash

# Test script to verify protocol mismatch detection
# This tests that clients using different protocols than the server will fail gracefully

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Protocol Mismatch Test${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Start server with binary protocol
echo -e "${YELLOW}Starting server with BINARY protocol...${NC}"
export THRIFT_PROTOCOL=binary
cd java-server
java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" com.example.Server > "../server-mismatch.log" 2>&1 &
SERVER_PID=$!
cd ..

sleep 3

# Try to connect with compact protocol (should fail)
echo -e "${YELLOW}Attempting to connect with COMPACT protocol (should fail)...${NC}"
export THRIFT_PROTOCOL=compact
cd java-client
java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" com.example.JavaClient > "../client-mismatch.log" 2>&1
if [ $? -ne 0 ]; then
    echo -e "${GREEN}✓ Protocol mismatch correctly detected and failed${NC}"
else
    echo -e "${RED}✗ Warning: Protocol mismatch not detected (unexpected success)${NC}"
fi
cd ..

# Stop server
kill $SERVER_PID 2>/dev/null || true
wait $SERVER_PID 2>/dev/null || true

echo ""
echo -e "${BLUE}Test completed. Check logs for details:${NC}"
echo "  - server-mismatch.log"
echo "  - client-mismatch.log"
