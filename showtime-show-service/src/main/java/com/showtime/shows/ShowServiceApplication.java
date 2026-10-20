package com.showtime.shows;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Second, independent Spring Boot service. Owns theatres and shows. Runs on port 8081. */
@SpringBootApplication
public class ShowServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShowServiceApplication.class, args);
    }
}
