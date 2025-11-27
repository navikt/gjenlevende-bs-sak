
#!/bin/bash

# Start-up script for gjenlevende-bs-sak lokal utvikling
# Dette scriptet starter alle services (Texas)

set -e

echo "Starter gjenlevende-bs-sak lokal utviklingsmiljø..."
echo ""

# Sjekk om vi er i riktig directory
if [ ! -f "docker-compose.yml" ]; then
    echo "Feil: docker-compose.yml ikke funnet. Kjør skriptet fra repo sin root."
    exit 1
fi

# Load inn miljøvariabler
if [ -f ".env.local" ]; then
    echo "Loader miljøvariabler fra .env.local..."
    source .env.local
    echo "Miljøvariabler loaded, BAM!"
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
echo "Starter Docker-services..."
docker-compose up -d

echo ""
echo "Venter på at services skal være ferdig..."

# Vent på Texas
echo -n "Venter på Texas (http://localhost:7575)..."
for i in {1..30}; do
    if curl -s http://localhost:7575/health > /dev/null 2>&1; then
        echo "Ferdig"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "Timeout (fortsetter uansett)"
    fi
    sleep 1
done

echo ""
echo "Startet alle servicer!"
echo ""
echo "Servicestatus:"
docker-compose ps
echo ""
echo "Tilgjengelige tjenester:"
echo "   • Texas:    http://localhost:7575"
echo ""
echo "Miljøvariabler er satt i din shell-session."
echo ""
echo "Neste steg:"
echo "   1. Åpne IntelliJ IDEA"
echo "   2. Gå til Run → Edit Configurations → ApplicationLocal"
echo "   3. Sørg for at miljøvariabler er hentet (bruk EnvFile-plugin eller kopier fra .env.local)"
echo "   4. Kjør ApplicationLocal"
echo ""
echo "For å stoppe alle tjenester, kjør: docker-compose down"
echo ""
