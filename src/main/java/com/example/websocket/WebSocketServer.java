package com.example.websocket;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class WebSocketServer extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    private static final int PORT = 8080;
    private static final String WEBSOCKET_PATH = "/websocket";
    
    // Store active WebSocket connections
    private final Map<String, ServerWebSocket> connections = new ConcurrentHashMap<>();
    
    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new WebSocketServer())
            .onSuccess(id -> logger.info("WebSocket Server deployed successfully with deployment ID: {}", id))
            .onFailure(throwable -> {
                logger.error("Failed to deploy WebSocket Server", throwable);
                vertx.close();
            });
    }

    @Override
    public void start(Promise<Void> startPromise) {
        HttpServer server = vertx.createHttpServer();
        Router router = Router.router(vertx);
        
        // Serve static files (for a simple web client)
        router.route("/static/*").handler(StaticHandler.create("static"));
        
        // Health check endpoint
        router.get("/health").handler(ctx -> {
            JsonObject health = new JsonObject()
                .put("status", "UP")
                .put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .put("activeConnections", connections.size());
            ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(health.encode());
        });
        
        // WebSocket handler
        server.webSocketHandler(this::handleWebSocket);
        
        // HTTP routes
        server.requestHandler(router);
        
        server.listen(PORT)
            .onSuccess(httpServer -> {
                logger.info("WebSocket Server started on port {}", PORT);
                logger.info("WebSocket endpoint: ws://localhost:{}{}", PORT, WEBSOCKET_PATH);
                logger.info("Health check: http://localhost:{}/health", PORT);
                startPromise.complete();
            })
            .onFailure(throwable -> {
                logger.error("Failed to start WebSocket Server", throwable);
                startPromise.fail(throwable);
            });
    }
    
    private void handleWebSocket(ServerWebSocket webSocket) {
        logger.info("Handling new websocket connection");
        
        String path = webSocket.path();
        
        if (!WEBSOCKET_PATH.equals(path)) {
            logger.warn("Invalid WebSocket path: {}", path);
            webSocket.reject();
            return;
        }
        
        String connectionId = UUID.randomUUID().toString();
        String clientAddress = webSocket.remoteAddress().toString();
        
        logger.info("New WebSocket connection established - ID: {}, Client: {}", connectionId, clientAddress);
        
        // Store the connection
        connections.put(connectionId, webSocket);
        
        // Send welcome message
        JsonObject welcomeMsg = new JsonObject()
            .put("type", "welcome")
            .put("connectionId", connectionId)
            .put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .put("message", "Connected to WebSocket server");
        
        webSocket.writeTextMessage(welcomeMsg.encode());
        
        // Broadcast to all clients that a new user joined
        broadcastMessage(new JsonObject()
            .put("type", "user_joined")
            .put("connectionId", connectionId)
            .put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            .put("totalConnections", connections.size()), connectionId);
        
        // Handle incoming messages
        webSocket.textMessageHandler(message -> {
            logger.info("Received message from {}: {}", connectionId, message);
            handleIncomingMessage(connectionId, message);
        });
        
        // Handle connection close
        webSocket.closeHandler(v -> {
            logger.info("WebSocket connection closed - ID: {}", connectionId);
            connections.remove(connectionId);
            
            // Broadcast to all clients that a user left
            broadcastMessage(new JsonObject()
                .put("type", "user_left")
                .put("connectionId", connectionId)
                .put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .put("totalConnections", connections.size()), null);
        });
        
        // Handle exceptions
        webSocket.exceptionHandler(throwable -> {
            logger.error("WebSocket error for connection {}", connectionId, throwable);
            connections.remove(connectionId);
        });
    }
    
    private void handleIncomingMessage(String connectionId, String message) {
        try {
            JsonObject msgObj = new JsonObject(message);
            String type = msgObj.getString("type", "message");
            
            switch (type) {
                case "ping":
                    handlePing(connectionId, msgObj);
                    break;
                case "broadcast":
                    handleBroadcast(connectionId, msgObj);
                    break;
                case "private":
                    handlePrivateMessage(connectionId, msgObj);
                    break;
                case "echo":
                    handleEcho(connectionId, msgObj);
                    break;
                default:
                    // Handle as general message
                    handleGeneralMessage(connectionId, msgObj);
                    break;
            }
        } catch (Exception e) {
            logger.error("Error processing message from {}: {}", connectionId, message, e);
            sendErrorMessage(connectionId, "Invalid message format");
        }
    }
    
    private void handlePing(String connectionId, JsonObject msgObj) {
        ServerWebSocket webSocket = connections.get(connectionId);
        if (webSocket != null) {
            JsonObject pongMsg = new JsonObject()
                .put("type", "pong")
                .put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .put("originalTimestamp", msgObj.getString("timestamp"));
            webSocket.writeTextMessage(pongMsg.encode());
        }
    }
    
    private void handleBroadcast(String connectionId, JsonObject msgObj) {
        JsonObject broadcastMsg = new JsonObject()
            .put("type", "broadcast")
            .put("from", connectionId)
            .put("message", msgObj.getString("message", ""))
            .put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        broadcastMessage(broadcastMsg, connectionId);
    }
    
    private void handlePrivateMessage(String connectionId, JsonObject msgObj) {
        String targetId = msgObj.getString("targetId");
        ServerWebSocket targetSocket = connections.get(targetId);
        
        if (targetSocket != null) {
            JsonObject privateMsg = new JsonObject()
                .put("type", "private")
                .put("from", connectionId)
                .put("message", msgObj.getString("message", ""))
                .put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            targetSocket.writeTextMessage(privateMsg.encode());
            
            // Send confirmation to sender
            ServerWebSocket senderSocket = connections.get(connectionId);
            if (senderSocket != null) {
                JsonObject confirmMsg = new JsonObject()
                    .put("type", "private_sent")
                    .put("targetId", targetId)
                    .put("message", msgObj.getString("message", ""))
                    .put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                senderSocket.writeTextMessage(confirmMsg.encode());
            }
        } else {
            sendErrorMessage(connectionId, "Target connection not found: " + targetId);
        }
    }
    
    private void handleEcho(String connectionId, JsonObject msgObj) {
        ServerWebSocket webSocket = connections.get(connectionId);
        if (webSocket != null) {
            JsonObject echoMsg = new JsonObject()
                .put("type", "echo")
                .put("originalMessage", msgObj.getString("message", ""))
                .put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            webSocket.writeTextMessage(echoMsg.encode());
        }
    }
    
    private void handleGeneralMessage(String connectionId, JsonObject msgObj) {
        JsonObject responseMsg = new JsonObject()
            .put("type", "message_received")
            .put("connectionId", connectionId)
            .put("receivedMessage", msgObj)
            .put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        ServerWebSocket webSocket = connections.get(connectionId);
        if (webSocket != null) {
            webSocket.writeTextMessage(responseMsg.encode());
        }
    }
    
    private void broadcastMessage(JsonObject message, String excludeConnectionId) {
        connections.entrySet().parallelStream()
            .filter(entry -> !entry.getKey().equals(excludeConnectionId))
            .forEach(entry -> {
                try {
                    entry.getValue().writeTextMessage(message.encode());
                } catch (Exception e) {
                    logger.warn("Failed to send message to connection {}", entry.getKey(), e);
                }
            });
    }
    
    private void sendErrorMessage(String connectionId, String errorMessage) {
        ServerWebSocket webSocket = connections.get(connectionId);
        if (webSocket != null) {
            JsonObject errorMsg = new JsonObject()
                .put("type", "error")
                .put("message", errorMessage)
                .put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            webSocket.writeTextMessage(errorMsg.encode());
        }
    }
    
    @Override
    public void stop(Promise<Void> stopPromise) {
        logger.info("Stopping WebSocket Server...");
        
        // Close all WebSocket connections
        connections.values().forEach(ws -> {
            try {
                ws.close();
            } catch (Exception e) {
                logger.warn("Error closing WebSocket connection", e);
            }
        });
        connections.clear();
        
        stopPromise.complete();
    }
}
