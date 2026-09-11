package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.dal.mappers.LongRowMapper;
import ru.yandex.practicum.filmorate.dal.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.ValidationNotObjectException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class, LongRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserDbStorage userDbStorage;

    @Test
    void addUserStorageTest() {
        User user = createTestUser();

        User created = userDbStorage.addUserStorage(user);

        assertNotNull(created.getId());
        assertEquals("test@mail.ru", created.getEmail());
        assertEquals("testuser", created.getLogin());
        assertEquals("Test User", created.getName());
        assertEquals(LocalDate.of(1990, 1, 1), created.getBirthday());
    }

    @Test
    void addUserStorageWithoutNameTest() {
        User user = User.builder()
                .email("test@mail.ru")
                .login("testuser")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        User created = userDbStorage.addUserStorage(user);

        assertNotNull(created.getId());
        assertEquals("testuser", created.getName());
    }

    @Test
    void addUserStorageWithDuplicateEmailTest() {
        User user1 = createTestUser();
        userDbStorage.addUserStorage(user1);

        User user2 = User.builder()
                .email("test@mail.ru")
                .login("anotheruser")
                .name("Another User")
                .birthday(LocalDate.of(1991, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> userDbStorage.addUserStorage(user2));
    }

    @Test
    void addUserStorageWithEmptyLoginTest() {
        User user = User.builder()
                .email("test@mail.ru")
                .login("")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> userDbStorage.addUserStorage(user));
    }

    @Test
    void addUserStorageWithLoginContainingSpacesTest() {
        User user = User.builder()
                .email("test@mail.ru")
                .login("test user")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> userDbStorage.addUserStorage(user));
    }

    @Test
    void addUserStorageWithFutureBirthdayTest() {
        User user = User.builder()
                .email("test@mail.ru")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.now().plusDays(1))
                .build();

        assertThrows(ValidationException.class, () -> userDbStorage.addUserStorage(user));
    }

    @Test
    void addUserStorageWithEmptyEmailTest() {
        User user = User.builder()
                .email("")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> userDbStorage.addUserStorage(user));
    }

    @Test
    void updateUserStorageTest() {
        User user = createTestUser();
        User created = userDbStorage.addUserStorage(user);

        User updatedUser = User.builder()
                .id(created.getId())
                .email("updated@mail.ru")
                .login("updateduser")
                .name("Updated User")
                .birthday(LocalDate.of(1991, 2, 2))
                .build();

        User updated = userDbStorage.updateUserStorage(updatedUser);

        assertEquals(updatedUser.getEmail(), updated.getEmail());
        assertEquals(updatedUser.getLogin(), updated.getLogin());
        assertEquals(updatedUser.getName(), updated.getName());
        assertEquals(updatedUser.getBirthday(), updated.getBirthday());
    }

    @Test
    void removeUserStorageTest() {
        User user = createTestUser();
        User created = userDbStorage.addUserStorage(user);

        userDbStorage.removeUserStorage(created.getId());

        assertThrows(ValidationNotObjectException.class,
                () -> userDbStorage.getUserById(created.getId()));
    }

    @Test
    void getUserStorageTest() {
        userDbStorage.addUserStorage(createTestUser());
        userDbStorage.addUserStorage(createTestUser2());

        List<User> users = userDbStorage.getUserStorage();

        assertEquals(5, users.size());
    }

    @Test
    void getUserByIdTest() {
        User user = createTestUser();
        User created = userDbStorage.addUserStorage(user);

        User found = userDbStorage.getUserById(created.getId());

        assertNotNull(found);
        assertEquals(created.getId(), found.getId());
        assertEquals(created.getEmail(), found.getEmail());
        assertEquals(created.getLogin(), found.getLogin());
    }

    @Test
    void getUserByIdNotFoundTest() {
        assertThrows(ValidationNotObjectException.class,
                () -> userDbStorage.getUserById(999L));
    }

    @Test
    void checkingIdTest() {
        User user = createTestUser();
        User created = userDbStorage.addUserStorage(user);

        assertTrue(userDbStorage.checkingId(created.getId()));
        assertFalse(userDbStorage.checkingId(999L));
    }

    @Test
    void addFriendTest() {
        User user1 = userDbStorage.addUserStorage(createTestUser());
        User user2 = userDbStorage.addUserStorage(createTestUser2());

        userDbStorage.addFriend(user1.getId(), user2.getId());

        List<User> friends = userDbStorage.getAllFriends(user1.getId());
        assertEquals(1, friends.size());
        assertEquals(user2.getId(), friends.getFirst().getId());
    }

    @Test
    void addFriendMutualTest() {
        User user1 = userDbStorage.addUserStorage(createTestUser());
        User user2 = userDbStorage.addUserStorage(createTestUser2());

        userDbStorage.addFriend(user1.getId(), user2.getId());
        userDbStorage.addFriend(user2.getId(), user1.getId());

        List<User> friends1 = userDbStorage.getAllFriends(user1.getId());
        List<User> friends2 = userDbStorage.getAllFriends(user2.getId());

        assertEquals(1, friends1.size());
        assertEquals(1, friends2.size());
        assertEquals(user2.getId(), friends1.getFirst().getId());
        assertEquals(user1.getId(), friends2.getFirst().getId());
    }

    @Test
    void deleteFriendTest() {
        User user1 = userDbStorage.addUserStorage(createTestUser());
        User user2 = userDbStorage.addUserStorage(createTestUser2());

        userDbStorage.addFriend(user1.getId(), user2.getId());
        userDbStorage.deleteFriend(user1.getId(), user2.getId());

        List<User> friends = userDbStorage.getAllFriends(user1.getId());
        assertTrue(friends.isEmpty());
    }

    @Test
    void deleteFriendNotFoundTest() {
        User user1 = userDbStorage.addUserStorage(createTestUser());
        User user2 = userDbStorage.addUserStorage(createTestUser2());

        assertDoesNotThrow(() -> userDbStorage.deleteFriend(user1.getId(), user2.getId()));
    }

    @Test
    void getAllFriendsTest() {
        User user1 = userDbStorage.addUserStorage(createTestUser());
        User user2 = userDbStorage.addUserStorage(createTestUser2());
        User user3 = userDbStorage.addUserStorage(createTestUser3());

        userDbStorage.addFriend(user1.getId(), user2.getId());
        userDbStorage.addFriend(user1.getId(), user3.getId());

        List<User> friends = userDbStorage.getAllFriends(user1.getId());

        assertEquals(2, friends.size());
        assertTrue(friends.stream().anyMatch(u -> u.getId().equals(user2.getId())));
        assertTrue(friends.stream().anyMatch(u -> u.getId().equals(user3.getId())));
    }

    @Test
    void getAllFriendsWhenNoFriendsTest() {
        User user = userDbStorage.addUserStorage(createTestUser());

        List<User> friends = userDbStorage.getAllFriends(user.getId());

        assertTrue(friends.isEmpty());
    }

    @Test
    void getCommonFriendsTest() {
        User user1 = userDbStorage.addUserStorage(createTestUser());
        User user2 = userDbStorage.addUserStorage(createTestUser2());
        User user3 = userDbStorage.addUserStorage(createTestUser3());

        userDbStorage.addFriend(user1.getId(), user2.getId());
        userDbStorage.addFriend(user1.getId(), user3.getId());

        userDbStorage.addFriend(user2.getId(), user1.getId());
        userDbStorage.addFriend(user2.getId(), user3.getId());

        List<User> commonFriends = userDbStorage.getCommonFriends(user1.getId(), user2.getId());

        assertEquals(1, commonFriends.size());
        assertEquals(user3.getId(), commonFriends.getFirst().getId());
    }

    @Test
    void getCommonFriendsNoCommonTest() {
        User user1 = userDbStorage.addUserStorage(createTestUser());
        User user2 = userDbStorage.addUserStorage(createTestUser2());
        User user3 = userDbStorage.addUserStorage(createTestUser3());

        userDbStorage.addFriend(user1.getId(), user2.getId());

        List<User> commonFriends = userDbStorage.getCommonFriends(user1.getId(), user3.getId());

        assertTrue(commonFriends.isEmpty());
    }

    @Test
    void getCommonFriendsWhenNoFriendsTest() {
        User user1 = userDbStorage.addUserStorage(createTestUser());
        User user2 = userDbStorage.addUserStorage(createTestUser2());

        List<User> commonFriends = userDbStorage.getCommonFriends(user1.getId(), user2.getId());

        assertTrue(commonFriends.isEmpty());
    }

    @Test
    void addFriendToNonExistentUserTest() {
        User user = userDbStorage.addUserStorage(createTestUser());

        assertThrows(Exception.class, () -> userDbStorage.addFriend(user.getId(), 999L));
    }

    private User createTestUser() {
        return User.builder()
                .email("test@mail.ru")
                .login("testuser")
                .name("Test User")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    private User createTestUser2() {
        return User.builder()
                .email("test2@mail.ru")
                .login("testuser2")
                .name("Test User 2")
                .birthday(LocalDate.of(1992, 2, 2))
                .build();
    }

    private User createTestUser3() {
        return User.builder()
                .email("test3@mail.ru")
                .login("testuser3")
                .name("Test User 3")
                .birthday(LocalDate.of(1993, 3, 3))
                .build();
    }
}