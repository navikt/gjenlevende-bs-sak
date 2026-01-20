#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/lib/startup-utils.sh"

print_header "Dev Miljø"

# Sjekk om vi er i riktig directory
if [ ! -f "$SCRIPT_DIR/docker-compose.yml" ]; then
    print_fail "docker-compose.yml ikke funnet. Kjør scriptet fra repo sin root."
    exit 1
fi

# Load inn miljøvariabler
if [ -f "$SCRIPT_DIR/.env.local" ]; then
    print_step "Loader miljøvariabler..."
    source "$SCRIPT_DIR/.env.local"
    print_ok "Miljøvariabler loaded fra .env.local"
else
    print_warn ".env.local finnes ikke. Kjør './hent-og-lagre-miljovariabler.sh' først."
    echo ""
    read -p "Vil du hente miljøvariabler nå? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        "$SCRIPT_DIR/hent-og-lagre-miljovariabler.sh"
        source "$SCRIPT_DIR/.env.local"
    else
        print_fail "Kan ikke starte uten miljøvariabler."
        exit 1
    fi
fi

# Idempotency check - skip if already running
if is_container_running "gjenlevende-bs-sak-postgres-1" && \
   is_port_responding "http://localhost:7575/health"; then
    print_success_box "Dev miljø kjører allerede!"
    exit 0
fi

print_step "Starter Docker services..."

docker compose --profile dev up -d > /dev/null 2>&1 &
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

# Texas
if wait_for_condition "curl -s http://localhost:7575/health > /dev/null 2>&1" "Texas 🤠..." 30; then
    print_ok "Texas 🤠"
else
    print_warn "Texas (timeout - fortsetter uansett)"
fi

print_success_box "Dev miljø er klart!"

echo -e "${DIM}Tilgjengelige tjenester:${NC}"
echo -e "${DIM}   - PostgreSQL: localhost:5432${NC}"
echo -e "${DIM}   - Texas:      http://localhost:7575${NC}"
echo ""
echo -e "${DIM}For å stoppe: docker compose --profile dev down${NC}"
echo -e "${DIM}For å slette data: docker compose --profile dev down -v${NC}"
