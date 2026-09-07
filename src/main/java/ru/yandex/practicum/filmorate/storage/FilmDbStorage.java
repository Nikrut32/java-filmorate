package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
import java.util.stream.Collectors;

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
    private static final String DELETE_QUERY = "DELETE FROM films WHERE film_id = ?";
    private static final String GET_BY_ID_QUERY = "SELECT fl.*, rt.name_rating FROM films AS fl " +
            "LEFT JOIN rating AS rt ON fl.rating_id = rt.rating_id WHERE film_id = ?";
    private static final String UPDATE_QUERY = "UPDATE films SET name = ?, description = ?, release_date = ?," +
            " duration = ?, rating_id = ? WHERE film_id = ?";
    private static final String ADD_LIKE_QUERY = "INSERT INTO liked_film (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM liked_film WHERE film_id = ? AND user_id = ?";
    private static final String GET_TOP_QUERY = "SELECT fl.film_id, name, description, release_date, " +
            "duration, fl.rating_id, r.name_rating, COUNT(lf.user_id) AS likes_count " +
            "FROM liked_film AS lf " +
            "RIGHT JOIN films AS fl ON lf.film_id = fl.film_id " +
            "LEFT JOIN rating AS r ON fl.rating_id = r.rating_id " +
            "GROUP BY fl.film_id, fl.name, fl.description, fl.release_date, fl.duration, fl.rating_id, r.name_rating " +
            "ORDER BY likes_count DESC, fl.film_id ASC LIMIT ?";
    private static final String ADD_GENRE_QUERY = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String CHECK_LIKE_QUERY = "SELECT COUNT(*) FROM liked_film WHERE film_id = ?";

    private static final String GET_COMMON_FILMS_QUERY = "SELECT fl.film_id, fl.name, fl.description, fl.release_date, " +
            "fl.duration, fl.rating_id, r.name_rating, " +
            "COUNT(lf.user_id) AS likes_count " +
            "FROM films fl " +
            "LEFT JOIN rating r ON fl.rating_id = r.rating_id " +
            "LEFT JOIN liked_film lf ON fl.film_id = lf.film_id " +
            "WHERE EXISTS ( " +
            "    SELECT 1 FROM liked_film lf1 " +
            "    WHERE lf1.film_id = fl.film_id AND lf1.user_id = ? " +
            ") AND EXISTS ( " +
            "    SELECT 1 FROM liked_film lf2 " +
            "    WHERE lf2.film_id = fl.film_id AND lf2.user_id = ? " +
            ") " +
            "GROUP BY fl.film_id, fl.name, fl.description, fl.release_date, " +
            "fl.duration, fl.rating_id, r.name_rating " +
            "ORDER BY likes_count DESC, fl.film_id ASC";

    @Override
    public List<Film> getCommonFilms(long userId, long friendId) {
        log.trace("Вход в метод getCommonFilms с параметрами userId={}, friendId={}", userId, friendId);
        List<Film> films = findAll(GET_COMMON_FILMS_QUERY, userId, friendId);
        films.forEach(film -> film.setGenres(genreStorage.getFilmIdGenreStorage(film.getId())));
        log.info("Найдено {} общих фильмов у пользователей с id={} и id={}", films.size(), userId, friendId);
        return films;
    }

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> rowMapper) {
        super(jdbc, rowMapper);
    }

    @Override
    public Film addFilmStorage(Film film) {
        exceptionFilm(film);

        if (film.getGenres() == null) {
            film.setGenres(List.of());
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
        delete(checkingId(filmId), DELETE_QUERY, filmId);
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
        try {
            insertNotId(ADD_LIKE_QUERY, filmId, userId);
        } catch (DuplicateKeyException e) {
            log.warn("Пользователь с id {} уже поставил лайк на фильм с id: {}", userId, filmId);
        }

    }

    @Override
    public void deleteLikeFilm(long filmId, long userId) {
        delete(checkLikeId(filmId), DELETE_LIKE_QUERY, filmId, userId);
    }

    @Override
    public List<Film> getTopFilms(long count) {
        List<Film> films = findAll(GET_TOP_QUERY, count);
        films.forEach(film -> film.setGenres(genreStorage.getFilmIdGenreStorage(film.getId())));
        return films;
    }

    @Override
    public void addGenreFilm(long filmId, long genreId) {
        insertNotId(ADD_GENRE_QUERY, filmId, genreId);
    }

    public Optional<Film> getFilmByIdTest(String query, long filmId) {
        return findOne(query, filmId);
    }

    private void saveGenres(Long filmId, List<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        genreStorage.deleteGenreByFilmId(filmId);

        List<Long> uniqueGenreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toList());

        for (Long genreId : uniqueGenreIds) {
            try {
                insertNotId(ADD_GENRE_QUERY, filmId, genreId);
            } catch (DuplicateKeyException e) {
                log.warn("Жанр с id: {} уже добавлен для фильма с id: {}", genreId, filmId);
            }
        }
    }

    private boolean checkName(String name) {
        Optional<Film> film = findOne(GET_BY_NAME_QUERY, name);
        return film.isPresent();
    }

    private boolean checkLikeId(long filmId) {
        Integer count = jdbc.queryForObject(CHECK_LIKE_QUERY, Integer.class, filmId);
        return count != null && count != 0;
    }

    private void exceptionFilm(Film film) {
        if (film.getName() == null) {
            log.warn("Валидация не пройдена: название фильма не указано");
            throw new ValidationException("Название фильма не указано");
        }
        if (film.getName().isBlank()) {
            log.warn("Валидация не пройдена: название фильма пустое");
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() == null) {
            log.warn("Валидация не пройдена: описание фильма не указано");
            throw new ValidationException("Описание фильма не указано");
        }
        if (film.getDescription().isBlank()) {
            log.warn("Валидация не пройдена: описание фильма пустое");
            throw new ValidationException("Описание фильма не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            log.warn("Валидация не пройдена: описание фильма длиннее 200 символов");
            throw new ValidationException("Максимальная длина описания - 200 символов");
        }
        if (film.getReleaseDate() == null) {
            log.warn("Валидация не пройдена: дата релиза не указана");
            throw new ValidationException("Дата релиза должна быть указана");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Валидация не пройдена: некорректная дата релиза {}", film.getReleaseDate());
            throw new ValidationException("Дата релиза должная быть не раньше 28 декабря 1895 года и не в будущем");
        }
        if (film.getDuration() <= 0) {
            log.warn("Валидация не пройдена: отрицательная продолжительность {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}
