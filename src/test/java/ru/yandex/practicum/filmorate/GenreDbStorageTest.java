package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.MpaRowMapper;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaDbStorage;
import ru.yandex.practicum.filmorate.dal.mappers.DirectorRowMapper;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class, GenreDbStorage.class, GenreRowMapper.class,
        MpaRowMapper.class, MpaDbStorage.class, DirectorDbStorage.class, DirectorRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class GenreDbStorageTest {

    private final GenreDbStorage genreDbStorage;
    private final FilmDbStorage filmDbStorage;

    @Test
    void checkGenreId_ShouldReturnTrueIfExists() {
        assertTrue(genreDbStorage.checkGenreId(1L));
        assertTrue(genreDbStorage.checkGenreId(6L));
        assertFalse(genreDbStorage.checkGenreId(999L));
    }

    @Test
    void getGenreById_ShouldReturnGenre() {
        Genre genre = genreDbStorage.getGenreById(1L);

        assertNotNull(genre);
        assertEquals(1L, genre.getId());
        assertEquals("Комедия", genre.getName());
    }

    @Test
    void getGenreById_NotFound_ShouldThrowException() {
        assertThrows(ValidationNotObjectException.class, () -> genreDbStorage.getGenreById(999L));
    }

    @Test
    void getGenreStorage_ShouldReturnAllGenres() {
        List<Genre> genres = genreDbStorage.getGenreStorage();

        assertNotNull(genres);
        assertEquals(6, genres.size());
        assertEquals("Комедия", genres.get(0).getName());
        assertEquals("Драма", genres.get(1).getName());
        assertEquals("Мультфильм", genres.get(2).getName());
        assertEquals("Триллер", genres.get(3).getName());
        assertEquals("Документальный", genres.get(4).getName());
        assertEquals("Боевик", genres.get(5).getName());
    }

    @Test
    void getFilmIdGenreStorage_ShouldReturnGenresForFilm() {
        Film film = Film.builder().name("Test Film").description("Test Description").releaseDate(LocalDate.of(2020, 1, 1)).duration(120L).mpa(Mpa.builder().id(1L).build()).genres(List.of(Genre.builder().id(1L).build(), Genre.builder().id(2L).build())).build();

        Film created = filmDbStorage.addFilmStorage(film);

        List<Genre> genres = genreDbStorage.getFilmIdGenreStorage(created.getId());

        assertNotNull(genres);
        assertEquals(2, genres.size());
        assertTrue(genres.stream().anyMatch(g -> g.getId() == 1L));
        assertTrue(genres.stream().anyMatch(g -> g.getId() == 2L));
    }

    @Test
    void getFilmIdGenreStorage_WhenNoGenres_ShouldReturnEmptyList() {
        Film film = Film.builder().name("Test Film").description("Test Description").releaseDate(LocalDate.of(2020, 1, 1)).duration(120L).mpa(Mpa.builder().id(1L).build()).build();

        Film created = filmDbStorage.addFilmStorage(film);

        List<Genre> genres = genreDbStorage.getFilmIdGenreStorage(created.getId());

        assertNotNull(genres);
        assertTrue(genres.isEmpty());
    }

    @Test
    void deleteGenreByFilmId_ShouldDeleteAllGenresForFilm() {
        Film film = Film.builder().name("Test Film").description("Test Description").releaseDate(LocalDate.of(2020, 1, 1)).duration(120L).mpa(Mpa.builder().id(1L).build()).genres(List.of(Genre.builder().id(1L).build(), Genre.builder().id(2L).build())).build();

        Film created = filmDbStorage.addFilmStorage(film);

        List<Genre> genresBefore = genreDbStorage.getFilmIdGenreStorage(created.getId());
        assertEquals(2, genresBefore.size());

        genreDbStorage.deleteGenreByFilmId(created.getId());

        List<Genre> genresAfter = genreDbStorage.getFilmIdGenreStorage(created.getId());
        assertTrue(genresAfter.isEmpty());
    }

    @Test
    void deleteGenreByFilmId_WhenNoGenres_ShouldNotThrowException() {
        Film film = Film.builder().name("Test Film").description("Test Description").releaseDate(LocalDate.of(2020, 1, 1)).duration(120L).mpa(Mpa.builder().id(1L).build()).build();

        Film created = filmDbStorage.addFilmStorage(film);

        assertDoesNotThrow(() -> genreDbStorage.deleteGenreByFilmId(created.getId()));
    }
}
