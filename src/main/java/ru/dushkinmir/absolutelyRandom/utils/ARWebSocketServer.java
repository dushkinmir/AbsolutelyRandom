package ru.dushkinmir.absolutelyRandom.utils;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ARWebSocketServer extends WebSocketServer {
    private final Logger logger;
    private final Map<WebSocket, ScheduledFuture<?>> timeoutTasks = new HashMap<>();
    private final Map<WebSocket, Boolean> authenticatedClients;

    public ARWebSocketServer(String ip, int port, Logger logger) {
        super(new InetSocketAddress(ip, port));
        this.logger = logger;
        this.authenticatedClients = new HashMap<>();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        logger.info("Новый клиент подключен: " + conn.getRemoteSocketAddress());
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        ScheduledFuture<?> task = scheduler.schedule(() -> {
            logger.info("Не получено сообщение от клиента \"%s\" за 2 секунды. Отключение...".formatted(conn.getRemoteSocketAddress()));
            conn.close(1000, "ну шо ты лысый плаке плаке");
        }, 2, TimeUnit.SECONDS);
        timeoutTasks.put(conn, task);
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        logger.info("Клиент отключен: " + conn.getRemoteSocketAddress());
        ScheduledFuture<?> task = timeoutTasks.remove(conn);
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }
        authenticatedClients.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        logger.info("Сообщение от клиента: " + message);
        ScheduledFuture<?> task = timeoutTasks.remove(conn);
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
        }
        if (!(authenticatedClients.containsKey(conn) && authenticatedClients.get(conn))) {
            if (authenticateClient(message)) {
                logger.info("Клиент успешно аутентифицирован: " + conn.getRemoteSocketAddress());
                authenticatedClients.put(conn, true);
            } else {
                logger.warning("Ошибка аутентификации. Соединение будет закрыто: " + conn.getRemoteSocketAddress());
                conn.close(1000, "дебил у тя креды неверные,лох");
            }
        }
        final Pattern COMMAND_PATTERN = Pattern.compile("h-hii! c-can u pls send this for all\\?~ (.+?);");
        Matcher matcher = COMMAND_PATTERN.matcher(message);
        if (matcher.matches()) {
            broadcast(matcher.group(1));
        }
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
        final Pattern COMMAND_PATTERN = Pattern.compile("h-hii! this is my creds!~ (.+?):(.+?);");
        Matcher matcher = COMMAND_PATTERN.matcher(playerCreds);

        if (matcher.matches()) {
            String name = matcher.group(1);
            String receivedHash = matcher.group(2);

            boolean playerExists = PlayerUtils.getOnlinePlayers().stream()
                    .anyMatch(player -> player.getName().equals(name));

            if (playerExists) {
                String expectedHash = HashUtils.computeSHA256Hash(name);

                return receivedHash.equals(expectedHash);
            } else if (name.equals("посхалко")) {
                return receivedHash.equals("идите все нахуй");
            }
        }
        return false;
    }
}
