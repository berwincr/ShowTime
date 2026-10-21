import { useEffect, useState } from "react";
import { Link } from "react-router-dom";

const API_BASE_URL = "http://localhost:8080/api";

function BookingHistory() {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [cancellingId, setCancellingId] = useState(null);
  const [cancelMessage, setCancelMessage] = useState("");

  useEffect(() => {
    async function fetchBookings() {
      const token = localStorage.getItem("token");

      if (!token) {
        setError("Please log in to view your bookings.");
        setLoading(false);
        return;
      }

      try {
        const response = await fetch(`${API_BASE_URL}/bookings/my`, {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        });

        const data = await response.json().catch(() => ({}));

        if (!response.ok) {
          throw new Error(
            data.message || data.error || "Unable to load your bookings."
          );
        }

        setBookings(Array.isArray(data) ? data : []);
      } catch (err) {
        setError(err.message || "Unable to load your bookings.");
      } finally {
        setLoading(false);
      }
    }

    fetchBookings();
  }, []);

  const handleCancel = async (bookingId) => {
    const confirmed = window.confirm(
      "Are you sure you want to cancel this booking?"
    );

    if (!confirmed) return;

    setCancellingId(bookingId);
    setCancelMessage("");
    setError("");

    try {
      const response = await fetch(
        `${API_BASE_URL}/bookings/${bookingId}/cancel`,
        {
          method: "POST",
          headers: {
            Authorization: `Bearer ${localStorage.getItem("token")}`,
          },
        }
      );

      const data = await response.json().catch(() => ({}));

      if (!response.ok) {
        throw new Error(
          data.message || data.error || "Failed to cancel booking."
        );
      }

      setBookings((previousBookings) =>
        previousBookings.map((booking) =>
          booking.id === bookingId ? { ...booking, ...data } : booking
        )
      );

      setCancelMessage("Booking cancelled successfully.");
    } catch (err) {
      setError(err.message || "Failed to cancel booking.");
    } finally {
      setCancellingId(null);
    }
  };

  if (loading) {
    return (
      <section className="booking-section">
        <p>Loading your bookings...</p>
      </section>
    );
  }

  if (error && bookings.length === 0) {
    return (
      <section className="booking-section">
        <h2>My Bookings</h2>
        <p className="error-message">{error}</p>
        <Link to="/" className="primary-button">
          Browse movies
        </Link>
      </section>
    );
  }

  return (
    <section className="booking-section">
      <div className="section-heading">
        <div>
          <div className="section-kicker">YOUR SHOWTIME ACTIVITY</div>
          <h2>
            My <span>Bookings</span>
          </h2>
          <p>View your movie tickets and booking status.</p>
        </div>
      </div>

      {cancelMessage && (
        <p className="success-message">{cancelMessage}</p>
      )}

      {error && <p className="error-message">{error}</p>}

      {bookings.length === 0 ? (
        <div className="booking-panel">
          <h3>No bookings yet</h3>
          <p>Your movie bookings will appear here after you book tickets.</p>
          <Link to="/" className="primary-button">
            Explore movies →
          </Link>
        </div>
      ) : (
        <div className="booking-history-list">
          {bookings.map((booking) => {
            const isPending = booking.status === "PENDING";
            const isUnpaid =
              !booking.paymentStatus ||
              booking.paymentStatus === "PENDING" ||
              booking.paymentStatus === "FAILED";

            const canCancel = isPending && isUnpaid;

            return (
              <article className="booking-panel" key={booking.id}>
                <div className="section-heading">
                  <div>
                    <div className="section-kicker">
                      BOOKING #{booking.id}
                    </div>

                    <h3>{booking.movieTitle || "Movie"}</h3>
                    <p>{booking.theatreName || "Theatre"}</p>
                  </div>

                  <span className="booking-status">
                    {booking.status || "UNKNOWN"}
                  </span>
                </div>

                <div className="booking-summary">
                  <div>
                    <span className="summary-label">SHOWTIME</span>
                    <strong>
                      {booking.showTime
                        ? new Date(booking.showTime).toLocaleString()
                        : "—"}
                    </strong>
                  </div>

                  <div>
                    <span className="summary-label">SEATS</span>
                    <strong>{booking.seats || "—"}</strong>
                  </div>

                  <div>
                    <span className="summary-label">TICKETS</span>
                    <strong>{booking.seatCount ?? "—"}</strong>
                  </div>

                  <div>
                    <span className="summary-label">AMOUNT</span>
                    <strong>
                      {booking.amount != null
                        ? `₹${booking.amount}`
                        : "—"}
                    </strong>
                  </div>

                  <div>
                    <span className="summary-label">PAYMENT</span>
                    <strong>{booking.paymentStatus || "—"}</strong>
                  </div>
                </div>

                {canCancel && (
                  <button
                    type="button"
                    className="secondary-button"
                    onClick={() => handleCancel(booking.id)}
                    disabled={cancellingId !== null}
                  >
                    {cancellingId === booking.id
                      ? "Cancelling..."
                      : "Cancel Booking"}
                  </button>
                )}
              </article>
            );
          })}
        </div>
      )}

      <Link to="/" className="secondary-button">
        ← Back to movies
      </Link>
    </section>
  );
}

export default BookingHistory;