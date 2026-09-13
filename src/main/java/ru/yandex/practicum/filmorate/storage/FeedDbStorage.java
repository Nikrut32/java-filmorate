package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

@Repository
public class FeedDbStorage extends BaseDbStorage<Event> implements FeedStorage {

    private static final String INSERT_QUERY =
            "INSERT INTO feed (user_id, entity_id, event_type, operation, created_at) VALUES (?, ?, ?, ?, ?)";
    private static final String GET_FEED_QUERY =
            "SELECT * FROM feed WHERE user_id = ? ORDER BY created_at DESC, event_id DESC";

    public FeedDbStorage(JdbcTemplate jdbc, RowMapper<Event> rowMapper) {
        super(jdbc, rowMapper);
    }

    @Override
    public void addEvent(long userId, long entityId, String eventType, String operation) {
        insertNotId(INSERT_QUERY, userId, entityId, eventType, operation, System.currentTimeMillis());
    }

    @Override
    public List<Event> getFeed(long userId) {
        return findAll(GET_FEED_QUERY, userId);
    }

}