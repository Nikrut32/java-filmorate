package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;

@Service
@Slf4j
public class FilmService {
    @Autowired
    private FilmStorage filmStorage;
    @Autowired
    private UserStorage userStorage;

    public void addLike(long filmId, long userId) {
        log.trace("Вход в метод addLike с параметрами filmId={}, userId={}", filmId, userId);

        if (!filmStorage.checkingId(filmId)) {
            log.warn("Попытка добавить лайк к несуществующему фильму с id={}", filmId);
            throw new ValidationNotObjectException("Фильм с таким ID: " + filmId + " не найден");
        }
        Film film = filmStorage.getFilmById(filmId);
        log.trace("Фильм с id={} найден: {}", filmId, film.getName());
        if (!userStorage.checkingId(userId)) {
            log.warn("Попытка добавить лайк от несуществующего пользователя с id={}", userId);
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        User user = userStorage.getUserById(userId);
        log.trace("Пользователь с id={} найден: {}", userId, user.getLogin());
        film.getUsersWhoLiked().add(userId);
        log.info("Пользователь {} (id={}) поставил лайк фильму '{}' (id={})",
                user.getLogin(), userId, film.getName(), filmId);
        log.trace("Текущее количество лайков у фильма '{}': {}", film.getName(), film.getUsersWhoLiked().size());
    }

    public void deleteLike(long filmId, long userId) {
        log.trace("Вход в метод deleteLike с параметрами filmId={}, userId={}", filmId, userId);
        if (!filmStorage.checkingId(filmId)) {
            log.warn("Попытка удалить лайк у несуществующего фильма с id={}", filmId);
            throw new ValidationNotObjectException("Фильм с таким ID:" + filmId + " не найден");
        }
        Film film = filmStorage.getFilmById(filmId);
        log.trace("Фильм с id={} найден: {}", filmId, film.getName());
        if (!userStorage.checkingId(userId)) {
            log.warn("Попытка удалить лайк от несуществующего пользователя с id={}", userId);
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        User user = userStorage.getUserById(userId);
        log.trace("Пользователь с id={} найден: {}", userId, user.getLogin());
        film.getUsersWhoLiked().remove(userId);
        log.info("Пользователь {} (id={}) убрал лайк с фильма '{}' (id={})",
                user.getLogin(), userId, film.getName(), filmId);
        log.trace("Текущее количество лайков у фильма '{}': {}", film.getName(), film.getUsersWhoLiked().size());
    }

    public Collection<Film> getTopFilms(long count) {
        log.trace("Вход в метод getTopFilms с параметром count={}", count);
        if (count <= 0) {
            log.warn("Некорректное значение count={}", count);
            throw new ValidationException("Количество фильмов в топе не может быть ноль или меньше ноля");
        }
        Collection<Film> topFilms = filmStorage.getFilmStorage().values().stream()
                .sorted(Comparator.comparing((Film film) -> film.getUsersWhoLiked().size()).reversed())
                .limit(count)
                .toList();
        log.info("Успешно получен топ-{} фильмов. Результат содержит {} фильмов", count, topFilms.size());
        return topFilms;
    }
}
