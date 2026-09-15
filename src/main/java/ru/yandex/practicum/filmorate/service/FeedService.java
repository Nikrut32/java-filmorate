package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedService {

    private final FeedStorage feedStorage;
    private final UserStorage userStorage;

    public List<Event> getFeed(long userId) {
        log.info("Запрос ленты событий для пользователя id={}", userId);
        if (!userStorage.checkingId(userId)) {
            log.warn("Пользователь с id={} не найден", userId);
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        List<Event> feed = feedStorage.getFeed(userId);
        log.info("Для пользователя id={} возвращено {} событий", userId, feed.size());
        return feed;
    }
}