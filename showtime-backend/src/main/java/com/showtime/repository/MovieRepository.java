package com.showtime.repository;

import com.showtime.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    /** HQL/JPQL query #1: search movies by title (case-insensitive, partial match). */
    @Query("select m from Movie m where lower(m.title) like lower(concat('%', :q, '%')) "
         + "order by m.popularity desc")
    List<Movie> search(@Param("q") String q);

    List<Movie> findByNowPlayingTrueOrderByPopularityDesc();

    @Modifying
    @Query("update Movie m set m.nowPlaying = false")
    void clearNowPlaying();
}
