package com.showtime.shows.repository;

import com.showtime.shows.entity.MovieShow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ShowRepository extends JpaRepository<MovieShow, Long> {

    /** HQL/JPQL query: shows of a movie on one date, grouped by theatre. */
    @Query("select s from MovieShow s where s.movieId = :movieId and s.showDate = :date "
         + "order by s.theatre.name, s.showTime")
    List<MovieShow> findForMovieOnDate(@Param("movieId") Long movieId, @Param("date") LocalDate date);

    /** HQL/JPQL query: upcoming shows of a movie. */
    @Query("select s from MovieShow s where s.movieId = :movieId and s.showDate >= :from "
         + "order by s.showDate, s.theatre.name, s.showTime")
    List<MovieShow> findUpcomingForMovie(@Param("movieId") Long movieId, @Param("from") LocalDate from);

    @Query("select count(s) from MovieShow s where s.movieId = :movieId and s.theatre.id = :theatreId and s.showDate = :date")
    long countForDay(@Param("movieId") Long movieId, @Param("theatreId") Long theatreId, @Param("date") LocalDate date);
}
