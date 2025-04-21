#!/bin/bash

# Build the project
echo "Building project..."
./gradlew clean build

# Start the server in the background
echo "Starting server..."
java -jar dax-server/build/libs/dax-server-0.0.1.jar &
SERVER_PID=$!

# Wait for server to start
sleep 2

# Start the client in the background
echo "Starting client..."
java -jar dax-client/build/libs/dax-client-0.0.1.jar &
CLIENT_PID=$!

# Wait for client to start
sleep 2

# Test basic operations
echo "Testing basic operations..."
echo "ADD test value1" | nc localhost 7099
echo "GET test" | nc localhost 7099
echo "HEARTBEAT" | nc localhost 7099

# Kill the server to test reconnection
echo "Killing server to test reconnection..."
kill $SERVER_PID
sleep 5

# Restart the server
echo "Restarting server..."
java -jar dax-server/build/libs/dax-server-0.0.1.jar &
SERVER_PID=$!

# Wait for server to start
sleep 2

# Test operations after reconnection
echo "Testing operations after reconnection..."
echo "GET xyz1" | nc localhost 7099
echo "ADD xyz2 value2" | nc localhost 7099
echo "GET xyz2" | nc localhost 7099

# Cleanup
echo "Stopping client and server..."
kill $CLIENT_PID
kill $SERVER_PID 