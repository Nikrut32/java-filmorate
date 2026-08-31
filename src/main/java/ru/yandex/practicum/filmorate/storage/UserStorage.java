package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserStorage {
    public User addUserStorage(User user);

    public void removeUserStorage(long userId);

    public User updateUserStorage(User newUser);

    public User getUserById(long userId);

    public List<User> getUserStorage();

    public boolean checkingId(long id);

    public void addFriend(long userId, long friendId);

    public void deleteFriend(long userId, long friendId);

    public List<User> getAllFriends(long userId);

    public List<User> getCommonFriends(long userId,  long otherId);
}
