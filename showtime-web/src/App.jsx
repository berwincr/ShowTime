
import { BrowserRouter, Routes, Route, Link } from "react-router-dom";

import Home from "./pages/Home";
import MovieDetails from "./pages/MovieDetails";
import SeatSelection from "./pages/SeatSelection";
import Login from "./pages/Login";
import Register from "./pages/Register";
import BookingConfirmation from "./pages/BookingConfirmation";
import BookingHistory from "./pages/BookingHistory";
import "./App.css";

function App() {
  return (
    <BrowserRouter>
      <div className="app">
        <nav className="navbar">
          <Link to="/" className="brand">
            <span className="brand-icon">S</span>
            <span>ShowTime</span>
          </Link>

          <div className="nav-links">
            <Link to="/">Movies</Link>

            {localStorage.getItem("token") && (
  <Link to="/bookings">My Bookings</Link>
)}

            {localStorage.getItem("token") ? (
              <button
                className="nav-auth-button"
                onClick={() => {
                  localStorage.removeItem("token");
                  localStorage.removeItem("userId");
                  localStorage.removeItem("userName");
                  localStorage.removeItem("userEmail");
                  window.location.href = "/";
                }}
              >
                Logout
              </button>
            ) : (
              <>
                <Link to="/login">Login</Link>
                <Link to="/register">Register</Link>
              </>
            )}
          </div>
        </nav>

        <main>
          <Routes>
            <Route path="/" element={<Home />} />

            <Route
              path="/movie/:movieId"
              element={<MovieDetails />}
            />

            <Route
              path="/booking/seats/:showId"
              element={<SeatSelection />}
            />

            <Route
  path="/booking/:id"
  element={<BookingConfirmation />}
/>
<Route path="/bookings" element={<BookingHistory />} />

            <Route path="/login" element={<Login />} />

            <Route path="/register" element={<Register />} />

            <Route
              path="*"
              element={
                <section className="empty-state">
                  <h2>Page not found</h2>
                  <Link to="/">Back to movies</Link>
                </section>
              }
            />
          </Routes>
        </main>

        <footer className="footer">
          <p>ShowTime · Your next movie experience starts here.</p>
        </footer>
      </div>
    </BrowserRouter>
  );
}

export default App;
