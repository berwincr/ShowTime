import { useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";

const API_BASE_URL = "http://localhost:8080/api";

function BookingConfirmation() {
  const { id } = useParams();
  const location = useLocation();
  const navigate = useNavigate();

  const paymentSucceeded =
  new URLSearchParams(location.search).get("payment") === "success";

 const [booking, setBooking] = useState(location.state?.booking || null);
  const movie = location.state?.movie;
  const show = location.state?.show;
  const selectedSeats = location.state?.selectedSeats || [];

  useEffect(() => {
  async function fetchBooking() {
    const token = localStorage.getItem("token");

    if (!token) {
      setError("Please log in to view your booking details.");
      return;
    }

    try {
      const response = await fetch(`${API_BASE_URL}/bookings/${id}`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      const data = await response.json().catch(() => ({}));

      if (!response.ok) {
        throw new Error(
          data.message || data.error || "Unable to load booking details."
        );
      }

      setBooking(data);
    } catch (err) {
      setError(err.message || "Unable to load booking details.");
    }
  }

  fetchBooking();
}, [id]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  async function proceedToPayment() {
    const token = localStorage.getItem("token");

    if (!token) {
      navigate("/login", { state: { from: location } });
      return;
    }

    setLoading(true);
    setError("");

    try {
      const response = await fetch(`${API_BASE_URL}/payments/order`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          bookingId: Number(id),
        }),
      });

      const data = await response.json().catch(() => ({}));

      if (!response.ok) {
        throw new Error(
          data.message || data.error || "Could not create the payment order."
        );
      }

      if (!data.payUrl) {
        throw new Error("The backend did not return a Razorpay checkout URL.");
      }

      window.location.href = data.payUrl;
    } catch (err) {
      setError(err.message || "Unable to start payment.");
      setLoading(false);
    }
  }

  return (
    <section className="booking-section">
      <button className="secondary-button" onClick={() => navigate("/")}>
        ← Back to movies
      </button>

      <div className="booking-panel">
        <div className="section-heading">
          <div>
            <div className="section-kicker">SHOWTIME BOOKING</div>
            <h2>
  {paymentSucceeded ? (
    <>Payment <span>successful!</span></>
  ) : (
    <>Booking <span>created!</span></>
  )}
</h2>

<p>
  {paymentSucceeded
    ? "Your payment was successful. Thank you for booking with ShowTime!"
    : "Your seats are held temporarily while you complete payment."}
</p>
          </div>
        </div>

        <div className="booking-summary">
          <div>
            <span className="summary-label">BOOKING ID</span>
            <strong>#{id}</strong>
          </div>

          <div>
            <span className="summary-label">MOVIE</span>
            <strong>
              {booking?.movieTitle || movie?.title || "Movie"}
            </strong>
          </div>

          <div>
            <span className="summary-label">SEATS</span>
            <strong>
              {booking?.seats || selectedSeats.join(", ") || "—"}
            </strong>
          </div>

          <div>
            <span className="summary-label">AMOUNT</span>
            <strong>₹{booking?.amount ?? "—"}</strong>
          </div>

          <div>
  <span className="summary-label">STATUS</span>
  <strong>
    {paymentSucceeded ? "CONFIRMED" : booking?.status || "PENDING"}
  </strong>
</div>
        </div>

        <p>{show?.theatreName || booking?.theatreName || "Theatre"}</p>

        {error && <p className="error-message">{error}</p>}

        <button
          className="primary-button"
          onClick={proceedToPayment}
          disabled={loading}
        >
          {loading ? "Connecting to Razorpay..." : "Proceed to Payment →"}
        </button>
      </div>
    </section>
  );
}

export default BookingConfirmation;
