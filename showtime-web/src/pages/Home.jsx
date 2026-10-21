import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

const API_BASE_URL = "http://localhost:8080/api";

function Home() {
  const [movies, setMovies] = useState([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const navigate = useNavigate();

  useEffect(() => {
    const controller = new AbortController();

    async function fetchMovies() {
      try {
        setLoading(true);
        setError("");

        const query = search.trim();

        const url = query
          ? `${API_BASE_URL}/movies?query=${encodeURIComponent(query)}`
          : `${API_BASE_URL}/movies`;

        const response = await fetch(url, {
          signal: controller.signal,
        });

        if (!response.ok) {
          throw new Error(`Failed to load movies (${response.status}).`);
        }

        const data = await response.json();

        if (!Array.isArray(data)) {
          throw new Error("Invalid movie data received from the server.");
        }

        setMovies(data);
      } catch (err) {
        if (err.name !== "AbortError") {
          setError(err.message || "Unable to connect to the backend.");
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    fetchMovies();

    return () => controller.abort();
  }, [search]);

  function getGenres(movie) {
    return Array.isArray(movie.genres)
      ? movie.genres.join(", ")
      : movie.genres || "Genre information unavailable";
  }

  function getRating(movie) {
    const rating = Number(movie.rating);

    return Number.isFinite(rating) && rating > 0
      ? rating.toFixed(1)
      : "N/A";
  }

  return (
    <>
      <section className="hero" id="home">
        <div className="hero-background" />

        <div className="hero-content">
          <div className="eyebrow">
            <span className="live-dot" />
            YOUR NEXT MOVIE NIGHT STARTS HERE
          </div>

          <h1>
            Stories worth
            <br />
            seeing <span>together.</span>
          </h1>

          <p>
            Discover incredible films, find your perfect seat,
            and make every movie night unforgettable.
          </p>

          <div className="hero-actions">
            <a className="primary-button" href="#movies">
              Explore Movies <span>→</span>
            </a>
          </div>
        </div>

        <div className="hero-note">
          <span className="note-line" />
          <span>YOUR SEAT. YOUR STORY.</span>
        </div>
      </section>

      <section className="movies-section" id="movies">
        <div className="section-heading">
          <div>
            <div className="section-kicker">
              DISCOVER YOUR NEXT FILM
            </div>

            <h2>
              Now <span>Showing</span>
            </h2>

            <p>Choose a movie to explore its available shows.</p>
          </div>

          <label className="search-box">
            <span>⌕</span>
            <input
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              placeholder="Search movies..."
              aria-label="Search movies"
            />
          </label>
        </div>

        {loading && (
          <p className="empty-state">Loading movies...</p>
        )}

        {!loading && error && (
          <div className="empty-state" role="alert">
            <p>{error}</p>

            <button
              className="primary-button"
              onClick={() => setSearch((current) => current)}
            >
              Try Again
            </button>
          </div>
        )}

        {!loading && !error && (
          <>
            <div className="movie-grid">
              {movies.map((movie) => (
                <article className="movie-card" key={movie.id}>
                  <button
                    className="poster-button"
                    onClick={() =>
                      navigate(`/movie/${movie.id}`, {
                        state: { movie },
                      })
                    }
                    aria-label={`View ${movie.title} showtimes`}
                  >
                    {movie.posterUrl ? (
                      <img
                        src={movie.posterUrl}
                        alt={movie.title}
                        loading="lazy"
                      />
                    ) : (
                      <div className="empty-state">
                        Poster unavailable
                      </div>
                    )}

                    <span className="poster-rating">
                      ★ {getRating(movie)}
                    </span>

                    <span className="poster-overlay">
                      <span className="play-icon">▶</span>
                      View Showtimes
                    </span>
                  </button>

                  <div className="movie-info">
                    <h3>{movie.title}</h3>
                    <p>{getGenres(movie)}</p>

                    <div className="movie-bottom">
                      <span>
                        {movie.runtimeMinutes
                          ? `◷ ${movie.runtimeMinutes} min`
                          : "Runtime unavailable"}
                      </span>

                      <button
                        onClick={() =>
                          navigate(`/movie/${movie.id}`, {
                            state: { movie },
                          })
                        }
                      >
                        Book <span>↗</span>
                      </button>
                    </div>
                  </div>
                </article>
              ))}
            </div>

            {movies.length === 0 && (
              <p className="empty-state">
                No matching movies. Try another title.
              </p>
            )}
          </>
        )}
      </section>
    </>
  );
}

export default Home;