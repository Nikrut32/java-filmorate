package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.AnswerString;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Set;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserStorage userStorage;
    private final UserService userService;

    @GetMapping
    public Collection<User> getUsers() {
        log.info("Получен запрос GET /users. Текущее количество пользователей: {}",
                userStorage.getUserStorage().size());
        log.info("Успешно возвращено {} пользователей", userStorage.getUserStorage().size());
        return userStorage.getUserStorage().values();
    }

    @GetMapping("/{id}/friends")
    public Set<User> getFriends(@PathVariable long id) {
        log.info("Получен запрос GET /users/{}/friends на получение списка друзей", id);
        return userService.getAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Set<User> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        log.info("Получен запрос GET /users/{}/friends/common/{} на поиск общих друзей", id, otherId);
        Set<User> commonFriends = userService.getCommonFriends(id, otherId);
        log.info("Найдено {} общих друзей между пользователями с id={} и id={}",
                commonFriends.size(), id, otherId);
        return commonFriends;
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable long id) {
        return   userStorage.getUserById(id);
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Получен запрос POST /users на создание пользователя: {}", user);
        User createdUser = userStorage.addUserStorage(user);
        log.info("Пользователь успешно создан с id={}: {}", createdUser.getId(), createdUser.getLogin());
        return createdUser;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        log.info("Получен запрос PUT /users на обновление пользователя с id={}: {}",
                newUser.getId(), newUser.getLogin());
        User updatedUser = userStorage.updateUserStorage(newUser);
        log.info("Пользователь с id={} успешно обновлен: {}", updatedUser.getId(), updatedUser.getLogin());
        return updatedUser;
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public AnswerString addFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("Получен запрос PUT /users/{}/friends/{} на добавление в друзья", id, friendId);
        userService.addFriend(id, friendId);
        String userLogin = userStorage.getUserById(id).getLogin();
        String friendLogin = userStorage.getUserById(friendId).getLogin();
        log.info("Пользователь {} успешно добавил в друзья пользователя {}", userLogin, friendLogin);
        return new AnswerString("Пользователь " + userLogin + " успешно добавил в друзья пользователя " + friendLogin);
    }

    @DeleteMapping("/{deleteUserId}")
    public AnswerString deleteUser(@PathVariable long deleteUserId) {
        log.info("Получен запрос DELETE /users/{} на удаление пользователя", deleteUserId);
        userStorage.removeUserStorage(deleteUserId);
        log.info("Пользователь с id={} успешно удален", deleteUserId);
        return new AnswerString("Пользователь с Id: " + deleteUserId + ", успешно удален.");
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public AnswerString deleteFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("Получен запрос DELETE /users/{}/friends/{} на удаление из друзей", id, friendId);
        userService.deleteFriend(id, friendId);
        String userLogin = userStorage.getUserById(id).getLogin();
        String friendLogin = userStorage.getUserById(friendId).getLogin();
        log.info("Пользователь {} успешно удалил из друзей пользователя {}", userLogin, friendLogin);
        return new AnswerString("Пользователь " + userLogin + " успешно удалил из друзей пользователя " + friendLogin);
    }
}
