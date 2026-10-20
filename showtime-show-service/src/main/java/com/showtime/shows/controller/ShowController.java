package com.showtime.shows.controller;

import com.showtime.shows.dto.ShowDto;
import com.showtime.shows.dto.TheatreRequest;
import com.showtime.shows.entity.Theatre;
import com.showtime.shows.repository.ShowRepository;
import com.showtime.shows.repository.TheatreRepository;
import com.showtime.shows.service.ShowGeneratorService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ShowController {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final TheatreRepository theatres;
    private final ShowRepository shows;
    private final ShowGeneratorService generator;

    public ShowController(TheatreRepository theatres, ShowRepository shows, ShowGeneratorService generator) {
        this.theatres = theatres;
        this.shows = shows;
        this.generator = generator;
    }

    // ---------- Theatres (CRUD) ----------

    @GetMapping("/theatres")
    public List<Theatre> allTheatres() {
        return theatres.findAll();
    }

    @PostMapping("/theatres")
    public Theatre addTheatre(@RequestBody TheatreRequest r) {
        return theatres.save(new Theatre(r.name(), r.area(), r.basePrice()));
    }

    @DeleteMapping("/theatres/{id}")
    public Map<String, String> deleteTheatre(@PathVariable("id") Long id) {
        theatres.deleteById(id);
        return Map.of("status", "deleted");
    }

    // ---------- Shows ----------

    /** GET /api/shows?movieId=550&date=2026-10-21   (date is optional: omit it for all upcoming shows) */
    @GetMapping("/shows")
    public List<ShowDto> shows(@RequestParam("movieId") Long movieId,
                               @RequestParam(name = "date", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        var list = (date == null)
                ? shows.findUpcomingForMovie(movieId, LocalDate.now(IST))
                : shows.findForMovieOnDate(movieId, date);
        return list.stream().map(ShowDto::from).toList();
    }

    @GetMapping("/shows/{id}")
    public ShowDto show(@PathVariable("id") Long id) {
        return shows.findById(id).map(ShowDto::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Show not found"));
    }

    /** POST /api/shows/generate?movieId=550&title=Fight Club&days=7  - creates missing shows (idempotent). */
    @PostMapping("/shows/generate")
    public Map<String, Object> generate(@RequestParam("movieId") Long movieId,
                                        @RequestParam("title") String title,
                                        @RequestParam(name = "days", defaultValue = "7") int days) {
        int created = generator.generate(movieId, title, Math.min(Math.max(days, 1), 14));
        return Map.of("created", created);
    }

    @DeleteMapping("/shows/{id}")
    public Map<String, String> deleteShow(@PathVariable("id") Long id) {
        shows.deleteById(id);
        return Map.of("status", "deleted");
    }
}
