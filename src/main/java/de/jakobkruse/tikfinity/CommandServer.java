package de.jakobkruse.tikfinity;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class CommandServer {
    private final CommandExecutor executor;

    public CommandServer(CommandExecutor executor) {
        this.executor = executor;
    }

    private HttpServer server = null;

    public void startServer() {
        if(server == null) {
            try {
                server = HttpServer.create(new InetSocketAddress(4567), 0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            server.createContext("/v1/exec", new HTTPServer());
            server.setExecutor(null);
        }
        server.start();
    }

    public void stopServer() {
        if(server == null) {
            return;
        }
        try {
            server.stop(0);
        } catch (Exception e) {
            TikfinityMod.LOGGER.error("Error stopping server", e);
        }
    }

    private class HTTPServer implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            InputStream reqStream = t.getRequestBody();

            String requestString = IOUtils.toString(reqStream, StandardCharsets.UTF_8);

            String command = requestString.split("=")[1];

            TikfinityMod.LOGGER.info("Received command: " + command);

            executor.execute(command);

            String response = "OK";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

    }
}
