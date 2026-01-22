#!/bin/bash

# Start All Services Script for Shopping App
# This script starts both backend and frontend in separate terminals

echo "🚀 Starting Shopping App..."
echo ""

# Check if MongoDB is running
if ! docker ps | grep -q mongo; then
    echo "⚠️  MongoDB doesn't appear to be running"
    echo "   Start it with: docker start mongodb-test"
    echo "   Or create new: docker run -d -p 27017:27017 --name mongodb-test mongo:latest"
    echo ""
fi

# Get the project root directory
PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"

# Start Backend in new terminal
echo "📦 Starting Backend (port 8080)..."
osascript -e "tell app \"Terminal\" to do script \"cd '$PROJECT_ROOT/backend' && ./restart.sh\""

# Wait a moment for backend to start
sleep 3

# Start Frontend in new terminal
echo "🎨 Starting Frontend (port 3000)..."
osascript -e "tell app \"Terminal\" to do script \"cd '$PROJECT_ROOT/frontend' && source ~/.nvm/nvm.sh && nvm use 20 && npm run dev\""

echo ""
echo "✅ Services starting..."
echo ""
echo "   Backend:  http://localhost:8080"
echo "   Frontend: http://localhost:3000"
echo "   Swagger:  http://localhost:8080/swagger-ui.html"
echo ""
echo "Check the new terminal windows for server output."
