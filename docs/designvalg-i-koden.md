# Designvalg i den nuværende kode

Dette dokument forklarer de vigtigste designvalg i den uversionerede kode. Det er skrevet som intern arbejdsdokumentation, men flere af pointerne kan omskrives og bruges i rapportens afsnit om arkitektur, teknologivalg, implementation og evaluering.

Dokumentet skal ikke forstås som en påstand om, at alle valg er endelige eller optimale. En del af koden er lavet som et tidligt fundament for en full-stack prototype. Derfor beskrives både begrundelser, fordele, ulemper og hvad der bør forbedres senere.

## Overordnet arkitektur

Projektet er struktureret som en todelt webapplikation:

- React frontend i `frontend/`
- Spring Boot backend i `src/main/java/`
- PostgreSQL database via Docker
- REST API som kommunikationslag mellem frontend og backend

### Hvorfor adskilt frontend og backend?

Valget om at adskille frontend og backend gør systemet mere modulært. Frontend har ansvar for brugergrænseflade, navigation og interaktion, mens backend har ansvar for forretningsdata, validering og databaseadgang.

Det passer godt til LiftLog, fordi appen på sigt skal have en interaktiv brugeroplevelse under træning. En aktiv træningssession, timer og live registrering af sæt passer bedre til en frontend, der kan opdatere UI uden fulde sideindlæsninger.

Fordele:

- frontend kan udvikles og testes separat fra backend
- backend kan senere genbruges af andre klienter, fx en mobilapp
- REST API'et giver en tydelig kontrakt mellem UI og database
- arkitekturen viser moderne full-stack udvikling i rapporten

Ulemper:

- lokal udvikling kræver to processer: backend på `8080` og frontend på `5173`
- CORS skal konfigureres
- der er mere opsætning end ved en klassisk server-renderet app

Rapportvinkel: Dette valg kan beskrives som et bevidst kompromis mellem kompleksitet og fleksibilitet.

## Backend: Spring Boot REST API

Backend er bygget med Spring Boot og REST controllers.

Relevante filer:

- `api/HealthController.java`
- `api/ExerciseController.java`
- `api/WorkoutTemplateController.java`
- `config/WebConfig.java`
- `exercise/Exercise.java`
- `template/WorkoutTemplate.java`
- `template/WorkoutTemplateItem.java`

### Hvorfor REST controllers?

Controllers er valgt fordi de giver en enkel måde at eksponere funktionalitet som HTTP endpoints:

- `GET /api/exercises`
- `POST /api/exercises`
- `PUT /api/exercises/{id}`
- `DELETE /api/exercises/{id}`
- `GET /api/templates`
- `POST /api/templates`

REST passer til projektet, fordi frontend skal kunne hente og sende JSON-data. Det er også let at teste med browser, Postman, curl eller PowerShell.

Fordele:

- simpelt og velkendt API-mønster
- passer godt til React frontend
- let at dokumentere i rapporten
- tydelig kobling mellem krav og endpoints

Ulemper:

- uden en separat service layer ligger noget logik direkte i controllerne
- API'et er endnu ikke versionsstyret, fx `/api/v1/...`

Rapportvinkel: I en prototype er det acceptabelt at holde controllerlaget relativt simpelt. Hvis systemet vokser, bør forretningslogik flyttes til serviceklasser.

## Health endpoint

`HealthController` indeholder endpointet:

`GET /api/health`

Formålet er ikke domænefunktionalitet, men udviklings- og integrationstest. Frontend bruger endpointet til at vise om backend er online.

Designbegrundelse:

- gør det hurtigt at kontrollere om backend kører
- hjælper med at teste forbindelsen mellem frontend og backend
- gav konkret værdi under opsætning af PostgreSQL og lokal udvikling

Rapportvinkel: Endpointet kan nævnes i testafsnittet som en simpel integrationstest mellem frontend og backend.

## Domænemodel: Exercise

`Exercise` repræsenterer en øvelse i systemet.

Felter:

- `id`
- `name`
- `muscleGroup`
- `notes`
- `createdAt`
- `updatedAt`

### Hvorfor starte med exercises?

Øvelser er en grundlæggende byggesten for resten af LiftLog. Workout templates, aktive sessions, historik og progression vil alle afhænge af, at systemet kender øvelserne.

Designbegrundelse:

- lav kompleksitet
- nemt at teste databaseforbindelse med CRUD
- naturligt første skridt i domænet
- bruges senere af templates og session logging

### Hvorfor timestamps?

`createdAt` og `updatedAt` sættes med `@PrePersist` og `@PreUpdate`.

Det giver systemet metadata om, hvornår data er oprettet og ændret. Det er ikke strengt nødvendigt for første prototype, men det er nyttigt i et system hvor historik og udvikling over tid er centralt.

Fordele:

- ændringer kan spores
- data bliver mere brugbare til debugging og historik
- kræver ikke manuel håndtering i controlleren

Ulemper:

- timestamps håndteres i applikationen, ikke databasen
- tidszoner skal overvejes senere, hvis appen skal bruges bredere

## Domænemodel: WorkoutTemplate og WorkoutTemplateItem

Workout templates er delt i to entities:

- `WorkoutTemplate`
- `WorkoutTemplateItem`

En template har mange items. Hvert item peger på en exercise og angiver:

- rækkefølge (`orderIndex`)
- antal target sets
- antal target reps

### Hvorfor to tabeller?

En template er ikke bare en tekstliste. Hver øvelse i en template har egne metadata, fx rækkefølge og planlagte sæt/reps. Derfor er der brug for en mellem-entity i stedet for kun en direkte liste af exercises.

Dette gør modellen mere fleksibel:

- samme exercise kan bruges i mange templates
- hver template kan have sin egen rækkefølge
- samme exercise kan have forskellige target reps i forskellige templates

Rapportvinkel: Dette kan forklares som normalisering af databasen og som en modelering af mange-til-mange-lignende relation med ekstra attributter.

### Hvorfor `cascade = CascadeType.ALL` og `orphanRemoval = true`?

`WorkoutTemplate` ejer sine `WorkoutTemplateItem` rækker. Hvis en template slettes, giver det ikke mening at dens items bliver liggende alene.

Derfor bruges:

- `cascade = CascadeType.ALL`
- `orphanRemoval = true`

Fordele:

- items følger template lifecycle
- controlleren skal ikke manuelt gemme og slette items
- færre forældreløse rækker i databasen

Ulemper:

- man skal være forsigtig ved update, fordi `template.getItems().clear()` sletter gamle items
- hvis item-data senere får selvstændig historisk betydning, skal modellen ændres

## Repositories og Spring Data JPA

Der bruges Spring Data JPA repositories:

- `ExerciseRepository extends JpaRepository`
- `WorkoutTemplateRepository extends JpaRepository`

### Hvorfor repositories?

Repositories reducerer mængden af manuel databasekode. CRUD-operationer som `findAll`, `findById`, `save` og `delete` kommer fra Spring Data JPA.

Fordele:

- hurtig udvikling
- mindre boilerplate
- tydelig separation mellem controller og persistence
- passer til et projekt med begrænset tid

Ulemper:

- abstraktionen kan skjule SQL-adfærd
- komplekse queries kræver stadig opmærksomhed
- performanceproblemer kan opstå, hvis relationer hentes ukritisk

Rapportvinkel: Dette valg kan begrundes med, at projektet fokuserer på applikationslogik og produktfunktionalitet frem for manuel SQL.

## EntityGraph på templates

`WorkoutTemplateRepository` bruger `@EntityGraph(attributePaths = { "items", "items.exercise" })`.

Formålet er at hente template, template items og tilknyttede exercises samlet.

Designbegrundelse:

- API response for templates skal bruge data fra både template, item og exercise
- `open-in-view` er slået fra, så lazy-loaded relationer bør hentes eksplicit
- reducerer risiko for lazy loading-fejl i controlleren

Fordele:

- mere forudsigelig datahentning
- bedre samspil med `spring.jpa.open-in-view=false`
- controllerens response mapping kan bruge relationerne uden ekstra databasekald efter sessionen er lukket

Ulemper:

- ved meget store templates kan det hente mange data på én gang
- query-strategien bør revurderes, hvis domænet vokser

## `open-in-view: false`

I `application.yaml` er `spring.jpa.open-in-view` sat til `false`.

Det betyder, at JPA sessionen ikke holdes åben gennem hele web requesten.

Designbegrundelse:

- tvinger applikationen til at hente nødvendige data i repository/service-laget
- undgår skjulte databasekald under JSON-serialisering
- er en mere kontrolleret praksis end at lade lazy loading ske sent i requesten

Fordele:

- mere forudsigelig backend-adfærd
- færre overraskelser i performance og databaseadgang

Ulemper:

- kræver mere bevidst håndtering af relationer
- derfor er `@EntityGraph` nødvendigt på templates

Rapportvinkel: Dette er et godt eksempel på et teknisk designvalg, der viser bevidsthed om backend-arkitektur.

## DTOs som Java records

Controllerne bruger records til request og response objekter, fx:

- `ExerciseUpsertRequest`
- `ExerciseResponse`
- `WorkoutTemplateUpsertRequest`
- `WorkoutTemplateResponse`

### Hvorfor ikke returnere entities direkte?

Entities repræsenterer database-/domænemodellen. API responses repræsenterer det dataformat, frontend skal se.

Ved at bruge DTOs undgår man at eksponere hele entity-strukturen direkte.

Fordele:

- API-kontrakten bliver tydeligere
- frontend får kun relevante felter
- man undgår problemer med JPA relationer og JSON-serialisering
- validering kan placeres på request DTOs

Ulemper:

- kræver mapping-kode i controlleren
- ved større projekt bør mapping måske flyttes ud af controlleren

Rapportvinkel: Dette kan beskrives som en bevidst adskillelse mellem persistence model og API model.

## Inputvalidering

Request records bruger Jakarta Validation:

- `@NotBlank`
- `@Size`
- `@NotNull`
- `@Min`

Designbegrundelse:

- ugyldige data skal stoppes ved API-grænsen
- database constraints alene giver dårligere fejlbeskeder
- validering er et ikke-funktionelt krav i rapportudkastet

Fordele:

- mere robust backend
- bedre API-kontrakt
- reducerer risiko for tomme eller ugyldige records i databasen

Ulemper:

- frontend bør også validere for bedre brugeroplevelse
- fejlresponsen er endnu ikke tilpasset med egne fejlbeskeder

## CORS-konfiguration

`WebConfig` tillader requests til `/api/**` fra `http://localhost:*`.

Designbegrundelse:

- frontend kører på Vite, typisk `localhost:5173`
- backend kører på `localhost:8080`
- browseren kræver CORS-tilladelse, når frontend og backend kører på forskellige ports

Fordele:

- gør lokal udvikling enkel
- begrænser CORS til API-routes
- tillader forskellige lokale ports

Ulemper:

- egner sig kun til udvikling
- production bør have en mere præcis origin-konfiguration

Rapportvinkel: CORS kan nævnes i implementationen som en nødvendig konsekvens af adskilt frontend/backend.

## Database og Docker Compose

PostgreSQL køres via Docker Compose.

`compose.yaml` definerer:

- image: `postgres:16`
- container name: `liftlog-postgres`
- database: `liftlog`
- user/password: `postgres/postgres`
- port: `5432:5432`

### Hvorfor PostgreSQL?

LiftLog arbejder med strukturerede relationelle data:

- exercises
- templates
- template items
- senere sessions og set logs

En relationel database passer godt til disse relationer.

Fordele:

- stærk understøttelse af relationer
- god integration med JPA/Hibernate
- realistisk valg til en backend-applikation
- Docker gør lokal opsætning mere reproducerbar

Ulemper:

- kræver lokal container
- migrations bør håndteres mere professionelt senere

### Hvorfor Docker?

Docker gør det muligt at køre samme databaseopsætning lokalt uden manuel installation af PostgreSQL.

Rapportvinkel: Docker Compose kan beskrives som et værktøj til udviklingsmiljø og reproducerbar lokal opsætning.

## Hibernate `ddl-auto: update`

`application.yaml` bruger:

`ddl-auto: ${LIFTLOG_DDL_AUTO:update}`

Det betyder, at Hibernate lokalt kan oprette eller opdatere tabeller ud fra entities.

Designbegrundelse:

- hurtig lokal udvikling
- gjorde det muligt at få database og JPA i gang uden manuel SQL
- passer til en tidlig prototype

Fordele:

- lav friktion i udvikling
- database schema følger entities
- nemt at demonstrere prototype

Ulemper:

- ikke ideelt til production
- schemaændringer bliver ikke dokumenteret som migrations
- Flyway eller Liquibase bør bruges senere, hvis projektet skal gøres mere professionelt

Rapportvinkel: Beskriv det som et udviklingsvalg, ikke som en produktionsstrategi.

## Frontend: React, Vite og Bootstrap

Frontend bruger:

- React
- React Router
- Vite
- Bootstrap
- et lille eget CSS-lag i `index.css`

### Hvorfor React?

React passer til en app med interaktive views og fremtidigt session-flow. Når brugeren logger sæt, starter timer og skifter mellem øvelser, er der brug for UI-state uden fulde sideindlæsninger.

Fordele:

- komponentbaseret struktur
- velegnet til interaktiv UI
- stor økosystemstøtte
- passer til frontend/backend separationen

Ulemper:

- kræver build-tooling
- tilføjer kompleksitet sammenlignet med server-renderede HTML-sider

### Hvorfor Vite?

Vite giver en enkel og hurtig udviklingsserver til React.

Fordele:

- hurtig startup
- hot reload under udvikling
- enkel frontend-build

Ulemper:

- frontend skal startes separat fra Spring Boot
- deployment skal planlægges senere

### Hvorfor Bootstrap?

Bootstrap blev valgt for hurtigt at få en responsiv og nogenlunde ensartet brugerflade uden at bygge et helt design system fra bunden.

Fordele:

- grid, cards, buttons og tables findes allerede
- responsiv adfærd kan bygges hurtigt
- reducerer CSS-arbejde i en tidlig prototype

Ulemper:

- UI kan virke generisk
- man skal stadig lave designarbejde for at få en stærk produktoplevelse

Rapportvinkel: Bootstrap kan forklares som et pragmatisk valg for at prioritere funktionalitet og responsivitet inden for tidsrammen.

## Frontend routing og layout

`App.jsx` definerer routes:

- `/`
- `/workouts`
- `/session`
- `/history`
- `/progress`

`AppLayout.jsx` samler navigation og sideindhold.

### Hvorfor denne struktur?

Appen er opdelt efter brugerens hovedområder:

- forsiden/status
- workouts/templates
- aktiv session
- historik
- progression

Det matcher rapportens funktionsmål og giver en tidlig informationsarkitektur.

Fordele:

- nemt at udvide hver side selvstændigt
- navigationen svarer til appens centrale flows
- rapporten kan beskrive UI-strukturen tydeligt

Ulemper:

- flere sider er endnu placeholders
- der mangler endnu reelle workflows på session, history og progress

## API service i frontend

`frontend/src/services/api.js` samler API-kald:

- `getHealth`
- `getExercises`

### Hvorfor samle API-kald?

Hvis fetch-kald placeres direkte i hver komponent, bliver det hurtigt svært at ændre backend URL, fejlbehandling og endpoints.

Fordele:

- API base URL er samlet ét sted
- komponenterne bliver mere fokuserede på UI
- lettere at udvide med flere API-funktioner

Ulemper:

- service-laget er meget simpelt lige nu
- der mangler endnu funktioner til create/update/delete fra frontend

Rapportvinkel: Dette kan forklares som en begyndende separation mellem UI-logik og datatilgang.

## AbortController i frontend

`HomePage` og `WorkoutsPage` bruger `AbortController` i `useEffect`.

Designbegrundelse:

- hvis komponenten unmountes, mens fetch stadig kører, kan requesten annulleres
- undgår state updates på komponenter, der ikke længere findes

Fordele:

- mere robust fetch-håndtering
- god praksis ved async datahentning i React

Ulemper:

- lidt mere kode i simple komponenter
- ved større projekt kan data fetching håndteres af et bibliotek

## Bevidst simple placeholders

Siderne `SessionPage`, `HistoryPage` og `ProgressPage` indeholder endnu ikke fuld funktionalitet. De fungerer som placeholders for appens planlagte hovedområder.

Designbegrundelse:

- navigation og informationsarkitektur kunne etableres tidligt
- gør det tydeligt hvilke funktioner appen skal udvides med
- frontend kan demonstrere struktur, selv før alle backend-modeller findes

Ulempe:

- rapporten må ikke beskrive disse som færdig implementation
- der er risiko for at UI ser mere færdigt ud end produktet faktisk er

Rapportvinkel: De bør beskrives som planlagte eller delvist implementerede moduler, indtil funktionaliteten er bygget.

## Hvorfor der ikke er service layer endnu

Backend-controllerne kalder repositories direkte.

Det er valgt for at holde den tidlige prototype enkel. På nuværende tidspunkt er domænelogikken begrænset:

- normalisering af input
- validering af exercise IDs
- mapping til responses

Fordele:

- færre filer
- hurtigere at komme fra krav til fungerende API
- lettere at følge i et lille projekt

Ulemper:

- controllerne kan blive for store
- forretningslogik blandes med HTTP-logik
- testbarhed bliver dårligere, hvis domænet vokser

Rapportvinkel: Det bør beskrives som et bevidst prototypevalg. En senere iteration bør indføre serviceklasser, især når session logging og progression implementeres.

## Vigtige forbedringer før aflevering

Følgende bør overvejes, før koden bruges som grundlag for endelig rapport/aflevering:

1. Ret encoding-fejl i frontend-tekster
   - Eksempel: `vÃ¦gt`, `sÃ¦t`, `NÃ¦ste`.

2. Afstem kravspecifikation med faktisk funktionalitet
   - Login, session, timer, historik og progression er ikke færdige endnu.

3. Tilføj backend-tests
   - Controller/API-tests for exercises og templates.
   - Test af validering og 404/400 cases.

4. Overvej service layer
   - Især før session logging og progression bliver implementeret.

5. Overvej database migrations
   - `ddl-auto:update` er fint til udvikling, men Flyway/Liquibase er mere professionelt.

6. Udbyg frontend CRUD
   - Frontend kan pt. hente exercises, men ikke oprette/redigere/slette dem via UI.

7. Implementer session-domænet
   - Hvis rapporten skal handle om workout tracking, er session og set logging centralt.

## Kort rapportformulering

En mulig formulering til rapporten kunne være:

> Systemet er opbygget som en todelt webapplikation med en React-baseret frontend og et Spring Boot-baseret backend-API. Denne opdeling er valgt for at adskille brugergrænseflade fra databehandling og persistence. Frontenden kommunikerer med backend via REST endpoints, mens backend anvender Spring Data JPA til at gemme og hente data i en PostgreSQL-database. I den første prototype er der fokuseret på at etablere fundamentet for øvelser og workout templates, da disse udgør grundlaget for senere funktioner som aktiv træningssession, historik og progression.

En anden mulig formulering:

> Flere tekniske valg er truffet med udgangspunkt i projektets tidsramme og prototypekarakter. Hibernate anvendes midlertidigt til automatisk schema-opdatering under lokal udvikling, mens Docker Compose anvendes til at gøre PostgreSQL-databasen nem at starte og reproducere. Disse valg reducerer opsætningsarbejdet i udviklingsfasen, men vil i en mere produktionsnær løsning skulle suppleres med egentlige database migrations og en mere kontrolleret deployment-strategi.

