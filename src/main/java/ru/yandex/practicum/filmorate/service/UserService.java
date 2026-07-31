package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.getFriends().add(friend);
        friend.getFriends().add(user);
    }

    public void deleteFriend(long userId, long friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);
        user.getFriends().remove(friend);
        friend.getFriends().remove(user);
    }

    public Set<User> getAllFriends(long userId) {
        User user = userStorage.getUserById(userId);
        return user.getFriends();
    }

    public Set<User> getCommonFriends(long userId, long otherId) {
        User user = userStorage.getUserById(userId);
        User otherUser = userStorage.getUserById(otherId);
        return user.getFriends().stream()
                .filter(us -> otherUser.getFriends().contains(us))
                .collect(Collectors.toSet());
    }
}
