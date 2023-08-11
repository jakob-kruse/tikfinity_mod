package de.jakobkruse.tikfinity;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class CommandServer {
    private final CommandExecutor executor;

    public CommandServer(CommandExecutor executor) {
        this.executor = executor;
    }

    private HttpServer server = null;

    public void startServer() {
        ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
        if(server == null) {
            try {
                server = HttpServer.create(new InetSocketAddress(config.port), 0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            server.createContext("/v1/server", new HTTPServer());
            server.createContext("/v1/server/exec", new HTTPServer());
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
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.getResponseHeaders().add("Access-Control-Allow-Methods", "*");
            t.getResponseHeaders().add("Access-Control-Allow-Headers", "*");

            if (t.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
                t.sendResponseHeaders(204, -1);
                return;
            }

            switch (t.getRequestURI().toString()) {
                case "/v1/server" -> handleServer(t);
                case "/v1/server/exec" -> handleExecute(t);
                default -> t.sendResponseHeaders(404, 0);
            }

            t.close();

        }

        private void handleExecute(HttpExchange t) throws IOException {
            InputStream reqStream = t.getRequestBody();

            String requestString = IOUtils.toString(reqStream, StandardCharsets.UTF_8);

            String command = requestString.contains("command=") ? URLDecoder.decode(requestString.split("=")[1], StandardCharsets.UTF_8)  : "";

            TikfinityMod.LOGGER.info("Received command: " + command);

            executor.execute(command);

            String response = "OK";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

        private void handleServer(HttpExchange t) throws IOException {
            String playerName = null;

            try {
                MinecraftServer server = MinecraftClient.getInstance().getServer();

                if (server != null) {
                    String[] playerNames = server.getPlayerManager().getPlayerNames();

                    if (playerNames.length > 0) {
                        String firstPlayer = playerNames[0];

                        ServerPlayerEntity player = server.getPlayerManager().getPlayer(firstPlayer);

                        if(player != null) {
                            playerName = player.getEntityName();
                        }
                    }
                }
            } catch (NullPointerException ignored) {

            }

            t.getResponseHeaders().add("Content-Type", "application/json");
            String response = String.format("""
                    {
                      "name": "TikFinity Mod",
                      "motd": "string",
                      "version": "1.20.1",
                      "bukkitVersion": "string",
                      "tps": "string",
                      "playerName": %s,
                      "health": {
                        "cpus": 0,
                        "uptime": 0,
                        "totalMemory": 0,
                        "maxMemory": 0,
                        "freeMemory": 0
                      },
                      "bannedIps": [
                        {
                          "target": "string",
                          "source": "string",
                          "reason": "string",
                          "expiration": "2023-08-01T22:55:55.756Z"
                        }
                      ],
                      "bannedPlayers": [
                        {
                          "target": "string",
                          "source": "string",
                          "reason": "string",
                          "expiration": "2023-08-01T22:55:55.756Z"
                        }
                      ],
                      "whitelistedPlayers": [
                        {
                          "uuid": "string",
                          "name": "string"
                        }
                      ],
                      "maxPlayers": 0,
                      "onlinePlayers": 0
                    }""", playerName != null ? "\"" + playerName + "\"" : "null");
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }

    }
}
