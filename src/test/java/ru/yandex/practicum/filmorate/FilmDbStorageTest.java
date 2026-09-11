package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.mappers.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.DirectorDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class, UserDbStorage.class, UserRowMapper.class, MpaRowMapper.class, GenreRowMapper.class, MpaDbStorage.class, GenreDbStorage.class, DirectorDbStorage.class, DirectorRowMapper.class, LongRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmDbStorage filmDbStorage;
    private final UserDbStorage userDbStorage;
    private final DirectorDbStorage directorDbStorage;

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
        film.setGenres(List.of(Genre.builder().id(1L).build(), Genre.builder().id(2L).build()));

        Film created = filmDbStorage.addFilmStorage(film);

        assertEquals(2, created.getGenres().size());
        assertTrue(created.getGenres().stream().anyMatch(g -> g.getId() == 1L));
    }

    @Test
    void addFilmStorageWithDirectorShouldSaveDirector() {
        Director director = directorDbStorage.addDirector(Director.builder().name("Кристофер Нолан").build());

        Film film = createTestFilm();
        film.setDirectors(List.of(Director.builder().id(director.getId()).build()));

        Film created = filmDbStorage.addFilmStorage(film);

        assertNotNull(created.getDirectors());
        assertEquals(1, created.getDirectors().size());
        assertEquals(director.getId(), created.getDirectors().getFirst().getId());
        assertEquals("Кристофер Нолан", created.getDirectors().getFirst().getName());
    }

    @Test
    void addFilmStorageWithSeveralDirectorsShouldSaveAllDirectors() {
        Director director1 = directorDbStorage.addDirector(Director.builder().name("Кристофер Нолан").build());

        Director director2 = directorDbStorage.addDirector(Director.builder().name("Стивен Спилберг").build());

        Film film = createTestFilm();
        film.setDirectors(List.of(Director.builder().id(director1.getId()).build(), Director.builder().id(director2.getId()).build()));

        Film created = filmDbStorage.addFilmStorage(film);

        assertEquals(2, created.getDirectors().size());

        assertTrue(created.getDirectors().stream().anyMatch(d -> d.getId().equals(director1.getId())));

        assertTrue(created.getDirectors().stream().anyMatch(d -> d.getId().equals(director2.getId())));
    }

    @Test
    void addFilmStorageWithInvalidDirectorShouldThrowException() {
        Film film = createTestFilm();

        film.setDirectors(List.of(Director.builder().id(999L).build()));

        assertThrows(ValidationNotObjectException.class, () -> filmDbStorage.addFilmStorage(film));
    }

    @Test
    void getFilmByIdShouldReturnDirectors() {
        Director director = directorDbStorage.addDirector(Director.builder().name("Кристофер Нолан").build());

        Film film = createTestFilm();

        film.setDirectors(List.of(Director.builder().id(director.getId()).build()));

        Film created = filmDbStorage.addFilmStorage(film);

        Film found = filmDbStorage.getFilmById(created.getId());

        assertEquals(1, found.getDirectors().size());
        assertEquals(director.getId(), found.getDirectors().getFirst().getId());
        assertEquals("Кристофер Нолан", found.getDirectors().getFirst().getName());
    }

    @Test
    void updateFilmStorageShouldUpdateDirectors() {
        Director director1 = directorDbStorage.addDirector(Director.builder().name("Кристофер Нолан").build());

        Director director2 = directorDbStorage.addDirector(Director.builder().name("Стивен Спилберг").build());

        Film film = createTestFilm();

        film.setDirectors(List.of(Director.builder().id(director1.getId()).build()));

        Film created = filmDbStorage.addFilmStorage(film);

        Film updatedFilm = Film.builder().id(created.getId()).name("Updated Film").description("Updated Description").releaseDate(LocalDate.of(2021, 2, 2)).duration(130L).mpa(Mpa.builder().id(2L).build()).genres(List.of()).directors(List.of(Director.builder().id(director2.getId()).build())).build();

        Film updated = filmDbStorage.updateFilmStorage(updatedFilm);

        assertEquals(1, updated.getDirectors().size());
        assertEquals(director2.getId(), updated.getDirectors().getFirst().getId());
        assertEquals("Стивен Спилберг", updated.getDirectors().getFirst().getName());
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

        Film updatedFilm = Film.builder().id(created.getId()).name("Updated Film").description("Updated Description").releaseDate(LocalDate.of(2021, 2, 2)).duration(130L).mpa(Mpa.builder().id(2L).build()).genres(List.of(Genre.builder().id(3L).build())).build();

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

        assertThrows(ValidationNotObjectException.class, () -> filmDbStorage.getFilmById(created.getId()));
    }

    @Test
    void getFilmStorageShouldReturnAllFilms() {
        filmDbStorage.addFilmStorage(createTestFilm());
        filmDbStorage.addFilmStorage(createTestFilm2());

        List<Film> films = filmDbStorage.getFilmStorage();

        assertEquals(5, films.size());
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
        assertThrows(ValidationNotObjectException.class, () -> filmDbStorage.getFilmById(999L));
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
        assertEquals(4, topFilms.size());
        assertEquals(film.getId(), topFilms.getFirst().getId());
    }

    @Test
    void addLikeFilmDuplicateShouldNotThrowException() {
        Film film = filmDbStorage.addFilmStorage(createTestFilm());
        User user = userDbStorage.addUserStorage(createTestUser());

        filmDbStorage.addLikeFilm(film.getId(), user.getId());
        filmDbStorage.addLikeFilm(film.getId(), user.getId());

        List<Film> topFilms = filmDbStorage.getTopFilms(10);
        assertEquals(4, topFilms.size());
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
        assertEquals(film1.getId(), topFilms.getFirst().getId());
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

    @Test
    void searchFilmsByTitleShouldReturnMatchingFilms() {
        Film film1 = createTestFilm();
        film1.setName("The Dark Knight");

        Film film2 = createTestFilm2();
        film2.setName("Interstellar");

        filmDbStorage.addFilmStorage(film1);
        filmDbStorage.addFilmStorage(film2);

        List<Film> films = filmDbStorage.searchFilms("dark", "title");

        assertEquals(1, films.size());
        assertEquals("The Dark Knight", films.getFirst().getName());
    }

    @Test
    void searchFilmsByDirectorShouldReturnMatchingFilms() {
        Director director = directorDbStorage.addDirector(Director.builder().name("Кристофер Нолан").build());

        Film film = createTestFilm();
        film.setName("The Dark Knight");
        film.setDirectors(List.of(Director.builder().id(director.getId()).build()));

        filmDbStorage.addFilmStorage(film);

        List<Film> films = filmDbStorage.searchFilms("нолан", "director");

        assertEquals(1, films.size());
        assertEquals("The Dark Knight", films.getFirst().getName());

        assertEquals(1, films.getFirst().getDirectors().size());
        assertEquals("Кристофер Нолан", films.getFirst().getDirectors().getFirst().getName());
    }

    @Test
    void searchFilmsByTitleAndDirectorShouldReturnMatchingFilms() {
        Director nolan = directorDbStorage.addDirector(Director.builder().name("Кристофер Нолан").build());

        Film matchingFilm = createTestFilm();
        matchingFilm.setName("The Dark Knight");
        matchingFilm.setDirectors(List.of(Director.builder().id(nolan.getId()).build()));

        Film anotherFilm = createTestFilm2();
        anotherFilm.setName("Interstellar");

        filmDbStorage.addFilmStorage(matchingFilm);
        filmDbStorage.addFilmStorage(anotherFilm);

        List<Film> films = filmDbStorage.searchFilms("нолан", "title,director");

        assertEquals(1, films.size());
        assertEquals("The Dark Knight", films.getFirst().getName());
    }

    @Test
    void searchFilmsShouldBeCaseInsensitive() {
        Film film = createTestFilm();
        film.setName("The Dark Knight");

        filmDbStorage.addFilmStorage(film);

        List<Film> films = filmDbStorage.searchFilms("DARK KNIGHT", "title");

        assertEquals(1, films.size());
        assertEquals("The Dark Knight", films.getFirst().getName());
    }

    @Test
    void searchFilmsShouldReturnEmptyListWhenNothingFound() {
        filmDbStorage.addFilmStorage(createTestFilm());

        List<Film> films = filmDbStorage.searchFilms("Harry Potter", "title");

        assertTrue(films.isEmpty());
    }

    @Test
    void searchFilmsByDirectorShouldNotReturnDuplicates() {
        Director director1 = directorDbStorage.addDirector(Director.builder().name("Кристофер Нолан").build());

        Director director2 = directorDbStorage.addDirector(Director.builder().name("Кристофер Нолан мл.").build());

        Film film = createTestFilm();
        film.setName("Test Film With Directors");
        film.setDirectors(List.of(Director.builder().id(director1.getId()).build(), Director.builder().id(director2.getId()).build()));

        filmDbStorage.addFilmStorage(film);

        List<Film> films = filmDbStorage.searchFilms("кристофер", "director");

        assertEquals(1, films.size());
        assertEquals("Test Film With Directors", films.getFirst().getName());
    }

    @Test
    void getFilmsRecommendationTest() {
        filmDbStorage.addLikeFilm(1, 1);
        filmDbStorage.addLikeFilm(3, 1);
        filmDbStorage.addLikeFilm(2, 2);
        filmDbStorage.addLikeFilm(1, 3);
        List<Film> recommendations = filmDbStorage.getFilmsRecommendation(3);

        assertNotNull(recommendations);
        assertEquals(1, recommendations.size());
        assertEquals(3, recommendations.get(0).getId());
        assertEquals("Начало", recommendations.get(0).getName());
    }

    @Test
    void getFilmsRecommendationNotLikedRecUserTest() {
        filmDbStorage.addLikeFilm(1, 1);
        filmDbStorage.addLikeFilm(2, 2);
        List<Film> recommendations = filmDbStorage.getFilmsRecommendation(3);

        assertNotNull(recommendations);
        assertEquals(0, recommendations.size());
    }

    @Test
    void getFilmsRecommendationNotLikedFilmsTest() {
        List<Film> recommendations = filmDbStorage.getFilmsRecommendation(3);

        assertNotNull(recommendations);
        assertEquals(0, recommendations.size());
    }

    private Film createTestFilm() {
        return Film.builder().name("Test Film").description("Test Description").releaseDate(LocalDate.of(2020, 1, 1)).duration(120L).mpa(Mpa.builder().id(1L).build()).build();
    }

    private Film createTestFilm2() {
        return Film.builder().name("Test Film 2").description("Test Description 2").releaseDate(LocalDate.of(2021, 2, 2)).duration(130L).mpa(Mpa.builder().id(2L).build()).build();
    }

    private User createTestUser() {
        return User.builder().email("test@mail.ru").login("testuser").name("Test User").birthday(LocalDate.of(1990, 1, 1)).build();
    }

    private User createTestUser2() {
        return User.builder().email("test2@mail.ru").login("testuser2").name("Test User 2").birthday(LocalDate.of(1992, 2, 2)).build();
    }
}