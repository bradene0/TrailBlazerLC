# Security notes

- Mapbox token and OpenWeather key are hard coded in JS source.
- Auth0 is frontend only; backend does not validate tokens.
- Trips are tied to `userName` string, so the API is not protected.
