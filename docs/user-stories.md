# User stories for LiftLog

Dette dokument kan bruges som grundlag for GitHub Projects/Kanban. Formålet er at få krav, kode og rapport til at hænge sammen.

Forslag til Kanban-kolonner:

- Backlog
- Ready
- In progress
- Review/Test
- Done

Forslag til labels:

- `frontend`
- `backend`
- `database`
- `testing`
- `documentation`
- `mvp`
- `stretch`

## Definition of Done

En user story bør først flyttes til Done, når:

- funktionaliteten virker lokalt
- relevante fejltilstande er håndteret
- koden er bygget/lintet/testet
- der er en kort forklaring af hvad der er lavet
- rapporten kan forklare designvalget, hvis storyen er central for projektet

## Implementeret eller delvist implementeret i nuværende kode

### US-001: Se om backend er online

**Status:** Done  
**Labels:** `frontend`, `backend`, `mvp`

Som bruger/udvikler vil jeg kunne se om backend er online, så jeg hurtigt kan kontrollere om frontend og backend har forbindelse.

**Acceptance criteria**

- Frontend kalder `GET /api/health`.
- Brugeren kan se om backend er online eller offline.
- Fejl vises i UI, hvis backend ikke svarer.

**Kodekobling**

- `src/main/java/Hoveopgave/Hovedopgave/api/HealthController.java`
- `frontend/src/services/api.js`
- `frontend/src/pages/HomePage.jsx`

### US-002: Se liste over øvelser

**Status:** Done  
**Labels:** `frontend`, `backend`, `database`, `mvp`

Som bruger vil jeg kunne se en liste over øvelser, så jeg kan få overblik over de øvelser, der kan bruges i træningsprogrammer.

**Acceptance criteria**

- Backend kan hente alle øvelser fra databasen.
- Frontend viser øvelser i en tabel.
- Tom liste håndteres med en besked.
- Fejl ved API-kald vises i UI.

**Kodekobling**

- `ExerciseController.findAll`
- `ExerciseRepository`
- `frontend/src/services/api.js`
- `frontend/src/pages/WorkoutsPage.jsx`

### US-003: Opret øvelse

**Status:** Done  
**Labels:** `frontend`, `backend`, `database`, `mvp`

Som bruger vil jeg kunne oprette en øvelse med navn, muskelgruppe og noter, så øvelsen kan bruges i senere workout templates.

**Acceptance criteria**

- Frontend viser en formular til oprettelse af øvelse.
- Navn og muskelgruppe er påkrævet.
- Backend validerer input.
- Den nye øvelse gemmes i PostgreSQL.
- Listen opdateres efter oprettelse.

**Kodekobling**

- `ExerciseController.create`
- `Exercise`
- `ExerciseRepository`
- `frontend/src/services/api.js`
- `frontend/src/pages/WorkoutsPage.jsx`

### US-004: Rediger og slet øvelser via API

**Status:** Delvist Done  
**Labels:** `backend`, `database`, `mvp`

Som bruger vil jeg kunne redigere og slette øvelser, så øvelseslisten kan vedligeholdes.

**Acceptance criteria**

- Backend understøtter `PUT /api/exercises/{id}`.
- Backend understøtter `DELETE /api/exercises/{id}`.
- Backend returnerer 404, hvis øvelsen ikke findes.

**Mangler**

- Frontend UI til redigering.
- Frontend UI til sletning.

**Kodekobling**

- `ExerciseController.update`
- `ExerciseController.delete`

### US-005: Opret workout template via API

**Status:** Delvist Done  
**Labels:** `backend`, `database`, `mvp`

Som bruger vil jeg kunne oprette en workout template med flere øvelser, så jeg kan genbruge et træningsprogram.

**Acceptance criteria**

- Backend kan oprette en template med navn og beskrivelse.
- Backend kan tilknytte øvelser til template items.
- Hvert item har rækkefølge, target sets og target reps.
- Backend afviser ukendte exercise IDs.

**Mangler**

- Frontend UI til template-oprettelse.
- Integrationstest af template-flow.

**Kodekobling**

- `WorkoutTemplateController`
- `WorkoutTemplate`
- `WorkoutTemplateItem`
- `WorkoutTemplateRepository`

### US-006: Lokal database med Docker

**Status:** Done  
**Labels:** `database`, `mvp`

Som udvikler vil jeg kunne starte en lokal PostgreSQL-database med Docker, så applikationen kan gemme data under udvikling.

**Acceptance criteria**

- Docker Compose definerer en PostgreSQL-container.
- Backend kan forbinde til databasen.
- Databasens port er tilgængelig på localhost.

**Kodekobling**

- `compose.yaml`
- `src/main/resources/application.yaml`

### US-007: Frontend kan kalde backend lokalt

**Status:** Done  
**Labels:** `frontend`, `backend`, `mvp`

Som udvikler vil jeg kunne køre frontend og backend på hver sin port lokalt, så frontend kan udvikles separat fra backend.

**Acceptance criteria**

- Frontend kører på Vite.
- Backend kører på Spring Boot.
- CORS tillader lokale API-kald til `/api/**`.

**Kodekobling**

- `WebConfig.java`
- `frontend/src/services/api.js`

## Næste små stories

### US-008: Slet øvelse fra frontend

**Status:** Ready  
**Labels:** `frontend`, `backend`, `mvp`

Som bruger vil jeg kunne slette en øvelse fra øvelseslisten, så jeg kan fjerne øvelser jeg ikke længere vil bruge.

**Acceptance criteria**

- Hver øvelse i listen har en slet-knap.
- Brugeren skal bekræfte sletning.
- Frontend kalder `DELETE /api/exercises/{id}`.
- Listen opdateres efter sletning.
- Fejl vises, hvis sletning mislykkes.

### US-009: Rediger øvelse fra frontend

**Status:** In progress  
**Labels:** `frontend`, `backend`, `mvp`

Som bruger vil jeg kunne redigere en øvelse, så jeg kan rette navn, muskelgruppe eller noter.

**Acceptance criteria**

- Brugeren kan vælge en øvelse fra listen.
- Formularen udfyldes med eksisterende data.
- Frontend kalder `PUT /api/exercises/{id}`.
- Listen opdateres efter gemt ændring.
- Valideringsfejl vises i UI.

### US-010: Opret workout template fra frontend

**Status:** In progress  
**Labels:** `frontend`, `backend`, `database`, `mvp`

Som bruger vil jeg kunne oprette en workout template ud fra eksisterende øvelser, så jeg kan genbruge et fast træningsprogram som udgangspunkt for mine træningssessioner.

**Acceptance criteria**

- Brugeren kan angive template-navn og beskrivelse.
- Brugeren kan tilføje en eller flere øvelser.
- Brugeren kan angive target sets og target reps for hver øvelse.
- Target sets/reps gemmes som standardværdier på templaten.
- Target sets/reps er ikke historiske træningsdata, men forslag/defaults til en senere session.
- Frontend kalder `POST /api/templates`.
- Den oprettede template vises i UI.

### US-011: Se workout templates

**Status:** Backlog  
**Labels:** `frontend`, `backend`, `database`, `mvp`

Som bruger vil jeg kunne se mine workout templates, så jeg kan vælge hvilket program jeg vil træne.

**Acceptance criteria**

- Backend returnerer templates med items og øvelsesnavne.
- Frontend viser template-navn, beskrivelse og øvelser.
- Tom liste håndteres med en besked.

## Fremtidige kernefunktioner

### US-012: Start aktiv træningssession fra template

**Status:** Backlog  
**Labels:** `frontend`, `backend`, `database`, `mvp`

Som bruger vil jeg kunne starte en aktiv træningssession fra en template, så jeg hurtigt kan begynde et træningspas.

**Acceptance criteria**

- Brugeren kan vælge en template.
- Systemet opretter en aktiv session.
- Sessionen indeholder template-øvelser i korrekt rækkefølge.
- Target sets/reps fra templaten bruges som startforslag i sessionen.
- Brugeren kan navigere mellem øvelser i sessionen.
- Ændringer i sessionen ændrer ikke selve workout templaten.

### US-013: Log sæt med vægt og repetitioner

**Status:** Backlog  
**Labels:** `frontend`, `backend`, `database`, `mvp`

Som bruger vil jeg kunne registrere og hurtigt ændre vægt og repetitioner for hvert sæt under en aktiv session, så jeg kan gemme hvad jeg faktisk udførte den dag.

**Acceptance criteria**

- Brugeren kan angive vægt og reps for hvert sæt.
- Brugeren kan hurtigt ændre reps under sessionen.
- Brugeren kan tilføje ekstra sæt.
- Brugeren kan fjerne et sæt.
- Brugeren kan gemme flere sæt pr. øvelse.
- Data gemmes som session-data i databasen.
- Session-data ændrer ikke workout templaten.
- Input valideres, så negative eller tomme værdier ikke gemmes.

### US-014: Brug pausetimer under træning

**Status:** Backlog  
**Labels:** `frontend`, `mvp`

Som bruger vil jeg kunne starte en pausetimer mellem sæt, så jeg lettere kan holde styr på mine pauser.

**Acceptance criteria**

- Brugeren kan starte, pause og nulstille timeren.
- Timeren viser resterende tid tydeligt.
- Timeren kan bruges uden at forlade session-siden.

### US-015: Afslut og gem træningssession

**Status:** Backlog  
**Labels:** `frontend`, `backend`, `database`, `mvp`

Som bruger vil jeg kunne afslutte og gemme en træningssession, så den senere kan vises i historikken.

**Acceptance criteria**

- Brugeren kan afslutte en aktiv session.
- Sessionens faktiske sæt/reps/vægt-data gemmes i databasen.
- Sessionen får dato/tidspunkt og varighed.
- Brugeren får feedback om at sessionen er gemt.
- Den gemte session kan senere bruges til historik og progression.
- Gemning af sessionen ændrer ikke workout templaten.

### US-016: Se træningshistorik

**Status:** Backlog  
**Labels:** `frontend`, `backend`, `database`, `mvp`

Som bruger vil jeg kunne se tidligere træningspas, så jeg kan følge med i hvad jeg har trænet.

**Acceptance criteria**

- Frontend viser en liste over tidligere sessions.
- Hver session viser dato og centrale data.
- Brugeren kan åbne en session og se sæt-data.

### US-017: Se progression for en øvelse

**Status:** Backlog  
**Labels:** `frontend`, `backend`, `database`, `mvp`

Som bruger vil jeg kunne se min progression for en øvelse, så jeg kan vurdere om jeg bliver stærkere over tid.

**Acceptance criteria**

- Brugeren kan vælge en øvelse.
- Systemet viser historiske resultater for øvelsen.
- Progression kan vises som simpel liste eller graf.
- Beregningen baseres på gemte session-data.

## Tekniske forbedringsstories

### US-018: Tilføj backend API-tests for exercises

**Status:** Backlog  
**Labels:** `backend`, `testing`, `mvp`

Som udvikler vil jeg have tests for exercise API'et, så jeg kan ændre koden uden at ødelægge eksisterende funktionalitet.

**Acceptance criteria**

- Test dækker oprettelse af øvelse.
- Test dækker hentning af øvelser.
- Test dækker valideringsfejl.
- Test dækker 404 ved ukendt ID.

### US-019: Gennemgå UI-tekster før aflevering

**Status:** Backlog  
**Labels:** `frontend`, `mvp`

Som bruger vil jeg se klare og konsekvente tekster i UI, så applikationen fremstår færdig og professionel.

**Acceptance criteria**

- Sider, knapper, formularlabels og fejlbeskeder er gennemgået.
- UI-tekster er konsekvente på tværs af appen.
- Tekster passer til domænet, f.eks. øvelser, workouts, sessioner og historik.
- Frontend build kører uden fejl.
- UI kontrolleres i browser.

### US-020: Indfør database migrations

**Status:** Stretch  
**Labels:** `backend`, `database`, `stretch`

Som udvikler vil jeg bruge migrations til databaseændringer, så schemaet kan versioneres og forklares mere professionelt.

**Acceptance criteria**

- Flyway eller Liquibase er tilføjet.
- Første migration opretter eksisterende tabeller.
- `ddl-auto:update` erstattes eller begrænses til udvikling.

## Mulige stretch stories

### US-021: Brugerlogin

**Status:** Stretch  
**Labels:** `backend`, `frontend`, `database`, `stretch`

Som bruger vil jeg kunne logge ind, så mine træningsdata er knyttet til mig.

**Bemærkning**

Login er nævnt i ideudkastet, men findes ikke i koden. Det bør kun være med i MVP, hvis der er tid nok til at gøre det ordentligt.

### US-022: Mobiloptimeret session UI

**Status:** Stretch  
**Labels:** `frontend`, `stretch`

Som bruger vil jeg have et mobilvenligt session-interface, så appen er hurtig at bruge i fitnesscenteret.

**Acceptance criteria**

- Knapper og inputs fungerer godt på lille skærm.
- Aktiv øvelse og næste sæt er tydeligt.
- Brugeren skal kunne logge et sæt med få tryk.
