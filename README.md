# Basic Vert.x WebSocket Server and Client for Beginners

A comprehensive WebSocket implementation using Eclipse Vert.x framework, featuring both server and client applications with real-time messaging capabilities.

## 🚀 Features

### Server Features
- **Multi-client support** - Handle multiple concurrent WebSocket connections
- **Message broadcasting** - Send messages to all connected clients
- **Private messaging** - Send direct messages between specific clients
- **Connection management** - Track and manage client connections
- **Health monitoring** - REST endpoint for server health checks
- **Comprehensive logging** - Structured logging with file rotation
- **Auto-reconnection support** - Built-in support for client reconnection
- **JSON-based messaging** - Structured message format for easy parsing

### Client Features
- **Interactive console** - Command-line interface for real-time interaction
- **Web client** - Browser-based client for testing
- **Multiple message types** - Support for ping/pong, broadcast, private, and echo messages
- **Connection status tracking** - Real-time connection status monitoring
- **Auto-reconnection** - Automatic reconnection on connection loss
- **User-friendly interface** - Clear command structure and feedback

## 📋 Prerequisites

- **Java 11** or higher
- **Maven 3.6** or higher
- **Internet connection** (for downloading dependencies)

## 🛠️ Installation & Setup

### 1. Clone or Download the Project
```bash
# If using git
git clone https://github.com/NagithaAbeywickrema/beginner-vertx-websocket-example.git
cd beginner-vertx-websocket-example

# Or extract the downloaded archive
cd beginner-vertx-websocket-example
```

### 2. Build the Project
```bash
mvn clean compile
```

### 3. Create Logs Directory
```bash
mkdir logs
```

## 🎯 Usage

### Quick Start with Bash Scripts

The easiest way to get started is using the provided bash scripts:

#### Starting the Server
```bash
# Make script executable (first time only)
chmod +x run-server.sh

# Start the server
./run-server.sh
```

The script will:
- ✅ Check for Maven and Java installation
- 🔨 Compile the project automatically
- 📁 Create logs directory
- 🚀 Start the WebSocket server on port 8080

#### Starting the Java Client
```bash
# Make script executable (first time only)
chmod +x run-client.sh

# Start the client (default: localhost:8080)
./run-client.sh

# Or connect to a custom host/port
./run-client.sh myserver.com 9090
```

The script will:
- ✅ Check for Maven and Java installation
- 🔨 Compile the project if needed
- 🔌 Connect to the WebSocket server
- 💬 Provide an interactive command interface

### Manual Usage Options

#### Starting the Server

##### Option 1: Using Maven
```bash
mvn exec:java@run-server
```

##### Option 2: Using Maven with explicit main class
```bash
mvn exec:java -Dexec.mainClass="com.example.websocket.WebSocketServer"
```

##### Option 3: Building and Running JAR
```bash
mvn clean package
java -jar target/vertx-websocket-1.0.0.jar
```

The server will start on `http://localhost:8080` with WebSocket endpoint at `ws://localhost:8080/websocket`.

#### Using the Java Client

##### Option 1: Using Maven
```bash
mvn exec:java@run-client
```

##### Option 2: Using Maven with explicit main class
```bash
mvn exec:java -Dexec.mainClass="com.example.websocket.WebSocketClient"
```

##### Option 3: With custom host and port
```bash
mvn exec:java -Dexec.mainClass="com.example.websocket.WebSocketClient" -Dexec.args="localhost 8080"
```

### Using the Web Client

1. Start the server (using `./run-server.sh` or manual method)
2. Open your browser and navigate to: `http://localhost:8080/static/index.html`
3. Click "Connect" to establish WebSocket connection
4. Use the interface to send different types of messages

## 💬 Message Types and Commands

### Java Client Commands

| Command | Syntax | Description |
|---------|--------|-------------|
| `help` | `help` | Show available commands |
| `ping` | `ping` | Send ping to server (receives pong) |
| `broadcast` | `broadcast <message>` | Send message to all clients |
| `private` | `private <connectionId> <message>` | Send private message to specific client |
| `echo` | `echo <message>` | Echo message back from server |
| `status` | `status` | Show connection status |
| `quit` | `quit` | Disconnect and exit |

### Message Format

All messages use JSON format:

```json
{
  "type": "message_type",
  "message": "content",
  "timestamp": "2024-01-15T10:30:00.123",
  "connectionId": "uuid-string"
}
```

### Supported Message Types

#### Client to Server:
- `ping` - Health check request
- `broadcast` - Message to all clients
- `private` - Direct message to specific client
- `echo` - Echo request

#### Server to Client:
- `welcome` - Connection established
- `pong` - Ping response
- `broadcast` - Broadcast message from another client
- `private` - Private message from another client
- `echo` - Echo response
- `user_joined` - New client connected
- `user_left` - Client disconnected
- `error` - Error message

## 🔧 Configuration

### Server Configuration

You can modify server settings in `WebSocketServer.java`:

```java
private static final int PORT = 8080;
private static final String WEBSOCKET_PATH = "/websocket";
```

### Logging Configuration

Logging is configured in `src/main/resources/logback.xml`:
- Console output with timestamp and level
- File logging with rotation (10MB per file, 30 days retention)
- Separate log levels for application and framework components

### Client Configuration

Default connection settings in `WebSocketClient.java`:

```java
private static final String DEFAULT_HOST = "localhost";
private static final int DEFAULT_PORT = 8080;
```

## 📁 Project Structure

```
beginner-vertx-websocket-example/
├── pom.xml                                    # Maven configuration
├── README.md                                  # This file
├── logs/                                      # Log files (created at runtime)
└── src/
    └── main/
        ├── java/com/example/websocket/
        │   ├── WebSocketServer.java           # Server implementation
        │   └── WebSocketClient.java           # Java client implementation
        └── resources/
            ├── logback.xml                    # Logging configuration
            └── static/
                └── index.html                 # Web client interface
```

## 🚀 Quick Deployment & Testing Guide

### Step-by-Step Deployment

#### 1. One-Command Server Setup
```bash
# Clone the project and start server in one go
git clone https://github.com/NagithaAbeywickrema/beginner-vertx-websocket-example.git && cd beginner-vertx-websocket-example
chmod +x *.sh
./run-server.sh
```

This single command will:
- ✅ Automatically check prerequisites (Java 11+, Maven 3.6+)
- 🔨 Compile the entire project
- 📁 Create necessary directories
- 🚀 Start the WebSocket server
- 🌐 Make the server available at `http://localhost:8080`

#### 2. Testing with Multiple Clients

**Terminal 1 (Server):**
```bash
./run-server.sh
```

**Terminal 2 (Java Client 1):**
```bash
./run-client.sh
```

**Terminal 3 (Java Client 2):**
```bash
./run-client.sh
```

**Browser (Web Client):**
```
Open: http://localhost:8080/static/index.html
```

### 🌐 Browser-Based Testing

#### Quick Browser Test
1. **Start Server:**
   ```bash
   ./run-server.sh
   ```

2. **Open Web Client:**
   - Navigate to: `http://localhost:8080/static/index.html`
   - Click "Connect" button
   - Server status should show "Connected"

3. **Test Basic Functionality:**
   ```
   🔍 Connection Status: Connected ✅
   📤 Send ping → Receive pong
   📢 Send broadcast → Message appears
   🔄 Send echo → Message echoes back
   ```

#### Multi-Browser Testing
```bash
# Open multiple browser tabs/windows:
# Tab 1: http://localhost:8080/static/index.html
# Tab 2: http://localhost:8080/static/index.html
# Tab 3: http://localhost:8080/static/index.html

# Connect all tabs and test cross-client messaging
```

### 🧪 Comprehensive Testing Scenarios

#### Scenario 1: Full Stack Test (5 minutes)
```bash
# Terminal 1: Start server
./run-server.sh

# Terminal 2: Start Java client
./run-client.sh
# Type: broadcast Hello from Java client!

# Browser: Open http://localhost:8080/static/index.html
# Connect and send: Hello from browser!

# Result: Both clients should see each other's messages
```

#### Scenario 2: Connection Resilience Test
```bash
# 1. Start server and connect clients
./run-server.sh                    # Terminal 1
./run-client.sh                    # Terminal 2

# 2. Stop server (Ctrl+C in Terminal 1)
# 3. Restart server
./run-server.sh                    # Terminal 1

# 4. Observe client auto-reconnection in Terminal 2
```

#### Scenario 3: Mixed Client Testing
```bash
# Start server
./run-server.sh

# Connect different client types:
./run-client.sh                              # Java client 1
./run-client.sh localhost 8080               # Java client 2
# Browser: http://localhost:8080/static/index.html  # Web client 1
# Browser: http://localhost:8080/static/index.html  # Web client 2

# Test private messaging between specific clients
# In Java client: private <connectionId> Hello specific client!
```

### 📊 Health Monitoring During Tests

#### Real-time Server Monitoring
```bash
# Terminal 1: Server
./run-server.sh

# Terminal 2: Health checks
curl http://localhost:8080/health

# Terminal 3: Log monitoring
tail -f logs/websocket.log
```

#### Expected Health Check Response:
```json
{
  "status": "UP",
  "timestamp": "2024-01-15T10:30:00.123",
  "activeConnections": 3
}
```

### 🔧 Troubleshooting Deployment Issues

#### Quick Fixes for Common Issues

**Port Already in Use:**
```bash
# Check what's using port 8080
lsof -i :8080

# Kill the process or use different port
./run-server.sh
# Edit WebSocketServer.java to change port if needed
```

**Permission Denied:**
```bash
# Make scripts executable
chmod +x run-server.sh run-client.sh

# Or run with bash directly
bash run-server.sh
bash run-client.sh
```

**Browser Connection Issues:**
```bash
# Verify server is running
curl http://localhost:8080/health

# Check WebSocket endpoint
curl -i -N -H "Connection: Upgrade" -H "Upgrade: websocket" http://localhost:8080/websocket
```

### 🎯 Production Deployment Checklist

#### Before Going Live:
- [ ] Test with `./run-server.sh` and `./run-client.sh`
- [ ] Verify browser client works at `http://localhost:8080/static/index.html`
- [ ] Test multi-client scenarios
- [ ] Check health endpoint: `curl http://localhost:8080/health`
- [ ] Monitor logs: `tail -f logs/websocket.log`
- [ ] Test connection resilience (stop/start server)
- [ ] Verify all message types work (ping, broadcast, private, echo)

#### Deployment Commands:
```bash
# Production build
mvn clean package

# Start with custom configuration
java -Dserver.port=8080 -jar target/vertx-websocket-1.0.0.jar

# Or use the bash script for development
./run-server.sh
```

## 🧪 Testing Scenarios

### 1. Single Client Testing
1. Start the server using `./run-server.sh`
2. Connect one client using `./run-client.sh`
3. Test all message types (ping, echo, broadcast)

### 2. Multi-Client Testing
1. Start the server using `./run-server.sh`
2. Connect multiple clients (Java and/or web clients)
3. Test broadcasting between clients
4. Test private messaging between specific clients
5. Observe user join/leave notifications

### 3. Connection Resilience
1. Start server and client using bash scripts
2. Stop the server (Ctrl+C)
3. Restart the server using `./run-server.sh`
4. Observe client reconnection behavior

### 4. Web Client Testing
1. Open multiple browser tabs with the web client
2. Connect all tabs
3. Test messaging between browser clients
4. Test mixed messaging (browser ↔ Java client)

## 🔍 Monitoring and Health Checks

### Health Check Endpoint
```bash
curl http://localhost:8080/health
```

Response:
```json
{
  "status": "UP",
  "timestamp": "2024-01-15T10:30:00.123",
  "activeConnections": 3
}
```

### Log Monitoring
```bash
# Follow live logs
tail -f logs/websocket.log

# View specific connection events
grep "WebSocket connection" logs/websocket.log
```

## 🚨 Troubleshooting

### Common Issues

#### 1. Port Already in Use
```
Error: Address already in use
```
**Solution:** Change the port in `WebSocketServer.java` or stop the conflicting process.

#### 2. Connection Refused
```
Error: Connection refused
```
**Solution:** Ensure the server is running before starting clients.

#### 3. Maven Dependencies
```
Error: Could not resolve dependencies
```
**Solution:** Check internet connection and run `mvn clean install`.

#### 4. Java Version Issues
```
Error: Unsupported class file major version
```
**Solution:** Ensure Java 11 or higher is installed and configured.

### Debug Mode

Enable debug logging by modifying `logback.xml`:
```xml
<logger name="com.example.websocket" level="DEBUG" />
```

## 🔮 Advanced Usage

### Custom Message Handlers

Extend the server to handle custom message types:

```java
private void handleCustomMessage(String connectionId, JsonObject msgObj) {
    // Your custom logic here
    String customData = msgObj.getString("customData");
    // Process and respond
}
```

### Authentication Integration

Add authentication by modifying the WebSocket handler:

```java
private void handleWebSocket(ServerWebSocket webSocket) {
    String token = webSocket.headers().get("Authorization");
    if (!isValidToken(token)) {
        webSocket.reject();
        return;
    }
    // Continue with connection handling
}
```

### Scaling Considerations

For production deployment:
- Use Vert.x clustering for horizontal scaling
- Implement Redis for shared session storage
- Add rate limiting for message sending
- Use SSL/TLS for secure connections

## 📝 License

This project is provided as-is for educational and development purposes.

## 🤝 Contributing

Feel free to submit issues, feature requests, or pull requests to improve this WebSocket implementation.

---

**Happy WebSocket coding! 🚀**
