# TrailBlazersLC Full Project Documentation

## What this project is

### High level (non technical)
This app is like a digital trip journal for Missouri state parks.
You can browse parks, check the weather for a park, look up plants and animals found in Missouri, and save your own Trips with notes and what you saw.

### Medium level (junior dev)
- The frontend is a React single page app running on port 3000.
- The backend is a Spring Boot REST API running on port 8080.
- React calls the Spring API using `fetch()` to load lists and to create, edit, and delete Trips.
- The backend persists data using JPA entities and repositories.
- The default database is an in memory H2 database, but you can point it at MySQL using environment variables.

### Low level (implementation detail)
- Spring Boot 3.2, Java 17, Spring Web + Spring Data JPA.
- Default JDBC URL is `jdbc:h2:mem:trail_blazers;MODE=MYSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`.
- On startup, `DataSeeder` reads CSV files under `databases/` and inserts rows into the database.
- React pages fetch:
  - `/animals` for fauna
  - `/plants` for plants
  - `/parks` for park locations
  - `/trips/*` for trip CRUD
- Weather data is fetched directly from OpenWeather on the frontend.

## Tech stack

### High level
- Frontend: React website
- Backend: Java API server
- Database: H2 (default) or MySQL (optional)
- External APIs: OpenWeather, Mapbox, Auth0

### Medium level
Frontend (React):
- React 18, react-router-dom, styled-components
- Auth0 React SDK for login/logout and profile display
- Mapbox GL for the park map
- Browser localStorage used for “Just Added” trips in the UI

Backend (Spring Boot):
- Spring Web REST controllers
- Spring Data JPA repositories
- H2 database for local demo, with MySQL connector included
- Apache Commons CSV for CSV import

### Low level
Backend Gradle dependencies include:
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-validation`
- `com.h2database:h2` (runtime)
- `com.mysql:mysql-connector-j` (runtime)
- `org.apache.commons:commons-csv`

Frontend dependencies include:
- `react`, `react-router-dom`
- `@auth0/auth0-react`
- `mapbox-gl`
- `styled-components`
- `firebase` (installed but not used in current source files)

## How the parts interact (React + Spring Boot + DB + APIs)

### High level
Your browser (React) is the screen.
When you click around, the browser asks the Java backend for data (plants, animals, parks, trips).
The Java backend reads and writes that data in the database.
For weather, the browser also asks OpenWeather directly.

### Medium level
Typical request flow:
1. User opens a page in React
2. React runs `useEffect(() => fetchInfo(), [])`
3. `fetchInfo()` calls the Spring endpoint with `fetch()`
4. Spring controller calls a repository like `findAll()` or `save()`
5. JPA reads or writes rows in the database
6. Spring returns JSON
7. React updates state (`setData(...)`) and re-renders

### Low level
- CORS: the backend allows requests from `http://localhost:3000` using `@CrossOrigin`.
- Persistence:
  - Entities are annotated with `@Entity`.
  - Repositories extend `JpaRepository<YourEntity, Integer>`.
  - `save()` persists the entity, and Hibernate updates the underlying tables.
- Trip relationships:
  - `Trips` owns the `@ManyToMany` relationship to `Plants` and `Fauna` (no `mappedBy` on `Trips`).
  - This creates join tables behind the scenes in the database.


## Architecture diagram (mental model)

### High level
```
Browser (React UI)
   |  fetch() JSON
   v
Spring Boot API (Controllers -> Repositories)
   |  JPA / Hibernate
   v
Database (H2 in-memory by default, optional MySQL)
```

### Medium level
- Controllers define the URL routes and JSON payloads
- Repositories are the “data access layer”
- Entities are the database rows mapped into Java objects

### Low level
- React runs on `localhost:3000`
- Spring runs on `localhost:8080`
- CORS is enabled for `http://localhost:3000` on most controllers


## Feature overview by page

### Home (Map search)
High level:
- Shows a map of Missouri with parks. Hover a park and click its name to open the official park webpage.

Medium level:
- Uses Mapbox GL and a hard coded GeoJSON data source inside `FilterMap.js`.
- Uses DOM operations and Mapbox events to populate a listing and a text filter.

Low level:
- `mapboxgl.Map({ container: 'map', style: 'mapbox://styles/mapbox/streets-v12', ... })`
- `renderListings()` creates `<a>` elements and wires `mouseover` to show a popup.

### Animals
High level:
- Select an animal, see a picture and basic details.

Medium level:
- `FaunaList` fetches `/animals` from Spring Boot and stores it in React state.
- A dropdown chooses the animal.

Low level:
- `useState([])` stores the fetched fauna list
- `handleAnimalChange()` finds the selected item by matching `commonName`

### Plants
High level:
- Search for a plant by name, then click it to view details and an image.

Medium level:
- Fetches `/plants` once, then filters in memory based on search input.

Low level:
- Search uses `data.filter(...)` and `toLowerCase().includes(...)`.

### Weather
High level:
- Pick a park and see the current weather.

Medium level:
- Fetches `/parks` from Spring to get lat/lon.
- Calls OpenWeather directly from the browser with the chosen coordinates.

Low level:
- `fetchWeatherInfo()` does `fetch(`${api.base}weather?lat=${lat}&lon=${lon}&appid=${api.key}&units=imperial`)`

### Trips
High level:
- Logged in users can add trips, edit trips, and delete trips.
- You can attach plants and animals you saw.

Medium level:
- Trips are stored in the backend database and tied to a `userName` string.
- “Just Added” trips are also stored in localStorage in the browser.
- The trip form fetches plants and animals from `/plants` and `/animals`.
- Trip list fetches saved trips from `/trips/all/{userName}`.

Low level:
- Add trip: `POST /trips/add` with a payload matching `TripsFPDTO`
- Update trip: `PUT /trips/update/{id}`
- Delete trip: `DELETE /trips/delete/{id}`
- `Trips` has `@ManyToMany List<Plants>` and `@ManyToMany List<Fauna>`.

## Common pitfalls to be aware of
- API keys are hard coded in source (Mapbox token and OpenWeather key). Move them to env vars for real deployments.
- Auth0 is used only on the frontend. The backend does not verify tokens. It trusts the `userName` string sent from the browser.
- Some fetch calls do not check `response.ok`, and some do not have error handling.
- `WeatherController` exists but is empty. Weather is handled from the frontend.
