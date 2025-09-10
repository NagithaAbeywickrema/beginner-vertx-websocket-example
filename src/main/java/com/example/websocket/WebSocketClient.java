package com.example.websocket;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class WebSocketClient extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(WebSocketClient.class);
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 8080;
    private static final String WEBSOCKET_PATH = "/websocket";
    
    private WebSocket webSocket;
    private String connectionId;
    private final AtomicBoolean connected = new AtomicBoolean(false);
    private HttpClient httpClient;
    
    public static void main(String[] args) {
        String host = args.length > 0 ? args[0] : DEFAULT_HOST;
        int port = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PORT;
        
        Vertx vertx = Vertx.vertx();
        WebSocketClient client = new WebSocketClient();
        
        vertx.deployVerticle(client)
            .onSuccess(id -> {
                logger.info("WebSocket Client deployed successfully");
                client.connectToServer(host, port);
            })
            .onFailure(throwable -> {
                logger.error("Failed to deploy WebSocket Client", throwable);
                vertx.close();
            });
    }

    @Override
    public void start(Promise<Void> startPromise) {
        httpClient = vertx.createHttpClient();
        startPromise.complete();
    }
    
    public void connectToServer(String host, int port) {
        WebSocketConnectOptions options = new WebSocketConnectOptions()
            .setHost(host)
            .setPort(port)
            .setURI(WEBSOCKET_PATH);
        
        logger.info("Connecting to WebSocket server at ws://{}:{}{}", host, port, WEBSOCKET_PATH);
        
        httpClient.webSocket(options)
            .onSuccess(this::onWebSocketConnected)
            .onFailure(throwable -> {
                logger.error("Failed to connect to WebSocket server", throwable);
                scheduleReconnect(host, port);
            });
    }
    
    private void onWebSocketConnected(WebSocket ws) {
        this.webSocket = ws;
        connected.set(true);
        logger.info("Successfully connected to WebSocket server");
        
        // Handle incoming messages
        ws.textMessageHandler(this::handleIncomingMessage);
        
        // Handle connection close
        ws.closeHandler(v -> {
            logger.info("WebSocket connection closed");
            connected.set(false);
            // Auto-reconnect could be implemented here
        });
        
        // Handle exceptions
        ws.exceptionHandler(throwable -> {
            logger.error("WebSocket error", throwable);
            connected.set(false);
        });
        
        // Start interactive console
        startInteractiveConsole();
    }
    
    private void handleIncomingMessage(String message) {
        try {
            JsonObject msgObj = new JsonObject(message);
            String type = msgObj.getString("type", "unknown");
            
            switch (type) {
                case "welcome":
                    handleWelcomeMessage(msgObj);
                    break;
                case "pong":
                    handlePongMessage(msgObj);
                    break;
                case "broadcast":
                    handleBroadcastMessage(msgObj);
                    break;
                case "private":
                    handlePrivateMessage(msgObj);
                    break;
                case "echo":
                    handleEchoMessage(msgObj);
                    break;
                case "user_joined":
                    handleUserJoinedMessage(msgObj);
                    break;
                case "user_left":
                    handleUserLeftMessage(msgObj);
                    break;
                case "error":
                    handleErrorMessage(msgObj);
                    break;
                default:
                    logger.info("Received message [{}]: {}", type, message);
                    break;
            }
        } catch (Exception e) {
            logger.error("Error processing incoming message: {}", message, e);
        }
    }
    
    private void handleWelcomeMessage(JsonObject msgObj) {
        connectionId = msgObj.getString("connectionId");
        String timestamp = msgObj.getString("timestamp");
        String welcomeMsg = msgObj.getString("message");
        
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🎉 " + welcomeMsg);
        System.out.println("📋 Your Connection ID: " + connectionId);
        System.out.println("⏰ Connected at: " + timestamp);
        System.out.println("=".repeat(50));
        printCommands();
    }
    
    private void handlePongMessage(JsonObject msgObj) {
        String timestamp = msgObj.getString("timestamp");
        String originalTimestamp = msgObj.getString("originalTimestamp");
        System.out.println("🏓 Pong received at " + timestamp + " (original: " + originalTimestamp + ")");
    }
    
    private void handleBroadcastMessage(JsonObject msgObj) {
        String from = msgObj.getString("from");
        String message = msgObj.getString("message");
        String timestamp = msgObj.getString("timestamp");
        System.out.println("📢 [" + timestamp + "] Broadcast from " + from + ": " + message);
    }
    
    private void handlePrivateMessage(JsonObject msgObj) {
        String from = msgObj.getString("from");
        String message = msgObj.getString("message");
        String timestamp = msgObj.getString("timestamp");
        System.out.println("💬 [" + timestamp + "] Private message from " + from + ": " + message);
    }
    
    private void handleEchoMessage(JsonObject msgObj) {
        String originalMessage = msgObj.getString("originalMessage");
        String timestamp = msgObj.getString("timestamp");
        System.out.println("🔄 [" + timestamp + "] Echo: " + originalMessage);
    }
    
    private void handleUserJoinedMessage(JsonObject msgObj) {
        String joinedId = msgObj.getString("connectionId");
        int totalConnections = msgObj.getInteger("totalConnections");
        if (!joinedId.equals(connectionId)) {
            System.out.println("✅ User " + joinedId + " joined (Total: " + totalConnections + ")");
        }
    }
    
    private void handleUserLeftMessage(JsonObject msgObj) {
        String leftId = msgObj.getString("connectionId");
        int totalConnections = msgObj.getInteger("totalConnections");
        System.out.println("❌ User " + leftId + " left (Total: " + totalConnections + ")");
    }
    
    private void handleErrorMessage(JsonObject msgObj) {
        String errorMessage = msgObj.getString("message");
        String timestamp = msgObj.getString("timestamp");
        System.out.println("❗ [" + timestamp + "] Error: " + errorMessage);
    }
    
    private void startInteractiveConsole() {
        vertx.executeBlocking(promise -> {
            Scanner scanner = new Scanner(System.in);
            
            while (connected.get()) {
                System.out.print("\n> ");
                String input = scanner.nextLine().trim();
                
                if (input.isEmpty()) {
                    continue;
                }
                
                processUserInput(input);
            }
            
            promise.complete();
        }, false);
    }
    
    private void processUserInput(String input) {
        String[] parts = input.split("\\s+", 2);
        String command = parts[0].toLowerCase();
        
        switch (command) {
            case "help":
            case "commands":
                printCommands();
                break;
            case "ping":
                sendPing();
                break;
            case "broadcast":
            case "bc":
                if (parts.length > 1) {
                    sendBroadcast(parts[1]);
                } else {
                    System.out.println("Usage: broadcast <message>");
                }
                break;
            case "private":
            case "pm":
                if (parts.length > 1) {
                    String[] privateParts = parts[1].split("\\s+", 2);
                    if (privateParts.length == 2) {
                        sendPrivateMessage(privateParts[0], privateParts[1]);
                    } else {
                        System.out.println("Usage: private <connectionId> <message>");
                    }
                } else {
                    System.out.println("Usage: private <connectionId> <message>");
                }
                break;
            case "echo":
                if (parts.length > 1) {
                    sendEcho(parts[1]);
                } else {
                    System.out.println("Usage: echo <message>");
                }
                break;
            case "quit":
            case "exit":
                disconnect();
                break;
            case "status":
                showStatus();
                break;
            default:
                System.out.println("Unknown command. Type 'help' for available commands.");
                break;
        }
    }
    
    private void printCommands() {
        System.out.println("\n📋 Available Commands:");
        System.out.println("  help                     - Show this help message");
        System.out.println("  ping                     - Send ping to server");
        System.out.println("  broadcast <message>      - Broadcast message to all clients");
        System.out.println("  private <id> <message>   - Send private message to specific client");
        System.out.println("  echo <message>           - Echo message back from server");
        System.out.println("  status                   - Show connection status");
        System.out.println("  quit                     - Disconnect and exit");
        System.out.println();
    }
    
    private void sendPing() {
        if (!connected.get()) {
            System.out.println("❌ Not connected to server");
            return;
        }
        
        JsonObject pingMsg = new JsonObject()
            .put("type", "ping")
            .put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        webSocket.writeTextMessage(pingMsg.encode());
        System.out.println("🏓 Ping sent");
    }
    
    private void sendBroadcast(String message) {
        if (!connected.get()) {
            System.out.println("❌ Not connected to server");
            return;
        }
        
        JsonObject broadcastMsg = new JsonObject()
            .put("type", "broadcast")
            .put("message", message)
            .put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        webSocket.writeTextMessage(broadcastMsg.encode());
        System.out.println("📢 Broadcast sent: " + message);
    }
    
    private void sendPrivateMessage(String targetId, String message) {
        if (!connected.get()) {
            System.out.println("❌ Not connected to server");
            return;
        }
        
        JsonObject privateMsg = new JsonObject()
            .put("type", "private")
            .put("targetId", targetId)
            .put("message", message)
            .put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        webSocket.writeTextMessage(privateMsg.encode());
        System.out.println("💬 Private message sent to " + targetId + ": " + message);
    }
    
    private void sendEcho(String message) {
        if (!connected.get()) {
            System.out.println("❌ Not connected to server");
            return;
        }
        
        JsonObject echoMsg = new JsonObject()
            .put("type", "echo")
            .put("message", message)
            .put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        webSocket.writeTextMessage(echoMsg.encode());
        System.out.println("🔄 Echo sent: " + message);
    }
    
    private void showStatus() {
        System.out.println("\n📊 Connection Status:");
        System.out.println("  Connected: " + (connected.get() ? "✅ Yes" : "❌ No"));
        System.out.println("  Connection ID: " + (connectionId != null ? connectionId : "N/A"));
        System.out.println("  WebSocket: " + (webSocket != null ? "Active" : "Inactive"));
    }
    
    private void disconnect() {
        if (webSocket != null && connected.get()) {
            webSocket.close();
        }
        connected.set(false);
        System.out.println("👋 Disconnected from server");
        vertx.close();
        System.exit(0);
    }
    
    private void scheduleReconnect(String host, int port) {
        vertx.setTimer(5000, id -> {
            logger.info("Attempting to reconnect...");
            connectToServer(host, port);
        });
    }
    
    @Override
    public void stop(Promise<Void> stopPromise) {
        logger.info("Stopping WebSocket Client...");
        
        if (webSocket != null && connected.get()) {
            webSocket.close();
        }
        
        if (httpClient != null) {
            httpClient.close();
        }
        
        stopPromise.complete();
    }
}
