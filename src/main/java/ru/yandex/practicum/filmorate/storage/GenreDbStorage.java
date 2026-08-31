package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Repository
public class GenreDbStorage extends BaseDbStorage<Genre> implements GenreStorage {
    private static final String GET_BY_ID_QUERY = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String GET_ALL_QUERY = "SELECT * FROM genres";
    private static final String GET_ALL_FIL_ID_QUERY = "SELECT gn.genre_id, gn.name FROM film_genres AS fg " +
            "JOIN genres AS gn ON fg.genre_id=gn.genre_id " +
            "JOIN films AS fl ON fg.film_id=fl.film_id WHERE fl.film_id = ?";
    private static final String DELETE_BY_FILM_ID_QUERY = "DELETE FROM film_genres WHERE film_id = ?";

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> rowMapper) {
        super(jdbc, rowMapper);
    }

    @Override
    public boolean checkGenreId(long genreId) {
        Optional<Genre> genre = findOne(GET_BY_ID_QUERY, genreId);
        return genre.isPresent();
    }

    @Override
    public Genre getGenreById(long genreId) {
        return findOne(GET_BY_ID_QUERY, genreId)
                .orElseThrow(() -> new ValidationNotObjectException("Жанра с таким id не существует"));
    }

    @Override
    public List<Genre> getGenreStorage() {
        return findAll(GET_ALL_QUERY);
    }

    @Override
    public List<Genre> getFilmIdGenreStorage(long filmId) {
        return findAll(GET_ALL_FIL_ID_QUERY,  filmId);
    }

    @Override
    public void deleteGenreByFilmId(long filmId) {
        delete(true, DELETE_BY_FILM_ID_QUERY, filmId);
    }
}
