# Feilsøking: Toggle finnes men blir ikke hentet

## Problem
Togglen `gjenlevende_frontend__test_setup` finnes i Unleash UI på:
https://etterlatte-unleash-web.iap.nav.cloud.nais.io/projects/default

Men applikasjonen rapporterer: `Tilgjengelige toggles: []`

## Sannsynlige årsaker

### 1. ❌ Feil environment i Unleash
**Applikasjonen kjører med environment: `dev-gcp`**

Sjekk i Unleash UI at togglen har en strategi aktivert for environment `dev-gcp`:

1. Gå til togglen i Unleash UI
2. Sjekk "Environments" seksjonen
3. Finn `dev-gcp` (eller `development`)
4. Sikre at:
   - ✅ Togglen er enabled (grønn/ON)
   - ✅ Det finnes minst én aktiv strategi (f.eks. "Standard")
   - ✅ Strategien er aktivert

**Løsning:**
- Hvis environment `dev-gcp` ikke finnes, legg den til
- Eller aktiver togglen for eksisterende environments

### 2. ❌ Toggle er i feil prosjekt
API-tokenet er konfigurert for prosjekt `etterlatte`, men togglen kan være i `default` prosjekt.

**Løsning A - Flytt toggle til riktig prosjekt:**
1. I Unleash UI, gå til togglen
2. Sjekk hvilket prosjekt den er i (toppen av siden)
3. Hvis den er i `default`, flytt den til `etterlatte` prosjekt

**Løsning B - Endre prosjekt i API-token:**
Hvis togglen skal være i `default` prosjektet, oppdater `.nais/unleash-apitoken.yaml`:
```yaml
spec:
    unleashInstance:
        name: default  # I stedet for 'etterlatte'
```

### 3. ❌ API-token har ikke tilgang til prosjektet
CLIENT-tokens må ha tilgang til det spesifikke prosjektet.

**Verifiser:**
- Sjekk at API-tokenet er generert for riktig prosjekt
- Sjekk i Unleash UI under API tokens at tokenet har tilgang

## Mest sannsynlig løsning

Basert på at applikasjonen kjører med environment `dev-gcp`, men togglen sannsynligvis er konfigurert for et annet environment:

1. **Gå til Unleash UI:** https://etterlatte-unleash-web.iap.nav.cloud.nais.io
2. **Finn togglen:** `gjenlevende_frontend__test_setup`
3. **Sjekk environment:** Hvilke environments er togglen aktivert for?
4. **Legg til environment:** `dev-gcp` (hvis den ikke finnes)
5. **Aktiver toggle** for `dev-gcp` med en "Standard" strategi
6. **Vent 10-60 sekunder** for synkronisering
7. **Test API-et** igjen

Alternativt, hvis `dev-gcp` ikke er et gyldig environment i Unleash, sjekk hvilke environments som finnes og aktiver togglen for riktig environment.

## Debugging-kommandoer

### Sjekk pod-logger:
```bash
kubectl logs -n etterlatte -l app=gjenlevende-bs-sak --tail=100 | grep -i unleash
```

### Restart poden for å tvinge ny synkronisering:
```bash
kubectl rollout restart deployment/gjenlevende-bs-sak -n etterlatte
```

### Test endepunktet:
```bash
curl -H "Authorization: Bearer <token>" \
  https://gjenlevende-bs-sak.intern.dev.nav.no/api/unleash/toggles
```

## Forventet resultat når det fungerer

### Logger:
```
INFO: Henter alle toggles. Antall funnet: 1
INFO: Toggle-navn: [gjenlevende_frontend__test_setup]
INFO: Sjekker toggle 'gjenlevende_frontend__test_setup': true
```

### API respons:
```bash
GET /api/unleash/test-setup
# Respons: true

GET /api/unleash/toggles
# Respons: {"gjenlevende_frontend__test_setup": true}
```
