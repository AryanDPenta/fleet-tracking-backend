# Fleet Tracking Backend

Spring Boot backend for the fleet-tracking system: driver GPS trip tracking,
admin live map, idle/rest detection, break notifications, and daily
drive-time threshold monitoring.

## Stack
- Java 17, Spring Boot 3.3
- Spring Web, Spring Data JPA, Spring Security (JWT, stateless), Spring WebSocket (STOMP/SockJS)
- PostgreSQL (swap the driver/URL in `application.yml` for MySQL if you prefer)
- Lombok

## Before you run it
1. Install a JDK 17+ and Maven (this project wasn't built/compiled in the sandbox
   it was generated in — there was no network access to Maven Central there — so
   run `mvn clean install` locally to pull dependencies and confirm it compiles).
2. Create a Postgres database, e.g. `fleet_tracking`.
3. Edit `src/main/resources/application.yml`:
   - `spring.datasource.*` — your DB credentials
   - `app.jwt.secret` — replace with a long random string (never commit the real one)
   - `app.cors.allowed-origins` — your React app's dev/prod URL
4. `mvn spring-boot:run`

## Getting data in for the first time
There's no UI yet, so bootstrap via the API (Postman/curl):
1. `POST /api/auth/register-company` — creates the admin/company account, returns a JWT
2. Using that admin JWT: `POST /api/admin/drivers` and `POST /api/admin/trucks`
3. Driver logs in: `POST /api/auth/login` with `{ identifier: phone, password }`
4. Driver starts a trip: `POST /api/trips/start`

## Key endpoints

| Method | Path | Who | Purpose |
|---|---|---|---|
| POST | /api/auth/register-company | public | Create admin/company account |
| POST | /api/auth/login | public | Login (email=admin, phone=driver) |
| POST | /api/admin/drivers | admin | Add a driver |
| POST | /api/admin/trucks | admin | Add a truck |
| POST | /api/trips/start | driver | Start a trip (truck + source + dest) |
| POST | /api/trips/{id}/end | driver | End a trip |
| GET  | /api/trips/active | driver | Get current active trip |
| POST | /api/locations/ping | driver | Send a GPS location update |
| POST | /api/trips/{tripId}/status-event | driver | Report breakfast/lunch/dinner/resumed/stop |
| GET  | /api/admin/live-trucks | admin | Latest position of every truck on an active trip |
| GET  | /api/admin/drivers | admin | List drivers |
| GET  | /api/admin/trips | admin | List all trips |
| GET  | /api/admin/analytics | admin | Trip/driver summary stats |
| POST | /api/alerts/send | admin | Send a message to a specific driver |
| GET  | /api/alerts/admin | admin | Idle/threshold/status alerts |
| GET  | /api/alerts/driver | driver | Admin messages + own threshold warnings |
| POST | /api/alerts/{id}/ack | either | Acknowledge an alert |
| GET  | /api/driver/summary | driver | Own day-by-day drive-time-vs-threshold history |

## WebSocket (STOMP over SockJS)
Connect: `new SockJS("http://localhost:8080/ws")`, then STOMP-connect (no auth
handshake wired in yet — see "Known gaps" below).

Subscribe to:
- `/topic/company/{companyId}/locations` — live truck positions (`LiveTruckDTO`), pushed on every ping
- `/topic/company/{companyId}/alerts` — idle/threshold/status alerts for the admin dashboard
- `/topic/driver/{driverId}/alerts` — messages/warnings for a specific driver

## How the core logic works

**Idle/rest detection** (`IdleDetectionService`): every incoming ping, and a
scheduled sweep every 5 min (`IdleCheckJob`), checks the last hour of pings
for a trip. If they're all within `app.idle.radius-meters` (default 100m) of
each other and span >= `app.idle.idle-threshold-minutes` (default 60), an
`IDLE_DETECTED` alert fires to the admin. This is separate from a driver's
*declared* break (breakfast/lunch/dinner) — those are logged via
`/status-event` and shown to admin as `TRIP_STATUS_UPDATE`, not `IDLE_DETECTED`.

**Daily threshold** (`ThresholdService` + `DailyThresholdJob`, runs 00:05
daily for the previous day): sums each driver's trip time for the day, minus
time spent in declared breaks, compares to `app.threshold.daily-drive-minutes`
(default 480 = 8h). If under, both admin and driver get a `THRESHOLD_BREACH`
alert, and the shortfall carries forward into `DriverDaySummary.carryOverShortfallMinutes`
until a day meets the threshold again.

**Location ping retention** (`LocationPingCleanupJob`, runs 01:30 daily):
`location_pings` gets a row every ~15s per active trip, which adds up fast —
this job bulk-deletes any ping older than `app.location-ping.retention-days`
(default 30). Nothing in the app needs pings older than that: idle-detection
only looks back an hour, the live map only cares about each trip's latest
ping, and a completed trip's driving totals already live permanently in
`DriverDaySummary`. Adjust the retention window in `application.yml` if you
want raw GPS history kept longer (e.g. for a future "replay this trip's
route" feature) — just know the table grows accordingly.

## Known gaps / things to decide before production
- **WebSocket auth**: the STOMP endpoint is `permitAll`. For production, add a
  `ChannelInterceptor` that validates the JWT on the STOMP `CONNECT` frame
  (frontend passes the token in STOMP headers), otherwise anyone can subscribe
  to any company's topic if they guess the ID.
- **Company-scoped ID collisions**: alerts, live-trucks, etc. use `admin.getId()`
  as the company ID directly since a Company IS the admin login. That's fine
  for one-admin-per-company; if you need multiple admin users per company later,
  you'll want a separate `AdminUser` entity referencing `Company`.
- **Driver self-registration** is intentionally not exposed — only an admin can
  create drivers (`POST /api/admin/drivers`). Add self-registration only if
  your product actually wants that.
- **Threshold override**: `Trip.thresholdMinutesOverride` exists but there's no
  admin endpoint to set a per-driver default yet — currently only settable at
  trip-start time via the request body.
- No pagination on list endpoints (`/api/admin/trips`, alert lists) — fine for
  early testing, add it before you have real volume.
- No tests included — this is a structural scaffold to build on, not a finished product.
