package com.showtime.entity;

import jakarta.persistence.*;

/** Movie details cached from TMDB. The id is the TMDB movie id (assigned, not generated). */
@Entity
@Table(name = "movies")
public class Movie {

    @Id
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String overview;

    private String posterUrl;
    private String genres;
    private String language;
    private Integer runtimeMinutes;
    private Double rating;
    private Double popularity;
    private String releaseDate;
    private boolean nowPlaying;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getOverview() { return overview; }
    public void setOverview(String overview) { this.overview = overview; }
    public String getPosterUrl() { return posterUrl; }
    public void setPosterUrl(String posterUrl) { this.posterUrl = posterUrl; }
    public String getGenres() { return genres; }
    public void setGenres(String genres) { this.genres = genres; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
    public Integer getRuntimeMinutes() { return runtimeMinutes; }
    public void setRuntimeMinutes(Integer runtimeMinutes) { this.runtimeMinutes = runtimeMinutes; }
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }
    public Double getPopularity() { return popularity; }
    public void setPopularity(Double popularity) { this.popularity = popularity; }
    public String getReleaseDate() { return releaseDate; }
    public void setReleaseDate(String releaseDate) { this.releaseDate = releaseDate; }
    public boolean isNowPlaying() { return nowPlaying; }
    public void setNowPlaying(boolean nowPlaying) { this.nowPlaying = nowPlaying; }
}
