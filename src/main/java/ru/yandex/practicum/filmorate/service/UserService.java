package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public void addFriend(long userId, long friendId) {
        log.trace("Вход в метод addFriend с параметрами userId={}, friendId={}", userId, friendId);
        if (!userStorage.checkingId(userId)) {
            log.warn("Попытка добавить друга для несуществующего пользователя с id={}", userId);
            throw new ValidationNotObjectException("Id пользователя не найден");
        }
        if (!userStorage.checkingId(friendId)) {
            log.warn("Попытка добавить несуществующего друга с id={}", friendId);
            throw new ValidationNotObjectException("Id друга не найден");
        }
        userStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        log.trace("Вход в метод deleteFriend с параметрами userId={}, friendId={}", userId, friendId);
        if (!userStorage.checkingId(userId)) {
            log.warn("Попытка удалить друга у несуществующего пользователя с id={}", userId);
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        if (!userStorage.checkingId(friendId)) {
            log.warn("Попытка удалить несуществующего друга с id={}", friendId);
            throw new ValidationNotObjectException("Друг с таким ID: " + friendId + " не найден");
        }
        userStorage.deleteFriend(userId, friendId);
    }

    public List<User> getAllFriends(long userId) {
        log.trace("Вход в метод getAllFriends с параметром userId={}", userId);
        if (!userStorage.checkingId(userId)) {
            log.warn("Запрос списка друзей у несуществующего пользователя с id={}", userId);
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        return userStorage.getAllFriends(userId);
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        log.trace("Вход в метод getCommonFriends с параметрами userId={}, otherId={}", userId, otherId);
        if (!userStorage.checkingId(userId)) {
            log.warn("Запрос общих друзей для несуществующего пользователя с id={}", userId);
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        if (!userStorage.checkingId(otherId)) {
            log.warn("Запрос общих друзей с несуществующим пользователем с id={}", otherId);
            throw new ValidationNotObjectException("Пользователь для сравнения с таким ID: " + otherId + " не найден");
        }
        return userStorage.getCommonFriends(userId, otherId);
    }

    public User updateUserStorage(long userId, UpdateUserRequest updateUser) {
        User user = userStorage.getUserById(userId);
        if (updateUser.hasName()) {
            user.setName(updateUser.getName());
        }
        if (updateUser.hasEmail()) {
            user.setEmail(updateUser.getEmail());
        }
        if  (updateUser.hasBirthday()) {
            user.setBirthday(updateUser.getBirthday());
        }
        if (updateUser.hasLogin()) {
            user.setLogin(updateUser.getLogin());
        }

        return userStorage.updateUserStorage(user);
    }
}