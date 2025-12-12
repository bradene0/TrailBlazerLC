# API Reference (Spring Boot)

Base URL (local): `http://localhost:8080`

## Health

### GET `/health`
Returns a simple string to confirm the backend is running.

Response:
```text
TrailBlazers API is running
```

## Animals (Fauna)

### GET `/animals`
Returns all `Fauna` rows.

## Plants

### GET `/plants`
Returns all `Plants` rows.

## Parks (Geolocations)

### GET `/parks`
Returns all `Geolocations` rows.

## Trips

### GET `/trips/all/{userName}`
Fetch all trips for a userName.

### POST `/trips/add`
Create a trip. Accepts a `TripsFPDTO`.

Example request body:
```json
{
  "tripName": "Weekend hike",
  "location": "Ha Ha Tonka State Park",
  "date": "2025-12-12",
  "notes": "Saw a deer",
  "plants": [{ "id": 1 }],
  "fauna": [{ "id": 99 }],
  "userName": "Braden Evans"
}
```

### PUT `/trips/update/{id}`
Update an existing trip.

Notes:
- If `notes` is an empty string, it is stored as `null`.
- If the trip ID does not exist, the controller throws `new Error("trip not found")` (a candidate for improving into a 404).

### DELETE `/trips/delete/{id}`
Delete a trip.

## CORS
Most controllers are annotated with:
```java
@CrossOrigin("http://localhost:3000")
```
