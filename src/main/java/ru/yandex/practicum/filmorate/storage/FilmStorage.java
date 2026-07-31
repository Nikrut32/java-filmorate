package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Map;

public interface FilmStorage {
    public Film addFilmStorage(Film film);
    public void removeFilmStorage(long filmId);
    public Film updateFilmStorage(Film newFilm);
    public Map<Long, Film> getFilmStorage();
    public Film getFilmById(long filmId);
}
