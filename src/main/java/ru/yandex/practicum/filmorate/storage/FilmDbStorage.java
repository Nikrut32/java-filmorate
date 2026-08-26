package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Qualifier
@Repository
@Slf4j
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {
    @Autowired
    private MpaStorage mpaStorage;
    @Autowired
    private GenreStorage genreStorage;

    private static final String INSERT_QUERY = "INSERT INTO films (name, description, release_date, duration, rating_id) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String GET_BY_NAME_QUERY = "SELECT fl.*, rt.name_rating FROM films AS fl " +
            "LEFT JOIN rating AS rt ON fl.rating_id = rt.rating_id WHERE fl.name = ?";
    private static final String GET_ALL_QUERY = "SELECT fl.*, rt.name_rating FROM films AS fl " +
            "LEFT JOIN rating AS rt ON fl.rating_id = rt.rating_id;";
    private static final String DELETE_QUERY = "DELETE CASCADE FROM films WHERE film_id = ?";
    private static final String GET_BY_ID_QUERY = "SELECT fl.*, rt.name_rating FROM films AS fl " +
            "LEFT JOIN rating AS rt ON fl.rating_id = rt.rating_id WHERE film_id = ?";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?," +
            " duration = ?, rating_id = ? WHERE film_id = ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO liked_film (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM liked_film WHERE film_id = ? AND user_id = ?";
    private static final String GET_TOP_QUERY = "SELECT fl.film_id, name, description, release_date, " +
            "duration, fl.rating_id, r.name_rating AS rating_name, COUNT(lf.user_id) AS likes_count " +
            "FROM liked_film AS lf " +
            "RIGHT JOIN films AS fl ON lf.film_id = fl.film_id " +
            "LEFT JOIN rating AS r ON fl.rating_id = r.rating_id " +
            "GROUP BY fl.film_id, fl.name, fl.description, fl.release_date, fl.duration, fl.rating_id, r.name_rating " +
            "ORDER BY likes_count DESC, fl.film_id ASC LIMIT ?";
    private static final String ADD_GENRE_QUERY = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";


    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> rowMapper) {
        super(jdbc, rowMapper);
    }

    @Override
    public Film addFilmStorage(Film film) {
        exceptionFilm(film);

        if (film.getGenres() == null) {
            film.setGenres(List.of());
        }
        if (checkName(film.getName())) {
            throw new ValidationException("Фильм с таким названием уже существует");
        }
        if (!mpaStorage.checkRatingId(film.getMpa().getId())) {
            throw new ValidationNotObjectException(
                    "Рейтинг с таким ID: " + film.getMpa().getId() + " не найден"
            );
        }
        for (Genre genre : film.getGenres()) {
            if (!genreStorage.checkGenreId(genre.getId())) {
                throw new ValidationNotObjectException(
                        "Жанр с таким ID: " + genre.getId() + " не найден"
                );
            }
        }

        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );

        film.setId(id);
        saveGenres(film.getId(), film.getGenres());
        film.setGenres(genreStorage.getFilmIdGenreStorage(film.getId()));
        film.setMpa(mpaStorage.getRatingById(film.getMpa().getId()));
        return film;
    }

    @Override
    public void removeFilmStorage(long filmId) {
        delete(DELETE_QUERY, filmId);
    }

    @Override
    public Film updateFilmStorage(Film updateFilm) {
        if (!mpaStorage.checkRatingId(updateFilm.getMpa().getId())) {
            throw new ValidationNotObjectException(
                    "Рейтинг с таким ID: " + updateFilm.getMpa().getId() + " не найден"
            );
        }

        genreStorage.deleteGenreByFilmId(updateFilm.getId());

        if (updateFilm.getGenres() != null) {
            saveGenres(updateFilm.getId(), updateFilm.getGenres());
        }

        update(
                UPDATE_QUERY,
                updateFilm.getName(),
                updateFilm.getDescription(),
                updateFilm.getReleaseDate(),
                updateFilm.getDuration(),
                updateFilm.getMpa().getId(),
                updateFilm.getId()
        );

        return getFilmById(updateFilm.getId());
    }

    @Override
    public List<Film> getFilmStorage() {
        List<Film> films = findAll(GET_ALL_QUERY);
        films.forEach(film -> film.setGenres(genreStorage.getFilmIdGenreStorage(film.getId())));
        return films;
    }

    @Override
    public Film getFilmById(long filmId) {
        Film film = findOne(GET_BY_ID_QUERY, filmId)
                .orElseThrow(() -> new ValidationNotObjectException("Фильм с id: " + filmId + " не найден"));
        film.setGenres(genreStorage.getFilmIdGenreStorage(filmId));
        return film;
    }

    @Override
    public boolean checkingId(long id) {
        Optional<Film> film = findOne(GET_BY_ID_QUERY, id);
        return film.isPresent();
    }

    @Override
    public void addLikeFilm(long filmId, long userId) {
        insertNotId(ADD_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void deleteLikeFilm(long filmId, long userId) {
        delete(DELETE_LIKE_QUERY, filmId, userId);
    }

    @Override
    public List<Film> getTopFilms(long count) {
        return findAll(GET_TOP_QUERY, count);
    }

    @Override
    public void addGenreFilm(long filmId, long genreId) {
        insertNotId(ADD_GENRE_QUERY, filmId, genreId);
    }

    private void saveGenres(Long filmId, List<Genre> genres) {
        for (int i = 0; i < genres.size(); i++) {
            update(ADD_GENRE_QUERY, filmId, genres.get(i).getId());
        }
    }

    private boolean checkName(String name) {
        Optional<Film> film = findOne(GET_BY_NAME_QUERY, name);
        return film.isPresent();
    }

    private void exceptionFilm(Film film) {
        if (film.getDescription().length() > 200) {
            log.warn("Валидация не пройдена: описание фильма длиннее 200 символов");
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }
        if (film.getDescription().isBlank()) {
            log.warn("Валидация не пройдена: описание фильма пустое");
            throw new ValidationException("Описание фильма не может быть пустым");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Валидация не пройдена: некорректная дата релиза {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза должная быть не раньше 28 декабря 1895 года и не в будущем");
        }
        if (film.getDuration() <= 0) {
            log.warn("Валидация не пройдена: отрицательная продолжительность {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        if (film.getName() == null) {
            log.warn("Валидация не пройдена: название фильма не указано");
            throw new ValidationException("Название фильма не указано");
        }
        if (film.getName().isBlank()) {
            log.warn("Валидация не пройдена: название фильма пустое");
            throw new ValidationException("Название фильма не может быть пустым");
        }
    }
}
