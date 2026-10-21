import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

const API_BASE_URL = "http://localhost:8080/api";

function Register() {
  const navigate = useNavigate();

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function handleRegister(event) {
    event.preventDefault();

    setError("");
    setSuccess("");

    // Match the original Swing validation.
    if (!name.trim() || !email.trim() || !password) {
      setError("Please fill in all the fields.");
      return;
    }

    // Validate the email address.
    if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(email.trim())) {
      setError("Please enter a valid email address.");
      return;
    }

    // Match the original Swing minimum of 6 characters.
    if (password.length < 6) {
      setError("Password must be at least 6 characters.");
      return;
    }

    setLoading(true);

    try {
      const response = await fetch(`${API_BASE_URL}/auth/register`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          name: name.trim(),
          email: email.trim(),
          password,
        }),
      });

      const data = await response.json().catch(() => ({}));

      if (!response.ok) {
        throw new Error(
          data.message ||
            data.error ||
            "Registration failed."
        );
      }

      setSuccess("Account created successfully! You can now sign in.");

      // Redirect to the login page after successful registration.
      setTimeout(() => {
        navigate("/login");
      }, 1200);
    } catch (err) {
      setError(err.message || "Unable to connect to the server.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="booking-section">
      <div className="booking-panel auth-panel">
        <div className="section-heading">
          <div>
            <div className="section-kicker">JOIN SHOWTIME</div>
            <h2>
              Create your <span>account.</span>
            </h2>
            <p>
              Sign up to start booking your favourite movies.
            </p>
          </div>
        </div>

        <form className="auth-form" onSubmit={handleRegister}>
          {/* Name */}
          <label htmlFor="name">Name</label>
          <input
            id="name"
            type="text"
            autoComplete="name"
            placeholder="Enter your name"
            value={name}
            onChange={(event) => setName(event.target.value)}
            required
          />

          {/* Email */}
          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            autoComplete="email"
            placeholder="you@example.com"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />

          {/* Password */}
          <label htmlFor="password">Password (min 6)</label>
          <input
            id="password"
            type="password"
            autoComplete="new-password"
            placeholder="At least 6 characters"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            minLength={6}
            required
          />

          {/* Error message */}
          {error && (
            <p className="auth-error" role="alert">
              {error}
            </p>
          )}

          {/* Success message */}
          {success && (
            <p className="auth-success" role="status">
              {success}
            </p>
          )}

          {/* Submit button */}
          <button
            className="primary-button auth-submit"
            type="submit"
            disabled={loading}
          >
            {loading ? "Creating account..." : "Create account"}
          </button>

          {/* Login link */}
          <p className="auth-switch">
            Already have an account? <Link to="/login">Sign in</Link>
          </p>
        </form>
      </div>
    </section>
  );
}

export default Register;