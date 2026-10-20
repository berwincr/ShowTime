package com.showtime.controller;

import com.showtime.dto.MovieDto;
import com.showtime.dto.SeatMapDto;
import com.showtime.dto.ShowDto;
import com.showtime.service.CatalogService;
import com.showtime.service.MovieService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class MovieController {

    private final MovieService movieService;
    private final CatalogService catalog;

    public MovieController(MovieService movieService, CatalogService catalog) {
        this.movieService = movieService;
        this.catalog = catalog;
    }

    /** GET /api/movies            -> movies now playing (live from TMDB, cached)
     *  GET /api/movies?query=leo  -> search by title */
    @GetMapping("/movies")
    public List<MovieDto> movies(@RequestParam(name = "query", required = false) String query) {
        return movieService.find(query).stream().map(MovieDto::from).toList();
    }

    /** GET /api/movies/{id}/shows?date=2026-10-21 -> theatres, showtimes, prices and seats left */
    @GetMapping("/movies/{id}/shows")
    public List<ShowDto> shows(@PathVariable("id") Long id,
                               @RequestParam(name = "date", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return catalog.showsForMovie(id, date);
    }

    /** GET /api/shows/{id}/seats -> seat layout + already booked seats */
    @GetMapping("/shows/{id}/seats")
    public SeatMapDto seats(@PathVariable("id") Long id) {
        return catalog.seatMap(id);
    }
}
