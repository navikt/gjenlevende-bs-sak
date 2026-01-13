# Gjenlevende-BS-Sak

Saksbehandler app som tar for seg barnetilsyn og skolepenger for etterlatte/gjenlevende.

## Lokal kjøring

Applikasjonen har to lokale utviklingsprofiler:

| Profil | Bruk | Fordeler |
|--------|------|----------|
| **Mock** (anbefalt) | Daglig utvikling | Ingen secrets, fullt offline, rask oppstart |
| **Dev** | Testing mot ekte dev-tjenester | Ekte data fra PDL, SAF, etc. |

---

### Mock-profil (Anbefalt for daglig utvikling)

Denne profilen krever **ingen secrets** og fungerer fullt offline.

#### 1. Start mock miljøet
```bash
./start-mock.sh
```
Dette starter:
- PostgreSQL (persistent database)
- mock-oauth2-server (for token validering)
- WireMock (mocker alle eksterne tjenester)

#### 2. Kjør applikasjonen
Kjør **ApplicationLocalMock** fra IntelliJ (ingen miljøvariabel konfigurasjon nødvendig).

#### 3. Test med mock token
```bash
# Hent token
TOKEN=$(curl -s -X POST http://localhost:8089/default/token \
  -d 'grant_type=client_credentials&client_id=test&client_secret=test' | jq -r '.access_token')

# Test API
curl -H "Authorization: Bearer $TOKEN" http://localhost:8082/internal/health
```

#### 4. Stopp tjenestene
```bash
docker compose --profile mock down      # Behold data
docker compose --profile mock down -v   # Slett data
```

---

### Dev profil (For testing mot real tjenester)

Bruk denne kun når du må teste mot ekte dev-tjenester (PDL, SAF, Tilgangsmaskin, etc.).

#### 1. Logg på Nais
```bash
nais login
```

#### Husk Nais device!

#### 2. Hent miljøvariabler

**Skift kontekst til DEV, svært viktig.**

```bash
kubectl config use-context dev-gcp
```

Endre namespace til Etterlatte.

```bash
kubectl config set-context --current --namespace=etterlatte
```

Se etter azure hemmelighet navn, typisk azure-gjenlevende-bs-sak-noe

```bash
kubectl get secrets | grep gjenlevende-bs-sak
```

Oppdater hent-og-lagre-miljøvaribler.sh med secret instans navn

```
get_secrets azure-gjenlevende-bs-sak-noe
```

Kjør hent-og-lagre-miljøvaribler:

```bash
./hent-og-lagre-miljøvariabler.sh
```

Dette oppretter en `.env.local` fil.

#### 3. Start dev miljøet
```bash
./start-dev.sh
```

#### 4. Konfigurer IntelliJ
1. Gå til **ApplicationLocalDev** profilen
2. Klikk **Edit Configuration**
3. Under **Environment variables**, legg til `.env.local` filen
4. Kjør **ApplicationLocalDev**

#### 5. Stoppe appen
```bash
docker compose --profile dev down       # Behold data
docker compose --profile dev down -v    # Slett data
```

---

## Database

Begge profiler bruker en **persistent PostgreSQL** database via Docker volume.
- Data overlever app restart
- Slett data: `docker compose --profile <mock|dev> down -v`
- Se data i Docker Desktop under "gjenlevende-bs-sak" gruppen

---

## Swagger
Du når Swagger ved å gå til:

**Ingress:**
- https://gjenlevende-bs-sak.intern.dev.nav.no/swagger-ui/index.html

**Lokalt (dev-profil):**
- http://localhost:8082/swagger-ui/index.html
