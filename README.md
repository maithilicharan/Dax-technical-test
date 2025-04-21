# DAX Technical Test

The technical test starting point for all engineers applying for a role within the DAX Exchange teams.

## Table of Contents

1. [The Task](#the-task)
2. [Prerequisites](#prerequisites)
3. [Building the project](#building-the-project)
4. [Running Each Component](#running-each-component)

## The Task

Your objective is to implement an in-memory cache, of which any number of clients can connect over a network. 

The following limitations apply:
- you must design a protocol on-top of Layer 4 (TCP/UDP) for communication between the client and server
- you must not use any third-party libraries for communication between the client and server (i.e. Spring, http client, gRPC, etc)

### The protocol

For communication between the client and server, the protocol you design must support the following operations:

```
GET "key"
```

which returns the value associated with the key specified, or an empty value if it is not present in the cache

```
GET ALL
```

which returns all the keys currently stored in the cache, or an empty value if the cache is empty

```
ADD "key" "value"
```

which inserts the value provided against the key, overwriting whatever value was previously associated with the key.
The server should then acknowledge the success of the operation to the client. 

```
DELETE "key"
```

which removes the value associated with the given key.
The server should then acknowledge the success of the operation to the client.

```
HEARTBEAT
```

The server should acknowledge the success of the operation to the client.

You can assume the following limitations on the key and value:
```
key = is a fixed size of 4 bytes
value = is variable length, with a maximum size of 2096 bytes
```

### The server must support the following

When executing the JAR, the server must begin listening on a chosen port (this can be hardcoded). The server must then
support multiple client connections, and the implementation of the protocol specified above. There is no need 
for this cache to be persisted between restarts.

### The client must support the following

When executing the JAR, the client must connect to the server and maintain a persistent connection. When a user enters 
a command, the client must send the command to the server and print some relative feedback to the user (i.e. ADD succeeded / failed). 
If the server connection is dead / failed, the client should attempt to reconnect with an appropriate backoff, reporting 
any failures to the user.

### The project structure

An initial starting point is provided in this repo, and has the following setup:
- [Client](./dax-client): where you can implement the code for the client
- [Server](./dax-server): where you can implement the code for the cache itself
- [Shared](./dax-shared): where you can implement common code between the client and server

### The submission

You should submit your work, using this code base as your starting point. Please also edit the README with any additional instructions needed 
to get your project running. You must also supply documentation on how your protocol works, which can be included in the README. 

We are expecting this to take candidates around 1 hour to complete. If you find there is improvements you could make given more time,
please take a note of these and include them in your README.

### What you will be assessed on

The project is giving you an opportunity to prove your knowledge in the following areas:
- code structure and testing: ensuring you write clear and concise code, which is well tested
- protocol design for efficient and reliable communication: ensuring you can work with low level abstractions over the network
- threading model chosen and its pros/cons: ensuring you understand concurrency, and can defend your chosen model
- performance of the implementation: expected a discussion on how you manage memory, and how this might affect your solutions latency and throughput

## Prerequisites

You need at-least JDK 17 installed on your local machine. Recommendation: [sdkman](https://sdkman.io/)

## Building the project

Run the following command from the root directory to build all projects.

```bash
./gradlew build
``` 

## Running each component

Both the dax-client and dax-server projects build executable JARs. To run each of them respectively:

```bash
./gradlew build
java -jar ./dax-server/build/libs/dax-server-0.0.1.jar
java -jar ./dax-client/build/libs/dax-client-0.0.1.jar 
```
