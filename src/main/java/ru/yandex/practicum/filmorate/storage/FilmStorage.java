package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
    Film addFilmStorage(Film film);

    void removeFilmStorage(long filmId);

    Film updateFilmStorage(Film newFilm);

    List<Film> getFilmStorage();

    Film getFilmById(long filmId);

    boolean checkingId(long id);

    void addLikeFilm(long filmId, long userId);

    void deleteLikeFilm(long filmId, long userId);

    List<Film> getTopFilms(long count, Long genreId, Integer year);

    boolean checkLike(long filmId, long userId);

    default List<Film> getTopFilms(long count) {
        return getTopFilms(count, null, null);
    }

    void addGenreFilm(long filmId, long genreId);

    List<Film> getFilmsByDirector(long directorId, String sortBy);

    List<Film> searchFilms(String query, String by);

    List<Film> getFilmsRecommendation(long userId);

    List<Film> getCommonFilms(long userId, long friendId);
}
