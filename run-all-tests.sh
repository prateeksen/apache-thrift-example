#!/bin/bash
# run-all-tests.sh - Run all unit tests for the Apache Thrift application

set -e  # Exit on error

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================"
echo "  Running All Unit Tests"
echo "========================================"

# Track results
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to print test results
print_result() {
    local name=$1
    local result=$2
    if [ $result -eq 0 ]; then
        echo -e "${GREEN}✅ $name PASSED${NC}"
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        echo -e "${RED}❌ $name FAILED${NC}"
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
}

# 1. Java Server Tests
echo ""
echo -e "${YELLOW}1. Running Java Server Tests...${NC}"
cd java-server
if mvn -q clean test > /tmp/java-server-test.log 2>&1; then
    TESTS=$(grep -oP 'Tests run: \K\d+' /tmp/java-server-test.log | head -1)
    echo "   Tests run: $TESTS"
    TOTAL_TESTS=$((TOTAL_TESTS + TESTS))
    print_result "Java Server Tests" 0
else
    print_result "Java Server Tests" 1
    cat /tmp/java-server-test.log
fi

# 2. Java Client Tests
echo ""
echo -e "${YELLOW}2. Running Java Client Tests...${NC}"
cd ../java-client
if mvn -q clean test > /tmp/java-client-test.log 2>&1; then
    TESTS=$(grep -oP 'Tests run: \K\d+' /tmp/java-client-test.log | head -1)
    SKIPPED=$(grep -oP 'Skipped: \K\d+' /tmp/java-client-test.log | head -1)
    echo "   Tests run: $TESTS (Skipped: $SKIPPED)"
    TOTAL_TESTS=$((TOTAL_TESTS + TESTS - SKIPPED))
    print_result "Java Client Tests" 0
else
    print_result "Java Client Tests" 1
    cat /tmp/java-client-test.log
fi

# 3. Python Client Tests
echo ""
echo -e "${YELLOW}3. Running Python Client Tests...${NC}"
cd ../python-client
if python3 test_client.py > /tmp/python-test.log 2>&1; then
    # Count OK tests
    TESTS=$(grep -oP 'Ran \K\d+' /tmp/python-test.log | head -1)
    SKIPPED=$(grep -oP 'skipped=\K\d+' /tmp/python-test.log | head -1 || echo 0)
    echo "   Tests run: $TESTS (Skipped: $SKIPPED)"
    TOTAL_TESTS=$((TOTAL_TESTS + TESTS - SKIPPED))
    print_result "Python Client Tests" 0
else
    print_result "Python Client Tests" 1
    cat /tmp/python-test.log
fi

# 4. Ruby Client Tests
echo ""
echo -e "${YELLOW}4. Running Ruby Client Tests...${NC}"
cd ../ruby-client
if ruby test_client.rb > /tmp/ruby-test.log 2>&1; then
    # Extract test count
    TESTS=$(grep -oP '\K\d+(?= tests)' /tmp/ruby-test.log | head -1)
    OMISSIONS=$(grep -oP '\K\d+(?= omissions)' /tmp/ruby-test.log | head -1 || echo 0)
    echo "   Tests run: $TESTS (Omitted: $OMISSIONS)"
    TOTAL_TESTS=$((TOTAL_TESTS + TESTS - OMISSIONS))
    print_result "Ruby Client Tests" 0
else
    print_result "Ruby Client Tests" 1
    cat /tmp/ruby-test.log
fi

# Summary
echo ""
echo "========================================"
echo "  Test Summary"
echo "========================================"
echo "Total Tests:  $TOTAL_TESTS"
echo -e "Passed:       ${GREEN}$PASSED_TESTS${NC}"
if [ $FAILED_TESTS -gt 0 ]; then
    echo -e "Failed:       ${RED}$FAILED_TESTS${NC}"
fi
echo "========================================"

if [ $FAILED_TESTS -eq 0 ]; then
    echo -e "${GREEN}✅ All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}❌ Some tests failed!${NC}"
    exit 1
fi
