
import { useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";

const API_BASE_URL = "http://localhost:8080/api";

function SeatSelection() {
  const { showId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();

  const [movie, setMovie] = useState(location.state?.movie || null);
  const [show, setShow] = useState(location.state?.show || null);
  const [seatLayout, setSeatLayout] = useState(null);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    const controller = new AbortController();

    async function loadBookingDetails() {
      try {
        setLoading(true);
        setError("");

        const response = await fetch(
          `${API_BASE_URL}/shows/${showId}/seats`,
          { signal: controller.signal }
        );

        if (!response.ok) {
          throw new Error(`Failed to load seats (${response.status}).`);
        }

        const data = await response.json();
        setSeatLayout(data);

        // Support opening this URL directly without navigation state.
        if (!movie || !show) {
          const moviesResponse = await fetch(`${API_BASE_URL}/movies`, {
            signal: controller.signal,
          });

          if (!moviesResponse.ok) {
            throw new Error("Unable to load movie details.");
          }

          const movies = await moviesResponse.json();

          const foundMovie =
            movies.find(
              (item) => String(item.id) === String(data.movieId)
            ) ||
            movies.find(
              (item) => item.title === data.movieTitle
            );

          if (foundMovie) setMovie(foundMovie);

          const showDate = data.showDate;
          const showsResponse = await fetch(
            `${API_BASE_URL}/movies/${foundMovie?.id}/shows?date=${showDate}`,
            { signal: controller.signal }
          );

          if (foundMovie && showsResponse.ok) {
            const shows = await showsResponse.json();
            const foundShow = shows.find(
              (item) => String(item.showId) === String(showId)
            );

            if (foundShow) setShow(foundShow);
          }
        }
      } catch (err) {
        if (err.name !== "AbortError") {
          setError(err.message || "Unable to load the seat layout.");
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    loadBookingDetails();

    return () => controller.abort();
  }, [showId]);

  const rows = Number(seatLayout?.rows) || 0;
  const cols = Number(seatLayout?.cols) || 0;

  const bookedSeats = Array.isArray(seatLayout?.bookedSeats)
    ? seatLayout.bookedSeats.map(String)
    : [];

  function toggleSeat(seat) {
    if (bookedSeats.includes(seat)) return;

    setSelectedSeats((current) =>
      current.includes(seat)
        ? current.filter((item) => item !== seat)
        : [...current, seat]
    );
  }

  const price = Number(show?.price) || Number(seatLayout?.price) || 0;
  const total = selectedSeats.length * price;

  async function continueBooking() {
  if (!selectedSeats.length) return;

  const token = localStorage.getItem("token");

  if (!token) {
    navigate("/login", {
      state: { from: location },
    });
    return;
  }

  try {
    setError("");

    const response = await fetch(`${API_BASE_URL}/bookings`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        showId: Number(showId),
        seats: selectedSeats,
      }),
    });

    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
      throw new Error(
        data.message || data.error || "Unable to create booking."
      );
    }

    navigate(`/booking/${data.id || data.bookingId}`, {
      state: { movie, show, selectedSeats, booking: data },
    });
  } catch (err) {
    setError(err.message || "Unable to create booking.");
  }
}

  if (loading) {
    return (
      <section className="booking-section">
        <p className="empty-state">Loading seat layout...</p>
      </section>
    );
  }

  if (error || !seatLayout) {
    return (
      <section className="booking-section">
        <button
          className="secondary-button"
          onClick={() => navigate(-1)}
        >
          ← Go back
        </button>
        <p className="empty-state" role="alert">
          {error || "Seat layout is unavailable."}
        </p>
      </section>
    );
  }

  return (
    <section className="booking-section">
      <button
        className="secondary-button"
        onClick={() => navigate(-1)}
      >
        ← Back to showtimes
      </button>

      <div className="section-heading">
        <div>
          <div className="section-kicker">YOUR MOVIE EXPERIENCE</div>
          <h2>
            Select your <span>seats.</span>
          </h2>
          <p>
            {movie?.title || seatLayout.movieTitle || "Selected movie"}
          </p>
          <p>
            {show?.theatreName || seatLayout.theatreName || "Cinema"}
            {show?.area ? ` · ${show.area}` : ""}
          </p>
        </div>
      </div>

      <div className="booking-panel">
        <div className="screen">
          <div className="screen-glow" />
          <span>SCREEN THIS WAY</span>
        </div>

        <div className="seat-map">
          {Array.from({ length: rows }, (_, rowIndex) => {
            const row = String.fromCharCode(65 + rowIndex);

            return (
              <div className="seat-row" key={row}>
                <span className="row-label">{row}</span>

                {Array.from({ length: cols }, (_, colIndex) => {
                  const seat = `${row}${colIndex + 1}`;
                  const booked = bookedSeats.includes(seat);
                  const selected = selectedSeats.includes(seat);

                  return (
                    <button
                      key={seat}
                      disabled={booked}
                      onClick={() => toggleSeat(seat)}
                      className={`seat ${
                        booked
                          ? "booked"
                          : selected
                            ? "selected"
                            : ""
                      }`}
                      aria-label={`Seat ${seat}${
                        booked
                          ? ", booked"
                          : selected
                            ? ", selected"
                            : ", available"
                      }`}
                    >
                      {colIndex + 1}
                    </button>
                  );
                })}

                <span className="row-label">{row}</span>
              </div>
            );
          })}
        </div>

        <div className="seat-legend">
          <span>
            <i className="legend-seat available" /> Available
          </span>
          <span>
            <i className="legend-seat chosen" /> Selected
          </span>
          <span>
            <i className="legend-seat unavailable" /> Booked
          </span>
        </div>

        <div className="booking-summary">
          <div>
            <span className="summary-label">YOUR SEATS</span>
            <strong>
              {selectedSeats.length
                ? selectedSeats.join(", ")
                : "Choose your seats"}
            </strong>
          </div>

          <div>
            <span className="summary-label">ESTIMATED TOTAL</span>
            <strong>₹{total}</strong>
          </div>

          <button
            className="primary-button"
            disabled={selectedSeats.length === 0}
            onClick={continueBooking}
          >
            Continue <span>→</span>
          </button>
        </div>
      </div>
    </section>
  );
}

export default SeatSelection;

