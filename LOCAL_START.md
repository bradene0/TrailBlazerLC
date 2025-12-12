# One-click local launch

Use the provided `start_local.sh` script to boot both the Spring Boot API and the React dev server from one command. This is ideal for demos where you just need everything running locally without re-learning the setup.

## Prerequisites
- Java 17+ installed and on your PATH (for the Spring Boot backend)
- Node.js 18+ and npm installed (for the React frontend)
- `curl` available for the built-in backend readiness check
- (Optional) A local MySQL instance if you want to use real data; otherwise the backend will boot with an in-memory H2 database by default.

## Optional environment overrides
The backend accepts overrides for the database via environment variables so you can point at MySQL (or any JDBC source) without editing files:

- `DB_URL` (defaults to `jdbc:h2:mem:trail_blazers;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE` for an embedded H2 database)
- `DB_USER` (defaults to `sa` for H2)
- `DB_PASS` (defaults to empty for H2)
- `DB_DRIVER` (defaults to `org.h2.Driver`)
- `SPRING_JPA_DATABASE` (defaults to `h2`; set to `mysql` when pointing at MySQL)
- `SPRING_JPA_SHOW_SQL` (defaults to `true`)
- `SPRING_JPA_DDL_AUTO` (defaults to `update`)
- `SPRING_JPA_DIALECT` (defaults to `org.hibernate.dialect.H2Dialect`)
- Data seeding controls (uses the bundled CSVs in `databases/`):
  - `DATA_SEED_ENABLED` (defaults to `true`; set to `false` to skip importing on startup)
  - `DATA_SEED_REFRESH` (defaults to `false`; set to `true` to wipe/reload data on each boot)
  - `DATA_SEED_BASE_PATH` (defaults to `../databases` when running from `back_end/`)
- `SPRING_JPA_SHOW_SQL` (defaults to `true`)
- `SPRING_JPA_DDL_AUTO` (defaults to `update`)
- `SPRING_JPA_DIALECT` (defaults to `org.hibernate.dialect.MySQL8Dialect`)

To target MySQL instead of the in-memory database, supply your JDBC settings, for example:
```bash
DB_URL="jdbc:mysql://localhost:3306/trail_blazers" DB_USER="trail_blazers" DB_PASS="trailblazerForever1!" DB_DRIVER="com.mysql.cj.jdbc.Driver" SPRING_JPA_DIALECT="org.hibernate.dialect.MySQL8Dialect" ./start_local.sh
```

## Usage
From the repository root:
```bash
./start_local.sh
```
What happens:
1. Starts the backend in the background (logs go to `back_end/build/bootRun.log`).
2. Waits for the backend health endpoint at `http://localhost:8080/health`, failing fast with a helpful message if it exits early.
3. Installs frontend dependencies on first run if needed.
4. Launches the React dev server in the foreground so you can stop everything with `Ctrl+C`.
5. Seeds the database from the included Missouri Department of Conservation CSVs (`databases/animal_information`, `databases/plant_information`, `databases/park_locations`) unless you disable it via `DATA_SEED_ENABLED=false`.

When you exit the script, the backend process is stopped automatically.

## Quick desktop shortcut (macOS/Linux)
1. Create a small launcher script somewhere convenient, e.g. `~/Desktop/trailblazers.sh`, containing:
   ```bash
   #!/usr/bin/env bash
   cd "$(dirname "$0")/TrailBlazerLC" && ./start_local.sh
   ```
2. Make it executable: `chmod +x ~/Desktop/trailblazers.sh`.
3. Double-click it (or run from a terminal) to start both servers.

## Running from IntelliJ IDEA
- Open the built-in terminal at the project root (the folder that contains `start_local.sh`, `back_end/`, and `front_end/`).
- If the terminal opens in a subdirectory, run `cd` back to the project root first.
- Execute `./start_local.sh` from there; the script handles building and starting both services for you.

## Notes
- The frontend still expects any Auth0 configuration it uses to be present in your environment (e.g., `REACT_APP_AUTH0_DOMAIN`, `REACT_APP_AUTH0_CLIENT_ID`). Add these before running if needed.
- If you do want MySQL, keep it running before launching the script; otherwise the backend will start with the built-in H2 database so you can demo without extra setup.
- CSV imports run once by default; set `DATA_SEED_REFRESH=true` if you want a clean reload on each start, or `DATA_SEED_ENABLED=false` to opt out entirely.
## Notes
- The frontend still expects any Auth0 configuration it uses to be present in your environment (e.g., `REACT_APP_AUTH0_DOMAIN`, `REACT_APP_AUTH0_CLIENT_ID`). Add these before running if needed.
- If you do want MySQL, keep it running before launching the script; otherwise the backend will start with the built-in H2 database so you can demo without extra setup.
