#!/bin/bash

# Vert.x WebSocket Server Startup Script

echo "🚀 Starting Vert.x WebSocket Server..."
echo "=================================="

# Create logs directory if it doesn't exist
mkdir -p logs

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed or not in PATH"
    echo "Please install Maven 3.6+ and try again"
    exit 1
fi

# Check Java version
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed or not in PATH"
    echo "Please install Java 11+ and try again"
    exit 1
fi

echo "📋 Environment Check:"
echo "   Maven: $(mvn -version | head -1)"
echo "   Java: $(java -version 2>&1 | head -1)"
echo ""

# Compile the project
echo "🔨 Compiling project..."
mvn clean compile -q

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed"
    exit 1
fi

echo "✅ Compilation successful"
echo ""

# Start the server
echo "🌐 Starting WebSocket Server on port 8080..."
echo "   WebSocket endpoint: ws://localhost:8080/websocket"
echo "   Health check: http://localhost:8080/health"
echo "   Web client: http://localhost:8080/static/index.html"
echo ""
echo "Press Ctrl+C to stop the server"
echo "=================================="

mvn exec:java -Dexec.mainClass="com.example.websocket.WebSocketServer" -q
