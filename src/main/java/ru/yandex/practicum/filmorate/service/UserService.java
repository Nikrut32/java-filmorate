package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserStorage userStorage;

    public void addFriend(long userId, long friendId) {
        log.trace("Вход в метод addFriend с параметрами userId={}, friendId={}", userId, friendId);
        if (!userStorage.checkingId(userId)) {
            log.warn("Попытка добавить друга для несуществующего пользователя с id={}", userId);
            throw new ValidationNotObjectException("Id пользователя не найден");
        }
        User user = userStorage.getUserById(userId);
        log.trace("Пользователь с id={} найден: {}", userId, user.getLogin());
        if (!userStorage.checkingId(friendId)) {
            log.warn("Попытка добавить несуществующего друга с id={}", friendId);
            throw new ValidationNotObjectException("Id друга не найден");
        }
        User friend = userStorage.getUserById(friendId);
        log.trace("Друг с id={} найден: {}", friendId, friend.getLogin());
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователь {} (id={}) и пользователь {} (id={}) стали друзьями",
                user.getLogin(), userId, friend.getLogin(), friendId);
        log.trace("Количество друзей у пользователя {}: {}", user.getLogin(), user.getFriends().size());
    }

    public void deleteFriend(long userId, long friendId) {
        log.trace("Вход в метод deleteFriend с параметрами userId={}, friendId={}", userId, friendId);
        if (!userStorage.checkingId(userId)) {
            log.warn("Попытка удалить друга у несуществующего пользователя с id={}", userId);
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        User user = userStorage.getUserById(userId);
        log.trace("Пользователь с id={} найден: {}", userId, user.getLogin());
        if (!userStorage.checkingId(friendId)) {
            log.warn("Попытка удалить несуществующего друга с id={}", friendId);
            throw new ValidationNotObjectException("Друг с таким ID: " + friendId + " не найден");
        }
        User friend = userStorage.getUserById(friendId);
        log.trace("Друг с id={} найден: {}", friendId, friend.getLogin());
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователь {} (id={}) и пользователь {} (id={}) перестали быть друзьями",
                user.getLogin(), userId, friend.getLogin(), friendId);
        log.trace("Количество друзей у пользователя {}: {}", user.getLogin(), user.getFriends().size());
    }

    public Set<User> getAllFriends(long userId) {
        log.trace("Вход в метод getAllFriends с параметром userId={}", userId);
        if (!userStorage.checkingId(userId)) {
            log.warn("Запрос списка друзей у несуществующего пользователя с id={}", userId);
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        User user = userStorage.getUserById(userId);
        log.trace("Пользователь с id={} найден: {}", userId, user.getLogin());
        log.info("Получен список друзей для пользователя {} (id={}). Количество друзей: {}",
                user.getLogin(), userId, user.getFriends().size());
        return user.getFriends().stream()
                .map(id -> userStorage.getUserById(id))
                .collect(Collectors.toSet());
    }

    public Set<User> getCommonFriends(long userId, long otherId) {
        log.trace("Вход в метод getCommonFriends с параметрами userId={}, otherId={}", userId, otherId);
        if (!userStorage.checkingId(userId)) {
            log.warn("Запрос общих друзей для несуществующего пользователя с id={}", userId);
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        User user = userStorage.getUserById(userId);
        log.trace("Пользователь с id={} найден: {}", userId, user.getLogin());
        if (!userStorage.checkingId(otherId)) {
            log.warn("Запрос общих друзей с несуществующим пользователем с id={}", otherId);
            throw new ValidationNotObjectException("Пользователь для сравнения с таким ID: " + otherId + " не найден");
        }
        User otherUser = userStorage.getUserById(otherId);
        log.trace("Пользователь для сравнения с id={} найден: {}", otherId, otherUser.getLogin());
        log.info("Найдены общие друзья для пользователей {} (id={}) и {} (id={}).",
                user.getLogin(), userId, otherUser.getLogin(), otherId);
        return user.getFriends().stream()
                .filter(id -> otherUser.getFriends().contains(id))
                .map(id -> userStorage.getUserById(id))
                .collect(Collectors.toSet());
    }
}