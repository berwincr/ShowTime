package com.showtime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShowTimeApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShowTimeApplication.class, args);
    }
}
