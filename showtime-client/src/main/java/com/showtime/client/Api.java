package com.showtime.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

/** All calls to the Spring Boot backend (http://localhost:8080). Run these off the Swing thread! */
public final class Api {

    /** Override with  java -Dshowtime.api=http://host:8080/api -jar showtime-client.jar */
    public static final String BASE = System.getProperty("showtime.api", "http://localhost:8080/api");

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();

    // logged-in user
    public static String token;
    public static long userId;
    public static String userName = "";

    private Api() {}

    public static class ApiException extends Exception {
        public final int status;

        public ApiException(int status, String message) {
            super(message);
            this.status = status;
        }
    }

    public static boolean isLoggedIn() { return token != null; }

    public static void clearSession() {
        token = null;
        userId = 0;
        userName = "";
    }

    public static Object get(String path) throws ApiException {
        return send(HttpRequest.newBuilder(URI.create(BASE + path)).GET());
    }

    public static Object post(String path, Object body) throws ApiException {
        String json = body == null ? "{}" : Json.stringify(body);
        return send(HttpRequest.newBuilder(URI.create(BASE + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json)));
    }

    private static Object send(HttpRequest.Builder builder) throws ApiException {
        builder.timeout(Duration.ofSeconds(60));
        if (token != null) builder.header("Authorization", "Bearer " + token);
        try {
            HttpResponse<String> res = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            String body = res.body() == null ? "" : res.body();
            if (res.statusCode() >= 400) {
                String message = "Request failed (" + res.statusCode() + ")";
                try {
                    Object parsed = Json.parse(body);
                    if (parsed instanceof Map<?, ?> m && m.get("error") != null) message = String.valueOf(m.get("error"));
                } catch (RuntimeException ignored) { /* body was not JSON */ }
                throw new ApiException(res.statusCode(), message);
            }
            return body.isBlank() ? null : Json.parse(body);
        } catch (IOException e) {
            throw new ApiException(0, "Cannot reach the ShowTime server at " + BASE
                    + ". Is the backend running?");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(0, "Request interrupted");
        }
    }
}
