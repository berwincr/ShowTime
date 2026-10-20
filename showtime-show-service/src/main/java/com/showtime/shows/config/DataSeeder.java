package com.showtime.shows.config;

import com.showtime.shows.entity.Theatre;
import com.showtime.shows.repository.TheatreRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/** Seeds sample Chennai theatres on first start. Edit names/prices freely (or use the REST API). */
@Component
public class DataSeeder implements CommandLineRunner {

    private final TheatreRepository theatres;

    public DataSeeder(TheatreRepository theatres) {
        this.theatres = theatres;
    }

    @Override
    public void run(String... args) {
        if (theatres.count() > 0) {
            return;
        }
        theatres.saveAll(List.of(
                new Theatre("Sathyam Cinemas", "Royapettah", 190),
                new Theatre("Escape Cinemas", "Express Avenue, Royapettah", 220),
                new Theatre("Luxe Cinemas", "Phoenix MarketCity, Velachery", 230),
                new Theatre("AGS Cinemas", "T. Nagar", 160),
                new Theatre("Devi Cineplex", "Anna Salai", 150),
                new Theatre("Kasi Theatre", "Ashok Nagar", 120)
        ));
        System.out.println("ShowTime: seeded 6 sample theatres.");
    }
}
