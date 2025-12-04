# Gjenlevende-BS-Sak

Saksbehandler app som tar for seg barnetilsyn og skolepenger for etterlatte/gjenlevende.

### Lokal kjøring

Følg disse stegene for å kjøre applikasjonen lokalt:

#### 1. Logg inn på Nais
```bash
nais login
```
Følg instruksjonene for å logge inn.

#### Husk Nais device!

#### 2. Hent miljøvariabler
Kjør scriptet for å hente miljøvariabler fra dev-gcp:
```bash
./hent-og-lagre-miljøvariabler.sh
```
Dette oppretter en `.env.local` fil (skjult fil) i prosjektmappen.

#### 3. Konfigurer ApplicationLocal profil i IntelliJ

##### 3a. Opprett profil (hvis den ikke eksisterer)
Hvis du ikke har en ApplicationLocal profil:
- Gå til `ApplicationLocal.kt` og forsøk å kjøre den
- Den vil feile, men det opprettes en profil i øvre høyre hjørne ved siden av kjøreknappen

##### 3b. Legg til miljøvariabler i profilen
1. Klikk på profilen i øvre høyre hjørne
2. Hold musen over **ApplicationLocal** profilen
3. Klikk på de tre prikkene (kebab-meny) og velg **Edit**
4. Klikk på **Modify options** (på samme linje som "Build and Run")
5. Under **Operating System**, velg **Environment variables**
6. Klikk på mappeikonet ved siden av Environment variables-feltet
7. Naviger til `gjenlevende-bs-sak` mappen
8. Finn `.env.local` filen (den er skjult som standard)
   - **Mac:** Trykk `Cmd + Shift + .` for å vise skjulte filer
9. Velg `.env.local` filen
10. Klikk **Apply** nederst i vinduet

#### 4. Start Docker-containere
1. Sørg for at **Docker Desktop** kjører
2. Sjekk om det er konflikterende containere som kjører:
   ```bash
   docker ps
   ```
   Hvis det er containere som kan ødelegge ting, fjern dem:
   ```
   docker stop <container-id>
   ```
   eller

    ```
   docker compose down -d
   ```

3. Start Texas-containeren:
   ```bash
   ./start-local.sh
   ```

#### 5. Kjør applikasjonen
Kjør **ApplicationLocal** fra profilen i øvre høyre hjørne i IntelliJ.

#### Feilsøking

##### Build feiler med feilmelding om InfotrygdController (eller whatever)
Kjør:
```bash
mvn clean install
```
Dette løser vanligvis problemet. Årsaken er ukjent, er noe cache greier.

#### Stoppe services
For å stoppe alle Docker-containere:
```bash
docker-compose down
```

## Swagger
Du når Swagger ved å gå til:

Ingress:
- https://gjenlevende-bs-sak.intern.dev.nav.no/swagger-ui/index.html

Lokalt:
- http://localhost:8082/swagger-ui.html
- http://localhost:8082/swagger-ui/index.html

