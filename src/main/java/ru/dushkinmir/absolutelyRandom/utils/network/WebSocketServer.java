package ru.dushkinmir.absolutelyRandom.utils.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import ru.dushkinmir.absolutelyRandom.utils.HashUtils;
import ru.dushkinmir.absolutelyRandom.utils.PlayerUtils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class WebSocketServer extends org.java_websocket.server.WebSocketServer {
    private static final int TIMEOUT_SECONDS = 2;

    private final Logger logger;
    private final Map<WebSocket, ScheduledFuture<?>> timeoutTasks = new HashMap<>();
    private final Map<WebSocket, Boolean> authenticatedClients;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final List<WebSocketMessageListener> listeners = new ArrayList<>();
    private final Gson gson = new Gson();

    public WebSocketServer(String ip, int port, Logger logger) {
        super(new InetSocketAddress(ip, port));
        this.logger = logger;
        this.authenticatedClients = new HashMap<>();
    }

    public void addListener(WebSocketMessageListener listener) {
        listeners.add(listener);
    }

    public void removeListener(WebSocketMessageListener listener) {
        listeners.remove(listener);
    }

    public List<WebSocketMessageListener> getListeners() {
        return new ArrayList<>(listeners);
    }


    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.info("Новый клиент подключен: " + conn.getRemoteSocketAddress());
        listeners.forEach(listener -> listener.onClientConnected(conn));
        scheduleTimeout(conn);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.info("Клиент отключен: " + conn.getRemoteSocketAddress());
        cancelTimeoutTask(conn);
        authenticatedClients.remove(conn);
        listeners.forEach(listener -> listener.onClientDisconnected(conn, code, reason));
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.info("Сообщение от клиента: " + message);
        cancelTimeoutTask(conn);

        if (!isClientAuthenticated(conn)) {
            if (authenticateClient(message)) {
                logger.info("Клиент успешно аутентифицирован: " + conn.getRemoteSocketAddress());
                authenticatedClients.put(conn, true);
            } else {
                logger.warning("Ошибка аутентификации. Соединение будет закрыто: " + conn.getRemoteSocketAddress());
                conn.close(1000, "дебил у тя креды неверные,лох");
                return;
            }
        }

        JsonObject jsonMessage = parseJson(message);
        if (jsonMessage != null && jsonMessage.has("broadcast")) {
            broadcast(jsonMessage.get("broadcast").getAsString());
        }


        listeners.forEach(listener -> listener.onMessageReceived(conn, message));
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.severe("Ошибка WebSocket сервера: " + ex.getMessage());
    }

    @Override
    public void onStart() {
    }

    private boolean authenticateClient(String jsonMessage) {
        JsonObject json = parseJson(jsonMessage);
        if (json == null || !json.has("name") || !json.has("hash")) {
            return false;
        }

        String name = json.get("name").getAsString();
        String receivedHash = json.get("hash").getAsString();

        return validatePlayerCredentials(name, receivedHash);
    }

    private boolean validatePlayerCredentials(String name, String receivedHash) {
        boolean playerExists = PlayerUtils.getOnlinePlayers().stream()
                .anyMatch(player -> player.getName().equals(name));

        if (playerExists) {
            String expectedHash = HashUtils.computeSHA256Hash(name);
            return receivedHash.equals(expectedHash);
        } else return name.equals("посхалко") && receivedHash.equals("идите все нахуй");
    }

    private boolean isClientAuthenticated(WebSocket conn) {
        return authenticatedClients.getOrDefault(conn, false);
    }

    private void scheduleTimeout(WebSocket conn) {
        ScheduledFuture<?> task = scheduler.schedule(() -> {
            logger.info("Не получено сообщение от клиента \"%s\" за 2 секунды. Отключение...".formatted(conn.getRemoteSocketAddress()));
            conn.close(1000, "ну шо ты лысый плаке плаке");
        }, TIMEOUT_SECONDS, TimeUnit.SECONDS);
        timeoutTasks.put(conn, task);
    }

    private void cancelTimeoutTask(WebSocket conn) {
        ScheduledFuture<?> task = timeoutTasks.remove(conn);
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }
    }

    private JsonObject parseJson(String jsonString) {
        try {
            return gson.fromJson(jsonString, JsonObject.class);
        } catch (JsonSyntaxException e) {
            logger.warning("Неверный формат JSON: " + e.getMessage());
            return null;
        }
    }
}
