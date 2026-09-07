package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;

@Repository
public class DirectorDbStorage extends BaseDbStorage<Director>
        implements DirectorStorage {

    private static final String INSERT_QUERY =
            "INSERT INTO directors (name) VALUES (?)";

    private static final String GET_BY_ID_QUERY =
            "SELECT director_id, name FROM directors WHERE director_id = ?";

    private static final String GET_ALL_QUERY =
            "SELECT director_id, name FROM directors ORDER BY director_id";

    private static final String UPDATE_QUERY =
            "UPDATE directors SET name = ? WHERE director_id = ?";

    private static final String DELETE_QUERY =
            "DELETE FROM directors WHERE director_id = ?";

    public DirectorDbStorage(
            JdbcTemplate jdbc,
            RowMapper<Director> rowMapper
    ) {
        super(jdbc, rowMapper);
    }

    @Override
    public Director addDirector(Director director) {
        long id = insert(
                INSERT_QUERY,
                director.getName()
        );

        director.setId(id);

        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        update(
                UPDATE_QUERY,
                director.getName(),
                director.getId()
        );

        return getDirectorById(director.getId());
    }

    @Override
    public void removeDirector(long directorId) {
        if (!checkingId(directorId)) {
            throw new ValidationNotObjectException(
                    "Режиссёр с id: " + directorId + " не найден"
            );
        }

        delete(
                true,
                DELETE_QUERY,
                directorId
        );
    }

    @Override
    public Director getDirectorById(long directorId) {
        return findOne(
                GET_BY_ID_QUERY,
                directorId
        ).orElseThrow(() ->
                new ValidationNotObjectException(
                        "Режиссёр с id: " + directorId + " не найден"
                )
        );
    }

    @Override
    public List<Director> getDirectors() {
        return findAll(GET_ALL_QUERY);
    }

    @Override
    public boolean checkingId(long directorId) {
        return findOne(
                GET_BY_ID_QUERY,
                directorId
        ).isPresent();
    }
}