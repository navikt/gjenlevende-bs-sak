# gjenlevende-bs-sak

Saksbehandler app som tar for seg barnetilsyn og skolepenger for etterlatte/gjenlevende.

## Kjøring lokalt

### Quick Start (anbefalt)

For å starte alle tjenester med ett kommando:

1. Sørg for at du er pålogget Naisdevice og Google
2. Kjør oppstartscriptet:
   ```bash
   ./start-local.sh
   ```
   Dette vil:
   - Hente miljøvariabler fra dev-gcp (hvis ikke allerede gjort)
   - Starte Texas (token exchange service)
   - Starte Unleash (feature toggle service)
   - Starte nødvendige databaser

3. Konfigurer miljøvariabler i IntelliJ:
   - Gå til **Run** → **Edit Configurations**
   - Velg **ApplicationLocal**
   - **Anbefalt:** Installer **EnvFile** plugin og pek til `.env.local`
   - **Alternativt:** Kopier miljøvariabler manuelt fra `.env.local`

4. Kjør applikasjonen med `ApplicationLocal`

For å stoppe alle tjenester:
```bash
docker-compose down
```

### Autentisering lokalt mot pre-prod (manuell oppsett)

For å kjøre applikasjonen lokalt med autentisering mot pre-prod, må du først hente secrets fra dev-gcp:

1. Sørg for at du er pålogget Naisdevice og Google
2. Kjør scriptet for å hente miljøvariabler:
   ```bash
   ./hent-og-lagre-miljovariabler.sh
   ```
3. Last inn miljøvariablene i din shell:
   ```bash
   source .env.local
   ```
4. Start Texas (token exchange service) med Docker:
   ```bash
   docker-compose up -d texas
   ```

   Valgfritt: Start Unleash (feature toggle service) for full lokal utvikling:
   ```bash
   docker-compose up -d unleash unleash-db
   ```
   **Merk:** Applikasjonen vil logge advarsler om Unleash-tilkobling hvis den ikke kjører, men vil fortsatt fungere.
5. Konfigurer miljøvariabler i IntelliJ:
   - Gå til **Run** → **Edit Configurations**
   - Velg **ApplicationLocal**
   - I **Environment variables** feltet, legg til:
     - `AZURE_APP_CLIENT_ID` (fra `.env.local`)
     - `AZURE_APP_CLIENT_SECRET` (fra `.env.local`)
     - `AZURE_APP_TENANT_ID` (fra `.env.local`)
   - Alternativt: Installer **EnvFile** plugin og pek til `.env.local`
6. Kjør applikasjonen med `ApplicationLocal`

#### Client id & client secret
Secrets kan hentes fra cluster med:
```bash
kubectl -n etterlatte get secret azuread-gjenlevende-bs-sak-lokal -o json | jq '.data | map_values(@base64d)'
```

Variablene som settes opp:
* `AZURE_APP_CLIENT_ID` (fra secret)
* `AZURE_APP_CLIENT_SECRET` (fra secret)
* `AZURE_APP_TENANT_ID` (fra secret)
* `AZURE_OPENID_CONFIG_ISSUER`
* Scopes for eksterne tjenester

### Kjøring med in-memory-database
For å kjøre opp appen lokalt, kan en kjøre `ApplicationLocal`.

Appen starter da opp med en testcontainer postgres-database og er tilgjengelig under `localhost:8080`.

**Docker Desktop:** Testcontainers vil automatisk grupperes under `gjenlevende-bs-sak` i Docker Desktop for bedre organisering.

## Tester

Integrasjonstester bruker Testcontainers for å starte opp nødvendige databaser. Disse vil automatisk vises under `gjenlevende-bs-sak` i Docker Desktop.

## Swagger
Du når Swagger lokalt ved å gå til:
- http://localhost:8080/swagger-ui.html
- Or: http://localhost:8080/swagger-ui/index.html

## Roller
Applikasjonen bruker følgende Azure AD grupper:
- `8df38a8c-6b34-49d7-b837-cefb153a03e8` - 0000-CA-Gjenlevende-BS-Saksbehandler
- `f9837eec-8d85-4f61-b89e-677e168fdf2f` - 0000-CA-Gjenlevende-BS-Beslutter
- `8c98e41f-4370-46e6-998a-2190c7f935bc` - 0000-CA-Gjenlevende-BS-Veileder
