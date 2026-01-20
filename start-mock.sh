#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib/startup-utils.sh"

print_header "Mock Miljø"

# Idempotency check - skip if already running
if is_container_running "gjenlevende-bs-sak-postgres-1" && \
   is_port_responding "http://localhost:8089/default/.well-known/openid-configuration" && \
   is_port_responding "http://localhost:8090/__admin/mappings"; then
    print_success_box "Mock miljø kjører allerede!"
    exit 0
fi

print_step "Starter Docker services..."

docker compose --profile mock up -d > /dev/null 2>&1 &
spin $! "Starter Docker containers..."
wait $!
print_ok "Docker containers startet"

# PostgreSQL
if wait_for_condition "docker exec gjenlevende-bs-sak-postgres-1 pg_isready -U postgres > /dev/null 2>&1" "PostgreSQL..." 30; then
    print_ok "PostgreSQL"
else
    print_fail "PostgreSQL (timeout)"
    exit 1
fi

# mock-oauth2-server
if wait_for_condition "curl -s http://localhost:8089/default/.well-known/openid-configuration > /dev/null 2>&1" "mock-oauth2-server..." 30; then
    print_ok "mock-oauth2-server"
else
    print_fail "mock-oauth2-server (timeout)"
    exit 1
fi

# WireMock
if wait_for_condition "curl -s http://localhost:8090/__admin/mappings > /dev/null 2>&1" "WireMock..." 30; then
    print_ok "WireMock"
else
    print_fail "WireMock (timeout)"
    exit 1
fi

print_success_box "Mock miljø er klart!"

echo "Kjør ApplicationLocalMock i IntelliJ"
echo ""
echo -e "${DIM}For å teste med mock-token:${NC}"
echo -e "${DIM}TOKEN=\$(curl -s -X POST http://localhost:8089/default/token -d 'grant_type=client_credentials&client_id=test&client_secret=test' | jq -r '.access_token')${NC}"
echo -e "${DIM}curl -H \"Authorization: Bearer \$TOKEN\" http://localhost:8082/internal/health${NC}"
echo ""
echo -e "${DIM}For å stoppe: docker compose --profile mock down${NC}"
echo -e "${DIM}For å slette data: docker compose --profile mock down -v${NC}"
