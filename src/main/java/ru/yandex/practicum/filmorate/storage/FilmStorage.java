package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    public Film addFilmStorage(Film film);

    public void removeFilmStorage(long filmId);

    public Film updateFilmStorage(Film newFilm);

    public List<Film> getFilmStorage();

    public Film getFilmById(long filmId);

    public boolean checkingId(long id);

    public void addLikeFilm(long filmId, long userId);

    public void deleteLikeFilm(long filmId, long userId);

    public List<Film> getTopFilms(long count, Long genreId, Integer year);

    default List<Film> getTopFilms(long count) {
        return getTopFilms(count, null, null);
    }

    public void addGenreFilm(long filmId, long genreId);
}
