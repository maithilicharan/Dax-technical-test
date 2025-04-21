package org.global.dax.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class DaxClient {
    private static final String HOST = "localhost";
    private static final int PORT = 7099;
    private static final int MAX_RETRIES = 5;
    private static final long INITIAL_BACKOFF_MS = 1000;
    private static final long MAX_BACKOFF_MS = 32000;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private volatile boolean running = true;

    public void start() {
        System.out.println("Client starting...");
        connectWithRetry();

        Scanner scanner = new Scanner(System.in);
        while (running) {
            try {
                if (System.in.available() > 0) {
                    String command = scanner.nextLine().trim();
                    if (command.isEmpty()) {
                        continue;
                    }

                    if (command.equalsIgnoreCase("exit")) {
                        running = false;
                        break;
                    }

                    if (!isValidCommand(command)) {
                        System.out.println("Invalid command. Valid commands are: GET,GET ALL,ADD, DELETE, HEARTBEAT");
                        continue;
                    }

                    if (!sendCommand(command)) {
                        System.out.println("Connection lost. Attempting to reconnect...");
                        connectWithRetry();
                        if (socket != null) {
                            sendCommand(command);
                        }
                    }
                } else {
                    Thread.sleep(100); // Small delay to prevent CPU spinning
                }
            } catch (Exception e) {
                System.err.println("Error processing command: " + e.getMessage());
            }
        }

        closeResources();
        scanner.close();
    }

    private boolean isValidCommand(String command) {
        String[] parts = command.split(" ");
        if (parts.length == 0) return false;

        String cmd = parts[0].toUpperCase();
        return switch (cmd) {
            case "GET" -> parts.length == 2 || (parts.length == 2 && parts[1].equals("ALL"));
            case "ADD" -> parts.length == 3;
            case "DELETE" -> parts.length == 2;
            case "HEARTBEAT" -> parts.length == 1;
            default -> false;
        };
    }

    private boolean sendCommand(String command) {
        try {
            if (socket == null || socket.isClosed()) {
                return false;
            }

            out.println(command);
            out.flush();

            String response = in.readLine();
            if (response == null) {
                return false;
            }

            System.out.println("Server response: " + response);
            return true;
        } catch (IOException e) {
            System.err.println("Error sending command: " + e.getMessage());
            return false;
        }
    }

    private void connectWithRetry() {
        int retryCount = 0;
        long backoffMs = INITIAL_BACKOFF_MS;

        while (retryCount < MAX_RETRIES) {
            try {
                if (connect()) {
                    System.out.println("Connected to server");
                    return;
                }
            } catch (IOException e) {
                System.err.println("Connection attempt " + (retryCount + 1) + " failed: " + e.getMessage());
            }

            retryCount++;
            if (retryCount < MAX_RETRIES) {
                try {
                    System.out.println("Retrying in " + backoffMs + "ms...");
                    TimeUnit.MILLISECONDS.sleep(backoffMs);
                    backoffMs = Math.min(backoffMs * 2, MAX_BACKOFF_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        System.err.println("Failed to connect after " + MAX_RETRIES + " attempts");
    }

    private boolean connect() throws IOException {
        closeResources();
        socket = new Socket(HOST, PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        return true;
    }

    private void closeResources() {
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new DaxClient().start();
    }
} 