package com.showtime.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.showtime.entity.Movie;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Fetches real-time movie data from The Movie Database (TMDB) v3 API. */
@Component
public class TmdbClient {

    private static final String BASE = "https://api.themoviedb.org/3";
    private static final String IMAGE = "https://image.tmdb.org/t/p/w342";

    private final RestClient http;
    private final String apiKey;

    public TmdbClient(RestClient http, @Value("${tmdb.api-key:}") String apiKey) {
        this.http = http;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
    }

    public boolean isConfigured() {
        return !apiKey.isEmpty();
    }

    /** Movies currently playing in India. */
    public List<Movie> nowPlaying(int limit) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("region", "IN");
        params.put("language", "en-US");
        params.put("page", "1");
        return withDetails(get("/movie/now_playing", params), limit);
    }

    /** Search any movie by title. */
    public List<Movie> search(String query, int limit) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("query", query);
        params.put("include_adult", "false");
        return withDetails(get("/search/movie", params), limit);
    }

    // ------------------------------------------------------------------

    private JsonNode get(String path, Map<String, String> params) {
        UriComponentsBuilder b = UriComponentsBuilder.fromUriString(BASE + path);
        for (Map.Entry<String, String> e : params.entrySet()) {
            b.queryParam(e.getKey(), e.getValue());
        }
        boolean bearer = apiKey.startsWith("eyJ");          // v4 "read access token" style
        if (!bearer) {
            b.queryParam("api_key", apiKey);
        }
        URI uri = b.build().encode().toUri();
        return http.get().uri(uri)
                .headers(h -> {
                    if (bearer) {
                        h.setBearerAuth(apiKey);
                    }
                })
                .retrieve()
                .body(JsonNode.class);
    }

    /** The list endpoints don't include runtime/genres, so each movie's detail page is fetched too. */
    private List<Movie> withDetails(JsonNode list, int limit) {
        List<Movie> out = new ArrayList<>();
        if (list == null) {
            return out;
        }
        for (JsonNode item : list.path("results")) {
            if (out.size() >= limit) {
                break;
            }
            long id = item.path("id").asLong();
            JsonNode detail = null;
            try {
                Map<String, String> params = new LinkedHashMap<>();
                params.put("language", "en-US");
                detail = get("/movie/" + id, params);
            } catch (RuntimeException ignored) {
                // fall back to the list item if the detail call fails
            }
            out.add(toMovie(detail != null ? detail : item));
        }
        return out;
    }

    private Movie toMovie(JsonNode n) {
        Movie m = new Movie();
        m.setId(n.path("id").asLong());
        m.setTitle(text(n, "title"));
        m.setOverview(cut(text(n, "overview"), 1900));
        String poster = text(n, "poster_path");
        m.setPosterUrl(poster.isEmpty() ? null : IMAGE + poster);

        List<String> names = new ArrayList<>();
        for (JsonNode g : n.path("genres")) {
            names.add(text(g, "name"));
        }
        m.setGenres(String.join(", ", names));

        String code = text(n, "original_language");
        String lang = code.isEmpty() ? "" : Locale.forLanguageTag(code).getDisplayLanguage(Locale.ENGLISH);
        m.setLanguage(lang.isEmpty() ? code : lang);

        int runtime = n.path("runtime").asInt(0);
        m.setRuntimeMinutes(runtime > 0 ? runtime : null);
        m.setRating(n.path("vote_average").asDouble(0));
        m.setPopularity(n.path("popularity").asDouble(0));
        m.setReleaseDate(text(n, "release_date"));
        m.setNowPlaying(false);
        return m;
    }

    private static String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        return (v == null || v.isNull()) ? "" : v.asText();
    }

    private static String cut(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
