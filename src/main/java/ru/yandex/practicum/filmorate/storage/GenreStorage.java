package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreStorage {
    public boolean checkGenreId(long genreId);

    public Genre getGenreById(long genreId);

    public List<Genre> getGenreStorage();

    public List<Genre> getFilmIdGenreStorage(long filmId);

    public void deleteGenreByFilmId(long filmId);
}
