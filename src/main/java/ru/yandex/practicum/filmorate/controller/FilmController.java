package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    @Autowired
    private FilmStorage filmStorage;
    @Autowired
    private FilmService filmService;

    @GetMapping
    public Collection<Film> getFilms() {
        log.info("Получен запрос GET /films. Текущее количество фильмов: {}"
                , filmStorage.getFilmStorage().size());
        return filmStorage.getFilmStorage().values();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
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
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        filmService.addLike(id, userId);
    }

    //Добавить отлов исключений
    @DeleteMapping("/{deleteFilmId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteFilm(@PathVariable long deleteFilmId) {
        filmStorage.removeFilmStorage(deleteFilmId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteLike(@PathVariable long id, @PathVariable long userId) {
        filmService.deleteLike(id, userId);
    }
}
