
package org.global.dax.server;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import static org.junit.jupiter.api.Assertions.*;


class DaxServerTest {

    private DaxServer server;
    private Thread serverThread;

    @BeforeEach
    void setUp() throws IOException {
        server = new DaxServer();
        serverThread = new Thread(() -> server.start());
        serverThread.start();

        // Server startup time
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    void tearDown() {
        server.stop();
        try {
            serverThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private String sendCommand(String command) throws IOException {
        int PORT = 7099;
        try (SocketChannel clientChannel = SocketChannel.open(new InetSocketAddress("localhost", PORT))) {
            ByteBuffer buffer = ByteBuffer.wrap(command.getBytes());
            clientChannel.write(buffer);

            ByteBuffer readBuffer = ByteBuffer.allocate(1024);
            clientChannel.read(readBuffer);
            readBuffer.flip();
            return new String(readBuffer.array(), 0, readBuffer.limit()).trim();
        }
    }

    @Test
    void testAddCommand() throws IOException {
        String response = sendCommand("ADD key1 value1");
        assertEquals("OK", response);
    }

    @Test
    void testGetCommand() throws IOException {
        sendCommand("ADD key2 value2");
        String response = sendCommand("GET key2");
        assertEquals("value2", response);
    }

    @Test
    void testGetCommandNotFound() throws IOException {
        String response = sendCommand("GET nonExistentKey");
        assertEquals("", response);
    }

    @Test
    void testDeleteCommand() throws IOException {
        sendCommand("ADD key3 value3");
        String response = sendCommand("DELETE key3");
        assertEquals("OK", response);
        String getResponse = sendCommand("GET key3");
        assertEquals("", getResponse);
    }

    @Test
    void testHeartbeatCommand() throws IOException {
        String response = sendCommand("HEARTBEAT");
        assertEquals("OK", response);
    }

    @Test
    void testUnknownCommand() throws IOException {
        String response = sendCommand("UNKNOWN");
        assertEquals("ERROR: Unknown command", response);
    }

    @Test
    void testInvalidAddCommandFormat() throws IOException {
        String response = sendCommand("ADD key");
        assertEquals("ERROR: Invalid command format", response);
    }

    @Test
    void testGetAllCommandEmptyCache() throws IOException {
        String response = sendCommand("GET ALL");
        assertEquals("No keys found", response);
    }

    @Test
    void testGetAllCommandWithKeys() throws IOException {
        sendCommand("ADD key4 value4");
        sendCommand("ADD key5 value5");
        String response = sendCommand("GET ALL");
        assertTrue(response.contains("key4") && response.contains("key5"));
    }

    @Test
    void testAddCommandWithSpacesInValue() throws IOException {
        String response = sendCommand("ADD key6 value with spaces");
        assertEquals("OK", response);
        String getValueResponse = sendCommand("GET key6");
        assertEquals("value with spaces", getValueResponse);
    }
}