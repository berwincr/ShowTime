
import { useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";

const API_BASE_URL = "http://localhost:8080/api";

function MovieDetails() {
  const { movieId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();

  const [movie, setMovie] = useState(location.state?.movie || null);
  const [selectedDate, setSelectedDate] = useState("2026-10-22");
  const [shows, setShows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (movie && String(movie.id) === movieId) return;

    const controller = new AbortController();

    async function fetchMovie() {
      try {
        setLoading(true);
        setError("");

        const response = await fetch(`${API_BASE_URL}/movies`, {
          signal: controller.signal,
        });

        if (!response.ok) {
          throw new Error(`Failed to load movie (${response.status}).`);
        }

        const movies = await response.json();
        const found = movies.find(
          (item) => String(item.id) === movieId
        );

        if (!found) {
          throw new Error("Movie not found.");
        }

        setMovie(found);
      } catch (err) {
        if (err.name !== "AbortError") {
          setError(err.message || "Unable to load movie details.");
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    fetchMovie();

    return () => controller.abort();
  }, [movieId, movie]);

  useEffect(() => {
    if (!movieId || !selectedDate) return;

    const controller = new AbortController();

    async function fetchShows() {
      try {
        setShows([]);
        setError("");

        const response = await fetch(
          `${API_BASE_URL}/movies/${movieId}/shows?date=${selectedDate}`,
          { signal: controller.signal }
        );

        if (!response.ok) {
          throw new Error(`Failed to load showtimes (${response.status}).`);
        }

        const data = await response.json();
        setShows(Array.isArray(data) ? data : []);
      } catch (err) {
        if (err.name !== "AbortError") {
          setError(err.message || "Unable to load showtimes.");
        }
      }
    }

    fetchShows();

    return () => controller.abort();
  }, [movieId, selectedDate]);

  const groupedShows = shows.reduce((groups, show) => {
  const theatre = show.theatreName || "Other theatres";

  if (!groups[theatre]) {
    groups[theatre] = {
      theatreName: theatre,
      area: show.area || "",
      shows: [],
    };
  }

  groups[theatre].shows.push(show);

  return groups;
}, {});

  const dates = Array.from({ length: 7 }, (_, index) => {
    const date = new Date();
    date.setHours(12, 0, 0, 0);
    date.setDate(date.getDate() + index);

    return {
      value: [
        date.getFullYear(),
        String(date.getMonth() + 1).padStart(2, "0"),
        String(date.getDate()).padStart(2, "0"),
      ].join("-"),
      day: date.toLocaleDateString("en-IN", { weekday: "short" }),
      date: date.getDate(),
      month: date.toLocaleDateString("en-IN", { month: "short" }),
    };
  });

  function formatTime(time) {
    if (!time) return "Time unavailable";

    const [hours, minutes] = time.split(":").map(Number);

    return new Date(2000, 0, 1, hours, minutes).toLocaleTimeString(
      "en-IN",
      {
        hour: "2-digit",
        minute: "2-digit",
        hour12: true,
      }
    );
  }

  if (loading && !movie) {
    return <section className="booking-section"><p className="empty-state">Loading movie...</p></section>;
  }

  if (error && !movie) {
    return <section className="booking-section"><p className="empty-state">{error}</p></section>;
  }

  if (!movie) {
    return <section className="booking-section"><p className="empty-state">Movie not found.</p></section>;
  }

  return (
    <section className="booking-section">
      <button
        className="secondary-button"
        onClick={() => navigate("/")}
      >
        ← Back to movies
      </button>

      <div className="booking-panel">
        <div className="booking-movie">
          {movie.posterUrl && (
            <img src={movie.posterUrl} alt={movie.title} />
          )}

          <div>
            <span className="section-kicker">NOW SHOWING</span>
            <h2>{movie.title}</h2>
            <p>
              {Array.isArray(movie.genres)
                ? movie.genres.join(", ")
                : movie.genres || "Genre information unavailable"}
            </p>
            {movie.runtimeMinutes && <p>{movie.runtimeMinutes} min</p>}
            {movie.overview && <p>{movie.overview}</p>}
          </div>
        </div>

        <div className="booking-divider" />

        <div className="booking-step">
          <h3><span>01</span> Select a date</h3>

          <div className="date-options">
            {dates.map((date) => (
              <button
                key={date.value}
                className={`date-option ${
                  selectedDate === date.value ? "selected" : ""
                }`}
                onClick={() => setSelectedDate(date.value)}
              >
                <span>{date.day}</span>
                <strong>{date.date}</strong>
                <span>{date.month}</span>
              </button>
            ))}
          </div>
        </div>

        <div className="booking-step">
          <h3><span>02</span> Choose a theatre and showtime</h3>

          {error && <p className="empty-state" role="alert">{error}</p>}

          {!error && shows.length === 0 && (
            <p className="empty-state">
              No shows available for this date.
            </p>
          )}

         <div className="theatre-list">
  {Object.values(groupedShows).map((theatre) => (
    <article
      className="theatre-card"
      key={theatre.theatreName}
    >
      <div className="theatre-heading">
        <div className="theatre-icon">
          <span>▣</span>
        </div>

        <div className="theatre-details">
          <h4>{theatre.theatreName}</h4>
          <p>{theatre.area}</p>
        </div>

        <span className="show-count">
          {theatre.shows.length}{" "}
          {theatre.shows.length === 1 ? "show" : "shows"}
        </span>
      </div>

      <div className="showtime-slots">
        {theatre.shows.map((show) => (
          <button
            key={show.showId}
            className="showtime-slot"
            onClick={() =>
              navigate(`/booking/seats/${show.showId}`, {
                state: { movie, show },
              })
            }
          >
            <strong>{formatTime(show.showTime)}</strong>
            <span>₹{show.price}</span>
            <span className="slot-seats">
              {show.availableSeats} seats left
            </span>
            <span className="slot-action">Select seats →</span>
          </button>
        ))}
      </div>
    </article>
  ))}
</div>
        </div>
      </div>
    </section>
  );
}

export default MovieDetails;
