package ru.yandex.practicum.filmorate.storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Component
@Getter
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users =  new HashMap<Long, User>();

    @Override
    public User addUserStorage(User user) {
        exceptionUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("У пользователя не указано имя, в качестве имени будет использован логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        user.setId(nextId());
        user.setFriends(new HashSet<>());
        log.trace("Пользователю присвоен Id = {}", user.getId());
        users.put(user.getId(), user);
        log.trace("Пользователь добавлен в общий список");
        log.info("Пользователь успешно создан с ID: {}", user.getId());
        return user;
    }

    @Override
    public void removeUserStorage(long userId) {
        if (!checkingId(userId)) {
            throw new ValidationNotObjectException("Пользователь с таким ID: " + userId + " не найден");
        }
        users.remove(userId);
    }

    @Override
    public User updateUserStorage(User newUser) {
        if (newUser.getId() == null) {
            log.warn("Ошибка обновления пользователя: ID не указан");
            throw new ValidationException("Id не должно быть пустым");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (newUser.getName() != null) {
                log.trace("Обновлено имя пользователя на {}",  newUser.getName());
                oldUser.setName(newUser.getName());
            }
            if (newUser.getLogin() != null) {
                log.trace("Обновлен логин пользователя на {}",   newUser.getLogin());
                oldUser.setLogin(newUser.getLogin());
            }
            if (newUser.getEmail() != null) {
                log.trace("Обновлена электронная почта пользователя на {}",   newUser.getEmail());
                oldUser.setEmail(newUser.getEmail());
            }
            if (newUser.getBirthday() != null) {
                log.trace("Обновлен день рождения пользователя на {}",    newUser.getBirthday());
                oldUser.setBirthday(newUser.getBirthday());
            }
            log.info("Пользователь с ID: {} успешно обновлен", newUser.getId());
            return oldUser;
        }
        log.warn("Ошибка обновления пользователя: пользователь с ID {} не найден", newUser.getId());
        throw new ValidationNotObjectException("Пользователя с таким Id = " + newUser.getId() + "нет в списке");
    }

    @Override
    public User getUserById(long userId) {
        return users.get(userId);
    }

    @Override
    public Map<Long, User> getUserStorage() {
        return users;
    }

    @Override
    public boolean checkingId(long id) {
        return users.containsKey(id);
    }

    private long nextId() {
        long maxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0L);
        return ++maxId;
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
