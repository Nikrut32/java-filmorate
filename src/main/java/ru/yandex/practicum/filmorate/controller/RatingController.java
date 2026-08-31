package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.MpaStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
@Slf4j
public class RatingController {

    private final MpaStorage mpaStorage;

    @GetMapping
    public List<Mpa> getRatings() {
        log.info("Получен запрос GET /genres. Текущее количество жанров: {}", mpaStorage.getRatingStorage().size());
        log.info("Успешно возвращено {} жанров", mpaStorage.getRatingStorage().size());
        return mpaStorage.getRatingStorage();
    }

    @GetMapping("/{id}")
    public Mpa getRatingById(@PathVariable long id) {
        log.info("Получен запрос GET /genres/{id}} с параметром id={}", id);
        return mpaStorage.getRatingById(id);
    }
}
