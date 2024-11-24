package ru.dushkinmir.absolutelyRandom.utils;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebSocketServer extends org.java_websocket.server.WebSocketServer {
    private static final Pattern CREDENTIALS_PATTERN = Pattern.compile("h-hii! this is my creds!~ (.+?):(.+?);");
    private static final Pattern BROADCAST_PATTERN = Pattern.compile("h-hii! c-can u pls send this for all\\?~ (.+?);");
    private static final int TIMEOUT_SECONDS = 2;

    private final Logger logger;
    private final Map<WebSocket, ScheduledFuture<?>> timeoutTasks = new HashMap<>();
    private final Map<WebSocket, Boolean> authenticatedClients;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final List<WebSocketMessageListener> listeners = new ArrayList<>();

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
        return new ArrayList<>(listeners); // Возвращаем копию списка слушателей для предотвращения изменений извне
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

        Matcher matcher = BROADCAST_PATTERN.matcher(message);
        if (matcher.matches()) {
            broadcast(matcher.group(1));
        }

        listeners.forEach(listener -> listener.onMessageReceived(conn, message));
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        logger.severe("Ошибка WebSocket сервера: " + ex.getMessage());
    }

    @Override
    public void onStart() {
        logger.info("WebSocket сервер запущен на " + getAddress().getHostString() + ":" + getPort());
    }

    private boolean authenticateClient(String playerCreds) {
        Matcher matcher = CREDENTIALS_PATTERN.matcher(playerCreds);
        if (matcher.matches()) {
            String name = matcher.group(1);
            return validatePlayerCredentials(name, matcher.group(2));
        }
        return false;
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
}
