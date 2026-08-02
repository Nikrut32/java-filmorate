package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserStorage userStorage;

    public void addFriend(long userId, long friendId) {
        if (!userStorage.checkingId(userId)) {
            throw new ValidationNotObjectException("Id пользователя не найден");
        }
        User user = userStorage.getUserById(userId);
        if (!userStorage.checkingId(friendId)) {
            throw new ValidationNotObjectException("Id друга не найден");
        }
        User friend = userStorage.getUserById(friendId);
        user.getFriends().add(friend);
        friend.getFriends().add(user);
    }

    public void deleteFriend(long userId, long friendId) {
        if (!userStorage.checkingId(userId)) {
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        User user = userStorage.getUserById(userId);
        if (!userStorage.checkingId(friendId)) {
            throw new ValidationNotObjectException("Друг с таким ID: " + friendId + " не найден");
        }
        User friend = userStorage.getUserById(friendId);
        user.getFriends().remove(friend);
        friend.getFriends().remove(user);
    }

    public Set<User> getAllFriends(long userId) {
        if (!userStorage.checkingId(userId)) {
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        User user = userStorage.getUserById(userId);
        return user.getFriends();
    }

    public Set<User> getCommonFriends(long userId, long otherId) {
        if (!userStorage.checkingId(userId)) {
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        User user = userStorage.getUserById(userId);
        if (!userStorage.checkingId(otherId)) {
            throw new ValidationNotObjectException("Пользователь для сравнения с таким ID: " + otherId + " не найден");
        }
        User otherUser = userStorage.getUserById(otherId);
        return user.getFriends().stream()
                .filter(us -> otherUser.getFriends().contains(us))
                .collect(Collectors.toSet());
    }
}
