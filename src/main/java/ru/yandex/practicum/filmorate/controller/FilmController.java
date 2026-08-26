package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.model.AnswerString;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
@Slf4j
public class FilmController {

    private final FilmStorage filmStorage;
    private final FilmService filmService;
    private final UserStorage userStorage;


    @GetMapping
    public List<Film> getFilms() {
        log.info("Получен запрос GET /films. Текущее количество фильмов: {}", filmStorage.getFilmStorage().size());
        log.info("Успешно возвращено {} фильмов", filmStorage.getFilmStorage().size());
        return filmStorage.getFilmStorage();
    }

    @GetMapping("/popular")
    public List<Film> topFilms(@RequestParam(defaultValue = "10") long count) {
        log.info("Получен запрос GET /films/popular с параметром count={}", count);
        return filmService.getTopFilms(count);
    }

    @GetMapping("/{id}")
    public Film getFilmById(@PathVariable long id) {
        log.info("Получен запрос GET /films/{id}} с параметром id={}", id);
        return filmStorage.getFilmById(id);
    }

    @PostMapping
    public Film createFilm(@RequestBody Film film) {
        log.info("Получен запрос POST /films на добавление фильма: {}", film);
        Film film2 = film;
        Film createdFilm = filmStorage.addFilmStorage(film);

        log.info("Фильм успешно создан с id={}: {}", createdFilm.getId(), createdFilm.getName());
        return createdFilm;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody UpdateFilmRequest updateFilm) {
        log.info("Получен запрос PUT /films на обновление фильма: {}", updateFilm);
        Film updatedFilm = filmService.updateFilm(updateFilm);
        log.info("Фильм с id={} успешно обновлен.", updatedFilm.getId());
        return updatedFilm;
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public AnswerString addLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Получен запрос PUT /films/{}/like/{} на добавление лайка", id, userId);
        filmService.addLike(id, userId);
        String filmName = filmStorage.getFilmById(id).getName();
        String userLogin = userStorage.getUserById(userId).getLogin();
        log.info("Пользователь {} успешно поставил лайк на фильм «{}»", userLogin, filmName);
        return new AnswerString("Пользователь " + userLogin + " поставил лайк на фильм «" + filmName + "»");
    }


    @DeleteMapping("/{deleteFilmId}")
    @ResponseStatus(HttpStatus.OK)
    public AnswerString deleteFilm(@PathVariable long deleteFilmId) {
        log.info("Получен запрос DELETE /films/{} на удаление фильма", deleteFilmId);
        filmStorage.removeFilmStorage(deleteFilmId);
        log.info("Фильм с id={} успешно удален", deleteFilmId);
        return new AnswerString("Фильм с Id: " + deleteFilmId + " успешно удален");
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public AnswerString deleteLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Получен запрос DELETE /films/{}/like/{} на удаление лайка", id, userId);
        filmService.deleteLike(id, userId);
        String filmName = filmStorage.getFilmById(id).getName();
        String userLogin = userStorage.getUserById(userId).getLogin();
        log.info("Пользователь {} успешно убрал лайк с фильма «{}»", userLogin, filmName);
        return new AnswerString("Пользователь " + userLogin + " убрал лайк поставленный на фильм «" + filmName + "»");
    }


}
