package com.showtime.shows.service;

import com.showtime.shows.entity.MovieShow;
import com.showtime.shows.entity.Theatre;
import com.showtime.shows.repository.ShowRepository;
import com.showtime.shows.repository.TheatreRepository;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Real theatre APIs (BookMyShow, PVR...) are not public, so theatre schedules are managed here.
 * When a movie is requested, we create 3 shows per theatre per day for the next N days.
 * The method is idempotent: days that already have shows are skipped.
 */
@Service
public class ShowGeneratorService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final String[] SLOTS = {"10:00", "13:30", "16:45", "19:30", "22:15"};

    private final TheatreRepository theatres;
    private final ShowRepository shows;

    public ShowGeneratorService(TheatreRepository theatres, ShowRepository shows) {
        this.theatres = theatres;
        this.shows = shows;
    }

    public int generate(Long movieId, String title, int days) {
        List<Theatre> all = theatres.findAll();
        LocalDate today = LocalDate.now(IST);
        int offset = (int) (movieId % SLOTS.length);
        List<MovieShow> toSave = new ArrayList<>();

        for (int t = 0; t < all.size(); t++) {
            Theatre theatre = all.get(t);
            for (int d = 0; d < days; d++) {
                LocalDate date = today.plusDays(d);
                if (shows.countForDay(movieId, theatre.getId(), date) > 0) {
                    continue;
                }
                boolean weekend = date.getDayOfWeek() == DayOfWeek.SATURDAY
                        || date.getDayOfWeek() == DayOfWeek.SUNDAY;
                for (int k = 0; k < 3; k++) {
                    LocalTime time = LocalTime.parse(SLOTS[(t + k * 2 + offset) % SLOTS.length]);
                    MovieShow s = new MovieShow();
                    s.setMovieId(movieId);
                    s.setMovieTitle(title);
                    s.setTheatre(theatre);
                    s.setShowDate(date);
                    s.setShowTime(time);
                    int price = theatre.getBasePrice();
                    if (time.isAfter(LocalTime.of(18, 0))) price += 30;   // evening shows cost more
                    if (weekend) price += 20;
                    s.setPrice(price);
                    toSave.add(s);
                }
            }
        }
        shows.saveAll(toSave);
        return toSave.size();
    }
}
