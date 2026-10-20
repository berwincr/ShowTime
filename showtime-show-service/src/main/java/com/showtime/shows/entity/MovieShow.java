package com.showtime.shows.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

/** One screening of a movie in a theatre. (Named MovieShow because SHOW is a MySQL keyword.) */
@Entity
@Table(name = "shows")
public class MovieShow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** TMDB id of the movie (the main backend uses the same id). */
    @Column(nullable = false)
    private Long movieId;

    @Column(nullable = false)
    private String movieTitle;

    @ManyToOne(optional = false)
    private Theatre theatre;

    @Column(nullable = false)
    private LocalDate showDate;

    @Column(nullable = false)
    private LocalTime showTime;

    private int price;
    private int seatRows = 8;
    private int seatCols = 10;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public Theatre getTheatre() { return theatre; }
    public void setTheatre(Theatre theatre) { this.theatre = theatre; }
    public LocalDate getShowDate() { return showDate; }
    public void setShowDate(LocalDate showDate) { this.showDate = showDate; }
    public LocalTime getShowTime() { return showTime; }
    public void setShowTime(LocalTime showTime) { this.showTime = showTime; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
    public int getSeatRows() { return seatRows; }
    public void setSeatRows(int seatRows) { this.seatRows = seatRows; }
    public int getSeatCols() { return seatCols; }
    public void setSeatCols(int seatCols) { this.seatCols = seatCols; }
}
