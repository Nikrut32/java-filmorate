package ru.yandex.practicum.filmorate.service;

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
public class FilmService {
    @Autowired
    private FilmStorage filmStorage;
    @Autowired
    private UserStorage userStorage;

    public void addLike(long filmId, long userId) {
        if (!filmStorage.checkingId(filmId)) {
           throw new ValidationNotObjectException("Фильм с таким ID: " + filmId + " не найден");
        }
        Film film = filmStorage.getFilmById(filmId);
        if (!userStorage.checkingId(userId)) {
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        User user = userStorage.getUserById(userId);
        film.getUsersWhoLiked().add(user);
    }

    public void deleteLike(long filmId, long userId) {
        if (!filmStorage.checkingId(filmId)) {
            throw new ValidationNotObjectException("Фильм с таким ID:" + filmId + " не найден");
        }
        Film film = filmStorage.getFilmById(filmId);
        if (!userStorage.checkingId(userId)) {
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        User user = userStorage.getUserById(userId);
        film.getUsersWhoLiked().remove(user);
    }

    public Collection<Film> getTopFilms(long count) {
        if (count <= 0) {
            throw new ValidationException("Количество фильмов в топе не может быть ноль или меньше ноля");
        }
        return filmStorage.getFilmStorage().values().stream()
                .sorted(Comparator.comparing((Film film) -> film.getUsersWhoLiked().size()).reversed())
                .limit(count)
                .toList();
    }
}
