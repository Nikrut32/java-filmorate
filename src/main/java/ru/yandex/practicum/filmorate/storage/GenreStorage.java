package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreStorage {
    boolean checkGenreId(long genreId);

    Genre getGenreById(long genreId);

    List<Genre> getGenreStorage();

    List<Genre> getFilmIdGenreStorage(long filmId);

    void deleteGenreByFilmId(long filmId);
}
