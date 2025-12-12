# Developer Guide

This guide explains how to add new Java objects (entities), expose them via the API, and use them in the React UI.

## 1. List of Java objects in this project

### 1.1 Entities (database tables)
- `AbstractEntity`  
  Base class that provides:
  - `id` primary key (generated)
  - `equals` / `hashCode` based on id

- `Trips`  
  A saved trip entry created by the user.

- `Plants`  
  Reference plant data loaded from CSV.

- `Fauna`  
  Reference animal data loaded from CSV.

- `Geolocations`  
  Reference park location data loaded from CSV.

### 1.2 DTOs (API payload helpers)
- `TripsFPDTO`  
  DTO used by `TripsController.addTrip(...)` as the request payload.

### 1.3 Repositories (database access)
- `TripsRepository` (main)
  - CRUD for Trips
  - `List<Trips> findByUserName(String userName)`

- `TripsFPRepository` (duplicate / redundant)
  - Another repository for Trips with the same `findByUserName(...)` method

- `PlantsRepository` (CRUD for Plants)
- `FaunaRepository` (CRUD for Fauna)
- `GeolocationsRepository` (CRUD for parks)

### 1.4 Controllers (REST endpoints)
- `HomeController`
  - `GET /health` (also `GET /`)

- `FaunaListController`
  - `GET /animals`

- `PlantsController`
  - `GET /plants`

- `GeolocationsController`
  - `GET /parks`

- `TripsController`
  - `GET /trips/all/{userName}`
  - `POST /trips/add`
  - `PUT /trips/update/{id}`
  - `DELETE /trips/delete/{id}`

- `WeatherController`
  - Present but currently has no endpoints implemented.

### 1.5 Startup / config
- `DataSeeder` (implements `CommandLineRunner`)
  - Runs on backend startup and seeds tables from CSV files.
  - Uses try/catch per seed stage so one failure does not prevent server startup.

## 2. How to create a new object (Entity) step by step

Example: add a new entity called `Trail`.

### High level (non technical)
You add a new “kind of thing” to the app by:
1. Defining it in the backend
2. Saving it in the database
3. Creating endpoints so the frontend can load it
4. Building a UI page so users can see it

### Medium level (junior dev)
Backend:
1. Create `Trail.java` in `back_end/.../model` and annotate it with `@Entity`
2. Decide relationships:
   - belongs to Park: `@ManyToOne Geolocations park`
   - belongs to Trip: could be `@ManyToMany` (a trail can be visited on many trips)
3. Create `TrailRepository extends JpaRepository<Trail, Integer>`
4. Create `TrailController`:
   - `GET /trails` returns all trails
   - `POST /trails` creates a trail
5. Optional: add CSV and seed it in `DataSeeder`

Frontend:
1. Create a page `Trails.js` that fetches `GET /trails`
2. Store results in `useState`
3. Render a list
4. Add the page to `App.js` routes and add a nav link in `Header.js`

### Low level (code templates)

#### 2.1 Entity template
```java
package com.liftoff.trail_blazers.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class Trail extends AbstractEntity {

    private String name;
    private double miles;

    @ManyToOne
    private Geolocations park;

    public Trail() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getMiles() { return miles; }
    public void setMiles(double miles) { this.miles = miles; }

    public Geolocations getPark() { return park; }
    public void setPark(Geolocations park) { this.park = park; }
}
```

#### 2.2 Repository template
```java
package com.liftoff.trail_blazers.data;

import com.liftoff.trail_blazers.model.Trail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrailRepository extends JpaRepository<Trail, Integer> {}
```

#### 2.3 Controller template
```java
package com.liftoff.trail_blazers.controllers;

import com.liftoff.trail_blazers.data.TrailRepository;
import com.liftoff.trail_blazers.model.Trail;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("http://localhost:3000")
@RequestMapping("/trails")
public class TrailController {
    private final TrailRepository repo;

    public TrailController(TrailRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Trail> all() {
        return repo.findAll();
    }

    @PostMapping
    public Trail create(@RequestBody Trail trail) {
        return repo.save(trail);
    }
}
```

#### 2.4 Frontend fetch template (React)
```js
import { useEffect, useState } from "react";

export default function Trails() {
  const [trails, setTrails] = useState([]);

  useEffect(() => {
    (async () => {
      try {
        const res = await fetch("http://localhost:8080/trails");
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const json = await res.json();
        setTrails(json);
      } catch (err) {
        console.error("Failed to load trails:", err);
      }
    })();
  }, []);

  return (
    <div className="page-container">
      <h1>Trails</h1>
      <ul>
        {trails.map(t => <li key={t.id}>{t.name} ({t.miles} mi)</li>)}
      </ul>
    </div>
  );
}
```

## 3. Parent / child and relationship rules

### High level
A parent “owns” child records, like a Trip owning a list of photos you added.

### Medium level
- One to many:
  - Parent has a list of children
  - Child has a foreign key back to parent
- Many to many:
  - Both sides are “peers”
  - Database uses a join table
- JPA owning side:
  - The owning side is the class without `mappedBy`
  - Only the owning side defines the join table mapping

### Low level (how this project does it)
Current relationships:
- `Trips` is the owning side of both many to many relations:
  - `@ManyToMany private List<Plants> plants;`
  - `@ManyToMany private List<Fauna> fauna;`
- `Plants` and `Fauna` are inverse sides:
  - `@ManyToMany(mappedBy = "plants") private List<Trips> trips;`
  - `@ManyToMany(mappedBy = "fauna") private List<Trips> trips;`

So, if you are thinking “parent/child”:
- `Trips` is the closest thing to the parent (because it owns the join tables).
- `Plants` and `Fauna` are shared lookup objects referenced by many trips.

## 4. How to add the new object to the Trip form

If your new entity should be selectable when creating a Trip (like Plants/Fauna):

1. Backend: add it to the Trip entity (relationship)
   - ex: `@ManyToMany private List<Trail> trails;`
2. Backend: update the DTO used by the form payload (like `TripsFPDTO`)
3. Frontend: fetch list of Trails in `HikeForm`
4. Frontend: add it to the payload when posting to `/trips/add`
5. Frontend: display it in `HikeList` (like it currently displays Plants Found and Animals Found)
