package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.UpdateReviewRequest;
import ru.yandex.practicum.filmorate.model.AnswerString;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {
    private final ReviewService reviewService;
    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;

    @GetMapping
    public List<Review> getAllReviews(@RequestParam(defaultValue = "0") long filmId,
                                      @RequestParam(defaultValue = "10") long count) {
        log.info("Получен запрос GET /reviews");
        log.info("Успешно возвращено {} отзывов", reviewStorage.getReviewStorage(filmId, count));
        return reviewStorage.getReviewStorage(filmId, count);
    }

    @PostMapping
    public Review createReview(@RequestBody Review review) {
        log.info("Получен запрос POST /reviews на добавление отзыва: {}", review);
        Review createReview = reviewStorage.addReview(review);
        log.info("Отзыв успешно создан с id={}", createReview.getReviewId());
        return createReview;
    }

    @PutMapping
    public Review updateReview(@RequestBody UpdateReviewRequest review) {
        log.info("Получен запрос PUT /reviews на обновление фильма: {}", review);
        Review updateReview = reviewService.updateReview(review);
        log.info("Отзыв с id={} успешно обновлен.", updateReview.getReviewId());
        return updateReview;
    }

    @PutMapping("{id}/like/{userId}")
    public AnswerString likeReview(@PathVariable long id, @PathVariable long userId) {
        log.info("Получен запрос PUT /reviews/{}/like/{} на добавление лайка", id, userId);
        reviewService.addLike(id, userId, true);
        String userLogin = userStorage.getUserById(userId).getLogin();
        log.info("Пользователь {} успешно поставил лайк на отзыв с id: {}", userLogin, id);
        return new AnswerString("Пользователь " + userLogin + " поставил лайк на отзыв с id: " + id);
    }

    @PutMapping("{id}/dislike/{userId}")
    public AnswerString dislikeReview(@PathVariable long id, @PathVariable long userId) {
        log.info("Получен запрос PUT /reviews/{}/dislike/{} на добавление дизлайка", id, userId);
        reviewService.addLike(id, userId, false);
        String userLogin = userStorage.getUserById(userId).getLogin();
        log.info("Пользователь {} успешно поставил дизлайк на отзыв с id: {}", userLogin, id);
        return new AnswerString("Пользователь " + userLogin + " поставил дизлайк на отзыв с id: " + id);
    }

    @DeleteMapping("/{id}")
    public AnswerString deleteReview(@PathVariable long id) {
        log.info("Получен запрос DELETE /reviews/{} на удаление отзыва", id);
        reviewStorage.removeReview(id);
        log.info("Отзыв с id={} успешно удален", id);
        return new AnswerString("Отзыв с id: " + id + " успешно удален");
    }

    @DeleteMapping("{id}/like/{userId}")
    public AnswerString deleteLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Получен запрос DELETE /reviews/{}/dislike/{} на удаление оценки", id, userId);
        reviewStorage.removeLikeOrDislikeReview(id, userId);
        String userLogin = userStorage.getUserById(userId).getLogin();
        log.info("Пользователь {} успешно убрал оценку с отзыва id: {}", userLogin, id);
        return new AnswerString("Пользователь " + userLogin + " убрал оценку с отзыва id: " + id);
    }
}
