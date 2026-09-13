package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.mappers.EventRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FeedDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FeedDbStorage.class, EventRowMapper.class, UserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FeedDbStorageTest {

    private final FeedDbStorage feedDbStorage;
    private final UserDbStorage userDbStorage;

    @Test
    void addEventAndGetFeed() {
        User user = userDbStorage.addUserStorage(User.builder()
                .email("test@mail.ru")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        feedDbStorage.addEvent(user.getId(), 100L, EventType.LIKE.name(), Operation.ADD.name());
        List<Event> feed = feedDbStorage.getFeed(user.getId());

        assertEquals(1, feed.size());
        assertEquals("LIKE", feed.get(0).getEventType());
        assertEquals("ADD", feed.get(0).getOperation());
        assertEquals(100L, feed.get(0).getEntityId());
        assertEquals(user.getId(), feed.get(0).getUserId());
        assertNotNull(feed.get(0).getEventId());
        assertNotNull(feed.get(0).getTimestamp());
    }

    @Test
    void getFeedForUnknownUserShouldReturnEmptyList() {
        List<Event> feed = feedDbStorage.getFeed(999L);

        assertNotNull(feed);
        assertTrue(feed.isEmpty());
    }

    @Test
    void feedShouldReturnEventsNewestFirst() throws InterruptedException {
        User user = userDbStorage.addUserStorage(User.builder()
                .email("order@mail.ru")
                .login("orderuser")
                .name("Order User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        feedDbStorage.addEvent(user.getId(), 1L, EventType.LIKE.name(), Operation.ADD.name());
        Thread.sleep(5);
        feedDbStorage.addEvent(user.getId(), 2L, EventType.FRIEND.name(), Operation.ADD.name());
        Thread.sleep(5);
        feedDbStorage.addEvent(user.getId(), 3L, EventType.REVIEW.name(), Operation.ADD.name());

        List<Event> feed = feedDbStorage.getFeed(user.getId());

        assertEquals(3, feed.size());
        // Сначала самые новые
        assertEquals(3L, feed.get(0).getEntityId());
        assertEquals(2L, feed.get(1).getEntityId());
        assertEquals(1L, feed.get(2).getEntityId());
    }

    @Test
    void feedShouldStoreAllEventTypesAndOperations() {
        User user = userDbStorage.addUserStorage(User.builder()
                .email("types@mail.ru")
                .login("typesuser")
                .name("Types User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build());

        feedDbStorage.addEvent(user.getId(), 10L, EventType.LIKE.name(), Operation.ADD.name());
        feedDbStorage.addEvent(user.getId(), 20L, EventType.LIKE.name(), Operation.REMOVE.name());
        feedDbStorage.addEvent(user.getId(), 30L, EventType.FRIEND.name(), Operation.ADD.name());
        feedDbStorage.addEvent(user.getId(), 40L, EventType.FRIEND.name(), Operation.REMOVE.name());
        feedDbStorage.addEvent(user.getId(), 50L, EventType.REVIEW.name(), Operation.ADD.name());
        feedDbStorage.addEvent(user.getId(), 60L, EventType.REVIEW.name(), Operation.UPDATE.name());
        feedDbStorage.addEvent(user.getId(), 70L, EventType.REVIEW.name(), Operation.REMOVE.name());

        List<Event> feed = feedDbStorage.getFeed(user.getId());

        assertEquals(7, feed.size());
        assertTrue(feed.stream().anyMatch(e -> "LIKE".equals(e.getEventType()) && "ADD".equals(e.getOperation())));
        assertTrue(feed.stream().anyMatch(e -> "LIKE".equals(e.getEventType()) && "REMOVE".equals(e.getOperation())));
        assertTrue(feed.stream().anyMatch(e -> "FRIEND".equals(e.getEventType()) && "ADD".equals(e.getOperation())));
        assertTrue(feed.stream().anyMatch(e -> "FRIEND".equals(e.getEventType()) && "REMOVE".equals(e.getOperation())));
        assertTrue(feed.stream().anyMatch(e -> "REVIEW".equals(e.getEventType()) && "ADD".equals(e.getOperation())));
        assertTrue(feed.stream().anyMatch(e -> "REVIEW".equals(e.getEventType()) && "UPDATE".equals(e.getOperation())));
        assertTrue(feed.stream().anyMatch(e -> "REVIEW".equals(e.getEventType()) && "REMOVE".equals(e.getOperation())));
    }
}