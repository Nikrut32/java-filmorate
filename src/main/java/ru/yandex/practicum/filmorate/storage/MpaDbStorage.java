package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaDbStorage extends BaseDbStorage<Mpa> implements MpaStorage {
    private static final String GET_BY_ID_QUERY = "SELECT * FROM rating WHERE rating_id = ?";
    private static final String GET_ALL_QUERY = "SELECT * FROM rating";

    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<Mpa> rowMapper) {
        super(jdbc, rowMapper);
    }

    @Override
    public boolean checkRatingId(long ratingId) {
        Optional<Mpa> rating = findOne(GET_BY_ID_QUERY, ratingId);
        return rating.isPresent();
    }

    @Override
    public Mpa getRatingById(long ratingId) {
        return findOne(GET_BY_ID_QUERY, ratingId)
                .orElseThrow(() -> new ValidationNotObjectException("Рейтинга с таким id не существует"));
    }

    @Override
    public List<Mpa> getRatingStorage() {
        return findAll(GET_ALL_QUERY);
    }
}
