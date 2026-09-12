package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final GenreStorage genreStorage;

    public void addLike(long filmId, long userId) {
        log.trace("Вход в метод addLike с параметрами filmId={}, userId={}", filmId, userId);

        if (!filmStorage.checkingId(filmId)) {
            log.warn("Попытка добавить лайк к несуществующему фильму с id={}", filmId);
            throw new ValidationNotObjectException("Фильм с таким ID: " + filmId + " не найден");
        }
        if (!userStorage.checkingId(userId)) {
            log.warn("Попытка добавить лайк от несуществующего пользователя с id={}", userId);
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        filmStorage.addLikeFilm(filmId, userId);

    }

    public void deleteLike(long filmId, long userId) {
        log.trace("Вход в метод deleteLike с параметрами filmId={}, userId={}", filmId, userId);
        if (!filmStorage.checkingId(filmId)) {
            log.warn("Попытка удалить лайк у несуществующего фильма с id={}", filmId);
            throw new ValidationNotObjectException("Фильм с таким ID:" + filmId + " не найден");
        }
        if (!userStorage.checkingId(userId)) {
            log.warn("Попытка удалить лайк от несуществующего пользователя с id={}", userId);
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        filmStorage.deleteLikeFilm(filmId, userId);
    }

    public List<Film> getTopFilms(long count) {
        log.trace("Вход в метод getTopFilms с параметром count={}", count);
        if (count <= 0) {
            log.warn("Некорректное значение count={}", count);
            throw new ValidationException("Количество фильмов в топе не может быть ноль или меньше ноля");
        }
        return filmStorage.getTopFilms(count);
    }

    public void addGenreFilm(long filmId, long genreId) {
        log.trace("Вход в метод addGenreFilm с параметрами filmId={}, genreId={}", filmId, genreId);
        if (!filmStorage.checkingId(filmId)) {
            log.warn("Попытка добавить жанр у несуществующего фильма с id={}", filmId);
            throw new ValidationNotObjectException("Фильм с таким ID:" + filmId + " не найден");
        }

        if (!genreStorage.checkGenreId(genreId)) {
            log.warn("Попытка добавить несуществующий жанр с id={}", genreId);
            throw new ValidationNotObjectException("Жанр с таким ID:" + genreId + " не найден");
        }
        filmStorage.addGenreFilm(filmId, genreId);
    }

    public List<Film> searchFilms(String query, String by) {
        if (query == null || query.isBlank()) {
            throw new ValidationException("Поисковый запрос не может быть пустым");
        }

        if (by == null || by.isBlank()) {
            throw new ValidationException("Параметр by не может быть пустым");
        }

        String[] searchTypes = by.split(",");

        for (String type : searchTypes) {
            if (!"title".equalsIgnoreCase(type.trim()) && !"director".equalsIgnoreCase(type.trim())) {
                throw new ValidationException("Параметр by должен содержать title и/или director");
            }
        }

        return filmStorage.searchFilms(query, by);
    }

    public Film updateFilm(UpdateFilmRequest updateFilm) {
        Film film = filmStorage.getFilmById(updateFilm.getId());
        if (updateFilm.hasName()) {
            film.setName(updateFilm.getName());
        }
        if (updateFilm.hasDescription()) {
            film.setDescription(updateFilm.getDescription());
        }
        if (updateFilm.hasReleaseDate()) {
            film.setReleaseDate(updateFilm.getReleaseDate());
        }
        if (updateFilm.hasDuration()) {
            film.setDuration(updateFilm.getDuration());
        }
        if (updateFilm.hasRating()) {
            film.setMpa(updateFilm.getMpa());
        }
        if (updateFilm.hasGenres()) {
            film.setGenres(updateFilm.getGenres());
        }
        if (updateFilm.hasDirectors()) {
            film.setDirectors(updateFilm.getDirectors());
        }

        return filmStorage.updateFilmStorage(film);
    }

    public List<Film> getCommonFilms(long userId, long friendId) {
        log.trace("Вход в метод getCommonFilms с параметрами userId={}, friendId={}", userId, friendId);

        if (!userStorage.checkingId(userId)) {
            log.warn("Запрос общих фильмов у несуществующего пользователя с id={}", userId);
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        if (!userStorage.checkingId(friendId)) {
            log.warn("Запрос общих фильмов с несуществующим пользователем с id={}", friendId);
            throw new ValidationNotObjectException("Пользователь для сравнения с таким ID: " + friendId + " не найден");
        }

        return filmStorage.getCommonFilms(userId, friendId);
    }
}
