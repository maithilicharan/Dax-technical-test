package org.global.dax.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ServerMain {
    private static final Logger LOG = LoggerFactory.getLogger(ServerMain.class);

    public static void main(String[] args) {
        LOG.info("Starting server...");
        try {
            DaxServer server = new DaxServer();
            server.start();
        } catch (Exception e) {
            LOG.error("Could not start server: " + e.getMessage(), e);
        }
    }
}
