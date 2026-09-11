package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mappers.LongRowMapper;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Repository
@Slf4j
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {
    @Autowired
    private LongRowMapper rowMapperLong;

    private static final String INSERT_QUERY = "INSERT INTO users (email, " + "login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM users WHERE user_id = ?";
    private static final String UPDATE_QUERY = "UPDATE users SET email = ?, login = ?, " + "name = ?, birthday = ? WHERE user_id = ?";
    private static final String GET_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String GET_ALL_QUERY = "SELECT * FROM users";
    private static final String GET_BY_EMAIL_QUERY = "SELECT * FROM users WHERE email = ?";
    private static final String ADD_FRIEND_QUERY = "INSERT INTO user_friends (user_id, friend_id, status) " + "VALUES (?, ?, ?)";
    private static final String DElETE_FRIEND_QUERY = "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?";
    private static final String CHECK_FRIEND_QUERY = "SELECT COUNT(*) FROM user_friends WHERE (user_id = ? " + "AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
    private static final String UPDATE_STATUS_FRIEND_QUERY = "UPDATE user_friends SET status = ? " + "WHERE user_id = ? AND friend_id = ?";
    private static final String GET_ALL_FRIENDS_QUERY = "SELECT us.user_id, us.email, us.login, us.name, us.birthday " + "FROM user_friends AS uf JOIN users AS us ON uf.friend_id=us.user_id WHERE uf.user_id = ?";
    private static final String GET_COMMON_FRIENDS_QUERY = "SELECT DISTINCT uf1.friend_id, us.user_id, " + "us.email, us.login, us.name, us.birthday " + "FROM user_friends uf1 JOIN user_friends uf2 ON uf1.friend_id = uf2.friend_id " + "JOIN users us ON uf1.friend_id = us.user_id WHERE uf1.user_id = ? AND uf2.user_id = ?";
    private static final String CHECK_FRIEND_BY_ID_QUERY = "SELECT COUNT(*) FROM user_friends " + "WHERE user_id = ? AND friend_id = ?";
    private static final String GET_ID_FILM_LIKED_BY_USER = "SELECT film_id FROM liked_film WHERE user_id = ?";
    private static final String GET_ALL_USERS_ID = "SELECT user_id FROM users";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> rowMapper) {
        super(jdbc, rowMapper);
    }

    @Override
    public User addUserStorage(User user) {
        exceptionUser(user);
        String name = (user.getName() == null || user.getName().isBlank()) ? user.getLogin() : user.getName();
        if (checkEmail(user.getEmail())) {
            throw new ValidationException("Пользователь с таким имейлом уже существует");
        }
        long id = insert(INSERT_QUERY, user.getEmail(), user.getLogin(), name, user.getBirthday());
        user.setName(name);
        user.setId(id);
        return user;
    }

    @Override
    public void removeUserStorage(long userId) {
        delete(checkingId(userId), DELETE_QUERY, userId);
    }

    @Override
    public User updateUserStorage(User updateUser) {
        update(UPDATE_QUERY, updateUser.getEmail(), updateUser.getLogin(), updateUser.getName(), updateUser.getBirthday(), updateUser.getId());
        return updateUser;
    }

    @Override
    public User getUserById(long userId) {
        return findOne(GET_BY_ID_QUERY, userId).orElseThrow(() -> new ValidationNotObjectException("Пользваотель с id: " + userId + " не найден"));
    }

    @Override
    public List<User> getUserStorage() {
        return findAll(GET_ALL_QUERY);
    }

    @Override
    public boolean checkingId(long id) {
        Optional<User> user = findOne(GET_BY_ID_QUERY, id);
        return user.isPresent();
    }

    @Override
    public void addFriend(long userId, long friendId) {
        boolean status = checkFriendAndConfirmation(userId, friendId);
        insertNotId(ADD_FRIEND_QUERY, userId, friendId, status);
    }

    @Override
    public void deleteFriend(long userId, long friendId) {
        delete(checkFriendById(userId, friendId), DElETE_FRIEND_QUERY, userId, friendId);
    }

    @Override
    public List<User> getAllFriends(long userId) {
        return findAll(GET_ALL_FRIENDS_QUERY, userId);
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherId) {
        return findAll(GET_COMMON_FRIENDS_QUERY, userId, otherId);
    }

    @Override
    public List<Long> recommendationsFilmsId(long userRecId) {
        List<Long> coincidencesUsersId = coincidencesUsersId(userRecId);
        Set<Long> filmsIdUserSet = new HashSet<>();

        for (Long userId : coincidencesUsersId) {
            List<Long> filmsId = filmsIdLikedByUser(userId);
            filmsIdUserSet.addAll(filmsId);
        }

        Set<Long> alreadyLiked = new HashSet<>(filmsIdLikedByUser(userRecId));
        filmsIdUserSet.removeAll(alreadyLiked); // исключаем уже просмотренное

        return new ArrayList<>(filmsIdUserSet);
    }

    private boolean checkEmail(String email) {
        Optional<User> user = findOne(GET_BY_EMAIL_QUERY, email);
        return user.isPresent();
    }

    private boolean checkFriendAndConfirmation(long userId, long friendId) {
        int count = jdbc.queryForObject(CHECK_FRIEND_QUERY, Integer.class, userId, friendId, friendId, userId);
        boolean status = count > 0;
        if (status) {
            update(UPDATE_STATUS_FRIEND_QUERY, true, friendId, userId);
        }
        return status;
    }

    private boolean checkFriendById(long userId, long friendId) {
        Integer count = jdbc.queryForObject(CHECK_FRIEND_BY_ID_QUERY, Integer.class, userId, friendId);
        return count != null;
    }

    private List<Long> filmsIdLikedByUser(long userId) {
        return jdbc.query(GET_ID_FILM_LIKED_BY_USER, rowMapperLong, userId);
    }

    private List<Long> coincidencesUsersId(long userRecId) {
        Set<Long> filmsIdUserSet = new HashSet<>(filmsIdLikedByUser(userRecId));

        List<Long> usersIds = jdbc.query(GET_ALL_USERS_ID, rowMapperLong);
        long max = -1;
        List<Long> result = new ArrayList<>();

        for (Long userId : usersIds) {
            if (userId == userRecId) continue;
            long count = filmsIdLikedByUser(userId).stream()
                    .filter(filmsIdUserSet::contains).count();

            if (count > max) {
                max = count;
                result = new ArrayList<>(List.of(userId));
            } else if (count == max) {
                result.add(userId);
            }
        }
        return result;
    }

    private void exceptionUser(User user) {
        if (user.getLogin() == null) {
            log.warn("Валидация не пройдена: логин не был указан");
            throw new ValidationException("Логин не был указан");
        }
        if (user.getLogin().isBlank()) {
            log.warn("Валидация не пройдена: логин пустой");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (user.getLogin().contains(" ")) {
            log.warn("Валидация не пройдена: логин {} содержит пробелы", user.getLogin());
            throw new ValidationException("Логин не должен содержать пробелов");
        }
        if (user.getBirthday() == null) {
            log.warn("Валидация не пройдена: день рождения не был указан");
            throw new ValidationException("День рождения не был указан");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Валидация не пройдена: дата рождения {} в будущем", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getEmail() == null) {
            log.warn("Валидация не пройдена: email не был указан");
            throw new ValidationException("Email не был указан");
        }
        if (user.getEmail().isBlank()) {
            log.warn("Валидация не пройдена: email пустой");
            throw new ValidationException("Указан пустой email");
        }

    }
}
