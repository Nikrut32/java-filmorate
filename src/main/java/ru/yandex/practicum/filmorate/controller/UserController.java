package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Set;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    private UserStorage userStorage;
    @Autowired
    private UserService userService;

    @GetMapping
    public Collection<User> getUsers() {
        log.info("Получен запрос GET /users. Текущее количество пользователей: {}"
                , userStorage.getUserStorage().size());
        return userStorage.getUserStorage().values();
    }

    @GetMapping("{id}/friends")
    public Set<User> getFriends(@PathVariable long id) {
        return userService.getAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Set<User> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        return userService.getCommonFriends(id, otherId);
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        log.info("Получен запрос POST /users на создание пользователя: {}", user);
        return userStorage.addUserStorage(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User newUser) {
        log.info("Получен запрос PUT /users на обновление пользователя: {}", newUser);
        return userStorage.updateUserStorage(newUser);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public void addFriend(@PathVariable long id, @PathVariable long friendId) {
        userService.addFriend(id, friendId);
    }

    //Добавить отлов исключений
    @DeleteMapping("/{deleteUserId}")
    public String deleteUser(@PathVariable long deleteUserId) {
        userStorage.removeUserStorage(deleteUserId);
        return "Фильм с Id: " + deleteUserId + ", успешно удален.";
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteFriend(@PathVariable long id, @PathVariable long friendId) {
        userService.deleteFriend(id, friendId);
    }
}
