#!/bin/bash

# Script to build frontend and prepare for ngrok hosting
# This allows hosting both frontend and backend via a single ngrok tunnel

set -e

echo "🚀 Building for ngrok deployment..."

# Navigate to project root
cd "$(dirname "$0")"

# Step 1: Build frontend with correct API URL (will be same origin)
echo "📦 Building frontend..."
cd frontend

# Create production env file pointing to same origin (no need for separate API URL)
echo 'VITE_API_URL=' > .env.production

# Build frontend
source ~/.nvm/nvm.sh 2>/dev/null || true
nvm use 20 2>/dev/null || true
npm run build

# Step 2: Copy built files to backend static folder
echo "📋 Copying frontend build to backend..."
rm -rf ../backend/src/main/resources/static/*
cp -r dist/* ../backend/src/main/resources/static/

cd ..

# Step 3: Configure Spring Boot to handle SPA routing
echo "⚙️  Frontend copied to backend/src/main/resources/static/"

echo ""
echo "✅ Build complete!"
echo ""
echo "📌 Next steps:"
echo "   1. Start the backend:  cd backend && ./gradlew bootRun"
echo "   2. Start ngrok:        ngrok http 8080"
echo "   3. Use the ngrok URL to access your app from any device!"
echo ""
echo "🔗 The ngrok URL will serve both the frontend UI and backend API."
