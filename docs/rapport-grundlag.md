# Rapport-grundlag for LiftLog

Dette dokument er ikke en færdig rapporttekst. Det er et arbejdsdokument, der kobler tre ting sammen:

- hvad der allerede står i rapportudkastet
- hvad der faktisk findes i koden lige nu
- hvad der mangler, hvis rapporten skal kunne forsvares teknisk

Formålet er at skabe overblik, så rapporten ikke bliver en efterrationalisering af kode, men en forklarbar udviklingsproces med tydelige valg, afgrænsninger og resultater.

## Eksterne rammer og rapportform

Offentlige studieordninger for datamatikeruddannelsens afsluttende eksamensprojekt beskriver typisk, at rapporten som minimum skal indeholde forside, indholdsfortegnelse, indledning med problemformulering, metode, analyse, eventuelt løsningsforslag, konklusion, litteraturliste og bilag. Derudover fremgår det, at projektet skal dokumentere forståelse for praksis, teori og metode i relation til en konkret praksisnær problemstilling.

Det betyder, at LiftLog-rapporten ikke kun bør beskrive funktioner. Den bør også forklare:

- hvilket problem systemet forsøger at løse
- hvordan kravene er afledt af problemet
- hvorfor teknologierne er valgt
- hvordan systemet er designet
- hvordan implementationen understøtter kravene
- hvordan systemet er testet
- hvilke begrænsninger der stadig findes

Relevante kilder fundet som strukturel inspiration:

- Zealand, studieordning for datamatiker, krav til afsluttende eksamensprojekt: https://www.zealand.dk/wp-content/uploads/2016/09/Studieordning-Datamatiker-august-2020.pdf
- Zealand, fagbeskrivelse for afsluttende eksamensprojekt 2025/2026: https://fag.zealand.dk/course/45055001/2025-2026
- Erhvervsakademi Aarhus, studieordning for datamatiker med afsluttende eksamensprojekt: https://www.eaaa.dk/media/0hjbg1jz/studieordning-datamatiker-2013.pdf

Disse kilder bruges kun til rapportstruktur og forventninger. De bruges ikke som kilde til selve LiftLog-løsningen.

## Status pa rapportudkastet

Det eksisterende rapportudkast indeholder allerede et brugbart fundament:

- Indledning
- Behov
- Malsaetning
- Afgrænsning
- Udvidet afgrænsning
- Kravspecifikation
- Proces/metode
- Teknologivalg

Det er en god begyndelse, fordi teksten allerede forklarer problemet, målgruppen og hvorfor projektet er afgrænset til en responsiv webapplikation. Det næste rapportarbejde bør derfor ikke starte forfra. Det bør bygge videre med analyse, design, implementation, test og evaluering.

## Vigtigt: hvad produktet er lige nu

Den nuværende kodebase er en tidlig full-stack prototype. Den er ikke endnu en komplet workout tracker.

Det der findes nu:

- React frontend med navigation og sider for home, workouts, session, history og progress
- Spring Boot backend med REST API
- PostgreSQL-forbindelse via Docker
- JPA entities og repositories for exercises og workout templates
- API endpoints for health, exercises og workout templates
- CORS-konfiguration, så frontend kan kalde backend lokalt
- Grundlæggende database schema-oprettelse via Hibernate

Det der endnu ikke findes som fuld funktionalitet:

- brugerlogin
- aktiv træningssession med gemte sæt
- logging af vægt og repetitioner i en session
- pausetimer koblet til træningsflowet
- historik over gennemførte træningspas
- progressionsvisning baseret på gemte træningsdata
- egentlig testdækning af API og frontend-flow

Rapporten bør derfor være tydelig omkring, om projektet beskriver den endelige ambition eller den aktuelle prototype. Hvis funktioner ikke er implementeret endnu, skal de enten flyttes til "videreudvikling" eller implementeres senere.

## Kodeoversigt

### Backend

Backend ligger under:

`src/main/java/Hoveopgave/Hovedopgave`

Centrale filer:

- `LiftlogApplication.java`
  - Starter Spring Boot-applikationen.

- `api/HealthController.java`
  - Simpelt endpoint til at kontrollere, om backend kører.
  - Relevant for test og udviklingsmiljø.

- `api/ExerciseController.java`
  - REST controller til CRUD-operationer på øvelser.
  - Understøtter oprettelse, hentning, opdatering og sletning.
  - Bruger inputvalidering med Jakarta Validation.

- `exercise/Exercise.java`
  - JPA entity for en øvelse.
  - Indeholder navn, muskelgruppe, noter og timestamps.

- `exercise/ExerciseRepository.java`
  - Spring Data JPA repository for `Exercise`.
  - Giver databaseoperationer uden manuel SQL.

- `api/WorkoutTemplateController.java`
  - REST controller for workout templates.
  - Gør det muligt at oprette templates med flere øvelser.
  - Validerer, at tilknyttede exercise IDs findes.

- `template/WorkoutTemplate.java`
  - JPA entity for en træningsskabelon.

- `template/WorkoutTemplateItem.java`
  - JPA entity for en øvelse inde i en template.
  - Indeholder rækkefølge, target sets og target reps.

- `template/WorkoutTemplateRepository.java`
  - Repository for templates.
  - Bruger `@EntityGraph` til at hente template items og øvelser samlet.

- `config/WebConfig.java`
  - Tillader frontend på localhost at kalde backendens `/api/**` endpoints.

### Frontend

Frontend ligger under:

`frontend/src`

Centrale filer:

- `App.jsx`
  - Definerer routes i React-applikationen.

- `layouts/AppLayout.jsx`
  - Fælles layout omkring siderne.

- `components/MainNav.jsx`
  - Navigation mellem appens hovedområder.

- `pages/HomePage.jsx`
  - Forside/dashboard.
  - Viser backend-status via health endpoint.

- `pages/WorkoutsPage.jsx`
  - Side for workouts/templates.

- `pages/SessionPage.jsx`
  - Side for aktiv træningssession.
  - Bør på sigt være central for projektets kerneflow.

- `pages/HistoryPage.jsx`
  - Side for træningshistorik.

- `pages/ProgressPage.jsx`
  - Side for progression.

- `services/api.js`
  - Samler frontendens API-kald mod backend.
  - Gør det lettere at ændre backend URL ét sted.

### Infrastruktur og konfiguration

- `compose.yaml`
  - Definerer lokal PostgreSQL-container.
  - Matcher nu appens default databasekonfiguration.

- `src/main/resources/application.yaml`
  - Definerer datasource til PostgreSQL.
  - Bruger miljøvariabler, så database-URL, username, password og `ddl-auto` kan overskrives.

## Kravsporbarhed

| Krav fra rapport/ideudkast | Status i kode | Kommentar |
|---|---:|---|
| Oprette øvelser | Delvist implementeret | Backend API findes. Frontend skal kobles fuldt på CRUD. |
| Se liste over øvelser | Delvist implementeret | Backend endpoint findes. Frontend har API-funktion til hentning. |
| Oprette workout templates | Delvist implementeret | Backend API og datamodel findes. Frontend-flow mangler sandsynligvis færdiggørelse. |
| Starte aktiv træningssession | Ikke implementeret | Der findes side/placeholder, men ikke domænemodel eller persistence. |
| Logge sæt, reps og vægt | Ikke implementeret | Kræver entities for session, exercise sets eller workout logs. |
| Pausetimer | Ikke implementeret | Kan implementeres frontend-only først, men bør kobles til session-flow. |
| Gemme træningsdata i database | Delvist implementeret | Exercises og templates gemmes. Sessiondata mangler. |
| Se tidligere træningspas | Ikke implementeret | Kræver sessionhistorik i backend. |
| Se progression | Ikke implementeret | Kræver historiske logs og beregning/visning. |
| Responsivt UI | Delvist implementeret | React/Vite frontend findes, men bør testes på mobil viewport. |
| Validering af input | Delvist implementeret | Backend bruger Jakarta Validation på API requests. |

## Mulig rapportstruktur

Denne struktur passer til dit nuværende udkast og til en typisk afsluttende projektrapport.

1. Indledning
   - Kort præsentation af LiftLog.
   - Problemfelt: fragmenteret træningsregistrering.
   - Formål med løsningen.

2. Problemformulering
   - Eksempel: "Hvordan kan der udvikles en responsiv webapplikation, der gør det muligt for brugere at registrere og følge styrketræning på en enkel og struktureret måde?"
   - Eventuelle underspørgsmål om arkitektur, datalagring, brugerflow og test.

3. Afgrænsning
   - Responsiv webapp frem for native app.
   - Ingen sociale funktioner, kosttracking, wearables eller avanceret AI.
   - Vigtigt: kun beskriv implementerede funktioner som resultat; resten som afgrænsning eller videreudvikling.

4. Metode og proces
   - Iterativ udvikling.
   - Kanban/GitHub Projects, hvis det faktisk bruges.
   - AI-assisteret udvikling som arbejdsform, med efterfølgende analyse, test og dokumentation.

5. Kravspecifikation
   - Funktionelle krav.
   - Ikke-funktionelle krav.
   - User stories.
   - Prioritering, fx Must/Should/Could.

6. Analyse og design
   - Målgruppe og brugssituation.
   - Centrale brugerflows.
   - Domænemodel: Exercise, WorkoutTemplate, WorkoutTemplateItem og senere Session/SetLog.
   - Databaseovervejelser.

7. Arkitektur og teknologivalg
   - React frontend.
   - Spring Boot backend.
   - REST API.
   - PostgreSQL.
   - Docker Compose til lokal database.
   - Fordele og ulemper ved adskilt frontend/backend.

8. Implementation
   - Backend API.
   - Database og JPA.
   - Frontend routing og API-kald.
   - CORS og lokal udviklingsopsætning.
   - Her kan kodeoversigten fra dette dokument omskrives til rapporttekst.

9. Test
   - Manuelle API-tests.
   - Maven test.
   - Browser-test af frontend/backend-forbindelse.
   - Manglende testdækning og hvad der bør testes senere.

10. Evaluering
   - Hvad fungerer?
   - Hvad mangler?
   - Hvilke krav er opfyldt, delvist opfyldt eller ikke opfyldt?
   - Hvad ville næste iteration være?

11. Konklusion
   - Svar på problemformuleringen.
   - Kort vurdering af resultatet i forhold til scope.

12. Perspektivering
   - Native app, lock-screen timer, offline/PWA, login, analytics, bedre progression.

## AI-assisteret udvikling

Da en væsentlig del af koden er udviklet med AI-hjælp, bør rapporten være transparent uden at gøre AI til hovedpersonen.

En sober formulering kunne være:

> Projektet er udviklet med brug af AI-assisterede værktøjer som støtte til kodegenerering, fejlfinding og strukturering. Den efterfølgende vurdering, tilpasning, test og dokumentation af løsningen indgår som en central del af projektarbejdet. Dette har gjort det nødvendigt løbende at validere, om den genererede kode faktisk opfylder projektets krav og kan forklares i rapporten.

Det vigtige er, at rapporten viser din forståelse:

- Hvad gør koden?
- Hvorfor er den struktureret sådan?
- Hvilke krav understøtter den?
- Hvordan er den testet?
- Hvad er dens begrænsninger?

## Kendte inkonsistenser og risici

### Rapporten lover mere end koden

Rapportudkastet beskriver historik, progression, aktiv session og timer som centrale mål. Koden indeholder endnu primært fundamentet for exercises og templates. Det skal enten implementeres senere eller beskrives som ikke færdigt/videreudvikling.

### Login nævnes i ideudkastet

Ideudkastet nævner brugerprofil og login, men den nuværende kode har ikke authentication. Hvis login ikke skal implementeres, bør det fjernes fra kravene eller placeres under afgrænsning/videreudvikling.

### Frontend indeholder placeholders

Flere frontend-sider eksisterer som strukturelle sider, men ikke nødvendigvis som færdige workflows. Rapporten bør ikke fremstille dem som fuldt implementerede funktioner, før de faktisk er det.

### Kodens tekst har encoding-problemer

Der ses tekst som `NÃ¦ste` og `trÃ¦ningssession` i frontend. Det tyder på tegnkodningsproblemer, som bør rettes før aflevering.

### Test er tynd

Der findes kun en standard Spring Boot context test. Der bør som minimum dokumenteres manuelle test, og helst tilføjes fokuserede backend-tests for API og validering.

## Næste dokumentationsskridt

1. Lav en prioriteret kravliste med status: implementeret, delvist implementeret, ikke implementeret.
2. Beslut hvilke krav der realistisk skal nås i produktet.
3. Omskriv rapportens kravspecifikation, så den passer til det realistiske scope.
4. Tilføj arkitekturdiagram og database/ER-diagram.
5. Skriv implementationsafsnit ud fra den faktiske kode.
6. Tilføj testafsnit med konkrete testcases og resultater.

