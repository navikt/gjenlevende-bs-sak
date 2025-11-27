#!/bin/bash

echo "Testing Unleash connection..."
echo "URL: http://localhost:4242/api/client/features"
echo "Token: *:*.unleash-insecure-api-token"
echo ""

response=$(curl -s -w "\nHTTP_CODE:%{http_code}" http://localhost:4242/api/client/features -H "Authorization: *:*.unleash-insecure-api-token")
http_code=$(echo "$response" | grep HTTP_CODE | cut -d':' -f2)
body=$(echo "$response" | grep -v HTTP_CODE)

if [ "$http_code" = "200" ]; then
    echo "✅ Unleash is responding correctly!"
    echo "Response: $body"
else
    echo "❌ Unleash connection failed with HTTP code: $http_code"
    echo "Response: $body"
fi
