## Server-Side Improvements:

**Improved Parsing**: Employing a more sophisticated parsing strategy, such as regular expressions or a state machine, to handle different command formats, optional arguments.

**Command Processing Layer**: Introduce separate command processing layer to asynchronously receive responses from process heavy workloads. 

## Cache Optimization:

**Tuning**: Tune ConcurrentHashMap with different initial capacity, load factor, and concurrency level based on the expected workload and read/write heavyness.

## Client-Side Improvements:

**Input Validation**: Strengthen input validation on both the client and server sides to handle invalid commands, malformed data and potential security issues.

**Configuration**: Externalize configurable parameters (e.g., server port, client retry settings, cache parameters) into a configuration file or environment variables.
 

## Building the project

Run the following command from the root directory to build all projects.

```bash
./gradlew build
``` 

## Running tests end 2 end

 Please use test.sh for building, lauching a server and multiple clients. It does clean up servers and clients end.

```bash
./test.sh 
```
