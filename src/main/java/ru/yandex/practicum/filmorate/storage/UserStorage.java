package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    User addUserStorage(User user);

    void removeUserStorage(long userId);

    User updateUserStorage(User newUser);

    User getUserById(long userId);

    List<User> getUserStorage();

    boolean checkingId(long id);

    void addFriend(long userId, long friendId);

    void deleteFriend(long userId, long friendId);

    List<User> getAllFriends(long userId);

    List<User> getCommonFriends(long userId,  long otherId);
}
