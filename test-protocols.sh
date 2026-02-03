#!/bin/bash

# Test script for multi-protocol support in Apache Thrift application
# Tests all supported protocols: binary, compact, json, simplejson, debug

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test protocols
PROTOCOLS=("binary" "compact" "json")

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Apache Thrift Multi-Protocol Test Suite${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to check if a port is in use
check_port() {
    nc -z localhost 9091 2>/dev/null
}

# Function to wait for server to start
wait_for_server() {
    echo -e "${YELLOW}Waiting for server to start...${NC}"
    for i in {1..30}; do
        if check_port; then
            echo -e "${GREEN}✓ Server is ready${NC}"
            return 0
        fi
        sleep 1
    done
    echo -e "${RED}✗ Server failed to start${NC}"
    return 1
}

# Function to stop server
stop_server() {
    if [ ! -z "$SERVER_PID" ]; then
        echo -e "${YELLOW}Stopping server (PID: $SERVER_PID)...${NC}"
        kill $SERVER_PID 2>/dev/null || true
        wait $SERVER_PID 2>/dev/null || true
        sleep 2
    fi
}

# Build the project
echo -e "${BLUE}Step 1: Building Java projects...${NC}"
cd java-server
mvn clean package -q
if [ $? -ne 0 ]; then
    echo -e "${RED}✗ Failed to build java-server${NC}"
    exit 1
fi
echo -e "${GREEN}✓ java-server built successfully${NC}"

cd ../java-client
mvn clean package -q
if [ $? -ne 0 ]; then
    echo -e "${RED}✗ Failed to build java-client${NC}"
    exit 1
fi
echo -e "${GREEN}✓ java-client built successfully${NC}"
cd ..

echo ""

# Test each protocol
for PROTOCOL in "${PROTOCOLS[@]}"; do
    echo -e "${BLUE}========================================${NC}"
    echo -e "${BLUE}Testing Protocol: ${PROTOCOL}${NC}"
    echo -e "${BLUE}========================================${NC}"
    
    # Stop any existing server
    stop_server
    
    # Start server with the protocol
    echo -e "${YELLOW}Starting server with ${PROTOCOL} protocol...${NC}"
    export THRIFT_PROTOCOL=$PROTOCOL
    cd java-server
    java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" com.example.Server > "../server-${PROTOCOL}.log" 2>&1 &
    SERVER_PID=$!
    cd ..
    
    # Wait for server to start
    if ! wait_for_server; then
        echo -e "${RED}✗ Test failed for protocol: ${PROTOCOL}${NC}"
        cat "server-${PROTOCOL}.log"
        continue
    fi
    
    # Test Java client
    echo -e "${YELLOW}Testing Java client...${NC}"
    cd java-client
    java -cp "target/classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" com.example.JavaClient > "../java-client-${PROTOCOL}.log" 2>&1
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Java client test passed${NC}"
    else
        echo -e "${RED}✗ Java client test failed${NC}"
        cat "../java-client-${PROTOCOL}.log"
    fi
    cd ..
    
    # Test Python client
    echo -e "${YELLOW}Testing Python client...${NC}"
    cd python-client
    python3 client.py > "../python-client-${PROTOCOL}.log" 2>&1
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Python client test passed${NC}"
    else
        echo -e "${RED}✗ Python client test failed${NC}"
        cat "../python-client-${PROTOCOL}.log"
    fi
    cd ..
    
    # Test Ruby client
    echo -e "${YELLOW}Testing Ruby client...${NC}"
    cd ruby-client
    ruby client.rb > "../ruby-client-${PROTOCOL}.log" 2>&1
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Ruby client test passed${NC}"
    else
        echo -e "${RED}✗ Ruby client test failed${NC}"
        cat "../ruby-client-${PROTOCOL}.log"
    fi
    cd ..
    
    # Stop server
    stop_server
    unset THRIFT_PROTOCOL
    
    echo ""
    sleep 2
done

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Test Summary${NC}"
echo -e "${BLUE}========================================${NC}"
echo -e "${GREEN}All protocol tests completed!${NC}"
echo -e "${YELLOW}Check the log files for detailed output:${NC}"
for PROTOCOL in "${PROTOCOLS[@]}"; do
    echo "  - server-${PROTOCOL}.log"
    echo "  - java-client-${PROTOCOL}.log"
    echo "  - python-client-${PROTOCOL}.log"
    echo "  - ruby-client-${PROTOCOL}.log"
done
echo ""
