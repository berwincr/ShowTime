package com.showtime.service;

import com.showtime.entity.Movie;
import com.showtime.integration.TmdbClient;
import com.showtime.repository.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Real-time movie data: "now playing" movies come from TMDB and are cached in MySQL
 * (refreshed every 6 hours). Searching for a title that is not cached asks TMDB directly.
 * If no TMDB key is configured (or TMDB is unreachable) a few sample movies are used instead.
 */
@Service
public class MovieService {

    private static final Logger log = LoggerFactory.getLogger(MovieService.class);
    private static final Duration REFRESH_EVERY = Duration.ofHours(6);
    private static final int NOW_PLAYING_LIMIT = 12;

    private final MovieRepository movies;
    private final TmdbClient tmdb;
    private final TransactionTemplate tx;

    private Instant lastSync = Instant.EPOCH;

    public MovieService(MovieRepository movies, TmdbClient tmdb, TransactionTemplate tx) {
        this.movies = movies;
        this.tmdb = tmdb;
        this.tx = tx;
    }

    public List<Movie> find(String query) {
        syncIfStale();
        boolean blank = query == null || query.isBlank();
        if (blank) {
            return movies.findByNowPlayingTrueOrderByPopularityDesc();
        }
        String q = query.trim();
        List<Movie> result = movies.search(q);
        if (result.isEmpty() && tmdb.isConfigured()) {
            try {
                List<Movie> found = tmdb.search(q, 5);
                movies.saveAll(found);
                result = movies.search(q);
            } catch (RuntimeException e) {
                log.warn("TMDB search failed: {}", e.getMessage());
            }
        }
        return result;
    }

    public Movie get(Long id) {
        return movies.findById(id).orElse(null);
    }

    private synchronized void syncIfStale() {
        boolean stale = Duration.between(lastSync, Instant.now()).compareTo(REFRESH_EVERY) > 0;
        if (!stale && movies.count() > 0) {
            return;
        }
        if (tmdb.isConfigured()) {
            try {
                List<Movie> fresh = tmdb.nowPlaying(NOW_PLAYING_LIMIT);
                if (!fresh.isEmpty()) {
                    for (Movie m : fresh) {
                        m.setNowPlaying(true);
                    }
                    tx.executeWithoutResult(s -> {
                        movies.clearNowPlaying();
                        movies.saveAll(fresh);
                    });
                    lastSync = Instant.now();
                    log.info("Synced {} now-playing movies from TMDB", fresh.size());
                    return;
                }
            } catch (RuntimeException e) {
                log.warn("TMDB sync failed ({}). Using cached/sample movies.", e.getMessage());
            }
        } else {
            log.warn("No TMDB API key configured. Using sample movies.");
        }
        if (movies.findByNowPlayingTrueOrderByPopularityDesc().isEmpty()) {
            movies.saveAll(sampleMovies());
        }
        lastSync = Instant.now();
    }

    private List<Movie> sampleMovies() {
        List<Movie> list = new ArrayList<>();
        list.add(sample(9000001L, "Leo", "Tamil", "Action, Thriller", 164, 7.2,
                "A mild-mannered cafe owner in Himachal Pradesh is pulled back into a violent past."));
        list.add(sample(9000002L, "Jailer", "Tamil", "Action, Comedy", 168, 7.0,
                "A retired jailer sets out to stop a criminal gang threatening his family."));
        list.add(sample(9000003L, "Vikram", "Tamil", "Action, Thriller", 175, 7.3,
                "A special agent investigates a series of murders by masked men."));
        list.add(sample(9000004L, "Ponniyin Selvan: I", "Tamil", "Drama, History", 167, 7.4,
                "Chola prince Arulmozhi Varman and his friend get caught in a web of palace intrigue."));
        list.add(sample(9000005L, "Kalki 2898 AD", "Telugu", "Sci-Fi, Action", 181, 6.9,
                "In a dystopian future, a bounty hunter crosses paths with a mysterious woman."));
        list.add(sample(9000006L, "3 Idiots", "Hindi", "Comedy, Drama", 170, 8.4,
                "Three engineering students chase their dreams, and a friend who vanished."));
        return list;
    }

    private Movie sample(Long id, String title, String lang, String genres, int runtime, double rating, String overview) {
        Movie m = new Movie();
        m.setId(id);
        m.setTitle(title);
        m.setLanguage(lang);
        m.setGenres(genres);
        m.setRuntimeMinutes(runtime);
        m.setRating(rating);
        m.setPopularity(rating * 10);
        m.setOverview(overview);
        m.setReleaseDate("");
        m.setNowPlaying(true);
        return m;
    }
}
