#!/bin/bash

# Quick test script for a single protocol
# Usage: ./test-single-protocol.sh [protocol]
# Example: ./test-single-protocol.sh compact

PROTOCOL=${1:-binary}
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo -e "${BLUE}Testing protocol: ${PROTOCOL}${NC}"
echo ""

# Set environment variable
export THRIFT_PROTOCOL=$PROTOCOL

# Start server in background
echo -e "${YELLOW}Starting server...${NC}"
cd java-server
java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" com.example.Server &
SERVER_PID=$!
cd ..

# Wait for server
sleep 3

# Test Java client
echo -e "${YELLOW}Testing Java client...${NC}"
cd java-client
java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" com.example.JavaClient
cd ..

echo ""
echo -e "${GREEN}Test completed!${NC}"
echo -e "${YELLOW}Press Enter to stop server and exit...${NC}"
read

# Stop server
kill $SERVER_PID 2>/dev/null || true
wait $SERVER_PID 2>/dev/null || true

echo -e "${GREEN}Server stopped${NC}"
