package com.showtime.integration;

import com.showtime.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Calls the second Spring Boot service (show-service) over REST. */
@Component
public class ShowClient {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final RestClient http;
    private final String baseUrl;
    /** movieId -> day on which we last asked the show-service to generate shows. */
    private final Map<Long, LocalDate> generatedOn = new ConcurrentHashMap<>();

    public ShowClient(RestClient http, @Value("${showservice.url}") String baseUrl) {
        this.http = http;
        this.baseUrl = baseUrl;
    }

    public void ensureShows(Long movieId, String title) {
        LocalDate today = LocalDate.now(IST);
        if (today.equals(generatedOn.get(movieId))) {
            return;
        }
        try {
            http.post()
                    .uri(baseUrl + "/api/shows/generate?movieId={id}&title={t}&days=7", movieId, title)
                    .retrieve()
                    .toBodilessEntity();
            generatedOn.put(movieId, today);
        } catch (RestClientException e) {
            throw unavailable(e);
        }
    }

    public List<ShowInfo> shows(Long movieId, LocalDate date) {
        try {
            List<ShowInfo> list = http.get()
                    .uri(baseUrl + "/api/shows?movieId={id}&date={d}", movieId, date.toString())
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<ShowInfo>>() {});
            return list == null ? List.of() : list;
        } catch (RestClientException e) {
            throw unavailable(e);
        }
    }

    public ShowInfo show(Long showId) {
        try {
            ShowInfo s = http.get().uri(baseUrl + "/api/shows/{id}", showId).retrieve().body(ShowInfo.class);
            if (s == null) {
                throw new ApiException(HttpStatus.NOT_FOUND, "Show not found");
            }
            return s;
        } catch (HttpClientErrorException.NotFound e) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Show not found");
        } catch (RestClientException e) {
            throw unavailable(e);
        }
    }

    private ApiException unavailable(RestClientException e) {
        return new ApiException(HttpStatus.SERVICE_UNAVAILABLE,
                "Show service is not reachable at " + baseUrl + ". Start showtime-show-service first.");
    }
}
