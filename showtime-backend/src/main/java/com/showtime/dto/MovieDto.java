package com.showtime.dto;

import com.showtime.entity.Movie;

public record MovieDto(Long id, String title, String overview, String posterUrl, String genres,
                       String language, Integer runtimeMinutes, Double rating, String releaseDate) {

    public static MovieDto from(Movie m) {
        return new MovieDto(m.getId(), m.getTitle(), m.getOverview(), m.getPosterUrl(), m.getGenres(),
                m.getLanguage(), m.getRuntimeMinutes(), m.getRating(), m.getReleaseDate());
    }
}
