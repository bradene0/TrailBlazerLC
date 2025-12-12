# Code Inventory

## Backend (Java)

Entities: AbstractEntity, Trips, Plants, Fauna, Geolocations  
DTOs: TripsFPDTO  
Controllers: HomeController, TripsController, PlantsController, FaunaListController, GeolocationsController  
Repositories: TripsRepository, TripsFPRepository, PlantsRepository, FaunaRepository, GeolocationsRepository  
Startup: DataSeeder  

## Frontend (React)

Routes: /, /about, /weather, /animals, /plants, /trip, /profile  
API calls: /animals, /plants, /parks, /trips/* plus OpenWeather  
Other: Mapbox GL map search, Auth0 login/profile
