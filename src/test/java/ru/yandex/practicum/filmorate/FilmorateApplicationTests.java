package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FilmorateApplicationTests {
	private final Gson gson = new GsonBuilder()
			.registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
			.create();

	@LocalServerPort
	private int port;

	private String baseUrl(String resource) {
		return "http://localhost:" + port + "/" + resource;
	}

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	void getFilmsShouldReturnEmptyListTest() {
		ResponseEntity<String> response = restTemplate.getForEntity(
				baseUrl("films"),
				String.class
		);

		assertEquals(200, response.getStatusCode().value());
		assertEquals("[]", response.getBody());
	}

	@Test
	void getAndPostFilmsShouldReturnFilmTest() {
		Film testFilm = Film.builder()
				.name("TestFilm")
				.description("Test")
				.releaseDate(LocalDate.of(2022, 11, 6))
				.duration(95L)
				.build();

		ResponseEntity<Film> responsePost = restTemplate.postForEntity(
				baseUrl("films"),
				testFilm,
				Film.class
		);

		assertEquals(HttpStatus.OK, responsePost.getStatusCode());

		ResponseEntity<String> responseGet = restTemplate.getForEntity(
				baseUrl("films"),
				String.class
		);

		testFilm.setId(1L);
		testFilm.setUsersWhoLiked(new HashSet<User>());
		Collection<Film> films = List.of(testFilm);
		String jsonString =  gson.toJson(films);

		assertEquals(HttpStatus.OK, responseGet.getStatusCode());
		assertEquals(jsonString, responseGet.getBody());
	}

	@Test
	void postNumberingIdFilmsTest() {
		Film testFilm1 = Film.builder()
				.name("TestFilm1")
				.description("Test")
				.releaseDate(LocalDate.of(2022, 11, 6))
				.duration(95L)
				.usersWhoLiked(new HashSet<User>())
				.build();

		ResponseEntity<Film> responsePost1 = restTemplate.postForEntity(
				baseUrl("films"),
				testFilm1,
				Film.class
		);

		assertEquals(HttpStatus.OK, responsePost1.getStatusCode());

		Film testFilm2 = Film.builder()
				.name("TestFilm2")
				.description("Test")
				.releaseDate(LocalDate.of(2010, 11, 6))
				.duration(95L)
				.build();

		ResponseEntity<Film> responsePost2 = restTemplate.postForEntity(
				baseUrl("films"),
				testFilm2,
				Film.class
		);

		assertEquals(HttpStatus.OK, responsePost1.getStatusCode());

		ResponseEntity<String> responseGet = restTemplate.getForEntity(
				baseUrl("films"),
				String.class
		);

		testFilm1.setId(1L);
		testFilm1.setUsersWhoLiked(new HashSet<User>());
		testFilm2.setId(2L);
		testFilm2.setUsersWhoLiked(new HashSet<User>());
		Collection<Film> films = List.of(testFilm1, testFilm2);
		String jsonString =  gson.toJson(films);

		assertEquals(HttpStatus.OK, responseGet.getStatusCode());
		assertEquals(jsonString, responseGet.getBody());
	}

	@Test
	void putFilmUpdateTest() {
		Film testFilm = Film.builder()
				.name("TestFilm")
				.description("Test")
				.releaseDate(LocalDate.of(2022, 11, 6))
				.duration(95L)
				.build();

		Film testNewFilm = Film.builder()
				.id(1L)
				.name("TestFilm")
				.description("TestUpdate")
				.build();

		ResponseEntity<Film> responsePost = restTemplate.postForEntity(
				baseUrl("films"),
				testFilm,
				Film.class
		);

		ResponseEntity<Film> responsePut = restTemplate.exchange(
				baseUrl("films"),
				HttpMethod.PUT,
				new HttpEntity<>(testNewFilm),
				Film.class
		);

		assertEquals(HttpStatus.OK, responsePut.getStatusCode());

		ResponseEntity<String> responseGet = restTemplate.getForEntity(
				baseUrl("films"),
				String.class
		);

		testFilm.setId(1L);
		testFilm.setUsersWhoLiked(new HashSet<User>());
		testFilm.setDescription(testNewFilm.getDescription());
		Collection<Film> films = List.of(testFilm);
		String jsonString =  gson.toJson(films);

		assertEquals(HttpStatus.OK, responseGet.getStatusCode());
		assertEquals(jsonString, responseGet.getBody());
	}

	@Test
	void postEmptyFilmNameTest() {
		Film testFilm = Film.builder()
				.name(" ")
				.description("Test")
				.releaseDate(LocalDate.of(2022, 11, 6))
				.duration(95L)
				.build();

		ResponseEntity<Film> responsePost = restTemplate.postForEntity(
				baseUrl("films"),
				testFilm,
				Film.class
		);

		assertEquals(HttpStatus.BAD_REQUEST, responsePost.getStatusCode());
	}

	@Test
	void postDescriptionMoreThan200CharactersFilmTest() {
		Film testFilm = Film.builder()
				.name("TestFilm")
				.description("Test ".repeat(50))
				.releaseDate(LocalDate.of(2022, 11, 6))
				.duration(95L)
				.build();

		ResponseEntity<Film> responsePost = restTemplate.postForEntity(
				baseUrl("films"),
				testFilm,
				Film.class
		);

		assertEquals(HttpStatus.BAD_REQUEST, responsePost.getStatusCode());
	}

	@Test
	void postNegativeDurationFilmTest() {
		Film testFilm = Film.builder()
				.name("TestFilm")
				.description("Test")
				.releaseDate(LocalDate.of(2022, 11, 6))
				.duration(-95L)
				.build();

		ResponseEntity<Film> responsePost = restTemplate.postForEntity(
				baseUrl("films"),
				testFilm,
				Film.class
		);

		assertEquals(HttpStatus.BAD_REQUEST, responsePost.getStatusCode());
	}

	@Test
	void postZeroDurationFilmTest() {
		Film testFilm = Film.builder()
				.name("TestFilm")
				.description("Test")
				.releaseDate(LocalDate.of(2022, 11, 6))
				.duration(0L)
				.build();

		ResponseEntity<Film> responsePost = restTemplate.postForEntity(
				baseUrl("films"),
				testFilm,
				Film.class
		);

		assertEquals(HttpStatus.BAD_REQUEST, responsePost.getStatusCode());
	}

	@Test
	void postDateBorderlineFilmTest() {
		Film testMinDateFilm = Film.builder()
				.name("TestMinDateFilm")
				.description("TestMinDate ")
				.releaseDate(LocalDate.of(1895, 12, 28))
				.duration(95L)
				.build();

		ResponseEntity<Film> responseMinDatePost = restTemplate.postForEntity(
				baseUrl("films"),
				testMinDateFilm,
				Film.class
		);

		assertEquals(HttpStatus.OK, responseMinDatePost.getStatusCode());

		Film testMaxDateFilm = Film.builder()
				.name("TestMaxDateFilm")
				.description("TestMaxDate ")
				.releaseDate(LocalDate.now())
				.duration(95L)
				.build();

		ResponseEntity<Film> responseMaxDatePost = restTemplate.postForEntity(
				baseUrl("films"),
				testMaxDateFilm,
				Film.class
		);

		assertEquals(HttpStatus.OK, responseMaxDatePost.getStatusCode());
	}

	@Test
	void postBeforeMinDateFilmTest() {
		Film testFilm = Film.builder()
				.name("TestFilm")
				.description("Test ")
				.releaseDate(LocalDate.of(1894, 11, 6))
				.duration(-95L)
				.build();

		ResponseEntity<Film> responsePost = restTemplate.postForEntity(
				baseUrl("films"),
				testFilm,
				Film.class
		);

		assertEquals(HttpStatus.BAD_REQUEST, responsePost.getStatusCode());
	}

	@Test
	void deleteFilmTest() {
		Film testFilm = Film.builder()
				.name("TestFilm")
				.description("Test")
				.releaseDate(LocalDate.of(2001, 11, 6))
				.duration(95L)
				.build();

		ResponseEntity<Film> responsePost = restTemplate.postForEntity(
				baseUrl("films"),
				testFilm,
				Film.class
		);

		ResponseEntity<Void> responseDelete = restTemplate.exchange(
				baseUrl("films/1"),
				HttpMethod.DELETE,
				HttpEntity.EMPTY,
				Void.class
		);

		assertEquals(HttpStatus.OK, responseDelete.getStatusCode());

		ResponseEntity<String> responseGet = restTemplate.getForEntity(
				baseUrl("films"),
				String.class
		);

		assertEquals("[]", responseGet.getBody());
	}

	@Test
	void putAddLikeFilmTest() {
		Film testFilm = Film.builder()
				.name("TestFilm")
				.description("Test")
				.releaseDate(LocalDate.of(2004, 11, 6))
				.duration(95L)
				.build();

		User testUser = User.builder()
				.email("TestUser1@mail.ru")
				.login("Test1")
				.name("Test1")
				.birthday(LocalDate.of(2003, 12, 11))
				.build();

		ResponseEntity<Film> responsePostFilm = restTemplate.postForEntity(
				baseUrl("films"),
				testFilm,
				Film.class
		);

		ResponseEntity<User> responsePostUser = restTemplate.postForEntity(
				baseUrl("users"),
				testUser,
				User.class
		);

		ResponseEntity<Void> responsePut = restTemplate.exchange(
				baseUrl("films/1/like/1"),
				HttpMethod.PUT,
				HttpEntity.EMPTY,
				Void.class
		);

		assertEquals(HttpStatus.OK, responsePut.getStatusCode());

		ResponseEntity<String> responseGet = restTemplate.getForEntity(
				baseUrl("films"),
				String.class
		);

		testFilm.setId(1L);
		testUser.setId(1L);
		testFilm.setUsersWhoLiked(Set.of(testUser));
		String jsonString = gson.toJson(Set.of(testFilm));

		assertEquals(jsonString, responseGet.getBody());
	}

	@Test
	void deleteLikeFilmTest() {
		Film testFilm = Film.builder()
				.name("TestFilm")
				.description("Test")
				.releaseDate(LocalDate.of(2004, 11, 6))
				.duration(95L)
				.build();

		User testUser = User.builder()
				.email("TestUser1@mail.ru")
				.login("Test1")
				.name("Test1")
				.birthday(LocalDate.of(2003, 12, 11))
				.build();

		ResponseEntity<Film> responsePostFilm = restTemplate.postForEntity(
				baseUrl("films"),
				testFilm,
				Film.class
		);

		ResponseEntity<User> responsePostUser = restTemplate.postForEntity(
				baseUrl("users"),
				testUser,
				User.class
		);

		ResponseEntity<Void> responsePut = restTemplate.exchange(
				baseUrl("films/1/like/1"),
				HttpMethod.PUT,
				HttpEntity.EMPTY,
				Void.class
		);

		ResponseEntity<Void> responseDelete = restTemplate.exchange(
				baseUrl("films/1/like/1"),
				HttpMethod.DELETE,
				HttpEntity.EMPTY,
				Void.class
		);

		assertEquals(HttpStatus.OK, responseDelete.getStatusCode());

		ResponseEntity<String> responseGet = restTemplate.getForEntity(
				baseUrl("films"),
				String.class
		);

		testFilm.setId(1L);
		testFilm.setUsersWhoLiked(new HashSet<>());
		String jsonString = gson.toJson(Set.of(testFilm));

		assertEquals(jsonString, responseGet.getBody());
	}

	@Test
	void getTopFilmsTestCountTwo() {
		Film testFilm1 = Film.builder()
				.name("TestFilm1")
				.description("Test1")
				.releaseDate(LocalDate.of(2004, 11, 6))
				.duration(95L)
				.build();

		Film testFilm2 = Film.builder()
				.name("TestFilm2")
				.description("Test2")
				.releaseDate(LocalDate.of(1998, 9, 4))
				.duration(74L)
				.build();

		Film testFilm3 = Film.builder()
				.name("TestFilm3")
				.description("Test3")
				.releaseDate(LocalDate.of(2015, 8, 24))
				.duration(120L)
				.build();

		User testUser1 = User.builder()
				.email("TestUser1@mail.ru")
				.login("Test1")
				.name("Test1")
				.birthday(LocalDate.of(2003, 12, 11))
				.build();

		User testUser2 = User.builder()
				.email("TestUser2@mail.ru")
				.login("Test2")
				.name("Test2")
				.birthday(LocalDate.of(2000, 11, 10))
				.build();

		User testUser3 = User.builder()
				.email("TestUser3@mail.ru")
				.login("Test3")
				.name("Test3")
				.birthday(LocalDate.of(2010, 10, 22))
				.build();

		ResponseEntity<Film> responsePostFilm1 = restTemplate.postForEntity(
				baseUrl("films"),
				testFilm1,
				Film.class
		);

		ResponseEntity<Film> responsePostFilm2 = restTemplate.postForEntity(
				baseUrl("films"),
				testFilm2,
				Film.class
		);

		ResponseEntity<Film> responsePostFilm3 = restTemplate.postForEntity(
				baseUrl("films"),
				testFilm3,
				Film.class
		);

		ResponseEntity<User> responsePostUser1 = restTemplate.postForEntity(
				baseUrl("users"),
				testUser1,
				User.class
		);

		ResponseEntity<User> responsePostUser2 = restTemplate.postForEntity(
				baseUrl("users"),
				testUser2,
				User.class
		);

		ResponseEntity<User> responsePostUser3 = restTemplate.postForEntity(
				baseUrl("users"),
				testUser3,
				User.class
		);

		restTemplate.put(baseUrl("films/1/like/1"), null);
		restTemplate.put(baseUrl("films/2/like/1"), null);
		restTemplate.put(baseUrl("films/3/like/1"), null);
		restTemplate.put(baseUrl("films/1/like/2"), null);
		restTemplate.put(baseUrl("films/2/like/2"), null);
		restTemplate.put(baseUrl("films/1/like/3"), null);

		ResponseEntity<String> responseGet = restTemplate.getForEntity(
				baseUrl("films/popular?count=2"),
				String.class
		);

		assertEquals(HttpStatus.OK, responseGet.getStatusCode());

		testUser1.setId(1L);
		testUser2.setId(2L);
		testUser3.setId(3L);
		testFilm1.setId(1L);
		testFilm1.setUsersWhoLiked(new LinkedHashSet<>(List.of(testUser1, testUser2, testUser3)));
		testFilm2.setId(2L);
		testFilm2.setUsersWhoLiked(new LinkedHashSet<>(List.of(testUser1, testUser2)));
		testFilm3.setId(3L);
		testFilm3.setUsersWhoLiked(new LinkedHashSet<>(List.of(testUser1)));
		String jsonString = gson.toJson(new LinkedHashSet<>(List.of(testFilm1, testFilm2)));

		assertEquals(jsonString, responseGet.getBody());

	}

	@Test
	void getUsersShouldReturnEmptyListTest() {
		ResponseEntity<String> response = restTemplate.getForEntity(
				baseUrl("users"),
				String.class
		);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals("[]", response.getBody());
	}

	@Test
	void getAndPostUsersShouldReturnUserTest() {
		User testUser = User.builder()
				.email("TestUser@mail.ru")
				.login("Test1234")
				.name("Test")
				.birthday(LocalDate.of(2003, 12, 11))
				.build();

		ResponseEntity<User> responsePost = restTemplate.postForEntity(
				baseUrl("users"),
				testUser,
				User.class
		);

		assertEquals(HttpStatus.OK, responsePost.getStatusCode());

		ResponseEntity<String> responseGet = restTemplate.getForEntity(
				baseUrl("users"),
				String.class
		);

		testUser.setId(1L);
		Collection<User> users = List.of(testUser);
		String jsonString =  gson.toJson(users);

		assertEquals(HttpStatus.OK, responseGet.getStatusCode());
		assertEquals(jsonString, responseGet.getBody());
	}

	@Test
	void postNumberingIdUsersTest() {
		User testUser1 = User.builder()
				.email("TestUser1@mail.ru")
				.login("Test1234")
				.name("Test1")
				.birthday(LocalDate.of(2003, 12, 11))
				.build();

		ResponseEntity<User> responsePost1 = restTemplate.postForEntity(
				baseUrl("users"),
				testUser1,
				User.class
		);

		assertEquals(HttpStatus.OK, responsePost1.getStatusCode());

		User testUser2 = User.builder()
				.email("TestUser2@mail.ru")
				.login("Test1234")
				.name("Test2")
				.birthday(LocalDate.of(2000, 10, 8))
				.build();

		ResponseEntity<User> responsePost2 = restTemplate.postForEntity(
				baseUrl("users"),
				testUser2,
				User.class
		);

		assertEquals(HttpStatus.OK, responsePost2.getStatusCode());

		ResponseEntity<String> responseGet = restTemplate.getForEntity(
				baseUrl("users"),
				String.class
		);

		testUser1.setId(1L);
		testUser2.setId(2L);
		Collection<User> users = List.of(testUser1,testUser2);
		String jsonString =  gson.toJson(users);

		assertEquals(HttpStatus.OK, responseGet.getStatusCode());
		assertEquals(jsonString, responseGet.getBody());
	}

	@Test
	void postNotNameUserTest() {
		User testUser = User.builder()
				.email("TestUser@mail.ru")
				.login("Test1234")
				.birthday(LocalDate.of(2003, 12, 11))
				.build();

		ResponseEntity<User> responsePost = restTemplate.postForEntity(
				baseUrl("users"),
				testUser,
				User.class
		);

		assertEquals(HttpStatus.OK, responsePost.getStatusCode());

		ResponseEntity<String> responseGet = restTemplate.getForEntity(
				baseUrl("users"),
				String.class
		);

		testUser.setId(1L);
		testUser.setName(testUser.getLogin());
		Collection<User> users = List.of(testUser);
		String jsonString =  gson.toJson(users);

		assertEquals(HttpStatus.OK, responseGet.getStatusCode());
		assertEquals(jsonString, responseGet.getBody());
	}

	@Test
	void postEmailEmptyUserTest() {
		User testUser = User.builder()
				.email(" ")
				.login("Test1234")
				.name("Test")
				.birthday(LocalDate.of(2003, 12, 11))
				.build();

		ResponseEntity<User> responsePost = restTemplate.postForEntity(
				baseUrl("users"),
				testUser,
				User.class
		);

		assertEquals(HttpStatus.BAD_REQUEST, responsePost.getStatusCode());
	}

	@Test
	void postEmailDoesNotContainSpecialCharacterUserTest() {
		User testUser = User.builder()
				.email("TestUsermail.ru")
				.login("Test1234")
				.name("Test")
				.birthday(LocalDate.of(2003, 12, 11))
				.build();

		ResponseEntity<User> responsePost = restTemplate.postForEntity(
				baseUrl("users"),
				testUser,
				User.class
		);

		assertEquals(HttpStatus.BAD_REQUEST, responsePost.getStatusCode());
	}

	@Test
	void postEmailInappropriateUserTest() {
		User testUser = User.builder()
				.email("@TestUsermail.ru")
				.login("Test1234")
				.name("Test")
				.birthday(LocalDate.of(2003, 12, 11))
				.build();

		ResponseEntity<User> responsePost = restTemplate.postForEntity(
				baseUrl("users"),
				testUser,
				User.class
		);

		assertEquals(HttpStatus.BAD_REQUEST, responsePost.getStatusCode());
	}

	@Test
	void postLoginEmptyUserTest() {
		User testUser = User.builder()
				.email("TestUser@mail.ru")
				.login(" ")
				.name("Test")
				.birthday(LocalDate.of(2003, 12, 11))
				.build();

		ResponseEntity<User> responsePost = restTemplate.postForEntity(
				baseUrl("users"),
				testUser,
				User.class
		);

		assertEquals(HttpStatus.BAD_REQUEST, responsePost.getStatusCode());
	}

	@Test
	void postBirthdayBorderlineUserTest() {
		User testUser = User.builder()
				.email("TestUser@mail.ru")
				.login("Test1234")
				.name("Test")
				.birthday(LocalDate.now())
				.build();

		ResponseEntity<User> responsePost = restTemplate.postForEntity(
				baseUrl("users"),
				testUser,
				User.class
		);

		assertEquals(HttpStatus.OK, responsePost.getStatusCode());
	}

	@Test
	void postBirthdayFutureUserTest() {
		User testUser = User.builder()
				.email("TestUser@mail.ru")
				.login("Test1234")
				.name("Test")
				.birthday(LocalDate.now().plusDays(1))
				.build();

		ResponseEntity<User> responsePost = restTemplate.postForEntity(
				baseUrl("users"),
				testUser,
				User.class
		);

		assertEquals(HttpStatus.BAD_REQUEST, responsePost.getStatusCode());
	}

	@Test
	void putUserUpdateTest() {
		User testUser = User.builder()
				.email("TestUser@mail.ru")
				.login("Test1234")
				.name("Test")
				.birthday(LocalDate.of(2003, 12, 11))
				.build();

		User testNewUser = User.builder()
				.id(1L)
				.email("TestUser@mail.ru")
				.login("Test1234Update")
				.birthday(LocalDate.of(2003, 12, 11))
				.build();

		ResponseEntity<User> responsePost = restTemplate.postForEntity(
				baseUrl("users"),
				testUser,
				User.class
		);

		ResponseEntity<User> responsePut = restTemplate.exchange(
				baseUrl("users"),
				HttpMethod.PUT,
				new HttpEntity<>(testNewUser),
				User.class
		);

		assertEquals(HttpStatus.OK, responsePut.getStatusCode());

		ResponseEntity<String> responseGet = restTemplate.getForEntity(
				baseUrl("users"),
				String.class
		);

		testUser.setId(1L);
		testUser.setLogin(testNewUser.getLogin());
		Collection<User> users = List.of(testUser);
		String jsonString =  gson.toJson(users);

		assertEquals(HttpStatus.OK, responseGet.getStatusCode());
		assertEquals(jsonString, responseGet.getBody());
	}

	@Test
	void putUserAddFriendAndGetFriendsTest() {
		User testUser1 = User.builder()
				.email("TestUser1@mail.ru")
				.login("Test1")
				.name("Test1")
				.birthday(LocalDate.of(2003, 12, 11))
				.build();

		User testUser2 = User.builder()
				.email("TestUser2@mail.ru")
				.login("Test2")
				.name("Test2")
				.birthday(LocalDate.of(2000, 11, 10))
				.build();

		ResponseEntity<User> responsePost1 = restTemplate.postForEntity(
				baseUrl("users"),
				testUser1,
				User.class
		);

		ResponseEntity<User> responsePost2 = restTemplate.postForEntity(
				baseUrl("users"),
				testUser2,
				User.class
		);

		ResponseEntity<Void> responsePut = restTemplate.exchange(
				baseUrl("users/1/friends/2"),
				HttpMethod.PUT,
				HttpEntity.EMPTY,
				Void.class
		);

		assertEquals(HttpStatus.OK, responsePut.getStatusCode());

		ResponseEntity<String> responseGetUser1 = restTemplate.getForEntity(
				baseUrl("users/1/friends"),
				String.class
		);

		assertEquals(HttpStatus.OK, responseGetUser1.getStatusCode());

		testUser1.setId(1L);
		testUser2.setId(2L);

        Set<User> friendsUser1 = Set.of(testUser2);
		String jsonStringUser1 = gson.toJson(friendsUser1);
		assertEquals(jsonStringUser1, responseGetUser1.getBody());

		ResponseEntity<String> responseGetUser2 = restTemplate.getForEntity(
				baseUrl("users/2/friends"),
				String.class
		);

		assertEquals(HttpStatus.OK, responseGetUser2.getStatusCode());

		Set<User> friendsUser2 = Set.of(testUser1);
		String jsonStringUser2 = gson.toJson(friendsUser2);
		assertEquals(jsonStringUser2, responseGetUser2.getBody());
	}

	@Test
	void deleteUserFriendsTest() {
		User testUser1 = User.builder()
				.email("TestUser1@mail.ru")
				.login("Test1")
				.name("Test1")
				.birthday(LocalDate.of(2003, 12, 11))
				.build();

		User testUser2 = User.builder()
				.email("TestUser2@mail.ru")
				.login("Test2")
				.name("Test2")
				.birthday(LocalDate.of(2000, 11, 10))
				.build();

		ResponseEntity<User> responsePost1 = restTemplate.postForEntity(
				baseUrl("users"),
				testUser1,
				User.class
		);

		ResponseEntity<User> responsePost2 = restTemplate.postForEntity(
				baseUrl("users"),
				testUser2,
				User.class
		);

		ResponseEntity<Void> responsePut = restTemplate.exchange(
				baseUrl("users/1/friends/2"),
				HttpMethod.PUT,
				HttpEntity.EMPTY,
				Void.class
		);

		ResponseEntity<Void> responseDelete = restTemplate.exchange(
				baseUrl("users/1/friends/2"),
				HttpMethod.DELETE,
				HttpEntity.EMPTY,
				Void.class
		);

		assertEquals(HttpStatus.OK, responseDelete.getStatusCode());

		ResponseEntity<String> responseGetUser1 = restTemplate.getForEntity(
				baseUrl("users/1/friends"),
				String.class
		);

		assertEquals("[]", responseGetUser1.getBody());

		ResponseEntity<String> responseGetUser2 = restTemplate.getForEntity(
				baseUrl("users/2/friends"),
				String.class
		);

		assertEquals("[]", responseGetUser2.getBody());
	}

	@Test
	void deleteUserTest() {
		User testUser = User.builder()
				.email("TestUser1@mail.ru")
				.login("Test1")
				.name("Test1")
				.birthday(LocalDate.of(2003, 12, 11))
				.build();

		ResponseEntity<User> responsePost = restTemplate.postForEntity(
				baseUrl("users"),
				testUser,
				User.class
		);

		ResponseEntity<Void> responseDelete = restTemplate.exchange(
				baseUrl("users/1"),
				HttpMethod.DELETE,
				HttpEntity.EMPTY,
				Void.class
		);

		assertEquals(HttpStatus.OK, responseDelete.getStatusCode());

		ResponseEntity<String> responseGet = restTemplate.getForEntity(
				baseUrl("users"),
				String.class
		);

		assertEquals("[]", responseGet.getBody());
	}

	@Test
	void getUserCommonFriendsTest() {
		User testUser1 = User.builder()
				.email("TestUser1@mail.ru")
				.login("Test1")
				.name("Test1")
				.birthday(LocalDate.of(2003, 12, 11))
				.build();

		User testUser2 = User.builder()
				.email("TestUser2@mail.ru")
				.login("Test2")
				.name("Test2")
				.birthday(LocalDate.of(2000, 11, 10))
				.build();

		User testUser3 = User.builder()
				.email("TestUser3@mail.ru")
				.login("Test3")
				.name("Test3")
				.birthday(LocalDate.of(2010, 10, 22))
				.build();

		ResponseEntity<User> responsePost1 = restTemplate.postForEntity(
				baseUrl("users"),
				testUser1,
				User.class
		);

		ResponseEntity<User> responsePost2 = restTemplate.postForEntity(
				baseUrl("users"),
				testUser2,
				User.class
		);

		ResponseEntity<User> responsePost3 = restTemplate.postForEntity(
				baseUrl("users"),
				testUser3,
				User.class
		);

		ResponseEntity<Void> responsePutFriendUser1AndUser2 = restTemplate.exchange(
				baseUrl("users/1/friends/2"),
				HttpMethod.PUT,
				HttpEntity.EMPTY,
				Void.class
		);

		ResponseEntity<Void> responsePutFriendUser1AndUser3 = restTemplate.exchange(
				baseUrl("users/1/friends/3"),
				HttpMethod.PUT,
				HttpEntity.EMPTY,
				Void.class
		);

		ResponseEntity<String> responseGet = restTemplate.getForEntity(
				baseUrl("users/2/friends/common/3"),
				String.class
		);

		assertEquals(HttpStatus.OK, responseGet.getStatusCode());

		testUser1.setId(1L);
		String jsonString = gson.toJson(Set.of(testUser1));
		assertEquals(jsonString, responseGet.getBody());
	}
}