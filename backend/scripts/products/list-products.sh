#!/bin/bash

# List approved products (public endpoint)
# Usage: ./list-products.sh [page] [size] [search]

BASE_URL=${BASE_URL:-"http://localhost:8080"}

PAGE=${1:-"0"}
SIZE=${2:-"10"}
SEARCH=${3:-""}

echo "📦 Listing approved products"
echo "📊 Page: $PAGE, Size: $SIZE"
if [ -n "$SEARCH" ]; then
    echo "🔍 Search: $SEARCH"
fi
echo ""

if [ -n "$SEARCH" ]; then
    curl -s -X GET "$BASE_URL/api/products/search?keyword=$SEARCH&page=$PAGE&size=$SIZE" | jq .
else
    curl -s -X GET "$BASE_URL/api/products?page=$PAGE&size=$SIZE" | jq .
fi