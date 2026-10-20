# ShowTime - Multi-Theatre Movie Ticket Booking

Java Swing desktop app + two Spring Boot services + MySQL.
Real-time movie data from **TMDB**, payments with **Razorpay (test mode)**.

```
 Swing client  --REST-->  showtime-backend (8080) --REST--> showtime-show-service (8081)
 (no libraries)           users, movies, bookings,           theatres + shows
                          seats, payments                    DB: showtime_shows
                          DB: showtime_db
                              |-- TMDB API (movies)   |-- Razorpay API (orders)
```

## 1. What you need (one time)

| Item | Notes |
|---|---|
| JDK 17 or newer | `java -version` |
| Maven 3.8+ | `mvn -version` (STS/Eclipse has it built in) |
| MySQL 8 | running on localhost:3306. The databases are created automatically |
| TMDB API key (free) | https://www.themoviedb.org/settings/api  (create account, request "Developer" key) |
| Razorpay TEST keys (free) | https://dashboard.razorpay.com -> switch to **Test Mode** -> Account & Settings -> API Keys -> Generate |

Without a TMDB key the app still runs using 6 built-in sample movies.
Without Razorpay keys everything works except the final payment step.

## 2. Configure

Set environment variables (recommended) **or** edit `showtime-backend/src/main/resources/application.properties`:

```
DB_USER=root
DB_PASSWORD=your_mysql_password
TMDB_API_KEY=xxxxxxxx
RAZORPAY_KEY_ID=rzp_test_xxxxxxxx
RAZORPAY_KEY_SECRET=xxxxxxxx
```
Windows (Command Prompt):  `set DB_PASSWORD=your_mysql_password`   (one `set` per variable, in the same window you start the services from)
Mac/Linux:                 `export DB_PASSWORD=your_mysql_password`

`DB_USER` / `DB_PASSWORD` are also read by `showtime-show-service`.

## 3. Run (3 terminals, in this order)

```
cd showtime-show-service
mvn spring-boot:run            # port 8081  (seeds 6 sample Chennai theatres)

cd showtime-backend
mvn spring-boot:run            # port 8080

cd showtime-client
mvn package
java -jar target/showtime-client.jar
```
In STS/Eclipse: *File > Import > Existing Maven Projects*, pick each folder, run `ShowServiceApplication`
and `ShowTimeApplication` as Spring Boot apps, and `App` (client) as a Java application.

Check: http://localhost:8080/api/movies and http://localhost:8081/api/theatres should return JSON.

## 4. Using the app

1. Register, then log in.
2. **Movies** tab: now-playing movies come live from TMDB. Search any title (not cached titles are looked up on TMDB).
3. Pick a date and a theatre/showtime, click **Select Seats**.
4. Choose seats (max 6) and click **Pay with Razorpay**. Your seats are held for 5 minutes.
5. The Razorpay page opens in your browser. Test payment:
   * UPI: `success@razorpay`   (failure test: `failure@razorpay`)
   * Card: `4111 1111 1111 1111`, any future expiry, any CVV, OTP `1234` if asked
6. The desktop app updates by itself and shows the ticket. **My Bookings** shows history.

## 5. What is real-time and what is not

* **Real-time (TMDB):** now-playing list, titles, posters, genres, language, runtime, rating, search.
* **Managed by us:** theatres, showtimes, prices and seats. India's theatre chains do not offer a public API.
  Showtimes are generated for the next 7 days per movie (3 per theatre per day). Theatre names are sample data:
  edit them through the show-service API (`POST /api/theatres`) or the `theatres` table.

## 6. How payment safety works

* The **server** calculates the amount and creates the Razorpay order. The client cannot change the price.
* After payment, Razorpay returns a signature. The backend recomputes
  `HMAC_SHA256(order_id + "|" + payment_id, key_secret)` and only then marks the booking `CONFIRMED`.
* Seats are protected by a UNIQUE database constraint `(show_id, seat_label)`, so two people can never book the same seat.
* Unpaid bookings expire after 5 minutes (8 minutes once the payment page was opened) and the seats are released.

Known limitations (fine for a college project): no refunds/webhooks (a payment made after the hold expired needs a manual
refund in the Razorpay dashboard), login tokens are kept in memory (restarting the backend logs everyone out),
all screens use the same 8x10 seat layout.

## 7. Syllabus concepts and where to find them

| Concept | Where |
|---|---|
| Spring MVC / REST (GET, POST) | `controller/*` , Postman collection in `postman/` |
| Spring Data JPA, CRUD | `repository/*`, `showtime-show-service` theatre CRUD |
| ORM relationships | `Booking -> User` (ManyToOne), `Payment -> Booking` (OneToOne), `SeatLock -> Booking`, `MovieShow -> Theatre` |
| HQL queries | `MovieRepository.search`, `BookingRepository.findHistory`, `ShowRepository.findForMovieOnDate` |
| Inheritance mapping | `demo/Staff` (JOINED) -> `Manager`, `Usher`. Try `GET /api/demo/hibernate` |
| SessionFactory / Session / Transaction | `demo/HibernateDemoService` |
| Dependency injection, BeanFactory | constructor injection everywhere; `GET /api/demo/context` |
| AOP logging | `config/LoggingAspect` (see console: `[AOP] BookingService.create finished in .. ms`) |
| Microservices | `showtime-show-service` (8081) called by `integration/ShowClient` |
| Maven / Git | one `pom.xml` per module, `.gitignore` included |

## 8. Troubleshooting

| Message | Fix |
|---|---|
| `Cannot reach the ShowTime server` | start `showtime-backend`; check port 8080 is free |
| `Show service is not reachable` | start `showtime-show-service` first |
| `Access denied for user 'root'` | set `DB_USER` / `DB_PASSWORD` |
| `Razorpay keys are not configured` | set the two Razorpay variables and restart the backend |
| `Razorpay error 401` | wrong keys, or live keys mixed with test keys |
| Movies show only 6 sample movies | `TMDB_API_KEY` missing/invalid (see backend log) |
| Browser does not open | the app shows the link; copy it into your browser |
