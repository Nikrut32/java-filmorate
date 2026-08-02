package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Map;

public interface UserStorage {
    public User addUserStorage(User user);

    public void removeUserStorage(long userId);

    public User updateUserStorage(User newUser);

    public User getUserById(long userId);

    public Map<Long, User> getUserStorage();

    public boolean checkingId(long id);
}
