package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
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
    private final FeedStorage feedStorage;

    @Transactional
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
        if (!filmStorage.checkLike(filmId, userId)) {
            filmStorage.addLikeFilm(filmId, userId);
        }
        feedStorage.addEvent(userId, filmId, EventType.LIKE.name(), Operation.ADD.name());
    }

    @Transactional
    public void deleteLike(long filmId, long userId) {
        log.trace("Вход в метод deleteLike с параметрами filmId={}, userId={}", filmId, userId);
        if (!filmStorage.checkingId(filmId)) {
            throw new ValidationNotObjectException("Фильм с таким ID:" + filmId + " не найден");
        }
        if (!userStorage.checkingId(userId)) {
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        if (filmStorage.checkLike(filmId, userId)) {
            filmStorage.deleteLikeFilm(filmId, userId);
        }
        feedStorage.addEvent(userId, filmId, EventType.LIKE.name(), Operation.REMOVE.name());
    }

    public List<Film> getTopFilms(long count, Long genreId, Integer year) {
        log.trace("Вход в метод getTopFilms: count={}, genreId={}, year={}", count, genreId, year);

        if (count <= 0) {
            log.warn("Некорректное значение count={}", count);
            throw new ValidationException("Количество фильмов в топе не может быть ноль или меньше ноля");
        }
        if (genreId != null && !genreStorage.checkGenreId(genreId)) {
            throw new ValidationNotObjectException("Жанр с таким ID: " + genreId + " не найден");
        }
        if (year != null && year < 1895) {
            throw new ValidationException("Год не может быть раньше 1895");
        }

        return filmStorage.getTopFilms(count, genreId, year);
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
        } else {
            film.setDirectors(List.of());
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
