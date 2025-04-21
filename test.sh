#!/bin/bash

# Build the project
echo "Building project..."
./gradlew clean build

# Start the server in the background
echo "Starting NIO server..."
java -jar dax-server/build/libs/dax-server-0.0.1.jar &
SERVER_PID=$!

# Wait for server to start
sleep 2

# Start multiple clients in the background
echo "Starting multiple clients..."
for i in {1..3}; do
    echo "Starting client $i..."
    java -jar dax-client/build/libs/dax-client-0.0.1.jar &
    CLIENT_PIDS[$i]=$!
    sleep 1
done

# Wait for clients to start
sleep 2

# Function to send command and get response
send_command() {
    local cmd=$1
    echo "Sending: $cmd"
    # Use printf to ensure proper line ending
    printf "%s\n" "$cmd" | nc localhost 7099
    echo "------------------------"
}

# Test operations from each client
echo "Testing operations from multiple clients..."
for i in {1..3}; do
    echo "Testing client $i..."
    # Each client adds a unique key-value pair (4-byte keys)
    send_command "ADD k$i$i$i v$i"
    # Each client gets their own key
    send_command "GET k$i$i$i"
    # Each client gets all keys
    send_command "GET ALL"
    # Each client sends a heartbeat
    send_command "HEARTBEAT"
    sleep 1
done

# Test concurrent operations
echo "Testing concurrent operations..."
for i in {1..3}; do
    echo "Sending concurrent ADD command $i..."
    printf "ADD c$i$i$i v$i\n" | nc localhost 7099 &
done

# Wait for all concurrent operations to complete
echo "Waiting for concurrent operations to complete..."
sleep 5

# Get all keys after concurrent operations
echo "Getting all keys after concurrent operations..."
send_command "GET ALL"

# Cleanup
echo "Stopping clients and server..."
for pid in "${CLIENT_PIDS[@]}"; do
    kill $pid
done
kill $SERVER_PID 