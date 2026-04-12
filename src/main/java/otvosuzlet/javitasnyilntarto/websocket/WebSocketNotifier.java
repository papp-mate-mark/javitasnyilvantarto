package otvosuzlet.javitasnyilntarto.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketNotifier extends TextWebSocketHandler {
    private final Map<String, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    public void registerSession(String sessionId, WebSocketSession session) {
        sessionMap.put(sessionId, session);
    }

    public void removeSession(WebSocketSession session) {
        sessionMap.values().remove(session); // remove by value
    }

    public void broadcastChange(String originSessionId) {
        sessionMap.forEach((sessionId, session) -> {
            if (!sessionId.equals(originSessionId) && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage("data changed by someone"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}