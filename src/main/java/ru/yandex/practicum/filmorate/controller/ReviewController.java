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
        return reviewStorage.getReviewStorage(filmId, count);
    }

    @PostMapping
    public Review createReview(@RequestBody Review review) {
        return reviewStorage.addReview(review);
    }

    @PutMapping
    public Review updateReview(@RequestBody UpdateReviewRequest review) {
        return reviewService.updateReview(review);
    }

    @PutMapping("{id}/like/{userId}")
    public AnswerString likeReview(@PathVariable long id, @PathVariable long userId) {
        reviewService.addLike(id, userId, true);
        String userLogin = userStorage.getUserById(userId).getLogin();
        return new AnswerString("Пользователь " + userLogin + " поставил лайк на отзыв с id: " + id);
    }

    @PutMapping("{id}/dislike/{userId}")
    public AnswerString dislikeReview(@PathVariable long id, @PathVariable long userId) {
        reviewService.addLike(id, userId, false);
        String userLogin = userStorage.getUserById(userId).getLogin();
        return new AnswerString("Пользователь " + userLogin + " поставил дизлайк на отзыв с id: " + id);
    }

    @DeleteMapping("/{id}")
    public AnswerString deleteReview(@PathVariable long id) {
        reviewStorage.removeReview(id);
        return new AnswerString("Отзыв с id: " + id + " успешно удален");
    }
}
