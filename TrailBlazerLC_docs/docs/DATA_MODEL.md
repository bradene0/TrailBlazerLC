# Data Model (JPA Entities)

## High level (non technical)
Think of the database like a set of lists:
- Trips are your saved journal entries
- Plants and Animals are master lists you can attach to your Trips
- Parks are a master list of park locations

## Medium level (junior dev)

### Entities
- `Trips`
- `Plants`
- `Fauna`
- `Geolocations`

### Relationships
- Trips to Plants: Many to Many
- Trips to Fauna: Many to Many
- Geolocations: currently standalone (Trips stores location as a string instead)

### What “parent/child” means here
- Many to many has no true parent in database terms.
- But in JPA there is an owning side and an inverse side.
- The owning side controls the join table mapping.

In this codebase:
- `Trips` is the owning side for both join tables.
- `Plants` and `Fauna` are inverse (`mappedBy`) sides.

## Low level (implementation detail)

### AbstractEntity
All entities extend `AbstractEntity`, which defines:
- `@Id @GeneratedValue private int id`
- `getId()`
- `equals` / `hashCode` based on id

### Trips
Fields:
- `tripName` (String)
- `location` (String)
- `date` (String)
- `notes` (String, nullable)
- `userName` (String)

Relations:
- `@ManyToMany private List<Plants> plants`
- `@ManyToMany private List<Fauna> fauna`

### Plants
Fields:
- `scientificName`, `commonName`, `currentDistribution`, `family`, `federalListingStatus`, `image`, `photoCredit`
Relation:
- `@ManyToMany(mappedBy = "plants") private List<Trips> trips`
JSON:
- `@JsonBackReference` prevents serializing Trips inside each Plant, avoiding infinite loops.

### Fauna
Similar to Plants, with fields like:
- `scientificName`, `commonName`, `currentDistribution`, `family`, `status`, `image`, `photoCredit`
Relation:
- `@ManyToMany(mappedBy = "fauna") private List<Trips> trips`
JSON:
- `@JsonBackReference` for loop prevention.

### Geolocations
Fields:
- `name`, `longitude`, `latitude`, `park_type`, `url`, `short_name`
No relations currently.

## If you want “Trips belongs to a Park” (recommended enhancement)
Instead of storing `Trips.location` as a string:
1. Add a real relationship:
   - `Trips` has `@ManyToOne Geolocations park`
2. Update request payloads to send `parkId`
3. Update UI to choose a park by id
