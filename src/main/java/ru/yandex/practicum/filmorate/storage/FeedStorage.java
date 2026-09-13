package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface FeedStorage {
    void addEvent(long userId, long entityId, String eventType, String operation);

    List<Event> getFeed(long userId);
}