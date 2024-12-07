package ru.dushkinmir.absolutelyRandom.utils.network;

import org.java_websocket.WebSocket;

public interface WebSocketMessageListener {
    void onMessageReceived(WebSocket conn, String message);

    void onClientConnected(WebSocket conn);

    void onClientDisconnected(WebSocket conn, int code, String reason);
}
