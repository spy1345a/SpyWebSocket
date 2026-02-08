package org.aj.spyWebSocket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;

public final class SpyWebSocket extends JavaPlugin {

    private WebSocketServer wsServer;
    private final Set<WebSocket> authedClients = new HashSet<>();

    private String wsUser;
    private String wsPass;

    @Override
    public void onEnable() {

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }

        reloadConfig();

        String host = getConfig().getString("websocket.host", "127.0.0.1");
        int port = getConfig().getInt("websocket.port", 8765);
        wsUser = getConfig().getString("websocket.username");
        wsPass = getConfig().getString("websocket.password");

        wsServer = new WebSocketServer(new InetSocketAddress(host, port)) {

            @Override
            public void onStart() {
                getLogger().info("WebSocket server started");
            }

            @Override
            public void onOpen(WebSocket conn, ClientHandshake handshake) {
                getLogger().info("WS connected: " + conn.getRemoteSocketAddress());
            }

            @Override
            public void onMessage(WebSocket conn, String message) {

                JsonObject msg;
                try {
                    msg = JsonParser.parseString(message).getAsJsonObject();
                } catch (Exception e) {
                    sendError(conn, "invalid_json");
                    return;
                }

                if (!msg.has("type")) {
                    sendError(conn, "missing_type");
                    return;
                }

                String type = msg.get("type").getAsString();

                switch (type) {

                    case "auth" -> {
                        String u = msg.get("username").getAsString();
                        String p = msg.get("password").getAsString();

                        if (u.equals(wsUser) && p.equals(wsPass)) {
                            authedClients.add(conn);
                            conn.send("{\"type\":\"auth_ok\"}");
                        } else {
                            conn.send("{\"type\":\"auth_fail\"}");
                            conn.close();
                        }
                    }

                    case "command" -> {
                        if (!isAuthed(conn)) return;
                        String command = msg.get("command").getAsString();
                        executeCommand(command);
                    }

                    case "get_players" -> {
                        if (!isAuthed(conn)) return;
                        conn.send(getAllPlayerCoordinates().toString());
                    }

                    default -> sendError(conn, "unknown_type");
                }
            }

            @Override
            public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                authedClients.remove(conn);
                getLogger().info("WS disconnected");
            }

            @Override
            public void onError(WebSocket conn, Exception ex) {
                getLogger().warning("WS error: " + ex.getMessage());
            }

            private boolean isAuthed(WebSocket conn) {
                if (!authedClients.contains(conn)) {
                    sendError(conn, "not_authenticated");
                    return false;
                }
                return true;
            }

            private void sendError(WebSocket conn, String reason) {
                JsonObject err = new JsonObject();
                err.addProperty("type", "error");
                err.addProperty("reason", reason);
                conn.send(err.toString());
            }
        };

        wsServer.start();
        getLogger().info("WebSocket listening on " + host + ":" + port);
    }

    @Override
    public void onDisable() {
        try {
            if (wsServer != null) {
                wsServer.stop();
            }
        } catch (Exception ignored) {}
    }

    /* ================= API ================= */

    public void executeCommand(String command) {
        Bukkit.getScheduler().runTask(this, () ->
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
        );
    }

    public JsonObject getAllPlayerCoordinates() {

        JsonObject root = new JsonObject();
        JsonObject players = new JsonObject();

        for (Player p : Bukkit.getOnlinePlayers()) {
            JsonObject pos = new JsonObject();
            pos.addProperty("x", p.getLocation().getX());
            pos.addProperty("y", p.getLocation().getY());
            pos.addProperty("z", p.getLocation().getZ());
            pos.addProperty("world", p.getWorld().getName());
            players.add(p.getName(), pos);
        }

        root.addProperty("type", "players");
        root.add("players", players);
        return root;
    }
}
