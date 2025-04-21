package org.global.dax.server;

import org.global.dax.shared.cache.ConcurrentHashMapCache;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

public class DaxServer {
    private static final int PORT = 7099;
    private final ConcurrentHashMapCache cache;
    private final Selector selector;
    private final ServerSocketChannel serverChannel;
    private volatile boolean running;

    public DaxServer() throws IOException {
        this.cache = new ConcurrentHashMapCache();
        this.selector = Selector.open();
        this.serverChannel = ServerSocketChannel.open();
        this.running = true;
        
        // Configure server socket
        serverChannel.bind(new InetSocketAddress(PORT));
        serverChannel.configureBlocking(false);
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void start() {
        System.out.println("NIO Server started on port " + PORT);
        
        while (running) {
            try {
                // Wait for events
                selector.select();
                
                // Get selected keys
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();
                
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        handleRead(key);
                    }
                    
                    iter.remove();
                }
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error in server loop: " + e.getMessage());
                }
            }
        }
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("New client connected: " + clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        
        try {
            int bytesRead = clientChannel.read(buffer);
            if (bytesRead == -1) {
                // Client closed connection
                clientChannel.close();
                return;
            }
            
            buffer.flip();
            String command = new String(buffer.array(), 0, buffer.limit()).trim();
            String response = processCommand(command);
            
            // Send response
            ByteBuffer responseBuffer = ByteBuffer.wrap((response + "\n").getBytes());
            while (responseBuffer.hasRemaining()) {
                clientChannel.write(responseBuffer);
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
            clientChannel.close();
        }
    }

    private String processCommand(String command) {
        String[] parts = command.split(" ", 3);
        if (parts.length == 0) return "ERROR: Empty command";

        try {
            switch (parts[0].toUpperCase()) {
                case "GET":
                    if (parts.length == 2) {
                        if (parts[1].equals("ALL")) {
                            String[] keys = cache.getAllKeys();
                            return keys.length > 0 ? String.join(",", keys) : "No keys found";
                        } else {
                            String value = cache.get(parts[1]);
                            return value != null ? value : "";
                        }
                    }
                    break;

                case "ADD":
                    if (parts.length == 3) {
                        cache.put(parts[1], parts[2]);
                        return "OK";
                    }
                    break;

                case "DELETE":
                    if (parts.length == 2) {
                        cache.remove(parts[1]);
                        return "OK";
                    }
                    break;

                case "HEARTBEAT":
                    return "OK";

                default:
                    return "ERROR: Unknown command";
            }
        } catch (IllegalArgumentException e) {
            return "ERROR: " + e.getMessage();
        }
        
        return "ERROR: Invalid command format";
    }

    public void stop() {
        running = false;
        try {
            selector.wakeup();
            selector.close();
            serverChannel.close();
        } catch (IOException e) {
            System.err.println("Error closing server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            DaxServer server = new DaxServer();
            server.start();
        } catch (IOException e) {
            System.err.println("Could not start server: " + e.getMessage());
        }
    }
} 