
# ShowTime — Movie Ticket Booking Platform

A full-stack movie ticket booking application built with **React, Spring Boot, Java, and MySQL**. ShowTime allows users to discover movies, explore theatres and showtimes, select seats, manage bookings, and complete payments through Razorpay's test-mode checkout.

The project uses a separate show service to manage theatre and show information, while the main backend handles authentication, bookings, and payments.

---

## Overview

ShowTime is a web-based movie ticket booking system designed to provide a simple and convenient booking experience.

Users can browse movies, search for titles, view available shows, select seats, create bookings, complete payment through Razorpay's test checkout, and view their booking history.

The application follows a client-server architecture:

- **Frontend:** React provides the user interface and communicates with the backend through REST APIs.
- **Backend:** Spring Boot handles authentication, business logic, bookings, and payments.
- **Show Service:** A separate Spring Boot service manages theatre and show information.
- **Database:** MySQL stores application data.
- **Movie Data:** The backend integrates with The Movie Database (TMDB) for movie information.
- **Payment Gateway:** Razorpay provides the test-mode payment checkout.


## Features

### Movie Discovery

- Browse the available movie catalogue.
- Search movies by title.
- View movie posters and available movie information.
- Open an individual movie page to explore its shows.

### Theatre and Showtime Selection

- View shows associated with a selected movie.
- Browse available show dates.
- View theatre information and showtimes.
- Choose a show before proceeding to seat selection.

### Interactive Seat Selection

- Display the seat layout for a selected show.
- Identify booked seats and available seats.
- Select and deselect available seats.
- Calculate the booking total based on selected seats.
- Prevent users from selecting seats that are already booked.

### User Authentication

- Register with a name, email address, and password.
- Log in using registered credentials.
- Maintain the authenticated session using a token.
- Protect booking and other authenticated actions.
- Log out of the application.

### Booking Management

- Create a booking for a selected show and its seats.
- View booking details and booking status.
- Access booking history.
- Cancel eligible unpaid bookings.

Confirmed, paid bookings cannot be cancelled through the current demonstration workflow.

### Razorpay Payment Integration

- Create payment orders through the backend.
- Open Razorpay checkout.
- Verify payment details through the backend.
- Associate payment information with the corresponding booking.
- Display booking confirmation after a successful payment flow.

**Note:** Razorpay is configured for test-mode use. This project is not a production ticketing service.

### Web Interface

- Component-based React architecture.
- Client-side navigation using React Router.
- Responsive page styling.
- Separate pages for movie browsing, show selection, seat selection, authentication, booking confirmation, and booking history.

---

## Technology Stack

| Layer | Technologies |
|---|---|
| Frontend | React, JavaScript, HTML5, CSS3 |
| Frontend tooling | Node.js, npm, Vite |
| Backend | Java, Spring Boot |
| API communication | REST APIs, JSON, HTTP |
| Database | MySQL |
| Authentication | Token-based authentication, password hashing |
| Movie information | TMDB API |
| Payment integration | Razorpay Test Mode |
| Build tools | Maven, npm |
| Version control | Git and GitHub |

---

## Architecture

```text
                 ┌────────────────────────┐
                 │      User / Browser    │
                 └───────────┬────────────┘
                             │
                             ▼
                 ┌────────────────────────┐
                 │    React Frontend      │
                 │    Vite Development    │
                 │    Server : 5173       │
                 └───────────┬────────────┘
                             │ REST API / JSON
                             ▼
                 ┌────────────────────────┐
                 │   Spring Boot Backend  │
                 │        : 8080          │
                 │                        │
                 │ Authentication          │
                 │ Movies                  │
                 │ Bookings                │
                 │ Payments                │
                 └───────┬────────┬───────┘
                         │        │
               ┌─────────▼───┐  ┌─▼────────────────┐
               │    MySQL    │  │ External Services │
               │ Application │  │ TMDB / Razorpay   │
               │    Data     │  └───────────────────┘
               └─────────────┘
                         ▲
                         │ REST API
                 ┌───────┴────────────────┐
                 │  Show Management       │
                 │  Spring Boot Service   │
                 │        : 8081          │
                 │  Theatres and Shows    │
                 └───────────┬────────────┘
                             │
                             ▼
                 ┌────────────────────────┐
                 │  MySQL Show Database   │
                 └────────────────────────┘
```

### Service Responsibilities

**React frontend**
- Renders the pages and interactive components.
- Sends requests to the backend.
- Manages navigation, form input, and visible application state.

**Main Spring Boot backend**
- Handles user registration and login.
- Provides movie-related endpoints.
- Creates and retrieves bookings.
- Coordinates seat selection and payment workflows.
- Integrates with TMDB and Razorpay.

**Show management service**
- Provides theatre and show information.
- Supplies show data required for the movie booking workflow.
- Uses its own database configuration.

**MySQL**
- Persists application records, including user, booking, and show-related data.

---

## Prerequisites

Install the following before running the project:

| Requirement | Purpose |
|---|---|
| Java JDK 17 or newer | Runs the Spring Boot services |
| Maven 3.8+ | Builds and runs the Java applications |
| Node.js and npm | Installs frontend dependencies and runs Vite |
| MySQL 8+ | Stores application data |
| Git | Clones and manages the repository |
| TMDB API key | Retrieves movie information |
| Razorpay test API keys | Enables test-mode payments |

Verify the installations:

```bash
java -version
mvn -version
node -v
npm -v
git --version
```

Make sure the MySQL server is running before starting the backend services.

---

## Installation and Setup

### 1. Clone the repository

```bash
git clone https://github.com/berwincr/ShowTime.git
cd ShowTime
```

### 2. Configure MySQL

Ensure that your MySQL server is running locally.

The project uses separate database configurations for the main backend and show service. Check the respective configuration files to determine the database names and connection settings used by your checkout.

Set the database username and password through environment variables or your local Spring Boot configuration.

For example, PowerShell:

```powershell
$env:DB_USER = "root"
$env:DB_PASSWORD = "your_mysql_password"
```

### 3. Configure the TMDB API key

Create a TMDB account and obtain an API key from:

https://www.themoviedb.org/settings/api

Configure the key using the variable expected by the main backend, commonly:

```text
TMDB_API_KEY=your_tmdb_api_key
```

Without a valid TMDB key, the backend may fall back to its built-in sample movie data.

### 4. Configure Razorpay

Create an account at:

https://dashboard.razorpay.com/

Switch to **Test Mode** and generate API keys.

Configure the backend with the test credentials:

```text
RAZORPAY_KEY_ID=rzp_test_your_key_id
RAZORPAY_KEY_SECRET=your_test_key_secret
```

### 5. Install frontend dependencies

Open a terminal in the repository root:

```bash
cd showtime-web
npm install
```

---

## Running the Application

Run the frontend and the two backend services in separate terminals.

### Terminal 1: Start the show management service

From the repository root:

```bash
cd showtime-show-service
mvn spring-boot:run
```

The show service is expected to run on port `8081`.

### Terminal 2: Start the main backend

Open another terminal from the repository root:

```bash
cd showtime-backend
mvn spring-boot:run
```

The main backend runs on port `8080`.

Wait for Spring Boot to finish starting before using the frontend.

### Terminal 3: Start the React frontend

Open another terminal:

```bash
cd showtime-web
npm run dev
```

Open the local URL printed by Vite. The usual development URL is:

http://localhost:5173

If Vite selects a different port because the default is occupied, use the URL it prints and ensure the backend's CORS configuration and payment return URL are compatible with that port.

### Verify the services

Check the movie endpoint:

http://localhost:8080/api/movies

Check the show service's theatre endpoint:

http://localhost:8081/api/theatres

These endpoints should return JSON when the respective services are running and correctly configured.

---

##  Application Workflow

### Step 1: Browse movies

Open the frontend and explore the movie catalogue. Use the search feature to find a movie by title.

### Step 2: Select a movie and show

Open a movie's details page, choose a date, and select an available theatre showtime.

### Step 3: Select seats

Choose available seats from the seat layout. The application calculates the total price based on the selected seats and the show's pricing.

### Step 4: Register or log in

Authenticate with an existing account or register a new account if required before booking.

### Step 5: Create a booking

Submit the selected show and seats. The backend validates the request and creates the booking.

### Step 6: Complete payment

Continue to Razorpay checkout and use test-mode payment details. The backend handles the order and payment verification workflow.

### Step 7: View booking confirmation

After a successful payment flow, view the booking details and payment status.

### Step 8: Manage bookings

Open booking history to review previous bookings and cancel eligible unpaid bookings.

---

## API Endpoints

The following endpoints are exposed by the application. Protected endpoints require authentication where configured.

Base URL:

```text
http://localhost:8080/api
```

### Movies

| Method | Endpoint | Description |
|---|---|---|
| GET | `/movies` | Retrieve movies |
| GET | `/movies?query={title}` | Search movies |
| GET | `/movies/{id}/shows?date=YYYY-MM-DD` | Retrieve shows for a movie and date |

### Shows and Seats

| Method | Endpoint | Description |
|---|---|---|
| GET | `/shows/{showId}/seats` | Retrieve seat availability for a show |

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/register` | Register a user |
| POST | `/auth/login` | Authenticate a user |
| POST | `/auth/logout` | Log out an authenticated user |

Example registration request:

```json
{
  "name": "Demo User",
  "email": "demo@example.com",
  "password": "your_password"
}
```

Example login request:

```json
{
  "email": "demo@example.com",
  "password": "your_password"
}
```

Use the actual response from the backend to obtain the authentication token. Protected requests use the expected authorization header:

```http
Authorization: Bearer YOUR_ACCESS_TOKEN
```

### Bookings

| Method | Endpoint | Description |
|---|---|---|
| POST | `/bookings` | Create a booking |
| GET | `/bookings/my` | Retrieve the current user's bookings |
| GET | `/bookings/{id}` | Retrieve a booking by ID |
| POST | `/bookings/{id}/cancel` | Cancel an eligible booking |

Example booking request:

```json
{
  "showId": 207,
  "seats": ["A1", "A2"]
}
```

Use a valid show ID and seat labels returned by the running application. The backend remains responsible for validating booking data and availability.

### Payments

| Method | Endpoint | Description |
|---|---|---|
| POST | `/payments/order` | Create a payment order |
| POST | `/payments/verify` | Verify payment information |
| POST | `/payments/failed` | Process a failed payment flow |

The payment request and response formats depend on the backend DTOs. Use the corresponding request classes or Postman examples for the exact payloads.

### Show Service

Base URL:

```text
http://localhost:8081/api
```

| Method | Endpoint | Description |
|---|---|---|
| GET | `/theatres` | Retrieve theatre information |

Refer to the show service controllers for any additional endpoints available in your current checkout.

---

## Database

ShowTime uses MySQL for persistent application data.

The main backend and show management service have separate database configurations.

The main application database supports the data needed for users, bookings, seats, and payments. The show service database supports theatre and show information.

Database initialization and table creation depend on the Spring Boot configuration in each service.

Before running the application, verify:

- MySQL is running.
- The configured database credentials are valid.
- The configured database names are accessible.
- The application has permission to create or modify tables if automatic schema initialization is enabled.

Do not manually change table structures without checking the corresponding JPA entities and database configuration.

---

##  Payment Integration

ShowTime integrates with Razorpay to demonstrate a payment-enabled booking workflow.

The general flow is:

1. The user creates a booking.
2. The frontend requests a payment order from the backend.
3. The backend creates the order using Razorpay credentials.
4. The user completes the test checkout.
5. Payment details are submitted for backend verification.
6. The backend updates the booking according to the payment result.

### Test Mode

Use Razorpay's official test credentials and test payment methods. Test-mode transactions do not represent real payments.

Official documentation:

https://razorpay.com/docs/payments/

**Important:** Never use production payment credentials in a public demo or expose the Razorpay secret key in frontend code.

---

## Security

The project includes security-related measures such as:

- Password hashing using Spring Security's password encoder.
- Token-based authentication for protected requests.
- Backend authorization checks for user-specific booking operations.
- Server-side booking and payment processing.
- Separation of frontend code from backend secrets.
- CORS configuration for the local frontend origin.


---

## Troubleshooting

### Frontend cannot connect to the backend

- Confirm that the main backend is running on port `8080`.
- Check the browser console and network tab for failed requests.
- Verify the frontend API base URL.
- Confirm that CORS allows the frontend's actual development origin.

### Movie information is missing

- Check that the main backend has started successfully.
- Verify the TMDB API key and related backend logs.
- Check whether the application is using its sample movie data fallback.

### Theatre or show information is unavailable

- Start the show management service on port `8081`.
- Check its startup logs and database connection.
- Verify the configured service URL used by the main backend.

### MySQL connection fails

- Confirm that MySQL is running.
- Check the database username and password.
- Verify the configured database names and connection properties.
- Inspect the Spring Boot logs for the underlying database error.

### Login or registration fails

- Confirm that the backend is running.
- Check the request payload and response in the browser's network tab.
- Review backend validation and authentication logs.
- Verify that the database is accessible.

### Razorpay checkout fails

- Confirm that both Razorpay test keys are configured.
- Ensure the key ID and secret belong to the same mode.
- Check the backend logs for order creation or verification errors.
- Verify that the frontend payment return URL points to the correct Vite port.

### Port is already in use

Stop the application using the port or configure a different port. If you change the frontend port, update any origin-dependent settings as well.

---

## Future Enhancements

Possible extensions to the current project include:

- Admin dashboard for managing movies, theatres, and shows.
- More comprehensive booking and payment status tracking.
- Automated integration and end-to-end tests.
- Improved accessibility and responsive design.
- Email notifications for booking confirmation.
- Deployment with environment-specific configuration.
- Enhanced monitoring, logging, and error reporting.
- Additional account-management and password-recovery flows.

These are potential improvements and are not necessarily implemented in the current version.

---

## Contributing

Contributions and suggestions are welcome.

1. Fork the repository.
2. Create a feature branch.
3. Make your changes.
4. Test the frontend and the affected backend services.
5. Commit your changes with a descriptive message.
6. Open a pull request describing the changes.

Keep API keys, passwords, database credentials, and other sensitive configuration out of commits.

---

## License

No license is specified in this README. Before reusing or redistributing the project, add an appropriate `LICENSE` file to the repository.

---

## Author

**Berwin C. R.**

GitHub: [@berwincr](https://github.com/berwincr)

Project Repository: [ShowTime](https://github.com/berwincr/ShowTime)

---

<p align="center">
  <strong>ShowTime — Discover movies. Choose your seats. Enjoy the show. </strong>
</p>
