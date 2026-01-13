#!/bin/bash

# Start-up script for gjenlevende-bs-sak dev profil med real stuff

set -e

echo "Starter gjenlevende-bs-sak dev miljø..."
echo ""

# Sjekk om vi er i riktig directory
if [ ! -f "docker-compose.yml" ]; then
    echo "Feil: docker-compose.yml ikke funnet. Kjør scriptet fra repo sin root."
    exit 1
fi

# Load inn miljøvariabler
if [ -f ".env.local" ]; then
    echo "Loader miljøvariabler fra .env.local..."
    source .env.local
    echo "Miljøvariabler loaded!"
else
    echo "OBS: .env.local finnes ikke. Kjør './hent-og-lagre-miljovariabler.sh' først."
    echo ""
    read -p "Vil du hente miljøvariabler nå? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        ./hent-og-lagre-miljovariabler.sh
        source .env.local
    else
        echo "Kan ikke starte uten miljøvariabler."
        exit 1
    fi
fi

echo ""
echo "Starter Docker services med dev profil..."
docker compose --profile dev up -d

echo ""
echo "Venter på at services skal bli ferdig..."

echo -n "Venter på PostgreSQL..."
until docker exec gjenlevende-bs-sak-postgres-1 pg_isready -U postgres > /dev/null 2>&1; do
    sleep 1
done
echo " Ferdig!"

echo -n "Venter på Texas 🤠 (http://localhost:7575)..."
for i in {1..30}; do
    if curl -s http://localhost:7575/health > /dev/null 2>&1; then
        echo " Ferdig!"
        break
    fi
    if [ $i -eq 30 ]; then
        echo " Timeout (fortsetter uansett)"
    fi
    sleep 1
done

echo ""
echo "Dev miljø er oppe!"
echo ""
echo "Servicestatus:"
echo ""
docker compose --profile dev ps
echo ""
echo "Tilgjengelige tjenester:"
echo "   - PostgreSQL: localhost:5432"
echo "   - Texas:      http://localhost:7575"
echo ""
echo "Neste steg:"
echo "   1. Åpne IntelliJ IDEA"
echo "   2. Gå til Run -> Edit Configurations -> ApplicationLocalDev"
echo "   3. Legg til .env.local som environment file"
echo "   4. Kjør ApplicationLocalDev"
echo ""
echo "For å stoppe: docker compose --profile dev down"
echo "For å slette data: docker compose --profile dev down -v"
echo ""
