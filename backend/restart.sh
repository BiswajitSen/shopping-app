#!/bin/bash

# Restart script for Shopping App Backend
# This script stops any running backend and starts a fresh instance

echo "🔄 Restarting Shopping App Backend..."

# Find and kill any existing Java process running on port 8080
PORT=8080
PID=$(lsof -ti:$PORT)

if [ -n "$PID" ]; then
    echo "⏹️  Stopping existing process on port $PORT (PID: $PID)..."
    kill -9 $PID 2>/dev/null
    sleep 2
    echo "✅ Process stopped"
else
    echo "ℹ️  No existing process found on port $PORT"
fi

# Also kill any gradle daemon processes for this project
pkill -f "GradleDaemon.*shopping-app" 2>/dev/null

# Change to script directory (backend folder)
cd "$(dirname "$0")"

echo "🚀 Starting backend server..."
echo "   URL: http://localhost:$PORT"
echo "   Swagger: http://localhost:$PORT/swagger-ui.html"
echo ""
echo "Press Ctrl+C to stop the server"
echo "-----------------------------------"

# Start the application
./gradlew bootRun
