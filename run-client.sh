#!/bin/bash

# Vert.x WebSocket Client Startup Script

echo "💻 Starting Vert.x WebSocket Client..."
echo "======================================"

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

# Parse command line arguments
HOST=${1:-localhost}
PORT=${2:-8080}

echo "📋 Configuration:"
echo "   Host: $HOST"
echo "   Port: $PORT"
echo "   Endpoint: ws://$HOST:$PORT/websocket"
echo ""

# Compile the project if needed
if [ ! -d "target/classes" ]; then
    echo "🔨 Compiling project..."
    mvn clean compile -q
    
    if [ $? -ne 0 ]; then
        echo "❌ Compilation failed"
        exit 1
    fi
    echo "✅ Compilation successful"
fi

echo "🔌 Connecting to WebSocket server..."
echo "   Use 'help' command to see available options"
echo "   Use 'quit' command to exit"
echo "======================================"

mvn exec:java -Dexec.mainClass="com.example.websocket.WebSocketClient" -Dexec.args="$HOST $PORT" -q
