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
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class, UserDbStorage.class, UserRowMapper.class,
        MpaRowMapper.class, GenreRowMapper.class, MpaDbStorage.class, GenreDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;

    @Test
    void addFilmStorageShouldCreateFilm() {
        Film film = createTestFilm();
        Film created = filmDbStorage.addFilmStorage(film);

        assertNotNull(created.getId());
        assertEquals("Test Film", created.getName());
        assertEquals("G", created.getMpa().getName());
        assertTrue(created.getGenres().isEmpty());
    }

    @Test
    void addFilmStorageWithGenresShouldSaveGenres() {
        Film film = createTestFilm();
        film.setGenres(List.of(
                Genre.builder().id(1L).build(),
                Genre.builder().id(2L).build()
        ));

        Film created = filmDbStorage.addFilmStorage(film);

        assertEquals(2, created.getGenres().size());
        assertTrue(created.getGenres().stream().anyMatch(g -> g.getId() == 1L));
    }

    @Test
    void addFilmStorageWithInvalidDataShouldThrowException() {
        Film film = createTestFilm();
        film.setName("");

        assertThrows(ValidationException.class, () -> filmDbStorage.addFilmStorage(film));
    }

    @Test
    void addFilmStorageWithInvalidRatingShouldThrowException() {
        Film film = createTestFilm();
        film.setMpa(Mpa.builder().id(999L).build());

        assertThrows(ValidationNotObjectException.class, () -> filmDbStorage.addFilmStorage(film));
    }

    @Test
    void updateFilmStorageShouldUpdateFilm() {
        Film film = createTestFilm();
        Film created = filmDbStorage.addFilmStorage(film);

        Film updatedFilm = Film.builder()
                .id(created.getId())
                .name("Updated Film")
                .description("Updated Description")
                .releaseDate(LocalDate.of(2021, 2, 2))
                .duration(130L)
                .mpa(Mpa.builder().id(2L).build())
                .genres(List.of(Genre.builder().id(3L).build()))
                .build();

        Film updated = filmDbStorage.updateFilmStorage(updatedFilm);

        assertEquals("Updated Film", updated.getName());
        assertEquals(2L, updated.getMpa().getId());
        assertEquals(1, updated.getGenres().size());
    }

    @Test
    void removeFilmStorageShouldDeleteFilm() {
        Film film = createTestFilm();
        Film created = filmDbStorage.addFilmStorage(film);

        filmDbStorage.removeFilmStorage(created.getId());

        assertThrows(ValidationNotObjectException.class,
                () -> filmDbStorage.getFilmById(created.getId()));
    }

    @Test
    void getFilmStorageShouldReturnAllFilms() {
        filmDbStorage.addFilmStorage(createTestFilm());
        filmDbStorage.addFilmStorage(createTestFilm2());

        List<Film> films = filmDbStorage.getFilmStorage();

        assertEquals(2, films.size());
    }

    @Test
    void getFilmByIdShouldReturnFilm() {
        Film film = createTestFilm();
        Film created = filmDbStorage.addFilmStorage(film);

        Film found = filmDbStorage.getFilmById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals(created.getName(), found.getName());
    }

    @Test
    void getFilmByIdNotFoundShouldThrowException() {
        assertThrows(ValidationNotObjectException.class,
                () -> filmDbStorage.getFilmById(999L));
    }

    @Test
    void checkingIdShouldReturnTrueIfExists() {
        Film film = createTestFilm();
        Film created = filmDbStorage.addFilmStorage(film);

        assertTrue(filmDbStorage.checkingId(created.getId()));
        assertFalse(filmDbStorage.checkingId(999L));
    }

    @Test
    void addLikeFilmShouldAddLike() {
        Film film = filmDbStorage.addFilmStorage(createTestFilm());
        User user = userDbStorage.addUserStorage(createTestUser());

        filmDbStorage.addLikeFilm(film.getId(), user.getId());

        List<Film> topFilms = filmDbStorage.getTopFilms(10);
        assertEquals(1, topFilms.size());
        assertEquals(film.getId(), topFilms.get(0).getId());
    }

    @Test
    void addLikeFilmDuplicateShouldNotThrowException() {
        Film film = filmDbStorage.addFilmStorage(createTestFilm());
        User user = userDbStorage.addUserStorage(createTestUser());

        filmDbStorage.addLikeFilm(film.getId(), user.getId());
        filmDbStorage.addLikeFilm(film.getId(), user.getId());

        List<Film> topFilms = filmDbStorage.getTopFilms(10);
        assertEquals(1, topFilms.size());
    }

    @Test
    void deleteLikeFilmShouldRemoveLike() {
        Film film = filmDbStorage.addFilmStorage(createTestFilm());
        User user = userDbStorage.addUserStorage(createTestUser());

        filmDbStorage.addLikeFilm(film.getId(), user.getId());
        filmDbStorage.deleteLikeFilm(film.getId(), user.getId());

        assertTrue(filmDbStorage.checkingId(film.getId()));
    }

    @Test
    void getTopFilmsShouldReturnMostLikedFilms() {
        Film film1 = filmDbStorage.addFilmStorage(createTestFilm());
        Film film2 = filmDbStorage.addFilmStorage(createTestFilm2());
        User user1 = userDbStorage.addUserStorage(createTestUser());
        User user2 = userDbStorage.addUserStorage(createTestUser2());

        filmDbStorage.addLikeFilm(film1.getId(), user1.getId());
        filmDbStorage.addLikeFilm(film1.getId(), user2.getId());
        filmDbStorage.addLikeFilm(film2.getId(), user1.getId());

        List<Film> topFilms = filmDbStorage.getTopFilms(2);

        assertEquals(2, topFilms.size());
        assertEquals(film1.getId(), topFilms.get(0).getId());
    }

    @Test
    void getTopFilmsWithLimitShouldReturnLimitedCount() {
        filmDbStorage.addFilmStorage(createTestFilm());
        filmDbStorage.addFilmStorage(createTestFilm2());

        List<Film> topFilms = filmDbStorage.getTopFilms(1);

        assertEquals(1, topFilms.size());
    }

    @Test
    void addGenreFilmShouldAddGenreToFilm() {
        Film film = filmDbStorage.addFilmStorage(createTestFilm());

        filmDbStorage.addGenreFilm(film.getId(), 1L);
        filmDbStorage.addGenreFilm(film.getId(), 2L);

        Film found = filmDbStorage.getFilmById(film.getId());
        assertEquals(2, found.getGenres().size());
    }

    private Film createTestFilm() {
        return Film.builder()
                .name("Test Film")
                .description("Test Description")
                .releaseDate(LocalDate.of(2020, 1, 1))
                .duration(120L)
                .mpa(Mpa.builder().id(1L).build())
                .build();
    }

    private Film createTestFilm2() {
        return Film.builder()
                .name("Test Film 2")
                .description("Test Description 2")
                .releaseDate(LocalDate.of(2021, 2, 2))
                .duration(130L)
                .mpa(Mpa.builder().id(2L).build())
                .build();
    }

    private User createTestUser() {
        return User.builder()
                .email("test@mail.ru")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    private User createTestUser2() {
        return User.builder()
                .email("test2@mail.ru")
                .login("testuser2")
                .name("Test User 2")
                .birthday(LocalDate.of(1992, 2, 2))
                .build();
    }
}