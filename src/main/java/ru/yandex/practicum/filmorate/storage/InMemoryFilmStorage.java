package ru.yandex.practicum.filmorate.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Component
@Slf4j
@Getter
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films =  new HashMap<Long, Film>();

    @Override
    public Film addFilmStorage(Film film) {
        exceptionFilm(film);
        film.setId(nextId());
        film.setUsersWhoLiked(new HashSet<User>());
        log.trace("Фильму присвоен Id = {}", film.getId());
        films.put(film.getId(), film);
        log.trace("Фильм добавлен в общий список");
        log.info("Фильм успешно добавлен с ID: {}", film.getId());
        return film;
    }

    @Override
    public void removeFilmStorage(long filmId) {
        if (!checkingId(filmId)) {
            throw new ValidationNotObjectException("Фильм с таким ID: " + filmId + " не найден");
        }
        films.remove(filmId);
    }

    @Override
    public Film updateFilmStorage(Film newFilm) {
        if (newFilm.getId() == null) {
            log.warn("Ошибка обновления фильма: ID не указан");
            throw new ValidationException("Id не должно быть пустым");
        }

        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            if (newFilm.getName() != null) {
                log.trace("Обновлено название фильма на {}", newFilm.getName());
                oldFilm.setName(newFilm.getName());
            }
            if (newFilm.getDescription() != null) {
                log.trace("Обновлено описание фильма на {}", newFilm.getDescription());
                oldFilm.setDescription(newFilm.getDescription());
            }
            if (newFilm.getReleaseDate() != null) {
                log.trace("Обновлена дата релиза фильма на {}", newFilm.getReleaseDate());
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }
            if (newFilm.getDuration() != null) {
                log.trace("Обновлена продолжительность фильма на {}",  newFilm.getDuration());
                oldFilm.setDuration(newFilm.getDuration());
            }
            log.info("Фильм с ID: {} успешно обновлен", newFilm.getId());
            return oldFilm;
        }

        log.warn("Ошибка обновления фильма: фильм с ID {} не найден", newFilm.getId());
        throw new ValidationNotObjectException("Фильма с таким Id = " + newFilm.getId() + "нет в списке");
    }

    @Override
    public Map<Long, Film> getFilmStorage() {
        return films;
    }

    @Override
    public Film getFilmById(long userId) {
        return films.get(userId);
    }

    @Override
    public boolean checkingId(long id) {
        return films.containsKey(id);
    }

    private long nextId() {
        long maxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L);
        return ++maxId;
    }

    private void exceptionFilm(Film film) {
        if (film.getDescription() == null) {
            log.warn("Валидация не пройдена: описание фильма не указано");
            throw new ValidationException("Описание фильма не указано");
        }
        if (film.getDescription().length() > 200) {
            log.warn("Валидация не пройдена: описание фильма длиннее 200 символов");
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }
        if (film.getDescription().isBlank()) {
            log.warn("Валидация не пройдена: описание фильма пустое");
            throw new ValidationException("Описание фильма не может быть пустым");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Валидация не пройдена: некорректная дата релиза {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза должная быть не раньше 28 декабря 1895 года и не в будущем");
        }
        if (film.getDuration() <= 0) {
            log.warn("Валидация не пройдена: отрицательная продолжительность {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        if (film.getName() == null) {
            log.warn("Валидация не пройдена: название фильма не указано");
            throw new ValidationException("Название фильма не указано");
        }
        if (film.getName().isBlank()) {
            log.warn("Валидация не пройдена: название фильма пустое");
            throw new ValidationException("Название фильма не может быть пустым");
        }
    }
}
