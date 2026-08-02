package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    @Autowired
    private FilmStorage filmStorage;
    @Autowired
    private FilmService filmService;
    @Autowired
    private UserStorage userStorage;

    @GetMapping
    public Collection<Film> getFilms() {
        log.info("Получен запрос GET /films. Текущее количество фильмов: {}"
                , filmStorage.getFilmStorage().size());
        return filmStorage.getFilmStorage().values();
    }

    @GetMapping("/popular")
    public Collection<Film> getTopFilms(@RequestParam(defaultValue = "10") long count) {
        return filmService.getTopFilms(count);
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        log.info("Получен запрос POST /films на добавление фильма: {}", film);
        return filmStorage.addFilmStorage(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film newFilm) {
        log.info("Получен запрос PUT /films на обновление фильма: {}", newFilm);
        return filmStorage.updateFilmStorage(newFilm);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public String addLike(@PathVariable long id, @PathVariable long userId) {
        filmService.addLike(id, userId);
        String filmName = filmStorage.getFilmById(id).getName();
        String userLogin = userStorage.getUserById(userId).getLogin();
        return "Пользователь " + userLogin + " поставил лайк на фильм «" + filmName + "»";
    }

    @DeleteMapping("/{deleteFilmId}")
    @ResponseStatus(HttpStatus.OK)
    public String deleteFilm(@PathVariable long deleteFilmId) {
        filmStorage.removeFilmStorage(deleteFilmId);
        String filmName = filmStorage.getFilmById(deleteFilmId).getName();
        return "Фильм «" + filmName + "» успешно удален";
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public String deleteLike(@PathVariable long id, @PathVariable long userId) {
        filmService.deleteLike(id, userId);
        String filmName = filmStorage.getFilmById(id).getName();
        String userLogin = userStorage.getUserById(userId).getLogin();
        return "Пользователь " + userLogin + " убрал лайк поставленный на фильм «" + filmName + "»";
    }
}
