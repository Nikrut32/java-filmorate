package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final Map<Long, User> users =  new HashMap<Long, User>();

    @GetMapping
    public Collection<User> getUsers() {
        log.info("Получен запрос GET /users. Текущее количество пользователей: {}", users.size());
        return users.values();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Получен запрос POST /users на создание пользователя: {}", user);
        exceptionUser(user);
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("У пользователя не указано имя, в качестве имени будет использован логин: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        user.setId(nextId());
        log.trace("Пользователю присвоен Id = {}", user.getId());
        users.put(user.getId(), user);
        log.trace("Пользователь добавлен в общий список");
        log.info("Пользователь успешно создан с ID: {}", user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        log.info("Получен запрос PUT /users на обновление пользователя: {}", newUser);
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
        throw new ValidationException("Фильма с таким Id = " + newUser.getId() + "нет в списке");
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
        if (user.getLogin().contains(" ")) {
            log.warn("Валидация не пройдена: логин {} содержит пробелы", user.getLogin());
            throw new ValidationException("Логин не должен содержать пробелов");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Валидация не пройдена: дата рождения {} в будущем", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }
}
