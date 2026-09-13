package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exception.DataProcessingException;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class BaseDbStorage<T> {
    protected final JdbcTemplate jdbc;
    protected final RowMapper<T> rowMapper;

    protected List<T> findAll(String query, Object... args) {
        return jdbc.query(query, rowMapper, (Object[]) args);
    }

    protected Optional<T> findOne(String query, Object... args) {
        try {
            T object = jdbc.queryForObject(query, rowMapper, (Object[]) args);
            return Optional.ofNullable(object);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    protected long insert(String query, Object... args) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            for (int i = 0; i < args.length; i++) {
                statement.setObject(i + 1, args[i]);
            }

            return statement;
        }, keyHolder);

        Integer id = keyHolder.getKeyAs(Integer.class);

        if (id != null) {
            return id;
        } else {
            throw new DataProcessingException("Сохранить данные не удалось");
        }
    }

    protected void update(String query, Object... args) {
        int result = jdbc.update(query, (Object[]) args);

        if (result == 0) {
            throw new DataProcessingException("Обновить данные не удалось");
        }
    }

    protected void delete(boolean check, String query, Long... id) {
        if (!check) {
            throw new DataProcessingException("Удалить данные не удалось");
        }

        jdbc.update(query, (Object[]) id);
    }

    protected void insertNotId(String query, Object... args) {
        jdbc.update(query, (Object[]) args);
    }
}