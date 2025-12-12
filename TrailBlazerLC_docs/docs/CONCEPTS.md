# Concepts used in this project (try/catch, async/await, state, functions)

## try/catch (Java and JavaScript)

### High level (non technical)
try/catch is a safety net.
You “try” something that might fail, and if it fails you “catch” the error so the app can keep going or show a message.

### Medium level (junior dev)
- Java:
  - try/catch handles exceptions
  - Most common uses are file I/O, parsing, database calls, external services
  - try-with-resources closes things like file handles automatically
- JavaScript:
  - try/catch handles thrown errors
  - `await fetch(...)` only throws on network failures; for HTTP errors you usually check `response.ok` and then `throw`

### Low level (examples from this project)

#### Java: DataSeeder uses try/catch per seeding stage
`DataSeeder.run(...)` wraps `seedPlants()`, `seedFauna()`, and `seedParks()` in try/catch so a single bad CSV row does not prevent the server from starting.

#### Java: try-with-resources in CSV parsing
`parseCsv(...)` uses try-with-resources so `Reader` and `CSVParser` are closed even if parsing fails.

#### JavaScript: try/catch in HikeForm CSV load
`HikeForm.fetchData(...)` wraps CSV loading/parsing in try/catch and logs errors.

#### JavaScript: .catch(...) in HikeList delete
The delete button calls `fetch(url, { method: "DELETE" })...catch(...)` to log fetch failures.

## async/await (JavaScript)

### High level
async/await is a way to write “wait for the network” code without deeply nested callbacks.

### Medium level
- `async function` returns a Promise
- `await` pauses inside that function until the Promise resolves
- Pair it with try/catch and `response.ok` checking

### Low level (recommended pattern for this codebase)
```js
async function loadPlants() {
  try {
    const res = await fetch("http://localhost:8080/plants");
    if (!res.ok) throw new Error(`HTTP ${res.status}`);
    const json = await res.json();
    setData(json);
  } catch (err) {
    console.error("Failed to load plants:", err);
  }
}
```

## React state (`useState`) and side effects (`useEffect`)

### High level
State is the app’s memory while it is running.
When state changes, React automatically re-draws the UI.

### Medium level
- `useState(initialValue)` returns:
  - the current value
  - a setter function to update it
- `useEffect(effectFn, deps)` runs after render:
  - `[]` means run once on mount
  - `[x]` means run when x changes

### Low level (examples in this project)
- `Trip.js`:
  - `hikes` state is synced into localStorage
- `FaunaList.js` and `Plants.js`:
  - data is fetched once on mount and stored in state
- `WeatherSearch.js`:
  - coordinates are stored in state
  - weather fetch is triggered when longitude changes

## Functions and definitions

### High level
A function is a reusable action.
You can call it many times instead of repeating code.

### Medium level
In this project you will see:
- Java methods (functions on classes)
- JavaScript functions (named, arrow, async)
- React function components (functions that return JSX)

### Low level: concrete definitions

#### Function
A block of code you can call by name to do something.
- JS example: `function dateBuilder(d) { ... }`

#### Method
A function that belongs to a class in Java.
- Java example: `TripsController.updateTrip(...)`

#### Callback function
A function passed into another function to run later.
- JS example: `useEffect(() => { fetchInfo(); }, [])`

#### React function component
A function that returns UI (JSX).
- JS example: `const Plants = () => <div>...</div>;`
