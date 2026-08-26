package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
@Slf4j
public class GenreController {

    private final GenreStorage genreStorage;

    @GetMapping
    public List<Genre> getGenres() {
        log.info("Получен запрос GET /genres. Текущее количество жанров: {}", genreStorage.getGenreStorage().size());
        log.info("Успешно возвращено {} жанров", genreStorage.getGenreStorage().size());
        return genreStorage.getGenreStorage();
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable long id) {
        log.info("Получен запрос GET /genres/{id}} с параметром id={}", id);
        return genreStorage.getGenreById(id);
    }
}
