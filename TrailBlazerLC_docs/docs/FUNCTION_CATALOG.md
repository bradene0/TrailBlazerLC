# Function and Method Catalog (what important functions do)

This file lists the main functions/methods in the project and what they do.

## Backend (Java)

### TripsController
- `displayAllTrips(String userName)`
  - **Purpose:** Return all trips that match a given userName.
  - **Used by:** React Trip history list (`HikeList`) when it calls `/trips/all/{userName}`.

- `addTrip(TripsFPDTO newTrips)`
  - **Purpose:** Create and save a new Trips record from the form payload.
  - **Also sets:** tripName, location, date, notes, plants, fauna, userName.

- `updateTrip(int id, Trips updatedTrips)`
  - **Purpose:** Replace fields of an existing trip and save it back.
  - **Special behavior:** If notes is an empty string, it stores notes as null.

- `deleteTrip(int id)`
  - **Purpose:** Delete a trip by id.

### PlantsController
- `getAllPlants()`
  - **Purpose:** Return the entire Plants table.

### FaunaListController
- `getAllFauna()`
  - **Purpose:** Return the entire Fauna table.

### GeolocationsController
- `getAllParks()`
  - **Purpose:** Return the entire Geolocations table.

### DataSeeder
- `run(...)`
  - **Purpose:** Application startup hook that seeds database tables from CSV.
  - **Error handling:** try/catch per seeding stage.

- `seedPlants()`, `seedFauna()`, `seedParks()`
  - **Purpose:** Parse CSV rows, map them into Entities, and persist them.

- `parseCsv(Path csvPath)`
  - **Purpose:** Read and parse a CSV file into iterable records.
  - **Safety:** uses try-with-resources.

## Frontend (React)

### Trip.js
- `handleAddHike(newHike)`
  - **Purpose:** Add a “Just Added” hike to the in-memory list and localStorage.

- `handleEditHike(editedHike)`
  - **Purpose:** Update a “Just Added” hike entry in local state.

- `handleDeleteHike(id)`
  - **Purpose:** Remove a “Just Added” hike after a confirmation.

### HikeForm.js
- `fetchPlantsInfo()`, `fetchAnimalsInfo()`
  - **Purpose:** Load lists used for selection UI from the backend.

- `handleSubmit(e)`
  - **Purpose:** Submit the trip creation form.
  - **Network:** POST `/trips/add`
  - **Local state:** Also appends the trip to local hikes list via `onSubmit(...)`.

- `updateTrip(e)`
  - **Purpose:** Submit the trip update form.
  - **Network:** PUT `/trips/update/{id}`

- `deleteCheckmarks()`
  - **Purpose:** UI helper to hide checkmark icons and reset add buttons after submit.

- `handleToggleClick()`
  - **Purpose:** Toggle between Plants and Animals selection panels.

### HikeList.js
- `fetchInfo()`
  - **Purpose:** Load saved trips from backend `GET /trips/all/{user.name}`.

- Delete button inline handler
  - **Purpose:** Delete a saved trip in the backend with `DELETE /trips/delete/{id}`.

### faunaList.js / Plants.js
- `fetchInfo()`
  - **Purpose:** Load list from backend once on mount.

### weatherSearch.js
- `fetchInfo()`
  - **Purpose:** Load park coordinates from backend `GET /parks`.

- `fetchWeatherInfo()`
  - **Purpose:** Call OpenWeather with the selected park lat/lon and store the result.

- `dateBuilder(d)`
  - **Purpose:** Format a Date into a readable string.
