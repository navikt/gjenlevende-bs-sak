# Gjenlevende-BS-Sak

Saksbehandler app som tar for seg barnetilsyn og skolepenger for etterlatte/gjenlevende.

### Quick start - kjøre lokalt

For å starte alle tjenester med en kommando:

1. Sørg for at du er pålogget Nais og Google CLI (nais login)


2. Kjør start-scriptet:
   ```bash
   ./start-local.sh
   ```
   Dette vil:
    - Hente miljøvariabler fra dev-gcp (hvis ikke allerede gjort)
    - Starte Texas (token exchange service)
    - Starte nødvendige databaser


3. Konfigurer miljøvariabler i IntelliJ:
    - Gå til **Run** → **Edit Configurations**
    - Velg **ApplicationLocal**
    - **Anbefalt:** Installer **EnvFile** plugin og pek til `.env.local`
    - **Alternativt:** Kopier miljøvariabler manuelt fra `.env.local`


4. Kjør applikasjonen med `ApplicationLocal`

For å stoppe alle services:
```bash
docker-compose down
```

### Autentisering lokalt mot pre-prod (manuell oppsett)

For å kjøre applikasjonen lokalt med autentisering mot pre-prod, må du først hente secrets fra dev-gcp:

1. Sørg for at du er pålogget Nais og Google CLI (nais login)


2. Kjør scriptet for å hente miljøvariabler:
   ```bash
   ./hent-og-lagre-miljovariabler.sh
   ```
3. Last inn miljøvariablene:
   ```bash
   source .env.local
   ```
4. Start Texas (token exchange service) med Docker:
   ```bash
   docker-compose up -d texas
   ```
5. Konfigurer miljøvariabler i IntelliJ:
    - Gå til **Run** → **Edit Configurations**
    - Velg **ApplicationLocal**
    - I **Environment variables** feltet, legg til:
        - `AZURE_APP_CLIENT_ID` (fra `.env.local`)
        - `AZURE_APP_CLIENT_SECRET` (fra `.env.local`)
        - `AZURE_APP_TENANT_ID` (fra `.env.local`)
    - Alternativt: Installer **EnvFile** plugin og pek til `.env.local`


6. Kjør applikasjonen med `ApplicationLocal`

### Kjøring med in-memory-database
For å kjøre opp appen lokalt, kan en kjøre `ApplicationLocal`.

Appen starter da opp med en testcontainer postgres-database og er tilgjengelig under `localhost:8080`.

## Swagger
Du når Swagger ved å gå til:

Ingress:
- https://gjenlevende-bs-sak.intern.dev.nav.no/swagger-ui/index.html

Lokalt:
- http://localhost:8080/swagger-ui.html
- http://localhost:8080/swagger-ui/index.html

Husk Nais device!

